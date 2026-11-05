import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import exec from 'k6/execution';
import { Rate, Trend, Counter } from 'k6/metrics';

const config = JSON.parse(open(__ENV.PERF_CONFIG || './config.local.json'));
const mode = __ENV.PERF_MODE || 'smoke';
const validModes = ['smoke', 'baseline', 'stress', 'spike', 'soak', 'booking', 'contention'];
if (!validModes.includes(mode)) throw new Error('Modo desconocido');
if (config.baseUrl !== 'http://127.0.0.1:18080') throw new Error('Esta suite está limitada al laboratorio local 127.0.0.1:18080');
const base = config.baseUrl;
const failures = new Rate('unexpected_errors');
const latency = new Trend('operation_duration', true);
const created = new Counter('bookings_created');
const conflicts = new Counter('booking_conflicts');
const sampleFailures = new Counter('fixture_exhausted');
const vus = Number(__ENV.PERF_VUS || 10);
const quick = __ENV.PERF_QUICK === '1';
const runId = __ENV.PERF_RUN_ID || `perf-${Date.now()}`;
const profiles = {
  smoke: { executor: 'shared-iterations', vus: 1, iterations: 10, maxDuration: '2m' },
  baseline: { executor: 'constant-arrival-rate', rate: 5, timeUnit: '1s', duration: '10m', preAllocatedVUs: 20, maxVUs: 100 },
  stress: { executor: 'ramping-arrival-rate', startRate: 5, timeUnit: '1s', preAllocatedVUs: 50, maxVUs: 300,
    stages: [5, 10, 20, 40, 80].flatMap(target => [{target, duration:quick?'10s':'1m'}, {target, duration:quick?'20s':'5m'}]).concat(quick?[{target:5,duration:'5s'},{target:5,duration:'30s'}]:[{target:5,duration:'2m'}]) },
  spike: { executor:'ramping-arrival-rate', startRate:5, timeUnit:'1s', preAllocatedVUs:50, maxVUs:300,
    stages:[{target:5,duration:'5m'},{target:15,duration:'10s'},{target:15,duration:'1m'},{target:5,duration:'10s'},{target:5,duration:'5m'}] },
  soak: { executor:'constant-arrival-rate', rate:Number(__ENV.PERF_RATE || 5), timeUnit:'1s', duration:__ENV.PERF_DURATION || '1h', preAllocatedVUs:50, maxVUs:300 },
  booking: { executor:'shared-iterations', vus, iterations:Number(__ENV.PERF_ITERATIONS || 10), maxDuration:'3m' },
  contention: { executor:'per-vu-iterations', vus, iterations:1, maxDuration:'2m' },
};
export const options = {
  scenarios: { workload: profiles[mode] },
  systemTags: ['status','method','name','scenario','expected_response'],
  thresholds: {
    unexpected_errors: ['rate<0.01', {threshold:'rate<0.05',abortOnFail:true,delayAbortEval:'30s'}],
    checks: ['rate==1'],
    ...(!['booking','contention'].includes(mode) ? Object.fromEntries(['catalogo','disponibilidad','mis_citas'].map(operation=>[`operation_duration{operation:${operation}}`,['p(95)<1000']])) : {}),
    ...(!['booking','contention'].includes(mode) ? {'operation_duration{kind:read}': ['p(95)<1000']} : {}),
    ...(mode === 'booking' || mode === 'contention' ? {'operation_duration{kind:write}':['p(95)<2000']} : {}),
    ...(mode === 'contention' ? {bookings_created:['count==1'], booking_conflicts:[`count==${vus-1}`]} : {}),
    ...(['baseline','stress','spike','soak'].includes(mode) ? {dropped_iterations:['count==0']} : {}),
  },
};
let token, tokenTime = 0;
function headers(data) {
  if (!token && data?.tokens?.length) {token=data.tokens[(exec.vu.idInTest-1)%data.tokens.length];tokenTime=Date.now();}
  if (!token || Date.now()-tokenTime > 45*60*1000) {
    const a = config.accounts[(exec.vu.idInTest-1)%config.accounts.length];
    const r = http.post(`${base}/api/auth/login`, JSON.stringify({login:a.login, clave:a.clave}),
      {headers:{'Content-Type':'application/json'},tags:{name:'login'},timeout:'15s'});
    let data; try {data=r.json();} catch (_) {}
    if (!check(r, {'login válido':()=>r.status===200 && !!data?.token})) fail('Login fallido; revisar cuenta de laboratorio');
    token=data.token; tokenTime=Date.now();
  }
  return {'Content-Type':'application/json',Authorization:`Bearer ${token}`};
}
function stageName() {
  if(mode!=='stress') return mode;
  const elapsed=(Date.now()-exec.scenario.startTime)/1000;
  const step=quick?30:360;
  const index=Math.floor(elapsed/step);
  if(index>=5) return 'recovery';
  return `${[5,10,20,40,80][index]}_`+(elapsed%step<(quick?10:60)?'ramp':'hold');
}
function record(r, name, kind, expected=200) {
  const ok=check(r, {[`${name}: HTTP esperado`]:x=>x.status===expected});
  failures.add(!ok,{operation:name,stage:stageName()});latency.add(r.timings.duration,{operation:name,kind,stage:stageName()});
}
export function setup() {
  if (!config.accounts?.length) throw new Error('Faltan cuentas de pruebas');
  const r=http.get(`${base}/api/servicios/lista`,{tags:{name:'preflight'},timeout:'10s'});
  if(r.status!==200) throw new Error('API local no disponible');
  const availability=http.get(`${base}/api/estilistas/${config.estilistaId}/disponibilidad?fecha=${config.fecha}&servicioId=${config.servicioId}`,{tags:{name:'preflight_disponibilidad'},timeout:'15s'});
  let slots;try{slots=availability.json('franjas');}catch(_){}
  if(availability.status!==200 || !Array.isArray(slots) || slots.length===0) throw new Error('Disponibilidad sin franjas: ejecutar seed con horarios de prueba');
  if(['booking','contention'].includes(mode) && !config.slots?.length) throw new Error('Faltan franjas de laboratorio');
  if(mode==='contention' && !config.contentionSlot) throw new Error('Falta contentionSlot exclusivo');
  if(mode==='booking' && config.slots.length < Number(__ENV.PERF_ITERATIONS || 10)) throw new Error('Franjas insuficientes');
  // Counters with zero samples must exist for thresholds even if all requests fail.
  created.add(0);conflicts.add(0);
  const tokens=config.accounts.map(a=>{
    const r=http.post(`${base}/api/auth/login`, JSON.stringify({login:a.login,clave:a.clave}),{headers:{'Content-Type':'application/json'},tags:{name:'setup_login'},timeout:'15s'});
    let body;try{body=r.json();}catch(_){}
    if(r.status!==200 || !body?.token) throw new Error('Login de preparación fallido');
    return body.token;
  });
  return {tokens};
}
export default function (data) {
  const h=headers(data);
  if(mode==='booking' || mode==='contention') {
    const slot=mode==='contention'?config.contentionSlot:config.slots[exec.scenario.iterationInTest];
    if(!slot){sampleFailures.add(1);fail('Franjas agotadas');}
    const payload={...slot,notasCliente:`${runId}:${mode}`,metodoPagoPreferente:'Efectivo'};
    const r=http.post(`${base}/api/citas`,JSON.stringify(payload),{headers:h,tags:{name:'crear_cita'},timeout:'15s',
      responseCallback:http.expectedStatuses(...(mode==='contention'?[201,409]:[201]))});
    let body;try{body=r.json();}catch(_){}
    const ok=r.status===201 && Number.isInteger(body?.id) || mode==='contention' && r.status===409;
    check(r,{'reserva responde según escenario':()=>ok});failures.add(!ok,{operation:'crear_cita'});
    latency.add(r.timings.duration,{kind:'write',operation:'crear_cita'});
    created.add(r.status===201?1:0);conflicts.add(r.status===409?1:0);
    // 409 is provisional in contention: the runner also verifies stored appointments.
    return;
  }
  const n=exec.scenario.iterationInTest%10;
  let path,name;
  if(n<5){path='/api/servicios/lista';name='catalogo';}
  else if(n<8){path=`/api/estilistas/${config.estilistaId}/disponibilidad?fecha=${config.fecha}&servicioId=${config.servicioId}`;name='disponibilidad';}
  else{path='/api/citas/mis-citas';name='mis_citas';}
  const r=http.get(base+path,{headers:h,tags:{name,stage:stageName()},timeout:'15s'});
  record(r,name,'read');
  if(mode==='smoke') sleep(1);
  check(r,{'respuesta JSON válida':x=>{try{return x.json()!==null;}catch(_){return false;}}});
}
export function handleSummary(data) {
  return {'summary.json':JSON.stringify({runId,mode,quick,environment:config.environment || 'local',baseUrl:base,result:data},null,2)};
}
