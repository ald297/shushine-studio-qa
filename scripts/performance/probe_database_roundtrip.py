#!/usr/bin/env python3
"""Measure SELECT 1 round trips on one already-connected develop psql session."""
import argparse,datetime,json,pathlib,re,statistics,subprocess
from develop import config,pg_env,verify_api
from lab import PG
from analyze_sustained import percentile
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('output',type=pathlib.Path);args=parser.parse_args()
verify_api();target=args.output;c=config()
query='\\timing on\n'+'SELECT 1;\n'*20+'EXPLAIN (ANALYZE, FORMAT JSON) SELECT 1;\n'
begin=datetime.datetime.now(datetime.timezone.utc).isoformat()
r=subprocess.run([str(PG/'psql'),'-X','-At','-v','ON_ERROR_STOP=1'],input=query,text=True,capture_output=True,env=dict(pg_env(c),LC_ALL='C'),check=True)
times=[float(x) for x in re.findall(r'Time: ([0-9.]+) ms',r.stdout)]
if len(times)!=21:raise RuntimeError('Unexpected probe output')
start=r.stdout.index('[');end=r.stdout.rindex(']')+1
plan=json.loads(r.stdout[start:end])[0]
report={'time':begin,'destination':'supabase-develop','samples':20,'clientRoundTripsMs':times[:20],
        'clientMedianMs':statistics.median(times[:20]),'clientP95Ms':percentile(times[:20]),
        'select1ServerExecutionMs':plan['Execution Time'],'select1ServerPlanningMs':plan['Planning Time'],
        'definition':'psql timing after connection; includes client, protocol, pooler, network and trivial SQL, not pure network RTT'}
target.write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
