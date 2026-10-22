#!/usr/bin/env python3
"""Laboratorio de carga aislado; jamás lee credenciales de producción."""
import argparse, datetime, json, os, pathlib, secrets, subprocess, time, urllib.request, urllib.error
ROOT = pathlib.Path(__file__).resolve().parents[2]
STATE = ROOT / '.performance'
RUNTIME = STATE / 'runtime'
PG = RUNTIME / 'pg/usr/lib/postgresql/17/bin'
CONFIG = ROOT / 'tests/performance/config.local.json'
BASE = 'http://127.0.0.1:18080'

def env_pg():
    return dict(os.environ, LD_LIBRARY_PATH=str(RUNTIME / 'pg/usr/lib/x86_64-linux-gnu'))

def sql(query):
    p = subprocess.run([str(PG/'psql'), '-h','127.0.0.1','-p','15432','-U','perf','-d','shushine_perf','-At','-v','ON_ERROR_STOP=1'],
                       input=query, text=True,capture_output=True,env=env_pg())
    if p.returncode: raise RuntimeError(p.stderr)
    return p.stdout.strip()

def api(path, body=None):
    data=json.dumps(body).encode() if body is not None else None
    req=urllib.request.Request(BASE+path,data=data,headers={'Content-Type':'application/json'})
    with urllib.request.urlopen(req,timeout=15) as r:return json.load(r)

def verify_api_local():
    pid=int((STATE/'api.pid').read_text())
    values=pathlib.Path(f'/proc/{pid}/environ').read_bytes().split(b'\x00')
    expected=b'SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:15432/shushine_perf'
    if expected not in values:raise RuntimeError('La API activa no apunta a la base local de pruebas')

def start():
    STATE.mkdir(exist_ok=True)
    data=STATE/'pgdata'
    if not data.exists():
        subprocess.run([str(PG/'initdb'),'-D',str(data),'-U','perf','--auth-local=trust','--auth-host=trust','--encoding=UTF8','--locale=C'],env=env_pg(),check=True,stdout=subprocess.DEVNULL)
        with (data/'postgresql.conf').open('a') as f:f.write("\nlisten_addresses='127.0.0.1'\nport=15432\nunix_socket_directories='/tmp'\nmax_connections=50\n")
    status=subprocess.run([str(PG/'pg_ctl'),'-D',str(data),'status'],env=env_pg(),stdout=subprocess.DEVNULL)
    if status.returncode:
        subprocess.run([str(PG/'pg_ctl'),'-D',str(data),'-l',str(STATE/'postgres.log'),'start'],env=env_pg(),check=True)
    # Only this local disposable database is touched.
    exists=subprocess.run([str(PG/'psql'),'-h','127.0.0.1','-p','15432','-U','perf','-d','postgres','-Atc',"SELECT 1 FROM pg_database WHERE datname='shushine_perf'"],env=env_pg(),capture_output=True,text=True,check=True).stdout.strip()
    if not exists:subprocess.run([str(PG/'createdb'),'-h','127.0.0.1','-p','15432','-U','perf','shushine_perf'],env=env_pg(),check=True)
    if sql("SELECT to_regclass('public.citas') IS NOT NULL") == 'f':
        source=STATE/'production-public-schema.sql'
        if not source.exists():raise RuntimeError('Falta el esquema sin datos .performance/production-public-schema.sql para inicializar el laboratorio local')
        sql("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='anon') THEN CREATE ROLE anon; END IF; IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='authenticated') THEN CREATE ROLE authenticated; END IF; END $$;")
        source_sql=source.read_text().replace('CREATE SCHEMA public;','CREATE SCHEMA IF NOT EXISTS public;')
        subprocess.run([str(PG/'psql'),'-h','127.0.0.1','-p','15432','-U','perf','-d','shushine_perf','-X','--single-transaction','-v','ON_ERROR_STOP=1'],input=source_sql,text=True,env=env_pg(),check=True,stdout=subprocess.DEVNULL)
    sql((ROOT/'database/migrations/20261016_reservas_sin_solapamientos.sql').read_text())
    try:api('/api/servicios/lista')
    except urllib.error.URLError:pass
    else:
        verify_api_local()
        print('API local ya disponible')
        return
    java=pathlib.Path((RUNTIME/'java-home.txt').read_text())/'bin/java'
    jar=ROOT/'backend-api/target/shushine-studio-api-0.0.1-SNAPSHOT.jar'
    if not jar.exists():raise RuntimeError('Compilar primero con Maven; ver README')
    appenv=dict(os.environ, SPRING_DATASOURCE_URL='jdbc:postgresql://127.0.0.1:15432/shushine_perf',SPRING_DATASOURCE_USERNAME='perf',SPRING_DATASOURCE_PASSWORD='',
        SPRING_JPA_HIBERNATE_DDL_AUTO='none', SPRING_JPA_SHOW_SQL='false',SECURITY_JWT_SECRET_KEY=secrets.token_hex(64),
        SUPABASE_URL='http://127.0.0.1:15433', SUPABASE_SERVICE_ROLE_KEY='local-disabled',PORT='18080',SERVER_ADDRESS='127.0.0.1')
    # Explicit command-line overrides beat all application.properties production fallbacks.
    args=[str(java),'-jar',str(jar),'--server.address=127.0.0.1','--server.port=18080',
        '--spring.datasource.url=jdbc:postgresql://127.0.0.1:15432/shushine_perf', '--spring.datasource.username=perf','--spring.datasource.password=',
        '--spring.jpa.hibernate.ddl-auto=none','--spring.jpa.show-sql=false','--supabase.url=http://127.0.0.1:15433','--supabase.service-role-key=local-disabled']
    with (STATE/'api.log').open('ab') as log:p=subprocess.Popen(args,env=appenv,stdout=log,stderr=log,start_new_session=True)
    (STATE/'api.pid').write_text(str(p.pid))
    for _ in range(90):
        try:api('/api/servicios/lista');print('API local disponible en '+BASE);return
        except Exception:
            if p.poll() is not None:raise RuntimeError('API detenida; revisar .performance/api.log')
            time.sleep(1)
    raise RuntimeError('La API no respondió a tiempo; revisar .performance/api.log')

