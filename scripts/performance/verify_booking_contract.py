#!/usr/bin/env python3
"""Compare financial, duration and response contracts across booking implementations."""
import argparse,datetime,json,urllib.request,urllib.error,uuid
from lab import BASE,STATE,CONFIG
from develop import verify_api,sql
p=argparse.ArgumentParser();p.add_argument('action',choices=['before','after']);args=p.parse_args();verify_api()
file=STATE/'booking-contract.json';cfg=json.loads(CONFIG.read_text());account=cfg['accounts'][0]
def request(path,body,token=None):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 req=urllib.request.Request(BASE+path,data=json.dumps(body).encode(),headers=headers)
 try:
  with urllib.request.urlopen(req,timeout=20) as response:return response.status,json.load(response)
 except urllib.error.HTTPError as response:return response.code,json.load(response)
status,auth=request('/api/auth/login',{'login':account['login'],'clave':account['clave']});assert status==200;token=auth['token']
def exercise(c):
 out=[]
 try:
  for item in c['requests']:
   status,body=request('/api/citas',item,token)
   if status==201:
    assert body['codigoCita'].startswith('SHU-'+item['fechaCita'][:4]+'-')
    stored=json.loads(sql(f"SELECT json_build_object('detalles',count(*),'subtotal',sum(precio_aplicado),'duracion',sum(duracion_minutos)) FROM cita_servicios WHERE id_cita={int(body['id'])}"))
    assert stored['subtotal']==body['subtotal'];assert stored['duracion']>0
    body={k:v for k,v in body.items() if k not in ('id','codigoCita')}
   else:
    stored=None;body={k:v for k,v in body.items() if k in ('status','title','detail')}
   out.append({'status':status,'body':body,'stored':stored})
 finally:sql(f"DELETE FROM citas WHERE notas_cliente='{c['tag']}'")
 return out
if args.action=='before':
 services=[int(x) for x in sql('SELECT id_servicio FROM servicios WHERE activo ORDER BY id_servicio LIMIT 2').splitlines()];assert len(services)==2
 day=datetime.date.fromisoformat(sql('SELECT max(fecha_cita)::text FROM citas'))+datetime.timedelta(days=100)
 tag='booking-contract-'+uuid.uuid4().hex
 variants=[[services[0]],list(reversed(services)),[services[0],services[0]],[services[0],2147483647],[2147483647]]
 c={'tag':tag,'requests':[dict(estilistaId=cfg['estilistaId'],fechaCita=str(day+datetime.timedelta(days=i)),horaInicio='10:00',servicioIds=ids,notasCliente=tag,metodoPagoPreferente='Tarjeta') for i,ids in enumerate(variants)]}
 c['expected']=exercise(c);assert [r['status'] for r in c['expected']]==[201,201,201,201,400];file.write_text(json.dumps(c,indent=2));print('Contrato anterior capturado:',len(c['expected']))
else:
 c=json.loads(file.read_text());actual=exercise(c);assert actual==c['expected'],{'expected':c['expected'],'actual':actual}
 (STATE/'booking-contract-verification.json').write_text(json.dumps({'passed':True,'cases':len(actual),'financialAndDetailsPreserved':True,'fixturesCleaned':True},indent=2));print('Contrato financiero, duración y detalles coinciden:',len(actual))
