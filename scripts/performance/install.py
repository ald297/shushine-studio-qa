#!/usr/bin/env python3
"""Instala runtimes en espacio de usuario (Debian 13 amd64), sin sudo."""
import hashlib, io, json, pathlib, platform, subprocess, tarfile, urllib.request
ROOT=pathlib.Path(__file__).resolve().parents[2]
RUNTIME=ROOT/'.performance/runtime'
RUNTIME.mkdir(parents=True,exist_ok=True)
def get(url):
    with urllib.request.urlopen(url,timeout=180) as r:return r.read()
def unpack(blob):
    with tarfile.open(fileobj=io.BytesIO(blob),mode='r:gz') as t:
        first=t.getnames()[0].split('/')[0];t.extractall(RUNTIME,filter='data');return RUNTIME/first
if platform.system()!='Linux' or platform.machine()!='x86_64':raise SystemExit('Este instalador requiere Linux amd64; k6 admite otras plataformas por instalación oficial.')
k6=pathlib.Path.home()/'.local/bin/k6'
if not k6.exists():
    v='v2.3.0';name=f'k6-{v}-linux-amd64.tar.gz';base=f'https://github.com/grafana/k6/releases/download/{v}/'
    b=get(base+name);lines=get(base+f'k6-{v}-checksums.txt').decode().splitlines()
    expected=next(l.split()[0] for l in lines if l.split()[-1].lstrip('*')==name)
    assert hashlib.sha256(b).hexdigest()==expected
    with tarfile.open(fileobj=io.BytesIO(b),mode='r:gz') as t:
        m=next(m for m in t.getmembers() if m.isfile() and m.name.endswith('/k6'))
        k6.parent.mkdir(parents=True,exist_ok=True);k6.write_bytes(t.extractfile(m).read());k6.chmod(0o755)
if not (RUNTIME/'java-home.txt').exists():
    release=json.loads(get('https://api.github.com/repos/adoptium/temurin21-binaries/releases/latest'))
    a=next(a for a in release['assets'] if a['name'].startswith('OpenJDK21U-jdk_x64_linux_hotspot_') and a['name'].endswith('.tar.gz'))
    url=a['browser_download_url'];b=get(url);assert hashlib.sha256(b).hexdigest()==get(url+'.sha256.txt').decode().split()[0]
    (RUNTIME/'java-home.txt').write_text(str(unpack(b).resolve()))
if not (RUNTIME/'apache-maven-3.9.11/bin/mvn').exists():
    url='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz'
    b=get(url);assert hashlib.sha512(b).hexdigest()==get(url+'.sha512').decode().split()[0];unpack(b)
if not (RUNTIME/'pg/usr/lib/postgresql/17/bin/postgres').exists():
    if 'VERSION_ID="13"' not in pathlib.Path('/etc/os-release').read_text():raise SystemExit('PostgreSQL portátil preparado para Debian 13; adaptar paquetes en otra distribución.')
    debs=RUNTIME/'debs';debs.mkdir(exist_ok=True)
    subprocess.run(['apt-get','download','postgresql-17','postgresql-client-17','libpq5','libllvm19','libxml2'],cwd=debs,check=True)
    for deb in debs.glob('*.deb'):subprocess.run(['dpkg-deb','-x',str(deb),str(RUNTIME/'pg')],check=True)
subprocess.run([str(k6),'version'],check=True)
print('Runtimes disponibles; ver README para compilar e iniciar.')
