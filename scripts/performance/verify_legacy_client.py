#!/usr/bin/env python3
"""Dos reservas paralelas de un usuario antiguo que todavía no tiene perfil Cliente."""
import concurrent.futures, datetime, json, secrets, threading, urllib.request, urllib.error, uuid
from lab import api, BASE, STATE
from develop import verify_api, sql
verify_api()
login='legacy_perf_'+secrets.token_hex(4)
role=int(sql("SELECT id_rol FROM roles WHERE upper(nombre)='CLIENTE' LIMIT 1"))
user=api('/api/auth/registro',{'nombre':'Prueba','apellido':'PerfilAntiguo','telefono':'70000000','login':login,'clave':secrets.token_urlsafe(24),'rolId':role})
uid=str(uuid.UUID(user['id']));tag='legacycheck:'+uuid.uuid4().hex
try:
    sql(f"DELETE FROM clientes WHERE id_usuario='{uid}'")
    start=datetime.date.fromisoformat(sql('SELECT max(fecha_cita)::text FROM citas'))+datetime.timedelta(days=50)
    days=[]
    while len(days)<2:
        if start.weekday()<6:days.append(start)
        start+=datetime.timedelta(days=1)
    stylist=int(sql('SELECT id_estilista FROM estilistas WHERE activo ORDER BY id_estilista LIMIT 1'))
    service=int(sql('SELECT id_servicio FROM servicios WHERE activo AND duracion_minutos<=120 ORDER BY id_servicio LIMIT 1'))
    barrier=threading.Barrier(2)
    def book(i):
        payload={'estilistaId':stylist,'fechaCita':days[i].isoformat(),'horaInicio':'10:00','servicioIds':[service],'notasCliente':tag}
        req=urllib.request.Request(BASE+'/api/citas',data=json.dumps(payload).encode(),headers={'Content-Type':'application/json','Authorization':'Bearer '+user['token']})
        barrier.wait()
        try:
            with urllib.request.urlopen(req,timeout=20) as r:return r.status,json.load(r)
        except urllib.error.HTTPError as e:return e.code,json.loads(e.read())
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:responses=list(pool.map(book,range(2)))
    assert all(code==201 for code,_ in responses),responses
    profiles=int(sql(f"SELECT count(*) FROM clientes WHERE id_usuario='{uid}'"))
    assert profiles==1
    assert len({body['codigoCita'] for _,body in responses})==2
    result={'requests':2,'created':2,'clientProfiles':profiles,'distinctCodes':2,'passed':True}
    (STATE/'legacy-client-verification.json').write_text(json.dumps(result,indent=2));print(result)
finally:
    sql(f"DELETE FROM citas WHERE notas_cliente='{tag}'; DELETE FROM clientes WHERE id_usuario='{uid}'; DELETE FROM usuarios WHERE id_usuario='{uid}';")
