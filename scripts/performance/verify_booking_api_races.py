#!/usr/bin/env python3
"""HTTP regressions for adjacent slots, partial overlaps and cancellation ownership."""
import concurrent.futures,datetime,json,urllib.request,urllib.error,uuid
from lab import BASE,CONFIG,STATE
from develop import sql,verify_api
verify_api();cfg=json.loads(CONFIG.read_text());tag='booking-races-'+uuid.uuid4().hex
account=cfg['accounts'][0]
def request(path,body,method='POST',token=None):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 req=urllib.request.Request(BASE+path,data=json.dumps(body).encode(),method=method,headers=headers)
 try:
  with urllib.request.urlopen(req,timeout=20) as r:return r.status,json.load(r)
 except urllib.error.HTTPError as r:return r.code,json.load(r)
status,auth=request('/api/auth/login',dict(login=account['login'],clave=account['clave']));assert status==200;token=auth['token']
style=cfg['estilistaId'];service=cfg['servicioId'];duration=int(sql(f'SELECT duracion_minutos FROM servicios WHERE id_servicio={service}'))
assert duration>0 and duration<=180
day=datetime.date.fromisoformat(sql('SELECT max(fecha_cita)::text FROM citas'))+datetime.timedelta(days=20)
def payload(date,hour,stylist=style):return dict(estilistaId=stylist,fechaCita=str(date),horaInicio=hour,servicioIds=[service],notasCliente=tag)
def post(body):return request('/api/citas',body,token=token)
def parallel(items):
 with concurrent.futures.ThreadPoolExecutor(max_workers=len(items)) as pool:return list(pool.map(post,items))
checks=[]
try:
 second=(datetime.datetime.combine(day,datetime.time(9))+datetime.timedelta(minutes=duration)).time().isoformat()
 responses=parallel([payload(day,'09:00'),payload(day,second)]);assert [s for s,_ in responses]==[201,201],responses
 checks.append({'case':'Franjas adyacentes del mismo estilista y día','created':2,'passed':True})
 other=int(sql(f'SELECT id_estilista FROM estilistas WHERE activo AND id_estilista<>{style} ORDER BY id_estilista LIMIT 1'))
 status,_=post(payload(day,'09:00',other));assert status==201
 checks.append({'case':'Otro estilista a la misma hora','created':1,'passed':True})
 identifier=responses[0][1]['id']
 status,_=request(f'/api/citas/{identifier}/cancelar',{'motivo':'Regresión controlada'},'PUT',token);assert status==200
 assert sql(f'SELECT version FROM citas WHERE id_cita={identifier}')=='1'
 status,_=post(payload(day,'09:00'));assert status==201
 checks.append({'case':'Cancelación por propietario conserva versión optimista y libera franja','passed':True})
 nextday=day+datetime.timedelta(days=1)
 overlap=(datetime.datetime.combine(nextday,datetime.time(14))+datetime.timedelta(minutes=max(1,duration//2))).time().isoformat()
 responses=parallel([payload(nextday,'14:00'),payload(nextday,overlap)])
 assert sorted(s for s,_ in responses)==[201,409],responses
 checks.append({'case':'Solapamiento parcial con distintas horas de inicio','created':1,'conflicts':1,'passed':True})
finally:sql(f"DELETE FROM citas WHERE notas_cliente='{tag}'")
(STATE/'booking-api-races-verification.json').write_text(json.dumps({'passed':True,'checks':checks,'fixturesCleaned':True},indent=2));print(json.dumps(checks,indent=2))
