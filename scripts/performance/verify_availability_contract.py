#!/usr/bin/env python3
"""Compare old/new availability responses using disposable develop fixtures."""
import argparse,datetime,json,urllib.request,urllib.error,uuid
from develop import sql,verify_api
from lab import STATE,CONFIG,BASE
parser=argparse.ArgumentParser();parser.add_argument('action',choices=['before','after']);args=parser.parse_args()
verify_api();file=STATE/'availability-contract.json'
def request(path):
    try:
        with urllib.request.urlopen(BASE+path,timeout=10) as r:return {'status':r.status,'body':json.load(r)}
    except urllib.error.HTTPError as r:return {'status':r.code,'body':json.load(r)}
def cleanup(c):
    stylist=c['stylist'];tag=c['tag']
    sql(f"BEGIN; DELETE FROM citas WHERE notas_cliente='{tag}'; DELETE FROM bloqueos_horarios WHERE id_estilista={stylist}; DELETE FROM horarios_estilistas WHERE id_estilista={stylist}; DELETE FROM estilistas WHERE id_estilista={stylist} AND nombre_completo='{tag}'; COMMIT;")
if args.action=='before':
    tag='availability-'+uuid.uuid4().hex
    stylist=int(sql(f"INSERT INTO estilistas(nombre_completo,especialidad_principal,activo) VALUES ('{tag}','Prueba',true) RETURNING id_estilista").splitlines()[0])
    c={'tag':tag,'stylist':stylist}
    file.write_text(json.dumps(c))
    try:
        client=int(sql("SELECT c.id_cliente FROM clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.login LIKE 'perf_%' ORDER BY c.id_cliente LIMIT 1"))
        day=datetime.date(2030,1,7);service=json.loads(CONFIG.read_text())['servicioId']
        sql(f"""BEGIN;
        INSERT INTO horarios_estilistas(id_estilista,dia_semana,hora_inicio,hora_fin,hora_inicio_almuerzo,hora_fin_almuerzo) VALUES ({stylist},1,'09:00','17:00','12:00','13:00');
        INSERT INTO bloqueos_horarios(id_estilista,fecha,hora_inicio,hora_fin,motivo) VALUES ({stylist},'{day}','15:00','16:00','Capacitación');
        INSERT INTO citas(codigo_cita,id_cliente,id_estilista,fecha_cita,hora_inicio,hora_fin,estado,subtotal,total,notas_cliente) VALUES
        ('T-{uuid.uuid4().hex[:18]}',{client},{stylist},'{day}','10:00','11:00','Confirmed',10,10,'{tag}'),
        ('T-{uuid.uuid4().hex[:18]}',{client},{stylist},'{day}','14:00','15:00','Cancelled',10,10,'{tag}');COMMIT;""")
        base=f'/api/estilistas/{stylist}/disponibilidad?fecha={day}'
        paths=[base,base+'&duracionMinutos=60',base+f'&servicioId={service}',base+f'&servicioId={service}&duracionMinutos=60',base+f'&servicioId={service}&duracionMinutos=0',base+'&servicioId=2147483647',f'/api/estilistas/{stylist}/disponibilidad?fecha=2030-01-06']
        c['cases']=[{'path':p,'expected':request(p)} for p in paths];file.write_text(json.dumps(c,indent=2))
        assert all(case['expected']['status']==200 for case in c['cases'])
        print('Respuestas anteriores guardadas:',len(c['cases']))
    except Exception:cleanup(c);raise
else:
    c=json.loads(file.read_text());results=[]
    try:
        for case in c['cases']:
            actual=request(case['path']);assert actual==case['expected'],case['path']
            results.append({'path':case['path'],'passed':True})
        # Immediate reads see changes made directly in Supabase; no availability cache.
        stylist=c['stylist'];base=c['cases'][0]['path']
        before=request(base)['body'];assert next(f for f in before['franjas'] if datetime.time.fromisoformat(f['horaInicio'])==datetime.time(10,0))['disponible'] is False
        sql(f"UPDATE citas SET estado='Cancelled' WHERE notas_cliente='{c['tag']}' AND estado='Confirmed'")
        after=request(base)['body'];assert next(f for f in after['franjas'] if datetime.time.fromisoformat(f['horaInicio'])==datetime.time(10,0))['disponible'] is True
        results.append({'case':'Cancelación externa visible en la siguiente consulta','passed':True})
    finally:cleanup(c)
    (STATE/'availability-contract-verification.json').write_text(json.dumps({'cases':results,'passed':True,'fixturesCleaned':True},indent=2));print('Contrato y frescura verificados:',len(results))