def seed(target='local'):
    dbsql=sql
    if target=='supabase-develop':
        from develop import sql as dbsql, verify_api
        verify_api()
    elif target!='local':raise ValueError('Entorno desconocido')
    else:verify_api_local()
    cfg=json.loads(CONFIG.read_text()) if CONFIG.exists() else {}
    if not cfg or cfg.get('environment','local')!=target:

        accounts=[]
        # Existing initializer already provides CLIENTE role. Do not assume its ID.
        role=int(dbsql("SELECT id_rol FROM roles WHERE upper(nombre)='CLIENTE' LIMIT 1"))
        for i in range(10):
            account={'login':f'perf_{secrets.token_hex(4)}','clave':secrets.token_urlsafe(24)}
            api('/api/auth/registro',dict(account,nombre='Carga',apellido=f'Prueba{i}',telefono='70000000',rolId=role))
            accounts.append(account)
        cfg={'baseUrl':BASE,'accounts':accounts,'environment':target}
    services=api('/api/servicios/lista');stylists=api('/api/estilistas/lista')
    service=services[0];stylist=stylists[0]
    cfg.update(estilistaId=stylist['id'],servicioId=service['id'])
    # A new date range on each seed prevents accidental reuse across executions.
    last=dbsql("SELECT COALESCE(max(fecha_cita), (CURRENT_DATE + INTERVAL '365 days')::date)::text FROM citas")
    start=datetime.date.fromisoformat(last)+datetime.timedelta(days=1)
    # Horarios ficticios de lunes a sábado; no sobrescribir horarios ya existentes.
    dbsql(f"INSERT INTO horarios_estilistas (id_estilista,dia_semana,hora_inicio,hora_fin,hora_inicio_almuerzo,hora_fin_almuerzo,activo) SELECT {stylist['id']},d,'09:00'::time,'17:00'::time,'12:00'::time,'13:00'::time,true FROM generate_series(1,6) d WHERE NOT EXISTS (SELECT 1 FROM horarios_estilistas h WHERE h.id_estilista={stylist['id']} AND h.dia_semana=d)")
    days=[]
    while len(days)<100:
        if start.weekday()<6:days.append(start)
        start+=datetime.timedelta(days=1)
    cfg['fecha']=days[0].isoformat()
    cfg['slots']=[{'estilistaId':stylist['id'],'fechaCita':day.isoformat(),'horaInicio':'10:00','servicioIds':[service['id']]} for day in days]
    cfg['contentionSlot']={**cfg['slots'][0],'horaInicio':'14:00'}
    CONFIG.write_text(json.dumps(cfg,indent=2));CONFIG.chmod(0o600)
    # Ensure Cliente rows exist before concurrent writes to isolate appointment contention.
    dbsql("INSERT INTO clientes (id_usuario,nivel_fidelidad,puntos_acumulados,es_walkin,fecha_creacion) SELECT u.id_usuario,'Bronce',0,false,now() FROM usuarios u WHERE u.login LIKE 'perf_%' AND NOT EXISTS (SELECT 1 FROM clientes c WHERE c.id_usuario=u.id_usuario)")
    print('Cuentas y franjas configuradas en '+target+'. Secretos guardados fuera de Git.')

def stop():
    pid=STATE/'api.pid'
    if pid.exists():
        process=int(pid.read_text())
        command=pathlib.Path(f'/proc/{process}/cmdline')
        if command.exists():
            if b'shushine-studio-api-0.0.1-SNAPSHOT.jar' not in command.read_bytes():raise RuntimeError('El PID ya no corresponde a la API de pruebas')
            try:os.kill(process,15)
            except ProcessLookupError:pass
        pid.unlink()
    subprocess.run([str(PG/'pg_ctl'),'-D',str(STATE/'pgdata'),'stop','-m','fast'],env=env_pg(),check=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('action',choices=['start','seed','stop']);a=parser.parse_args()
    globals()[a.action]()
