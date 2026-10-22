#!/usr/bin/env python3
"""Four ten-minute trials: cold/warm x 10/20 rps, with private telemetry."""
import datetime, json, pathlib, subprocess, time, threading, os, argparse, hashlib, platform
from lab import ROOT, STATE
from develop import start, verify_api, sql

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('folder',nargs='?',type=pathlib.Path,help='Reanudar una serie existente entre bloques completos')
parser.add_argument('--rates',nargs='+',type=int,choices=[10,20],default=[10,20],help='Tasas a verificar; por defecto 10 y 20')
args=parser.parse_args()
if args.folder and not (args.folder/'runs.json').is_file():parser.error('La serie existente debe contener runs.json')
OUTPUT=args.folder.resolve() if args.folder else ROOT/'tests/performance/results'/('sustained-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ'))
OUTPUT.mkdir(parents=True,exist_ok=bool(args.folder))
status=OUTPUT/'progress.json'
metadata=OUTPUT/'metadata.json'
if not metadata.exists():
    jar=ROOT/'backend-api/target/shushine-studio-api-0.0.1-SNAPSHOT.jar'
    metadata.write_text(json.dumps({'jarSha256':hashlib.sha256(jar.read_bytes()).hexdigest(),
        'startedUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'rates':args.rates,
        'generatorAndApiShareHost':True,'cpuCores':os.cpu_count(),'platform':platform.platform(),
        'databaseRegion':'ca-central-1','productionChanged':False,
        'coldDefinition':'API restarted; normal preflight and 10 setup logins precede measured traffic. Database is not restarted.',
        'warmDefinition':'Measured cold trial, two minute warmup, then measured warm trial.',
        'goal':'p95 <1000ms for each read route and global mixture; no HTTP errors or dropped iterations',
        'telemetryPeriodSeconds':2,'databaseProbePeriodSeconds':10,'pgStatementsPeriodSeconds':30},indent=2))

def progress(**values):
    temporary=status.with_suffix('.tmp');temporary.write_text(json.dumps(values,indent=2));temporary.replace(status)
    print(json.dumps(values),flush=True)

def sample_host(stop, destination):
    pid=int((STATE/'api.pid').read_text())
    previous=None
    with destination.open('w') as out:
        while not stop.is_set():
            try:
                now=time.time()
                fields=pathlib.Path(f'/proc/{pid}/stat').read_text().split()
                cpu=(int(fields[13])+int(fields[14]))/os.sysconf('SC_CLK_TCK')
                row={'time':now,'apiRssBytes':int(fields[23])*os.sysconf('SC_PAGE_SIZE'),'hostLoad':os.getloadavg(),
                    'cpuCores':os.cpu_count(),'hostCpu':pathlib.Path('/proc/stat').read_text().splitlines()[0],
                    'network':pathlib.Path('/proc/net/dev').read_text().splitlines()[2:]}
                if previous:row['apiCpuCoresUsed']=(cpu-previous[1])/(now-previous[0])
                previous=(now,cpu)
                out.write(json.dumps(row)+'\n');out.flush()
            except (OSError,ValueError):pass
            stop.wait(2)

def sample_database(stop,destination):
    query="""SELECT json_build_object('activity',(SELECT json_agg(x) FROM
        (SELECT state,wait_event_type,wait_event,count(*) AS connections FROM pg_stat_activity
         WHERE datname=current_database() AND pid<>pg_backend_pid() GROUP BY 1,2,3) x),
         'database',(SELECT row_to_json(d) FROM (SELECT xact_commit,xact_rollback,blks_read,blks_hit,
          tup_returned,tup_fetched,deadlocks,temp_bytes,blk_read_time,blk_write_time
          FROM pg_stat_database WHERE datname=current_database()) d))"""
    with destination.open('w') as out:
        while not stop.is_set():
            begin=time.time()
            try:row={'time':begin,'stats':json.loads(sql(query)),'psqlConnectAndQueryMs':(time.time()-begin)*1000}
            except Exception as error:row={'time':begin,'errorType':type(error).__name__}
            out.write(json.dumps(row)+'\n');out.flush();stop.wait(10)

def trial(label,rate,duration='10m'):
    verify_api()
    telemetry=STATE/'telemetry.ndjson'
    offset=telemetry.stat().st_size if telemetry.exists() else 0
    marker=time.time()
    progress(trial=label,rate=rate,duration=duration,state='running',output=str(OUTPUT))
    stop=threading.Event();thread=threading.Thread(target=sample_host,args=(stop,OUTPUT/(label+'-host.ndjson')),daemon=True);thread.start()
    dbthread=threading.Thread(target=sample_database,args=(stop,OUTPUT/(label+'-database.ndjson')),daemon=True);dbthread.start()
    with (OUTPUT/(label+'.log')).open('w') as log:
        result=subprocess.run(['python3',str(ROOT/'scripts/performance/run.py'),'soak','--rate',str(rate),'--duration',duration],stdout=log,stderr=subprocess.STDOUT)
    stop.set();thread.join(5);dbthread.join(20)
    if telemetry.exists():
        with telemetry.open('rb') as stream:stream.seek(offset);(OUTPUT/(label+'-telemetry.ndjson')).write_bytes(stream.read())
    candidates=[p for p in (ROOT/'tests/performance/results').glob('*-soak') if p.stat().st_mtime>=marker]
    entry={'label':label,'rate':rate,'duration':duration,'exitCode':result.returncode,'result':str(max(candidates,key=lambda p:p.stat().st_mtime)) if candidates else None}
    records.append(entry);(OUTPUT/'runs.json').write_text(json.dumps(records,indent=2));progress(**entry,state='finished')
    if result.returncode not in (0,99):raise RuntimeError('Trial failed; inspect log')

records=json.loads((OUTPUT/'runs.json').read_text()) if (OUTPUT/'runs.json').exists() else []
progress(state='starting',output=str(OUTPUT))
observer_log=(OUTPUT/'database-observer.log').open('a')
observer=subprocess.Popen(['python3',str(ROOT/'scripts/performance/database_observer.py'),str(OUTPUT)],stdout=observer_log,stderr=subprocess.STDOUT)
try:
    for rate in args.rates:
        if any(r['label']==f'warm-{rate}' for r in records):continue
        if any(r['label'] in (f'cold-{rate}',f'warmup-{rate}') for r in records):
            raise RuntimeError('Reanudar solo entre bloques completos; iniciar serie nueva si el bloque quedó incompleto')
        progress(state='restarting',rate=rate)
        start()
        trial(f'cold-{rate}',rate)
        # Explicit warmup is kept separate from measured ten-minute trials.
        trial(f'warmup-{rate}',rate,'2m')
        trial(f'warm-{rate}',rate)
    progress(state='complete',output=str(OUTPUT))
    observer.wait(timeout=40)
finally:
    if observer.poll() is None:
        observer.terminate()
        try:observer.wait(timeout=10)
        except subprocess.TimeoutExpired:observer.kill();observer.wait()
    observer_log.close()
