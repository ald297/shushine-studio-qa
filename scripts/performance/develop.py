#!/usr/bin/env python3
"""Importa esquema sin datos e inicia una API local conectada solo a Supabase develop."""
import argparse, json, os, pathlib, re, secrets, subprocess, time, urllib.request
from lab import ROOT, STATE, RUNTIME, PG, env_pg, api
FILE=STATE/'develop.local.json'
FORBIDDEN={'acikahicfjtojuvqcvxv','fbxygbcgnpgtfplwpkxu','mhbssowayuiuagcykpdx'}
def config():
    c=json.loads(FILE.read_text());ref=c['projectRef']
    if not re.fullmatch('[a-z0-9]{20}',ref) or ref in FORBIDDEN:raise ValueError('Configurar referencia exclusiva del nuevo proyecto develop')
    host=c['host'];user=c['username']
    if not (host==f'db.{ref}.supabase.co' or (host.endswith('.pooler.supabase.com') and user==f'postgres.{ref}')):raise ValueError('Host/usuario no corresponde al proyecto develop')
    if not c['password'] or '\n' in host:raise ValueError('Falta contraseña local de develop')
    if c.get('database','postgres')!='postgres' or int(c['port']) not in (5432,6543):raise ValueError('Base o puerto no permitido')
    if int(c['port'])==6543:raise ValueError('Usar conexión directa o session pooler en puerto 5432 para DDL/JPA')
    return c
def pg_env(c):
    return dict(env_pg(),PGHOST=c['host'],PGPORT=str(c['port']),PGDATABASE='postgres',PGUSER=c['username'],PGPASSWORD=c['password'],PGSSLMODE='require',PGCONNECT_TIMEOUT='15')
def sql(query):
    c=config();r=subprocess.run([str(PG/'psql'),'-At','-v','ON_ERROR_STOP=1'],input=query,text=True,capture_output=True,env=pg_env(c))
    if r.returncode:raise RuntimeError(r.stderr)
    return r.stdout.strip()
def verify_api():
    c=config();pid=int((STATE/'api.pid').read_text())
    values=pathlib.Path(f'/proc/{pid}/environ').read_bytes().split(b'\x00')
    expected=f"SPRING_DATASOURCE_URL=jdbc:postgresql://{c['host']}:{c['port']}/postgres?sslmode=require".encode()
    if expected not in values:raise RuntimeError('La API activa no apunta al proyecto develop configurado')
def migrate():
    c=config()
    migration=ROOT/'database/migrations/20261016_reservas_sin_solapamientos.sql'
    subprocess.run([str(PG/'psql'),'-X','-v','ON_ERROR_STOP=1'],input=migration.read_text(),text=True,
                   env=pg_env(c),check=True,stdout=subprocess.DEVNULL)
    print('Protección de reservas y secuencia verificadas en develop:',c['projectRef'])

def initialize():
    c=config()
    count=int(sql("SELECT count(*) FROM pg_tables WHERE schemaname='public'"))
    if count:raise RuntimeError('El esquema public ya contiene tablas; no se importará sobre una base existente')
    source=STATE/'production-public-schema.sql'
    schema=source.read_text().replace('CREATE SCHEMA public;','CREATE SCHEMA IF NOT EXISTS public;')
    if c['projectRef'] in FORBIDDEN or 'acikahicfjtojuvqcvxv' in schema:raise RuntimeError('Referencia de producción detectada')
    r=subprocess.run([str(PG/'psql'),'-X','--single-transaction','-v','ON_ERROR_STOP=1'],input=schema,text=True,env=pg_env(c),stdout=subprocess.DEVNULL,capture_output=False)
    if r.returncode:raise RuntimeError('Falló importación transaccional; revisar diagnóstico anterior')
    print('Tablas importadas en develop:',sql("SELECT count(*) FROM pg_tables WHERE schemaname='public'"))
def start():
    c=config();pidfile=STATE/'api.pid'
    # Stop only the API launched by our lab after verifying command identity.
    if pidfile.exists():
        pid=int(pidfile.read_text());proc=pathlib.Path(f'/proc/{pid}/cmdline')
        if proc.exists():
            # A child started by this orchestrator may have exited but still await reaping.
            def exited():
                try:state=pathlib.Path(f'/proc/{pid}/stat').read_text().split()[2]
                except FileNotFoundError:return True
                if state=='Z':
                    try:os.waitpid(pid,os.WNOHANG)
                    except ChildProcessError:pass
                    return True
                return False
            if not exited():
                if b'shushine-studio-api-0.0.1-SNAPSHOT.jar' not in proc.read_bytes():raise RuntimeError('PID no corresponde al laboratorio')
                os.kill(pid,15)
                for _ in range(30):
                    if exited():break
                    time.sleep(1)
                else:raise RuntimeError('API anterior aún activa')
    java=pathlib.Path((RUNTIME/'java-home.txt').read_text())/'bin/java'
    jar=ROOT/'backend-api/target/shushine-studio-api-0.0.1-SNAPSHOT.jar'
    jdbc=f"jdbc:postgresql://{c['host']}:{c['port']}/postgres?sslmode=require"
    env=dict(os.environ,SPRING_DATASOURCE_URL=jdbc,SPRING_DATASOURCE_USERNAME=c['username'],SPRING_DATASOURCE_PASSWORD=c['password'],
        SECURITY_JWT_SECRET_KEY=secrets.token_hex(64),SUPABASE_URL=f"https://{c['projectRef']}.supabase.co",SUPABASE_SERVICE_ROLE_KEY='disabled-for-api-load-tests')
    args=[str(java),'-jar',str(jar),'--server.address=127.0.0.1','--server.port=18080','--spring.jpa.hibernate.ddl-auto=none','--spring.jpa.show-sql=false',
        '--performance.telemetry.enabled=true','--performance.telemetry.file='+str(STATE/'telemetry.ndjson'),
        '--spring.jpa.properties.hibernate.generate_statistics=true','--logging.level.org.hibernate.stat=ERROR',
        '--logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=ERROR',
        '--spring.datasource.url='+jdbc,'--spring.datasource.username='+c['username'],
        '--supabase.url='+f"https://{c['projectRef']}.supabase.co",'--supabase.service-role-key=disabled-for-api-load-tests']
    with (STATE/'api-develop.log').open('ab') as log:p=subprocess.Popen(args,env=env,stdout=log,stderr=log,start_new_session=True)
    pidfile.write_text(str(p.pid))
    for _ in range(90):
        try:
            api('/api/servicios/lista')
            # Verify actual process environment, not merely HTTP availability.
            actual=pathlib.Path(f'/proc/{p.pid}/environ').read_bytes().split(b'\x00')
            if ('SPRING_DATASOURCE_URL='+jdbc).encode() not in actual:raise RuntimeError('La API no corresponde a develop')
            print('API local conectada a Supabase develop:',c['projectRef']);return
        except (urllib.error.URLError,ConnectionError):
            if p.poll() is not None:raise RuntimeError('API detenida: revisar .performance/api-develop.log')
            time.sleep(1)
    raise RuntimeError('API develop no respondió a tiempo')
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('action',choices=['check','initialize','start','seed','migrate']);a=p.parse_args()
    if a.action=='check':print('Conexión develop verificada:',sql('SELECT current_database()'))
    elif a.action=='initialize':initialize()
    elif a.action=='migrate':migrate()
    elif a.action=='seed':
        verify_api()
        from lab import seed
        seed('supabase-develop')
    else:start()
