#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Sincronización de Evidencias de Pruebas Móviles (.NET MAUI Android)
en Azure DevOps Boards y preparación para Git.
Responsable: Cristian Jordan Pérez
"""

import os
import shutil
import json
import base64
import urllib.request
import urllib.parse
import urllib.error

PAT = "9CRlJflkcBq87DSLtPpXCzXVY3zyf0mkTPXwKCImfuMqQdlC9LOyJQQJ99CJACAAAAAAAAAAAAASAZDOoe1S"
ORG = "https://dev.azure.com/lalafaro6"
PROJECT = "Testeo"
AUTH_HEADER = "Basic " + base64.b64encode(f":{PAT}".encode()).decode()

SRC_EVIDENCIAS = "/home/ministorm/salon-de-belleza/docs/Evidencias_pruebas"
DEST_MOVIL = "/home/ministorm/salon-de-belleza/docs/evidencias_movil"
os.makedirs(DEST_MOVIL, exist_ok=True)

# Mapeo de archivos de origen a nombres limpios y descriptivos
IMAGE_MAPPING = [
    {
        "src": "WhatsApp Image 2026-10-24 at 10.38.14 PM.jpeg",
        "clean_name": "CP-MOB-01_PBI178_BandejaRecientes_Shushine.jpeg",
        "comment": "CP-MOB-01 (PBI #178 / TSK-05.1): App Shushine Studio activa en bandeja de aplicaciones recientes de Android antes de cierre forzoso",
        "work_items": [178, 179, 265]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.38.14 PM (1).jpeg",
        "clean_name": "CP-MOB-01_PBI178_SwipeToClose_TerminacionLimpia.jpeg",
        "comment": "CP-MOB-01 (PBI #178 / TSK-05.2): Terminación limpia de proceso tras Swipe-to-Close (Bandeja vacía 'No hay aplicaciones recientes')",
        "work_items": [178, 180, 265]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.40.35 PM.jpeg",
        "clean_name": "CP-MOB-02_PBI181_DialogoConfirmacionLogout.jpeg",
        "comment": "CP-MOB-02 (PBI #181 / TSK-06.1): Modal de confirmación en ProfilePage ('¿Estás segura de que deseas salir de tu cuenta?')",
        "work_items": [181, 182, 266]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.40.34 PM (1).jpeg",
        "clean_name": "CP-MOB-02_PBI181_RedireccionLoginPageLimpia.jpeg",
        "comment": "CP-MOB-02 (PBI #181 / TSK-06.2): Redirección limpia a LoginPage con credenciales purgadas del almacenamiento local",
        "work_items": [181, 183, 266]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.40.34 PM.jpeg",
        "clean_name": "CP-MOB-02_PBI181_LoginNuevaCuentaCamila.jpeg",
        "comment": "CP-MOB-02 (PBI #181 / TSK-06.2): Conmutación exitosa e inicio de sesión con cuenta alterna 'Camila' sin colisión de tokens",
        "work_items": [181, 183, 266]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.43.20 PM.jpeg",
        "clean_name": "CP-MOB-03_PBI184_ShellNavegacionCatalogoServicios.jpeg",
        "comment": "CP-MOB-03 (PBI #184 / TSK-07.1): Navegación Shell a CatalogPage con catálogo de servicios y opciones de reserva",
        "work_items": [184, 185, 267]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.43.34 PM.jpeg",
        "clean_name": "CP-MOB-03_PBI184_ShellNavegacionSolicitudPersonalizada.jpeg",
        "comment": "CP-MOB-03 (PBI #184 / TSK-07.2): Navegación Shell hacia ruta interna CustomRequestPage sin excepciones de argumentos",
        "work_items": [184, 186, 267]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.44.29 PM.jpeg",
        "clean_name": "CP-MOB-03_PBI184_ShellNavegacionChatAtelier.jpeg",
        "comment": "CP-MOB-03 (PBI #184 / TSK-07.1): Navegación Shell a ChatPage con canal bidireccional en tiempo real",
        "work_items": [184, 185, 267]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.38.15 PM.jpeg",
        "clean_name": "CP-MOB-04_PBI187_EmptyStateSinCitasExclusionMutua.jpeg",
        "comment": "CP-MOB-04 (PBI #187 / TSK-08.2): Renderizado de EmptyStateView ('Sin citas pendientes') con IsVisible=False en tarjeta de cita",
        "work_items": [187, 189, 268]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.40.34 PM.jpeg",
        "clean_name": "CP-MOB-04_PBI187_TarjetaCitaActivaExclusionMutua.jpeg",
        "comment": "CP-MOB-04 (PBI #187 / TSK-08.1): Renderizado exclusivo de tarjeta de próxima cita activa (SHU-2026-1341) con EmptyState oculto",
        "work_items": [187, 188, 268]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.45.42 PM.jpeg",
        "clean_name": "CP-MOB-05_PBI190_DetalleCitaFolioSHU20261341.jpeg",
        "comment": "CP-MOB-05 (PBI #190 / TSK-09.1 / TSK-09.2): AppointmentDetailPage renderizada sin fallas de casteo XAML, mostrando folio SHU-2026-1341, estado Confirmed y desglose financiero",
        "work_items": [190, 191, 192, 269]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.48.49 PM.jpeg",
        "clean_name": "CP-MOB-06_PBI193_PullToRefreshSpinnerProximas.jpeg",
        "comment": "CP-MOB-06 (PBI #193 / TSK-10.2): Gesto Pull-to-Refresh en AppointmentsPage con indicador de carga circular animado",
        "work_items": [193, 195, 270]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.48.21 PM.jpeg",
        "clean_name": "CP-MOB-06_PBI193_SegmentoHistorial39Citas.jpeg",
        "comment": "CP-MOB-06 (PBI #193 / TSK-10.1): Selector de segmento en 'Historial (39)' mostrando filtrado reactivo de citas pasadas",
        "work_items": [193, 194, 270]
    },
    {
        "src": "WhatsApp Image 2026-10-24 at 10.49.46 PM.jpeg",
        "clean_name": "CP-MOB-06_PBI193_HistorialDetalleTarjetasEstados.jpeg",
        "comment": "CP-MOB-06 (PBI #193 / TSK-10.1 / TSK-10.2): Renderizado de historial completo con tarjetas formateadas e insignias Confirmed y Cancelled",
        "work_items": [193, 194, 195, 270]
    }
]

def upload_attachment(filepath, filename):
    with open(filepath, "rb") as f:
        data = f.read()
    encoded_name = urllib.parse.quote(filename)
    url = f"{ORG}/{PROJECT}/_apis/wit/attachments?fileName={encoded_name}&api-version=7.0"
    req = urllib.request.Request(url, data=data, headers={
        "Authorization": AUTH_HEADER,
        "Content-Type": "application/octet-stream"
    }, method="POST")
    with urllib.request.urlopen(req) as resp:
        res = json.loads(resp.read().decode())
        return res["url"]

def patch_work_item(work_item_id, patch_operations):
    url = f"{ORG}/{PROJECT}/_apis/wit/workitems/{work_item_id}?api-version=7.0"
    data = json.dumps(patch_operations).encode()
    req = urllib.request.Request(url, data=data, headers={
        "Authorization": AUTH_HEADER,
        "Content-Type": "application/json-patch+json"
    }, method="PATCH")
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode())

def main():
    print("=== 1. Copiando imágenes a docs/evidencias_movil/ ===")
    for item in IMAGE_MAPPING:
        src_path = os.path.join(SRC_EVIDENCIAS, item["src"])
        dest_path = os.path.join(DEST_MOVIL, item["clean_name"])
        shutil.copy2(src_path, dest_path)
        print(f"  [OK] {item['clean_name']}")

    print("\n=== 2. Subiendo evidencias a Azure DevOps Attachment Store ===")
    uploaded_urls = {}
    for item in IMAGE_MAPPING:
        clean_name = item["clean_name"]
        if clean_name not in uploaded_urls:
            filepath = os.path.join(DEST_MOVIL, clean_name)
            url = upload_attachment(filepath, clean_name)
            uploaded_urls[clean_name] = url
            print(f"  [SUBIDO] {clean_name} -> {url[:70]}...")

    print("\n=== 3. Vinculando evidencias a Work Items de Cristian Jordan ===")
    # Agrupar por work item
    work_item_links = {}
    for item in IMAGE_MAPPING:
        url = uploaded_urls[item["clean_name"]]
        comment = item["comment"]
        for wid in item["work_items"]:
            if wid not in work_item_links:
                work_item_links[wid] = []
            work_item_links[wid].append((url, comment))

    # Obtener relaciones actuales para evitar duplicados
    for wid, attachments in sorted(work_item_links.items()):
        patch_ops = []
        for url, comment in attachments:
            patch_ops.append({
                "op": "add",
                "path": "/relations/-",
                "value": {
                    "rel": "AttachedFile",
                    "url": url,
                    "attributes": {
                        "comment": comment
                    }
                }
            })
        
        # Determinar estado
        if 178 <= wid <= 193 and wid % 3 == 0 or wid in [178, 181, 184, 187, 190, 193]:
            # PBI -> Done
            target_state = "Done"
        elif 265 <= wid <= 270:
            # Test Case -> Ready
            target_state = "Ready"
        else:
            # Task -> Done
            target_state = "Done"

        patch_ops.append({
            "op": "add",
            "path": "/fields/System.State",
            "value": target_state
        })

        try:
            res = patch_work_item(wid, patch_ops)
            print(f"  [VINCULADO] #{wid} ({res['fields'].get('System.WorkItemType')}) -> Estado: {target_state}, Adjuntos: {len(attachments)}")
        except Exception as e:
            print(f"  [ERROR] #{wid}: {e}")

    print("\n=== 4. Transicionando Feature 3 (#177) a Done ===")
    feature_patch = [
        {
            "op": "add",
            "path": "/fields/System.State",
            "value": "Done"
        }
    ]
    res_f = patch_work_item(177, feature_patch)
    print(f"  [FEATURE] #177 Estado: {res_f['fields'].get('System.State')}")

    print("\n=== Sincronización con Azure DevOps completada con éxito ===")

if __name__ == "__main__":
    main()
