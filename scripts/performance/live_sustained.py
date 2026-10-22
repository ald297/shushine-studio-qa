#!/usr/bin/env python3
import json,pathlib,datetime,time
from analyze_sustained import percentile
root=pathlib.Path(__file__).resolve().parents[2]
folder=max((root/'tests/performance/results').glob('sustained-*'),key=lambda p:p.stat().st_mtime)
progress=json.loads((folder/'progress.json').read_text());print(progress)
results=list((root/'tests/performance/results').glob('*-soak'))
if results:
    latest=max(results,key=lambda p:p.stat().st_mtime);values=[];errors=0;dropped=0;operations={}
    for line in (latest/'metrics.json').open():
        try:item=json.loads(line)
        except json.JSONDecodeError:continue
        if item.get('type')=='Point' and item.get('metric')=='dropped_iterations':dropped+=item['data']['value']
        if item.get('type')!='Point' or item.get('metric')!='http_req_duration':continue
        d=item['data']
        if d.get('tags',{}).get('name') not in ('catalogo','disponibilidad','mis_citas'):continue
        t=datetime.datetime.fromisoformat(d['time'].replace('Z','+00:00')).timestamp()
        if t>time.time()-60:
            values.append(d['value']);operations.setdefault(d['tags']['name'],[]).append(d['value']);errors+=d.get('tags',{}).get('status')!='200'
    print('last60s',len(values),'requests; p95Ms',percentile(values),'errors',errors,'droppedTotal',dropped)
    print('routeP95Ms', {name:percentile(values) for name,values in operations.items()})
p=root/'.performance/telemetry.ndjson'
if p.exists():
    row=json.loads(p.read_text().splitlines()[-1]);print('pool',row.get('pool'))
