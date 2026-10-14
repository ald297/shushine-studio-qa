#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para sincronizar y cargar el Backlog completo del Plan de Pruebas de Software
en Azure DevOps Boards (Organización: lalafaro6, Proyecto: Testeo).
Estructura: Épica -> Features -> Product Backlog Items -> Tasks & Test Cases.
"""

import sys
import os
import json
import base64
import urllib.request
import urllib.parse
import urllib.error

ORGANIZATION = "lalafaro6"
PROJECT = "Testeo"
PAT = os.environ.get("AZURE_DEVOPS_PAT", "Eg57ze03jHFHqlNUr15NqKN9jrAqM6g88uMgS5DPMhhJcsCiXW3dJQQJ99CJACAAAAAAAAAAAAASAZDO3rCW").strip()
ALEX = "Alex alfaro <lalafaro6@gmail.com>"

AUTH_HEADER = "Basic " + base64.b64encode(f":{PAT}".encode("utf-8")).decode("utf-8")
ENCODED_PROJECT = urllib.parse.quote(PROJECT)

def post_work_item(item_type, patch_doc):
    encoded_type = urllib.parse.quote(f"${item_type}")
    url = f"https://dev.azure.com/{ORGANIZATION}/{ENCODED_PROJECT}/_apis/wit/workitems/{encoded_type}?api-version=7.0"
    data = json.dumps(patch_doc).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={
        "Content-Type": "application/json-patch+json",
        "Authorization": AUTH_HEADER
    }, method="POST")
    
    try:
        with urllib.request.urlopen(req) as resp:
            res_json = json.loads(resp.read().decode("utf-8"))
            return True, res_json["id"], None
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8", errors="ignore")
        return False, None, f"HTTP {e.code}: {err_msg}"
    except Exception as e:
        return False, None, str(e)

# Estructura del Plan de Pruebas
test_backlog = {
    "epic": {
        "title": "[QA-EPIC] Plan Integral de Pruebas de Software - Shushine Studio (ISO/IEC/IEEE 29119)",
        "sprint": "Sprint 1",
        "assigned": ALEX,
        "priority": 1,
        "desc": "<div>Plan maestro y matriz integral de pruebas de software para validar la App Móvil Cliente (.NET MAUI), el Panel Administrativo Web y la Sincronización en Tiempo Real con Supabase PostgreSQL según estándares ISO/IEC/IEEE 29119-3 e ISO 9001:2015.</div>",
        "features": [
            # -------------------------------------------------------------
            # FEATURE 1: NIVEL 1 - UNITARIAS Y ESTABILIDAD
            # -------------------------------------------------------------
            {
                "title": "Feature 1: Pruebas Unitarias y Estabilidad de Componentes (.NET MAUI & Web)",
                "sprint": "Sprint 1",
                "assigned": ALEX,
                "priority": 1,
                "value_area": "Architectural",
                "desc": "<div>Pruebas de nivel 1 de caja blanca (White-Box) para validar la lógica de presentación, persistencia de tokens de sesión con SecureStorage y Preferences, y mitigación de fallos de compilación XAML.</div>",
                "pbis": [
                    {
                        "title": "US-TEST-01: Verificación Unitaria de TokenStorageService y Persistencia Dual",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Pruebas unitarias sobre la persistencia híbrida de tokens JWT en Android, asegurando lectura síncrona y purga segura.</div>",
                        "criteria": "<div><b>Dado</b> que el usuario ha iniciado sesión, <b>Cuando</b> se consulta HasValidTokenSync(), <b>Entonces</b> retorna verdadero instantáneamente. <b>Dado</b> que se invoca ClearAsync(), <b>Cuando</b> se completa, <b>Entonces</b> SecureStorage y Preferences quedan vacíos sin residuos.</div>",
                        "tasks": [
                            {"title": "TSK-01.1: Escribir pruebas xUnit para guardado y lectura de Token en TokenStorageService", "hours": 3},
                            {"title": "TSK-01.2: Escribir pruebas xUnit para HasValidTokenSync() y ClearAsync()", "hours": 2}
                        ]
                    },
                    {
                        "title": "US-TEST-02: Verificación Unitaria de ClientHomeViewModel y Exclusión Mutua",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Pruebas unitarias de ClientHomeViewModel validando que las propiedades observables TieneProximaCita y NoTieneProximaCita sean mutuamente excluyentes y los comandos de navegación estén correctamente ligados.</div>",
                        "criteria": "<div><b>Dado</b> un ViewModel inicializado, <b>Cuando</b> se asigna una cita activa, <b>Entonces</b> TieneProximaCita es true y NoTieneProximaCita es false. <b>Dado</b> que no hay cita, <b>Cuando</b> se evalúa el estado, <b>Entonces</b> TieneProximaCita es false y NoTieneProximaCita es true sin solapamientos.</div>",
                        "tasks": [
                            {"title": "TSK-02.1: Pruebas unitarias para reactividad de propiedades de próxima cita", "hours": 3},
                            {"title": "TSK-02.2: Pruebas unitarias para IrADetalleProximaCitaCommand y navegación segura", "hours": 2}
                        ]
                    }
                ]
            },
            # -------------------------------------------------------------
            # FEATURE 2: NIVEL 2 - INTEGRACIÓN Y API REST
            # -------------------------------------------------------------
            {
                "title": "Feature 2: Pruebas de Integración y Contratos de API REST (Spring Boot & Supabase)",
                "sprint": "Sprint 1",
                "assigned": ALEX,
                "priority": 1,
                "value_area": "Architectural",
                "desc": "<div>Pruebas de nivel 2 de caja gris (Gray-Box) para validar contratos REST, autenticación JWT, seguridad RLS y persistencia transaccional en PostgreSQL de Supabase.</div>",
                "pbis": [
                    {
                        "title": "US-TEST-03: Pruebas de Integración de Endpoints REST y Manejo de Errores HTTP",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación de contratos JSON y respuestas 200/400/401/404 en API de Citas, Clientes y Servicios, además del comportamiento de ErrorDelegatingHandler ante 401.</div>",
                        "criteria": "<div><b>Dado</b> un token JWT válido en Authorization Bearer, <b>Cuando</b> se consulta /api/citas/proxima/{clienteId}, <b>Entonces</b> responde HTTP 200 con JSON tipado. <b>Dado</b> un token caducado, <b>Cuando</b> responde 401, <b>Entonces</b> el handler purga la sesión y redirige a login.</div>",
                        "tasks": [
                            {"title": "TSK-03.1: Ejecutar suite de integración Postman para endpoints REST de citas y clientes", "hours": 4},
                            {"title": "TSK-03.2: Validar interceptor ErrorDelegatingHandler ante respuestas 401 Unauthorized", "hours": 3}
                        ]
                    },
                    {
                        "title": "US-TEST-04: Pruebas de Integración de Persistencia Transaccional en Supabase PostgreSQL",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación de consistencia relacional, integridad referencial y actualización de existencias en Supabase al registrar citas y movimientos de inventario.</div>",
                        "criteria": "<div><b>Dado</b> un registro en movimientos_inventario de tipo 'entrada', <b>Cuando</b> se confirma la transacción, <b>Entonces</b> el stock de productos aumenta exactamente en la cantidad indicada.</div>",
                        "tasks": [
                            {"title": "TSK-04.1: Validar triggers de auditoría e integridad referencial en PostgreSQL Supabase", "hours": 3},
                            {"title": "TSK-04.2: Prueba de consistencia transaccional en movimientos de inventario", "hours": 3}
                        ]
                    }
                ]
            },
            # -------------------------------------------------------------
            # FEATURE 3: NIVEL 3 - UAT APP MÓVIL CLIENTE (.NET MAUI)
            # -------------------------------------------------------------
            {
                "title": "Feature 3: Pruebas de Aceptación UAT - App Móvil Cliente (.NET MAUI)",
                "sprint": "Sprint 1",
                "assigned": ALEX,
                "priority": 1,
                "value_area": "Business",
                "desc": "<div>Pruebas de aceptación de caja negra (Black-Box / BDD Gherkin) sobre la aplicación Android .NET MAUI en dispositivo físico real.</div>",
                "pbis": [
                    {
                        "title": "CP-MOB-01: Persistencia de Sesión al reiniciar la App Móvil",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación de persistencia de sesión: al salir de la app o cerrarla desde las aplicaciones recientes de Android, la sesión se mantiene activa y entra directo a la pantalla de Inicio sin pedir credenciales.</div>",
                        "criteria": "<div><b>Dado</b> que el cliente está autenticado exitosamente en el dispositivo, <b>Cuando</b> cierra la app desde la lista de aplicaciones recientes y la reabre desde el ícono, <b>Entonces</b> la app abre directamente en ClientHomePage sin solicitar credenciales ni mostrar la pantalla de Login.</div>",
                        "tasks": [
                            {"title": "TSK-05.1: Ejecutar prueba de ciclo de vida Android cerrando proceso desde aplicaciones recientes", "hours": 2},
                            {"title": "TSK-05.2: Validar inicio sincrónico en AppShell sin parpadeo de pantalla de login", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-MOB-02: Cierre de Sesión Voluntario y Cambio de Cuenta Limpio",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de logout voluntario: al presionar 'Cerrar sesión' en Perfil, se purgan las credenciales y se permite ingresar inmediatamente con otra cuenta sin fuga de datos.</div>",
                        "criteria": "<div><b>Dado</b> que el cliente tiene una sesión activa, <b>Cuando</b> va a 'Perfil' y presiona 'Cerrar sesión' confirmando el diálogo, <b>Entonces</b> el almacenamiento local se purga y permite ingresar credenciales de una segunda cuenta en tiempo real.</div>",
                        "tasks": [
                            {"title": "TSK-06.1: Ejecutar flujo de logout desde Perfil y verificar purga de almacenamiento local", "hours": 2},
                            {"title": "TSK-06.2: Iniciar sesión con cuenta alterna y verificar aislamiento total de tokens", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-MOB-03: Navegación Shell sin Excepciones ni Cierres Forzosos",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de enrutamiento Shell: los botones 'Agendar una cita', 'Servicios', 'Solicitudes' y 'Chat' navegan a sus rutas sin provocar ArgumentException ni salir de la app.</div>",
                        "criteria": "<div><b>Dado</b> el usuario en ClientHomePage, <b>Cuando</b> pulsa 'Agendar una cita', 'Servicios', 'Solicitudes' y 'Chat', <b>Entonces</b> cada botón redirige a su vista correspondiente sin salirse de la aplicación.</div>",
                        "tasks": [
                            {"title": "TSK-07.1: Prueba de navegación a Agendar Cita, Servicios, Solicitudes y Chat", "hours": 2},
                            {"title": "TSK-07.2: Verificación de rutas relativas y absolutas registradas en AppShell.xaml.cs", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-MOB-04: Exclusión Mutua en Tarjeta 'Tu Próxima Cita'",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación visual de tarjeta de próxima cita: si hay una cita confirmada, solo se muestra su tarjeta con fecha/hora/servicio y la tarjeta 'Sin citas pendientes' permanece estrictamente oculta.</div>",
                        "criteria": "<div><b>Dado</b> un cliente con 1 cita confirmada registrada en Supabase, <b>Cuando</b> abre la pantalla de Inicio, <b>Entonces</b> se visualiza únicamente la tarjeta con los datos de la cita y la tarjeta 'Sin citas pendientes' queda oculta.</div>",
                        "tasks": [
                            {"title": "TSK-08.1: Validar renderizado de tarjeta con cita activa y ocultación de 'Sin citas pendientes'", "hours": 2},
                            {"title": "TSK-08.2: Validar renderizado de EmptyState cuando el cliente no posee citas programadas", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-MOB-05: Transición al Detalle de Cita sin Excepciones de Casteo XAML",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación de navegación a detalle de cita: al presionar la tarjeta o 'Ver detalle ›', transiciona a AppointmentDetailPage sin errores de InvalidCastException en converters.</div>",
                        "criteria": "<div><b>Dado</b> la tarjeta de próxima cita visible en Inicio, <b>Cuando</b> el usuario toca sobre la tarjeta o en 'Ver detalle ›', <b>Entonces</b> la app transiciona limpiamente a AppointmentDetailPage mostrando el resumen sin cierres forzosos.</div>",
                        "tasks": [
                            {"title": "TSK-09.1: Probar tap en tarjeta y botón 'Ver detalle' hacia AppointmentDetailPage", "hours": 2},
                            {"title": "TSK-09.2: Verificar resolución de converters booleanos compilados sin InvalidCastException", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-MOB-06: Pestaña Mis Citas (Próximas vs Historial) y Pull-to-Refresh",
                        "sprint": "Sprint 1",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de vista Mis Citas: alternar entre selectores 'Próximas' e 'Historial' y deslizar hacia abajo para refrescar sin solapamientos visuales.</div>",
                        "criteria": "<div><b>Dado</b> un cliente con citas registradas, <b>Cuando</b> alterna entre 'Próximas' e 'Historial' o ejecuta Pull-to-Refresh, <b>Entonces</b> se renderizan las listas con sus insignias de estado correspondientes sin fallos visuales.</div>",
                        "tasks": [
                            {"title": "TSK-10.1: Probar alternancia de selector de segmento Próximas / Pasadas", "hours": 2},
                            {"title": "TSK-10.2: Probar gesto Pull-to-Refresh y sincronización con API", "hours": 2}
                        ]
                    }
                ]
            },
            # -------------------------------------------------------------
            # FEATURE 4: NIVEL 3 - UAT PANEL ADMINISTRATIVO WEB
            # -------------------------------------------------------------
            {
                "title": "Feature 4: Pruebas de Aceptación UAT - Panel Administrativo Web (Alpine.js)",
                "sprint": "Sprint 2",
                "assigned": ALEX,
                "priority": 1,
                "value_area": "Business",
                "desc": "<div>Pruebas de aceptación de caja negra sobre el Dashboard Web de gestión del salón, validando filtros de agenda, creación de citas, movimientos de stock, soporte y KPIs.</div>",
                "pbis": [
                    {
                        "title": "CP-WEB-01: Módulo de Chat & Soporte en Tiempo Real en Panel Web",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación del módulo de Chat y Soporte: visible en la barra lateral con capacidad de enviar y recibir mensajes enlazados a Supabase en tiempo real.</div>",
                        "criteria": "<div><b>Dado</b> el administrador logueado en el panel, <b>Cuando</b> va a 'Chat & Soporte', selecciona una conversación y envía respuesta, <b>Entonces</b> el mensaje se inserta en Supabase (tabla mensajes) y se refleja de inmediato en el hilo.</div>",
                        "tasks": [
                            {"title": "TSK-11.1: Validar menú lateral y renderizado de sala de chat de soporte", "hours": 3},
                            {"title": "TSK-11.2: Probar envío y recepción de mensajes admin con inserción en Supabase", "hours": 3}
                        ]
                    },
                    {
                        "title": "CP-WEB-02: Filtrado de Agenda: Citas de Hoy por Defecto",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de vista inicial de Agenda: la tabla muestra por defecto únicamente las citas de la fecha actual con su respectivo resumen diario.</div>",
                        "criteria": "<div><b>Dado</b> que existen citas de diversas fechas en la base de datos, <b>Cuando</b> se ingresa a 'Agenda & Citas', <b>Entonces</b> la tabla muestra por defecto únicamente las citas de la fecha actual con su banner de resumen.</div>",
                        "tasks": [
                            {"title": "TSK-12.1: Comprobar filtro inicial de fecha actual y banner de resumen de jornada", "hours": 2},
                            {"title": "TSK-12.2: Verificar conteo de citas confirmadas y pendientes en la tabla", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-WEB-03: Filtrado de Agenda: Ver Todo (Historial) y Filtro por Día",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de filtros avanzados de agenda: conmutar entre 'Ver Todo (Historial)' y el datepicker de fecha específica sin recargar la página.</div>",
                        "criteria": "<div><b>Dado</b> el módulo de Agenda abierto, <b>Cuando</b> el usuario pulsa 'Ver Todo' o escoge un día específico en el datepicker, <b>Entonces</b> la tabla conmuta en vivo mostrando el historial completo o solo las del día seleccionado.</div>",
                        "tasks": [
                            {"title": "TSK-13.1: Probar botón de alternancia Ver Todo (Historial completo)", "hours": 2},
                            {"title": "TSK-13.2: Probar datepicker para filtrar citas de una fecha específica", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-WEB-04: Creación de Cita desde Modal del Panel Web",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación de agendamiento administrativo: formulario '+ Nueva Cita' con inserción en Supabase, código único #SHU-2026-XXXX y notificación toast.</div>",
                        "criteria": "<div><b>Dado</b> el modal '+ Nueva Cita', <b>Cuando</b> se selecciona cliente, servicio, estilista, fecha y se confirma, <b>Entonces</b> la cita se inserta en Supabase con código #SHU-2026-XXXX y se notifica con Toast flotante.</div>",
                        "tasks": [
                            {"title": "TSK-14.1: Validar modal '+ Nueva Cita' con selección de cliente, servicio y estilista", "hours": 3},
                            {"title": "TSK-14.2: Comprobar generación de código #SHU-2026-XXXX y toast de confirmación", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-WEB-05: Registro de Entrada de Stock en Inventario del Salón",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de control de stock: modal de Entrada de Stock con registro de movimientos y actualización inmediata de existencias físicas en catálogo.</div>",
                        "criteria": "<div><b>Dado</b> el módulo de inventario, <b>Cuando</b> se pulsa 'Entrada de Stock', se indica producto, cantidad y motivo guardando, <b>Entonces</b> se registra el movimiento en movimientos_inventario y el badge de stock se actualiza a 'Óptimo'.</div>",
                        "tasks": [
                            {"title": "TSK-15.1: Probar modal de Entrada de Stock con selección de insumos y motivos", "hours": 2},
                            {"title": "TSK-15.2: Verificar actualización automática de badges de stock y existencias físicas", "hours": 2}
                        ]
                    },
                    {
                        "title": "CP-WEB-06: KPIs Ejecutivos y Alertas 100% Reales (Sin Datos Simulados)",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 3,
                        "desc": "<div>Validación de métricas ejecutivas: eliminación total de datos simulados/hardcoded; los 4 KPIs superiores y el centro de notificaciones operan con datos vivos de Supabase.</div>",
                        "criteria": "<div><b>Dado</b> el Dashboard Ejecutivo, <b>Cuando</b> carga la vista o se hace clic en la campana de notificaciones, <b>Entonces</b> las métricas de ingresos, citas y alertas provienen exclusivamente de consultas reales a Supabase.</div>",
                        "tasks": [
                            {"title": "TSK-16.1: Validar tarjetas de ingresos, total citas y clientes con consultas Supabase", "hours": 2},
                            {"title": "TSK-16.2: Probar centro de notificaciones reactivo ante stock crítico y reservas", "hours": 2}
                        ]
                    }
                ]
            },
            # -------------------------------------------------------------
            # FEATURE 5: NIVEL 3 - E2E INTEGRACIÓN OMNICANAL
            # -------------------------------------------------------------
            {
                "title": "Feature 5: Pruebas de Integración End-to-End (E2E Omnicanal en Tiempo Real)",
                "sprint": "Sprint 2",
                "assigned": ALEX,
                "priority": 1,
                "value_area": "Business",
                "desc": "<div>Pruebas de nivel 3 End-to-End (E2E) para verificar la sincronización bidireccional inmediata entre la App Móvil Cliente y el Panel Administrativo Web a través de Supabase Realtime.</div>",
                "pbis": [
                    {
                        "title": "CP-INT-01: Reflejo en Panel Web de Cita Agendada desde App Móvil",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación E2E: una cita reservada por el cliente desde el teléfono celular aparece automáticamente en el Panel Web en menos de 5 segundos con badge 'App Móvil'.</div>",
                        "criteria": "<div><b>Dado</b> que el cliente reserva una cita en la App Móvil, <b>Cuando</b> se confirma la transacción en Supabase, <b>Entonces</b> en menos de 5 segundos la cita aparece en la Agenda del Panel Web con etiqueta 'App Móvil'.</div>",
                        "tasks": [
                            {"title": "TSK-17.1: Ejecutar reserva en Android y verificar llegada reactiva a la Agenda Web", "hours": 3}
                        ]
                    },
                    {
                        "title": "CP-INT-02: Reflejo en App Móvil de Cita Agendada desde Panel Web",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación E2E: una cita agendada por el recepcionista en el Panel Web se refleja inmediatamente en 'Tu Próxima Cita' y 'Mis Citas' de la aplicación móvil del cliente.</div>",
                        "criteria": "<div><b>Dado</b> que el recepcionista registra una cita en el Panel Web, <b>Cuando</b> el cliente abre su app o refresca, <b>Entonces</b> la sección 'Tu Próxima Cita' muestra la nueva reserva agendada.</div>",
                        "tasks": [
                            {"title": "TSK-18.1: Registrar cita en el Panel Web y verificar renderizado en la App Móvil", "hours": 3}
                        ]
                    },
                    {
                        "title": "CP-INT-03: Mensajería Bidireccional Chat Móvil <-> Chat Admin Web",
                        "sprint": "Sprint 2",
                        "assigned": ALEX,
                        "points": 5,
                        "desc": "<div>Validación E2E: intercambio en vivo de mensajes entre la sala de chat de la app móvil y el módulo de soporte web sin recargar la página ni reiniciar la aplicación.</div>",
                        "criteria": "<div><b>Dado</b> que el cliente envía un mensaje desde la app móvil y el admin responde desde la web, <b>Cuando</b> se transmiten los paquetes, <b>Entonces</b> ambos mensajes se visualizan recíprocamente en tiempo real sin recargas manuales.</div>",
                        "tasks": [
                            {"title": "TSK-19.1: Probar intercambio bidireccional en vivo entre cliente móvil y soporte web", "hours": 3}
                        ]
                    }
                ]
            }
        ]
    }
}

def main():
    print("=" * 70)
    print("🚀 SINCRONIZANDO BACKLOG DEL PLAN DE PRUEBAS EN AZURE DEVOPS BOARDS")
    print(f"🏢 Organización: {ORGANIZATION} | 📁 Proyecto: {PROJECT}")
    print(f"👤 Asignado a: {ALEX}")
    print("=" * 70)
    
    epic_data = test_backlog["epic"]
    sprint_epic = f"{PROJECT}\\{epic_data['sprint']}"
    
    epic_patch = [
        {"op": "add", "path": "/fields/System.Title", "value": epic_data["title"]},
        {"op": "add", "path": "/fields/System.AreaPath", "value": PROJECT},
        {"op": "add", "path": "/fields/System.IterationPath", "value": sprint_epic},
        {"op": "add", "path": "/fields/System.Description", "value": epic_data["desc"]},
        {"op": "add", "path": "/fields/System.AssignedTo", "value": epic_data["assigned"]},
        {"op": "add", "path": "/fields/Microsoft.VSTS.Common.Priority", "value": epic_data["priority"]},
        {"op": "add", "path": "/fields/Microsoft.VSTS.Common.ValueArea", "value": "Business"},
        {"op": "add", "path": "/fields/System.State", "value": "New"}
    ]
    
    ok, epic_id, err = post_work_item("Epic", epic_patch)
    if not ok:
        print(f"❌ Error al crear la Épica principal: {err}")
        sys.exit(1)
        
    print(f"\n🌟 [ÉPICA CREADA] #{epic_id}: {epic_data['title']}")
    
    total_features = 0
    total_pbis = 0
    total_tasks = 0
    total_points = 0
    total_hours = 0
    
    for feat_data in epic_data["features"]:
        sprint_feat = f"{PROJECT}\\{feat_data['sprint']}"
        feat_patch = [
            {"op": "add", "path": "/fields/System.Title", "value": feat_data["title"]},
            {"op": "add", "path": "/fields/System.AreaPath", "value": PROJECT},
            {"op": "add", "path": "/fields/System.IterationPath", "value": sprint_feat},
            {"op": "add", "path": "/fields/System.Description", "value": feat_data["desc"]},
            {"op": "add", "path": "/fields/System.AssignedTo", "value": feat_data["assigned"]},
            {"op": "add", "path": "/fields/Microsoft.VSTS.Common.Priority", "value": feat_data["priority"]},
            {"op": "add", "path": "/fields/Microsoft.VSTS.Common.ValueArea", "value": feat_data["value_area"]},
            {"op": "add", "path": "/fields/System.State", "value": "New"},
            {
                "op": "add",
                "path": "/relations/-",
                "value": {
                    "rel": "System.LinkTypes.Hierarchy-Reverse",
                    "url": f"https://dev.azure.com/{ORGANIZATION}/_apis/wit/workItems/{epic_id}"
                }
            }
        ]
        
        ok_feat, feat_id, err_feat = post_work_item("Feature", feat_patch)
        if not ok_feat:
            print(f"   ❌ Error al crear Feature '{feat_data['title']}': {err_feat}")
            continue
            
        total_features += 1
        print(f"\n   🔹 [FEATURE #{feat_id}]: {feat_data['title']}")
        
        for pbi_data in feat_data["pbis"]:
            sprint_pbi = f"{PROJECT}\\{pbi_data['sprint']}"
            pbi_patch = [
                {"op": "add", "path": "/fields/System.Title", "value": pbi_data["title"]},
                {"op": "add", "path": "/fields/System.AreaPath", "value": PROJECT},
                {"op": "add", "path": "/fields/System.IterationPath", "value": sprint_pbi},
                {"op": "add", "path": "/fields/System.Description", "value": pbi_data["desc"]},
                {"op": "add", "path": "/fields/Microsoft.VSTS.Common.AcceptanceCriteria", "value": pbi_data["criteria"]},
                {"op": "add", "path": "/fields/Microsoft.VSTS.Scheduling.Effort", "value": float(pbi_data["points"])},
                {"op": "add", "path": "/fields/System.AssignedTo", "value": pbi_data["assigned"]},
                {"op": "add", "path": "/fields/Microsoft.VSTS.Common.Priority", "value": 1},
                {"op": "add", "path": "/fields/System.State", "value": "New"},
                {
                    "op": "add",
                    "path": "/relations/-",
                    "value": {
                        "rel": "System.LinkTypes.Hierarchy-Reverse",
                        "url": f"https://dev.azure.com/{ORGANIZATION}/_apis/wit/workItems/{feat_id}"
                    }
                }
            ]
            
            ok_pbi, pbi_id, err_pbi = post_work_item("Product Backlog Item", pbi_patch)
            if not ok_pbi:
                print(f"      ❌ Error al crear PBI '{pbi_data['title']}': {err_pbi}")
                continue
                
            total_pbis += 1
            total_points += pbi_data["points"]
            print(f"      📋 [PBI #{pbi_id} ({pbi_data['points']} pts)]: {pbi_data['title']}")
            
            for task_data in pbi_data.get("tasks", []):
                task_patch = [
                    {"op": "add", "path": "/fields/System.Title", "value": task_data["title"]},
                    {"op": "add", "path": "/fields/System.AreaPath", "value": PROJECT},
                    {"op": "add", "path": "/fields/System.IterationPath", "value": sprint_pbi},
                    {"op": "add", "path": "/fields/System.Description", "value": f"<div>Tarea técnica de validación del Plan de Pruebas: {task_data['title']}</div>"},
                    {"op": "add", "path": "/fields/System.AssignedTo", "value": ALEX},
                    {"op": "add", "path": "/fields/Microsoft.VSTS.Scheduling.OriginalEstimate", "value": float(task_data["hours"])},
                    {"op": "add", "path": "/fields/Microsoft.VSTS.Scheduling.RemainingWork", "value": float(task_data["hours"])},
                    {"op": "add", "path": "/fields/Microsoft.VSTS.Common.Priority", "value": 1},
                    {"op": "add", "path": "/fields/System.State", "value": "To Do"},
                    {
                        "op": "add",
                        "path": "/relations/-",
                        "value": {
                            "rel": "System.LinkTypes.Hierarchy-Reverse",
                            "url": f"https://dev.azure.com/{ORGANIZATION}/_apis/wit/workItems/{pbi_id}"
                        }
                    }
                ]
                
                ok_task, task_id, err_task = post_work_item("Task", task_patch)
                if not ok_task:
                    print(f"         ❌ Error al crear Task '{task_data['title']}': {err_task}")
                    continue
                    
                total_tasks += 1
                total_hours += task_data["hours"]
                print(f"         ⚙️ [TASK #{task_id} ({task_data['hours']}h)]: {task_data['title']}")

    print("\n" + "=" * 70)
    print("✅ SINCRONIZACIÓN EXITOSA CON AZURE DEVOPS BOARDS")
    print(f"🌟 Épica Maestra: #{epic_id}")
    print(f"🔹 Features Creadas: {total_features}")
    print(f"📋 Product Backlog Items (PBIs): {total_pbis} ({total_points} Story Points)")
    print(f"⚙️ Tareas Técnicas (Tasks): {total_tasks} ({total_hours} Horas Estimadas)")
    print(f"🌐 Ver en Azure Boards: https://dev.azure.com/{ORGANIZATION}/{ENCODED_PROJECT}/_boards")
    print("=" * 70)

if __name__ == "__main__":
    main()
