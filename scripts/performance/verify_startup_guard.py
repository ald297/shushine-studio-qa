#!/usr/bin/env python3
"""Comprueba que el backend no abre HTTP si falta la migración de reservas."""
import json, os, secrets, socket, subprocess, time, uuid
from pathlib import Path
from lab import ROOT, STATE, RUNTIME, PG, env_pg

db='startup_guard_'+uuid.uuid4().hex[:8]
subprocess.run([str(PG/'createdb'),'-h','127.0.0.1','-p','15432','-U','perf',db],env=env_pg(),check=True)
proc=None
try:
    schema=(STATE/'production-public-schema.sql').read_text().replace('CREATE SCHEMA public;','CREATE SCHEMA IF NOT EXISTS public;')
    subprocess.run([str(PG/'psql'),'-h','127.0.0.1','-p','15432','-U','perf','-d',db,'-X','--single-transaction','-v','ON_ERROR_STOP=1'],input=schema,text=True,env=env_pg(),check=True,stdout=subprocess.DEVNULL)
    java=Path((RUNTIME/'java-home.txt').read_text())/'bin/java'
    args=[str(java),'-Xmx256m','-jar',str(ROOT/'backend-api/target/shushine-studio-api-0.0.1-SNAPSHOT.jar'),
        '--server.address=127.0.0.1','--server.port=18082',f'--spring.datasource.url=jdbc:postgresql://127.0.0.1:15432/{db}',
        '--spring.datasource.username=perf','--spring.datasource.password=','--spring.jpa.hibernate.ddl-auto=none','--spring.jpa.show-sql=false',
        '--supabase.url=http://127.0.0.1:15433','--supabase.service-role-key=disabled']
    with (STATE/'guard-startup.log').open('w') as log:
        proc=subprocess.Popen(args,env=dict(os.environ,SECURITY_JWT_SECRET_KEY=secrets.token_hex(64)),stdout=log,stderr=log)
        deadline=time.monotonic()+60;accepted=False
        while proc.poll() is None and time.monotonic()<deadline:
            try:
                with socket.create_connection(('127.0.0.1',18082),timeout=.05):accepted=True
            except OSError:pass
            time.sleep(.05)
        assert proc.poll() is not None,'La prueba de arranque excedió 60 segundos'
    assert proc.returncode!=0 and not accepted
    assert 'Falta aplicar database/migrations/20261016_reservas_sin_solapamientos.sql' in (STATE/'guard-startup.log').read_text()
    result={'missingMigrationRejected':True,'httpPortOpened':accepted,'exitCode':proc.returncode}
    (STATE/'guard-startup-verification.json').write_text(json.dumps(result,indent=2));print(result)
finally:
    if proc and proc.poll() is None:
        proc.terminate()
        try:proc.wait(timeout=10)
        except subprocess.TimeoutExpired:proc.kill();proc.wait()
    subprocess.run([str(PG/'dropdb'),'-h','127.0.0.1','-p','15432','-U','perf',db],env=env_pg(),check=True)
