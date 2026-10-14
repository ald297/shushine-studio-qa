#!/usr/bin/env python3
import os
import sys
import json
import hashlib
import urllib.request
import urllib.error
import time

def deploy_to_vercel(token, project_name="shushine-studio-admin", deploy_dir="admin-web", team_id=None):
    headers_auth = {
        "Authorization": f"Bearer {token}"
    }

    print(f"[*] Verificando cuenta con Vercel API...")
    req_user = urllib.request.Request("https://api.vercel.com/v2/user", headers=headers_auth)
    try:
        with urllib.request.urlopen(req_user) as resp:
            u_data = json.loads(resp.read().decode())
            print(f"[+] Autenticado como: {u_data.get('user', {}).get('username')} ({u_data.get('user', {}).get('email')})")
            if not team_id:
                team_id = u_data.get('user', {}).get('defaultTeamId')
    except urllib.error.HTTPError as e:
        print(f"[-] Error autenticando usuario: {e.code} {e.read().decode()}")
        return False

    # 1. Preparar archivos
    files_map = []
    for root, _, filenames in os.walk(deploy_dir):
        for fname in filenames:
            fpath = os.path.join(root, fname)
            relpath = os.path.relpath(fpath, deploy_dir)
            with open(fpath, "rb") as f:
                content = f.read()
            sha = hashlib.sha1(content).hexdigest()
            files_map.append({
                "file": relpath,
                "sha": sha,
                "size": len(content),
                "content": content
            })

    print(f"[*] Preparados {len(files_map)} archivos para despliegue:")
    for f in files_map:
        print(f"    - {f['file']} ({f['size']} bytes, sha: {f['sha'][:8]}...)")

    # 2. Subir archivos a /v2/files
    print(f"[*] Subiendo archivos a Vercel Files API...")
    for f in files_map:
        url_upload = "https://api.vercel.com/v2/files"
        if team_id:
            url_upload += f"?teamId={team_id}"

        headers_upload = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/octet-stream",
            "x-vercel-digest": f["sha"],
            "Content-Length": str(f["size"])
        }

        req_up = urllib.request.Request(url_upload, data=f["content"], headers=headers_upload, method="POST")
        try:
            with urllib.request.urlopen(req_up) as resp:
                print(f"    [OK] Subido: {f['file']}")
        except urllib.error.HTTPError as e:
            if e.code == 409:
                print(f"    [SKIP] Archivo ya existente en caché: {f['file']}")
            else:
                err_text = e.read().decode()
                print(f"    [-] Error subiendo {f['file']}: HTTP {e.code} - {err_text}")
                return False

    # 3. Crear el Deployment
    print(f"[*] Creando deployment en Vercel...")
    deploy_url = "https://api.vercel.com/v13/deployments"
    if team_id:
        deploy_url += f"?teamId={team_id}"

    payload = {
        "name": project_name,
        "files": [{"file": f["file"], "sha": f["sha"], "size": f["size"]} for f in files_map],
        "projectSettings": {
            "framework": None
        }
    }

    req_dep = urllib.request.Request(
        deploy_url,
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        },
        method="POST"
    )

    deployment_data = None
    try:
        with urllib.request.urlopen(req_dep) as resp:
            deployment_data = json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        print(f"[-] Error creando deployment: HTTP {e.code} - {e.read().decode()}")
        return False

    dep_id = deployment_data.get("id")
    dep_url = deployment_data.get("url")
    print(f"[+] Deployment creado con éxito!")
    print(f"    ID:  {dep_id}")
    print(f"    URL: https://{dep_url}")

    # 4. Polling hasta READY
    print(f"[*] Esperando a que el despliegue esté READY...")
    for _ in range(30):
        time.sleep(2)
        check_url = f"https://api.vercel.com/v13/deployments/{dep_id}"
        if team_id:
            check_url += f"?teamId={team_id}"
        req_check = urllib.request.Request(check_url, headers=headers_auth)
        try:
            with urllib.request.urlopen(req_check) as resp:
                status_data = json.loads(resp.read().decode())
                ready_state = status_data.get("readyState")
                print(f"    Estado: {ready_state}")
                if ready_state == "READY":
                    print(f"\n🚀 ¡DESPLIEGUE EXITOSO EN VERCEL!")
                    print(f"🌐 URL en Vivo: https://{dep_url}")
                    return f"https://{dep_url}"
                elif ready_state in ["ERROR", "CANCELED"]:
                    print(f"[-] Falló el despliegue con estado: {ready_state}")
                    return False
        except Exception as ex:
            print(f"    Verificando estado... ({ex})")

    return f"https://{dep_url}"

if __name__ == "__main__":
    token = sys.argv[1] if len(sys.argv) > 1 else os.environ.get("VERCEL_TOKEN")
    if not token:
        print("Uso: python3 deploy_vercel.py <VERCEL_TOKEN>")
        sys.exit(1)
    deploy_to_vercel(token)
