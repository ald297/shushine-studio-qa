#!/usr/bin/env python3
"""Ejecuta k6 y verifica citas persistidas en el laboratorio local."""
import argparse, datetime, json, os, pathlib, shutil, subprocess, sys, uuid
from lab import ROOT, STATE, CONFIG, sql, seed
p=argparse.ArgumentParser()
p.add_argument('mode',choices=['smoke','baseline','stress','spike','soak','booking','contention'])
p.add_argument('--vus',type=int,default=10)
p.add_argument('--duration',default='1h')
p.add_argument('--rate',type=int,default=5)
p.add_argument('--iterations',type=int,default=10)
p.add_argument('--quick',action='store_true',help='Estrés exploratorio de 185 s en lugar de 32 min')
p.add_argument('--dashboard',action='store_true',help='Panel local 5665; cerrar pestaña para finalizar k6')
a=p.parse_args()
if a.quick and a.mode!='stress':p.error('--quick solo aplica a stress')
if not 1<=a.vus<=50 or not 1<=a.iterations<=100 or not 1<=a.rate<=80:p.error('Límites: VUs 1–50, iteraciones 1–100, tasa 1–80')
if not CONFIG.exists():p.error('Ejecutar lab.py start y lab.py seed primero')
cfg=json.loads(CONFIG.read_text())
target=cfg.get('environment','local')
if target=='supabase-develop':
    from develop import sql, verify_api
    verify_api()
elif target!='local':p.error('Entorno desconocido')
else:
    from lab import verify_api_local
    verify_api_local()
if a.mode in ['booking','contention']:
    seed(target)
    cfg=json.loads(CONFIG.read_text())
if cfg['baseUrl']!='http://127.0.0.1:18080':p.error('Destino distinto al laboratorio local')
# Confirm local database identity before traffic.
assert sql('SELECT current_database()')==('postgres' if target=='supabase-develop' else 'shushine_perf')
runid='perf-'+uuid.uuid4().hex
out=ROOT/'tests/performance/results'/(datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')+'-'+a.mode)
out.mkdir(parents=True,exist_ok=False)
env=dict(os.environ,PERF_QUICK='1' if a.quick else '0',PERF_CONFIG=str(CONFIG),PERF_MODE=a.mode,PERF_RUN_ID=runid,PERF_VUS=str(a.vus),PERF_DURATION=a.duration,PERF_RATE=str(a.rate),PERF_ITERATIONS=str(a.iterations),
    K6_WEB_DASHBOARD='true',K6_WEB_DASHBOARD_HOST='127.0.0.1',K6_WEB_DASHBOARD_PORT='5665' if a.dashboard else '-1',
    K6_WEB_DASHBOARD_PERIOD='1s',K6_WEB_DASHBOARD_EXPORT=str(out/'report.html'))
k6=shutil.which('k6') or str(pathlib.Path.home()/'.local/bin/k6')
cmd=[k6,'run','--out',f'json={out / "metrics.json"}',str(ROOT/'tests/performance/suite.js')]
print('Resultados:',out,flush=True)
r=subprocess.run(cmd,cwd=out,env=env)
report={'runId':runid,'mode':a.mode,'quick':a.quick,'k6ExitCode':r.returncode,'environment':target+'; no equivale a producción','baseUrl':cfg['baseUrl']}
if a.mode in ['booking','contention']:
    # runid is generated internally and contains only hexadecimal digits plus prefix.
    count=int(sql(f"SELECT count(*) FROM citas WHERE notas_cliente='{runid}:{a.mode}'"))
    expected=1 if a.mode=='contention' else a.iterations
    overlaps=int(sql(f"SELECT count(*) FROM citas a JOIN citas b ON a.id_cita<b.id_cita AND a.id_estilista=b.id_estilista AND a.fecha_cita=b.fecha_cita AND a.hora_inicio<b.hora_fin AND b.hora_inicio<a.hora_fin WHERE a.estado NOT IN ('Cancelled','CANCELADA','NO_ASISTIO') AND b.estado NOT IN ('Cancelled','CANCELADA','NO_ASISTIO') AND (a.notas_cliente='{runid}:{a.mode}' OR b.notas_cliente='{runid}:{a.mode}')"))
    report.update(persisted=count,expected=expected,overlappingPairs=overlaps,integrityPassed=count==expected and overlaps==0,
        conflictInterpretation='409 requiere correlación con logs; integridad persistida verificada por separado')
    if not report['integrityPassed']:r.returncode=r.returncode or 1
(out/'verification.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
sys.exit(r.returncode)
