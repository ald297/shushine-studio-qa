#!/usr/bin/env python3
"""Summarize completed sustained trials and sampled cumulative telemetry."""
import argparse, datetime, json, math, pathlib, statistics

def percentile(values,p=0.95):
    if not values:return None
    values=sorted(values);i=(len(values)-1)*p;lo=math.floor(i);hi=math.ceil(i)
    return round(values[lo]+(values[hi]-values[lo])*(i-lo),3)
def mean(values):return round(statistics.mean(values),4) if values else None
def rows(path):
    if not path.exists():return []
    return [json.loads(line) for line in path.read_text().splitlines() if line.strip()]
def meters(row,name):return [x for x in row.get('meters',[]) if x['name']==name]
def total(row,name,key):return sum(m['values'].get(key,0) for m in meters(row,name))
def timer_delta(first,last,name):
    count=total(last,name,'COUNT')-total(first,name,'COUNT')
    seconds=total(last,name,'TOTAL_TIME')-total(first,name,'TOTAL_TIME')
    return {'count':count,'meanMs':round(1000*seconds/count,3) if count>0 else None,
            'totalSeconds':round(seconds,3)}
def summarize(folder,run):
    result=pathlib.Path(run['result']);label=run['label']
    summary=json.loads((result/'summary.json').read_text())['result']['metrics']
    operations={};samples=[]
    with (result/'metrics.json').open() as stream:
        for line in stream:
            item=json.loads(line)
            if item.get('type')!='Point' or item.get('metric')!='http_req_duration':continue
            data=item['data'];name=data.get('tags',{}).get('name')
            if name not in ('catalogo','disponibilidad','mis_citas'):continue
            value=data['value'];time=datetime.datetime.fromisoformat(data['time'].replace('Z','+00:00')).timestamp()
            operations.setdefault(name,[]).append(value);samples.append((time,value))
    first_time=min((t for t,_ in samples),default=0)
    minutes={}
    for t,value in samples:minutes.setdefault(int((t-first_time)//60),[]).append(value)
    out=dict(run,requests=len(samples),operations={k:{'count':len(v),'p95Ms':percentile(v),'meanMs':mean(v)} for k,v in operations.items()},
        minutes={str(k):{'requests':len(v),'p95Ms':percentile(v)} for k,v in sorted(minutes.items())},
        p95Ms=percentile([v for _,v in samples]),
        errors=summary['unexpected_errors']['values'],dropped=summary.get('dropped_iterations',{}).get('values',{}).get('count',0),
        thresholdsPassed=all(t['ok'] for metric in summary.values() for t in metric.get('thresholds',{}).values()))
    telemetry=rows(folder/(label+'-telemetry.ndjson'));host=rows(folder/(label+'-host.ndjson'));db=rows(folder/(label+'-database.ndjson'))
    if len(telemetry)>1:
        first,last=telemetry[0],telemetry[-1]
        out['pool']={'acquire':timer_delta(first,last,'hikaricp.connections.acquire'),
            'usage':timer_delta(first,last,'hikaricp.connections.usage'),
            'timeouts':total(last,'hikaricp.connections.timeout','COUNT')-total(first,'hikaricp.connections.timeout','COUNT'),
            'maxPending':max(r.get('pool',{}).get('pending',0) for r in telemetry),
            'pendingSamplePercent':round(100*sum(r.get('pool',{}).get('pending',0)>0 for r in telemetry)/len(telemetry),2),
            'maxActive':max(r.get('pool',{}).get('active',0) for r in telemetry),
            'maxAcquireWindowMs':round(max(total(r,'hikaricp.connections.acquire','MAX') for r in telemetry)*1000,3)}
        out['hibernateDelta']={k:last['hibernate'][k]-first['hibernate'][k] for k in first['hibernate']}
        queries={q['id']:q for q in first['queries']};delta=[]
        for q in last['queries']:
            before=queries.get(q['id'],{});count=q['count']-before.get('count',0);elapsed=q['totalMs']-before.get('totalMs',0)
            if count:delta.append({'id':q['id'],'count':count,'totalMs':elapsed,'meanMs':round(elapsed/count,3),'lifetimeMaxMs':q['maxMs']})
        out['queryTimings']=sorted(delta,key=lambda q:q['totalMs'],reverse=True)
        out['gc']=timer_delta(first,last,'jvm.gc.pause')
        out['availabilitySnapshot']=timer_delta(first,last,'application.availability.snapshot')
        out['apiHttpTimers']=[{'tags':m['tags'],'values':m['values']} for m in last['meters'] if m['name']=='http.server.requests']
    out['host']={'apiCpuCoresMean':mean([r['apiCpuCoresUsed'] for r in host if 'apiCpuCoresUsed' in r]),
        'apiCpuCoresMax':max((r.get('apiCpuCoresUsed',0) for r in host),default=0),
        'apiRssMaxMiB':round(max((r['apiRssBytes'] for r in host),default=0)/1024**2,1),
        'cpuCores':host[-1]['cpuCores'] if host else None}
    if len(host)>1:
        initial=[int(v) for v in host[0]['hostCpu'].split()[1:9]];final=[int(v) for v in host[-1]['hostCpu'].split()[1:9]]
        diffs=[b-a for a,b in zip(initial,final)];all_ticks=sum(diffs)
        out['host']['hostCpuBusyPercent']=round(100*(all_ticks-diffs[3]-diffs[4])/all_ticks,2) if all_ticks else None
        def network(row):
            rx=tx=0
            for line in row['network']:
                name,data=line.split(':',1)
                if name.strip()=='lo':continue
                values=data.split();rx+=int(values[0]);tx+=int(values[8])
            return rx,tx
        initial_net=network(host[0]);final_net=network(host[-1])
        out['host']['hostExternalNetworkMiB']={'received':round((final_net[0]-initial_net[0])/1024**2,2),'sent':round((final_net[1]-initial_net[1])/1024**2,2)}
    out['database']={'probeCount':len(db),'probeErrors':sum('errorType' in r for r in db),
        'connectAndQueryP95Ms':percentile([r['psqlConnectAndQueryMs'] for r in db if 'psqlConnectAndQueryMs' in r]),
        'waitSamples':{}}
    for r in db:
        for activity in r.get('stats',{}).get('activity') or []:
            key=f"{activity.get('state')} / {activity.get('wait_event_type')} / {activity.get('wait_event')}"
            out['database']['waitSamples'][key]=out['database']['waitSamples'].get(key,0)+activity['connections']
    server=rows(folder/'sql-server.ndjson')
    if host:
        server=[r for r in server if host[0]['time']<=r['time']<=host[-1]['time'] and 'statements' in r]
    if len(server)>1:
        initial={q['id']:q for q in server[0]['statements']};categories={};ranked=[]
        for q in server[-1]['statements']:
            before=initial.get(q['id'],{});count=q['calls']-before.get('calls',0)
            elapsed=q['total_exec_time']-before.get('total_exec_time',0)
            if count<=0:continue
            cat=categories.setdefault(q['category'],{'calls':0,'serverTotalMs':0})
            cat['calls']+=count;cat['serverTotalMs']+=elapsed
            ranked.append({'id':q['id'],'category':q['category'],'calls':count,'serverTotalMs':round(elapsed,3),
                'serverMeanMs':round(elapsed/count,4),'sharedBlocksRead':q['shared_blks_read']-before.get('shared_blks_read',0)})
        for cat in categories.values():
            cat['serverMeanMs']=round(cat['serverTotalMs']/cat['calls'],4);cat['serverTotalMs']=round(cat['serverTotalMs'],3)
        out['sqlServer']={'observedSeconds':round(server[-1]['time']-server[0]['time'],2),'categories':categories,
            'statements':sorted(ranked,key=lambda r:r['serverTotalMs'],reverse=True)}
    return out
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('folder',type=pathlib.Path);args=parser.parse_args()
    reports=[summarize(args.folder,run) for run in json.loads((args.folder/'runs.json').read_text())]
    (args.folder/'analysis.json').write_text(json.dumps(reports,indent=2))
    for r in reports:print(r['label'],r['requests'],r['p95Ms'],'ms',r['dropped'],'dropped',r['thresholdsPassed'])
