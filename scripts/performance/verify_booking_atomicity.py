#!/usr/bin/env python3
"""Exercise the exact JDBC CTE against develop, including detail-FK rollback."""
import datetime,decimal,json,re,uuid
from develop import verify_api,sql
from lab import ROOT,STATE
verify_api();tag='booking-atomic-'+uuid.uuid4().hex
client=int(sql("SELECT c.id_cliente FROM clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.login LIKE 'perf_%' ORDER BY c.id_cliente LIMIT 1"))
stylist=int(sql('SELECT id_estilista FROM estilistas WHERE activo ORDER BY id_estilista LIMIT 1'))
service,price,duration=sql('SELECT id_servicio,precio_base,duracion_minutos FROM servicios WHERE activo ORDER BY id_servicio LIMIT 1').split('|')
price=decimal.Decimal(price);iva=(price*decimal.Decimal('.13')).quantize(decimal.Decimal('.01'),rounding=decimal.ROUND_HALF_UP)
day=datetime.date.fromisoformat(sql('SELECT max(fecha_cita)::text FROM citas'))+datetime.timedelta(days=200)
end=(datetime.datetime.combine(day,datetime.time(10,0))+datetime.timedelta(minutes=int(duration))).time().isoformat()
source=(ROOT/'backend-api/src/main/java/com/shushinestudio/repositorios/ReservaJdbcRepository.java').read_text()
statement='WITH turno AS MATERIALIZED'+source.split('WITH turno AS MATERIALIZED',1)[1].split('"""',1)[0]
values={'cliente':str(client),'estilista':str(stylist),'fecha':f"'{day}'::date",'anio':str(day.year),
 'inicio':"'10:00'::time",'fin':f"'{end}'::time",'subtotal':str(price),'iva':str(iva),'total':str(price+iva),
 'metodo':"'Efectivo'",'notas':f"'{tag}'"}
def execute(service_id):
 details=json.dumps([dict(id=service_id,precio=float(price),duracion=int(duration))])
 params=dict(values,detalles="'"+details+"'")
 query=re.sub(r'(?<!:):([A-Za-z]\w*)',lambda m:params[m.group(1)],statement)
 return sql('\\set VERBOSITY verbose\n'+query)
checks=[]
try:
 assert sql('SELECT count(*) FROM servicios WHERE id_servicio=2147483647')=='0'
 try:execute(2147483647)
 except RuntimeError as error:assert '23503' in str(error)
 else:raise AssertionError('Expected detail FK failure')
 assert sql(f"SELECT count(*) FROM citas WHERE notas_cliente='{tag}'")=='0'
 checks.append({'case':'Fallo de detalle revierte la cita completa en autocommit','passed':True})
 inserted=execute(int(service));row_id,code,details=inserted.split('|')
 assert details=='1' and re.fullmatch(f'SHU-{day.year}-[0-9]{{4,}}',code)
 assert sql(f"SELECT count(*) FROM cita_servicios WHERE id_cita={int(row_id)}")=='1'
 assert sql(f"SELECT version=0 AND descuento_puntos=0 AND origen='App' AND estado='Confirmed' AND estado_pago='Pending' FROM citas WHERE id_cita={int(row_id)}")=='t'
 checks.append({'case':'Cita y detalle se confirman juntos con estado y versión correctos','passed':True})
 try:execute(int(service))
 except RuntimeError as error:assert '23P01' in str(error)
 else:raise AssertionError('Expected overlap rejection')
 assert sql(f"SELECT count(*) FROM citas WHERE notas_cliente='{tag}'")=='1'
 checks.append({'case':'La restricción de exclusión rechaza un segundo INSERT sin dejar detalles','passed':True})
 sql(f"UPDATE citas SET estado='Cancelled' WHERE notas_cliente='{tag}'")
 execute(int(service))
 assert sql(f"SELECT count(*) FROM citas WHERE notas_cliente='{tag}' AND estado='Confirmed'")=='1'
 checks.append({'case':'Cancelar libera la franja para una nueva reserva','passed':True})
finally:sql(f"DELETE FROM citas WHERE notas_cliente='{tag}'")
(STATE/'booking-atomicity-verification.json').write_text(json.dumps({'passed':True,'checks':checks,'fixturesCleaned':True},indent=2))
print(json.dumps(checks,indent=2))
