#!/usr/bin/env python3
"""Regresiones contra PostgreSQL real, limitadas a develop y registros propios."""
import concurrent.futures, datetime, json, subprocess, threading, uuid
from pathlib import Path
from develop import config, pg_env, sql, PG, STATE

c=config()
tag='guardtest:'+uuid.uuid4().hex
client=int(sql("SELECT c.id_cliente FROM clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.login LIKE 'perf_%' ORDER BY c.id_cliente LIMIT 1"))
stylists=[int(x) for x in sql('SELECT id_estilista FROM estilistas WHERE activo ORDER BY id_estilista LIMIT 2').splitlines()]
assert len(stylists)==2,'Se necesitan dos estilistas ficticios activos'
day=datetime.date.fromisoformat(sql("SELECT COALESCE(max(fecha_cita),CURRENT_DATE)::text FROM citas"))+datetime.timedelta(days=200)
results=[]
def insert(date=day,start='10:00',end='11:00',state='Confirmed',stylist=stylists[0]):
    code='TEST-'+uuid.uuid4().hex[:15]
    return f"INSERT INTO public.citas (codigo_cita,id_cliente,id_estilista,fecha_cita,hora_inicio,hora_fin,estado,subtotal,iva,total,notas_cliente) VALUES ('{code}',{client},{stylist},'{date}','{start}','{end}','{state}',10,1.3,11.3,'{tag}');"
def execute(query):
    r=subprocess.run([str(PG/'psql'),'-X','-v','ON_ERROR_STOP=1'],input='\\set VERBOSITY verbose\n'+query,text=True,capture_output=True,env=pg_env(c),timeout=30)
    return r.returncode,r.stderr

def expect(name,query,error=None):
    rc,stderr=execute(query)
    assert (rc==0 if error is None else rc!=0 and error in stderr),name+': '+stderr
    results.append({'case':name,'passed':True,'expectedSQLState':error})
try:
    expect('Reserva inicial',insert())
    expect('Solapamiento parcial rechazado',insert(start='10:30',end='11:30'),'23P01')
    expect('Franjas adyacentes permitidas',insert(start='11:00',end='12:00'))
    expect('Otro estilista permitido',insert(stylist=stylists[1]))
    expect('Cancelada no bloquea',insert(state='Cancelled'))
    expect('Reactivar cancelada no puede solapar',f"UPDATE citas SET estado='Confirmed' WHERE notas_cliente='{tag}' AND estado='Cancelled';",'23P01')
    expect('Duración cero rechazada',insert(start='14:00',end='14:00'),'23514')
    expect('Horario invertido rechazado',insert(start='15:00',end='14:00'),'23514')
    barrier=threading.Barrier(8)
    def compete(_):
        query=insert(date=day+datetime.timedelta(days=1))
        barrier.wait()
        return execute('BEGIN; SET LOCAL lock_timeout=\'10s\'; '+query+'SELECT pg_sleep(0.5); COMMIT;')
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool: responses=list(pool.map(compete,range(8)))
    assert sum(rc==0 for rc,_ in responses)==1,responses
    assert all(rc==0 or '23P01' in err for rc,err in responses),responses
    results.append({'case':'8 conexiones por misma franja','passed':True,'created':1,'overlapsRejected':7})
    def distinct(i):return execute('BEGIN; '+insert(date=day+datetime.timedelta(days=10+i))+'SELECT pg_sleep(0.2); COMMIT;')
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool: responses=list(pool.map(distinct,range(5)))
    assert all(rc==0 for rc,_ in responses),responses
    results.append({'case':'5 fechas distintas concurrentes','passed':True,'created':5})
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
        codes=list(pool.map(lambda _:sql("SELECT nextval('public.cita_codigo_seq')"),range(5)))
    assert len(set(codes))==5
    results.append({'case':'Secuencia concurrente sin duplicados','passed':True})
    expect('Cancelar libera la franja',f"UPDATE citas SET estado='Cancelled' WHERE notas_cliente='{tag}' AND estado='Confirmed' AND fecha_cita='{day}' AND hora_inicio='10:00' AND id_estilista={stylists[0]};"+insert())
finally:
    sql(f"DELETE FROM public.citas WHERE notas_cliente='{tag}'")
(STATE/'reservation-db-regression.json').write_text(json.dumps({'projectRef':c['projectRef'],'checks':results},indent=2))
print(json.dumps(results,indent=2))
