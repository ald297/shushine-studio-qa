# ASIGNACIONES INDIVIDUALES PARA EL DOCUMENTO COMPARTIDO

## Plan Maestro de Pruebas de Software — Shushine Studio (STP-SHU-2026-V3)

**Institución:** Escuela Superior Franciscana Especializada / AGAPE (ESFE AGAPE)
**Módulo:** Aseguramiento de la Calidad de Software / Formulación del Plan de Pruebas
**Normas:** ISO/IEC/IEEE 29119-3:2021 / IEEE 829-2008 / ISO 9001:2015

---

### INSTRUCCIONES DE USO PARA EL EQUIPO

1. Este documento se encuentra dividido en **4 bloques independientes**, uno para cada integrante del equipo.
2. Cada integrante debe copiar **únicamente el contenido de su bloque asignado** y pegarlo en el documento compartido en el orden correspondiente.
3. Se han preservado todas las directrices formales: formato Arial, tono técnico riguroso, sin emojis y con los 16 casos de prueba técnicos y tablas completas.

---

# BLOQUE 1: ALEX FERNANDO ALFARO DIAZ

**Rol Técnico:** QA Lead, Estrategia Metodológica & Gobernanza
**Secciones a Copiar:** Portada, Índice, Sección 1 (Introducción, Alcance, Objetivos, Roles/RACI), Sección 2 (Introducción y Pirámide), Secciones 2.3, 2.4, 2.5 y Sección 3 (Entregables).

```markdown
# PLAN MAESTRO DE PRUEBAS DE SOFTWARE (SOFTWARE TEST PLAN)
## Conforme al Esquema Académico Institucional y Norma Internacional ISO/IEC/IEEE 29119-3 / IEEE 829

**Institución:** Escuela Superior Franciscana Especializada / AGAPE (ESFE AGAPE)  
**Carrera:** Técnico en Ingeniería de Desarrollo de Software  
**Módulo:** Aseguramiento de la Calidad de Software / Formulación del Plan de Pruebas  
**Proyecto:** Shushine Studio — Ecosistema Omnicanal de Gestión y Reservas de Belleza  
**Documento ID:** STP-SHU-2026-V3  
**Fase del Ciclo de Vida:** Planificación, Diseño y Especificación de Pruebas (QA Pre-Release)  
**Responsable Técnico / QA Lead:** Alex Fernando Alfaro Diaz  
**Equipo de Aseguramiento de Calidad (Testing Team):** Alex Fernando Alfaro Diaz, Cristian Jordan, Rodrigo Cortez, Cornelio Martinez  
**Ciclo Académico:** Ciclo II - 2026  
**Fecha de Presentación:** 12 de Octubre de 2026  

---

## ÍNDICE GENERAL DEL PLAN DE PRUEBAS

1. Introducción  
   1.1 Alcance  
       1.1.1 Alcance  
       1.1.2 Fuera de alcance  
   1.2 Objetivo de Calidad  
   1.3 Roles y Responsabilidades  
2. Metodología de prueba  
   2.1 Compendio  
   2.2 Niveles de prueba  
   2.3 Clasificación de errores  
   2.4 Criterios de suspensión y requisitos de reanudación  
   2.5 Completitud de la prueba  
3. Entregables de la prueba  
4. Necesidades de recursos y medio ambiente  
   4.1 Herramientas de prueba  
   4.2 Entorno de prueba  
* Términos / Acrónimos  
* Alineación con la Norma Internacional ISO/IEC/IEEE 29119  
* Firmas y Conformidad del Equipo de Pruebas  

---

## 1. INTRODUCCIÓN

El presente Plan Maestro de Pruebas de Software define de manera estructurada, rigurosa y aplicable la estrategia de aseguramiento de calidad técnico para el sistema Shushine Studio, una plataforma tecnológica omnicanal concebida para la gestión integral de reservas, control de inventario y fidelización de clientes en salones de belleza y estética profesional.

En estricto cumplimiento con las directrices académicas y los estándares internacionales ISO/IEC/IEEE 29119-3:2021 (Documentation Processes) e IEEE 829-2008, este documento establece la fase de planificación, diseño y especificación formal de pruebas. En consecuencia, este documento no expone resultados prematuros de ejecución, sino que define los criterios unívocos, precondiciones, procedimientos paso a paso y resultados esperados verificables requeridos para certificar la confiabilidad y estabilidad del software previo a su liberación formal a producción.

---

### 1.1 Alcance

El sistema bajo prueba (System Under Test - SUT) comprende una arquitectura desacoplada de tres capas interdependientes:
1. Cliente Móvil (.NET MAUI en Android): Aplicación nativa multiplataforma orientada al cliente final para autenticación de usuarios, catálogo visual de servicios, selección de estilistas, agenda de citas y canal de mensajería interactiva con el salón.
2. Panel Administrativo Web (Alpine.js + Tailwind CSS): Plataforma web reactiva de escritorio orientada a recepcionistas, estilistas y gerentes del salón para control de agenda operativa diaria, creación de reservas manuales, administración de insumos en bodega, registro de compras y seguimiento de KPIs.
3. Capa Backend & Persistencia (Spring Boot REST API + Supabase PostgreSQL Realtime): Servicios de lógica de negocio, persistencia relacional transaccional, autenticación JWT, directivas de seguridad a nivel de fila (Row Level Security - RLS) y bus de eventos reactivo mediante WebSockets.

---

#### 1.1.1 Alcance

Se especifica la cobertura formal de pruebas sobre los siguientes módulos críticos del sistema:

| Identificador / Módulo | Tecnologías Base | Elementos Críticos Específicos Evaluados |
| :--- | :--- | :--- |
| MÓV-01: Autenticación y Sesión | .NET MAUI, Android Keystore, Preferences | Servicio TokenStorageService, persistencia dual síncrona/asíncrona, interceptor ErrorDelegatingHandler, preservación de sesión ante el ciclo de vida del proceso en memoria (Swipe-to-Close) y purga en logout. |
| MÓV-02: Navegación y Shell | .NET MAUI Shell, CommunityToolkit MVVM | Rutas relativas y absolutas registradas en AppShell.xaml.cs, comandos de transición rápida (Agendar, Servicios, Solicitudes, Chat), integridad de pila de navegación sin ArgumentException. |
| MÓV-03: Vistas Reactivas de Citas | XAML Compilado, Converters Fuertemente Tipados | ClientHomePage, MyAppointmentsPage, exclusión mutua entre tarjeta informativa de cita y vista de estado vacío (EmptyStateView), resolución de enlaces sin InvalidCastException. |
| WEB-01: Monitoreo y Dashboard | Alpine.js, Tailwind CSS, Vercel Edge | Cálculo dinámico de KPIs sin valores simulados, banner de jornada, centro reactivo de notificaciones y alertas automáticas de inventario. |
| WEB-02: Agenda Operativa | Alpine.js, Supabase JS SDK (REST) | Filtro por defecto (Citas de Hoy), vista de historial desfiltrada (Ver Todo), selector de fecha por datepicker con query params, modal de nueva cita con folio único #SHU-2026-XXXX. |
| WEB-03: Control de Inventario | Alpine.js, PostgreSQL Functions | Catálogo de insumos de salón, umbrales de stock mínimo/óptimo, modal de entrada de stock, actualización física y registro auditable en movimientos_inventario. |
| WEB-04: Chat & Soporte Atelier | Supabase Realtime (WebSockets) | Canal de atención interactivo cliente-salón, persistencia en tabla mensajes, sincronización instantánea bidireccional. |
| INT-01: API REST & Sincronización Omnicanal | Spring Boot 3.x, Supabase Realtime | Contratos JSON, latencia de propagación de eventos móvil <-> web inferior a 5 segundos, persistencia transaccional ACID. |
| SEC-01: Seguridad y Políticas RLS | PostgreSQL 15, Supabase Auth | Políticas Row Level Security en tabla citas y mensajes, aislamiento de datos por cliente_id = auth.uid(), mitigación de ataques BOLA y bloqueo de anon_key pública. |

---

#### 1.1.2 Fuera de alcance

Quedan formalmente excluidas de esta fase de pruebas las siguientes características:
* Pasarela de pagos en vivo con tarjeta de crédito/débito: La validación financiera se realizará mediante el registro manual de cobros en efectivo o transferencias bancarias verificadas por recepción.
* Compatibilidad con sistema operativo iOS: Esta iteración se encuentra delimitada exclusivamente para el entorno Android SDK 34 compilado sobre .NET 9.
* Módulos contables externos y nómina salarial: No forman parte del alcance del MVP de titulación.

---

### 1.2 Objetivo de Calidad

El objetivo de calidad del presente plan es establecer las condiciones de verificación que certifiquen que Shushine Studio alcanza los estándares de robustez, seguridad y rendimiento exigidos para su pase a producción. Se establecen los siguientes objetivos cuantitativos y medibles:

1. Estabilidad del Proceso Móvil: Cero fallos catastróficos (crashes) o cierres forzosos ante muerte de proceso por el gestor de memoria de Android (MIUI / HyperOS), logrando la recuperación de credenciales en un tiempo inferior a 10 ms mediante lectura síncrona en AppShell.
2. Latencia Omnicanal en Vivo: Garantizar que las reservas creadas desde la app móvil se reflejen en la agenda del panel web en un tiempo inferior a 5 segundos mediante canales WebSockets de Supabase sin requerir refresco manual (F5).
3. Aislamiento Estricto de Datos (RLS): Cumplimiento del 100% de denegación ante peticiones REST que intenten acceder o alterar citas de otros clientes portando tokens de terceros o claves anónimas públicas (anon_key), mitigando vulnerabilidades OWASP API1 (BOLA).
4. Completitud Funcional de Casos de Prueba: Ejecución del 100% de los 16 casos de prueba técnicos especificados en la Sección 2.
5. Cero Tolerancia a Defectos Críticos: Registrar cero (0) defectos abiertos clasificados como Severidad 1 (Bloqueante) o Severidad 2 (Crítico) en el momento de corte de la fase de pruebas.
6. Tasa de Éxito en Defectos Menores: Lograr una tasa de aprobación mínima del 95% en defectos de severidad menor (S3 y S4), documentando los planes de mitigación para el sprint subsecuente.

---

### 1.3 Roles y Responsabilidades

En cumplimiento con la cláusula 5.3 de la norma ISO 9001:2015 (Roles, responsabilidades y autoridades en la organización), el equipo de Aseguramiento de Calidad (QA) distribuyó equitativamente el diseño, especificación y supervisión técnica entre sus 4 integrantes:

| Integrante | Rol Técnico Asignado | Módulos y Secciones a su Cargo | Casos de Prueba Diseñados |
| :--- | :--- | :--- | :---: |
| Alex Fernando Alfaro Diaz | QA Lead & Estrategia Metodológica | Secciones 1, 2.3, 2.4, 2.5, 3 y Gobernanza: Marco normativo ISO 29119, pirámide de pruebas, criterios de entrada/salida (DoD), clasificación de errores (S1-S4), gestión de riesgos cuantitativos con SLAs y aprobación final. | Gobernanza Global |
| Cristian Jordan | QA Frontend Móvil & Usabilidad Android | Secciones 1.1.1 (MOV), 4 y 2.1 (CP-MOB): Plataforma móvil Android, herramientas ADB y Scrcpy, y diseño exhaustivo de los 6 casos de prueba del cliente móvil .NET MAUI. | CP-MOB-01 al CP-MOB-06 (6 casos) |
| Rodrigo Cortez | QA Panel Web & Operación de Salón | Secciones 1.1.1 (WEB), 4 y 2.1 (CP-WEB): Plataforma web Chrome DevTools, auditoría de red REST, gestión operativa de agenda, inventario del salón y los 6 casos de prueba del panel administrativo. | CP-WEB-01 al CP-WEB-06 (6 casos) |
| Cornelio Martinez | QA Backend, Integración & Seguridad RLS | Secciones 1.1.1 (INT/SEC), 4.2 y 2.1 (CP-INT/SEC): Datos de línea base, pruebas de sincronización omnicanal en tiempo real, canales WebSockets Supabase y auditoría de directivas Row Level Security (RLS). | CP-INT-01 al CP-INT-03 y CP-SEC-01 (4 casos) |

#### Matriz de Asignación de Responsabilidades (RACI)

| Rol / Integrante | Planificación | Especificación de Casos | Gestión de Entorno | Corrección de Código | Auditoría RLS | Sign-off Final |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| QA Lead / Estrategia & Riesgos (Alex Fernando Alfaro Diaz) | A / R | R | A | C | R | A / R |
| QA Frontend Móvil & Usabilidad (Cristian Jordan) | R | R | R | C | I | R |
| QA Panel Web & Operación de Salón (Rodrigo Cortez) | R | R | R | C | I | R |
| QA Backend, Integración & Seguridad (Cornelio Martinez) | R | R | R | C | R | R |

Referencias: R = Responsable de ejecución/diseño (Responsible), A = Aprobador final (Accountable), C = Consultado técnico (Consulted), I = Informado (Informed).

---

## 2. METODOLOGÍA DE PRUEBA

### 2.1 Compendio

La estrategia de pruebas adopta el modelo de la Pirámide de Pruebas de Mike Cohn en articulación con el estándar internacional ISO/IEC/IEEE 29119-3, distribuyendo el esfuerzo en tres niveles complementarios:
* Nivel 3 (Aceptación y Sistema E2E - Caja Negra): Flujos de usuario en móvil Android real (.NET MAUI), flujos operativos en panel web (Alpine.js) y sincronización omnicanal en vivo.
* Nivel 2 (Integración y Contratos de Servicio - Caja Gris): Endpoints REST Spring Boot con Postman, políticas RLS y triggers PostgreSQL Supabase, suscripciones WebSockets Realtime.
* Nivel 1 (Pruebas Unitarias y Componentes - Caja Blanca): Servicios y ViewModels .NET MAUI con xUnit, manejadores de estado Alpine.js, converters y validadores tipados.

---

### 2.3 Clasificación de errores

Todo desvío detectado durante la ejecución futura del plan se registrará formalmente en el sistema de incidentes de Azure DevOps bajo la siguiente escala técnica de severidad:

| Severidad | Definición Técnica | SLA de Resolución | Ejemplo Práctico en Shushine Studio |
| :--- | :--- | :---: | :--- |
| S1: Bloqueante (Blocker) | El sistema o módulo queda completamente inoperativo; crash de la aplicación móvil, bloqueo total de base de datos o fallo insubsanable al iniciar sesión. | Inmediato (< 4 horas) | Cierre forzoso de la aplicación Android al pulsar una tarjeta de cita o fallo catastrófico en el arranque (AppShell). |
| S2: Crítico (Critical) | Pérdida de funcionalidad principal del negocio sin procedimiento alternativo viable; fallo en la inserción transaccional de reservas o corrupción en inventario. | < 24 horas | El botón de guardar cita en el modal web no inserta el registro en Supabase. |
| S3: Mayor (Major) | Funcionalidad relevante afectada pero existe un procedimiento alternativo o manual para completar la operación. | < 48 horas | El filtro por fecha del datepicker requiere presionar dos veces para refrescar la tabla de citas. |
| S4: Menor (Cosmetic) | Detalle cosmético, error tipográfico, espaciado visual o badge desalineado que no afecta la lógica ni la integridad operativa. | Próximo Sprint | Desalineación de un icono en la barra de navegación lateral del panel administrativo web. |

---

### 2.4 Criterios de suspensión y requisitos de reanudación

* Criterios de Suspensión:  
  La ejecución de pruebas se suspenderá formalmente si se presenta cualquiera de las siguientes condiciones bloqueantes:
  1. Detección de un defecto de Severidad 1 (Bloqueante) que imposibilite la navegación básica o genere un cierre forzoso de la aplicación (Crash on Launch).
  2. Indisponibilidad no planificada del servicio de base de datos Supabase o caída total de la API REST backend.
  3. Corrupción masiva en los datos de prueba de línea base que invalide la reproducibilidad de los casos.

* Requisitos de Reanudación:  
  Las actividades de prueba se reanudarán una vez satisfechos los siguientes requisitos:
  1. Despliegue de un parche correctivo (Hotfix) por parte del equipo de desarrollo en el repositorio central.
  2. Ejecución y aprobación de pruebas unitarias de no-regresión sobre el componente subsanado.
  3. Confirmación de operatividad de los servicios de backend y restauración de los datos de línea base.

---

### 2.5 Completitud de la prueba

Para certificar el cierre formal de la fase de pruebas y emitir la recomendación de pase a producción (Definition of Done - DoD), se debe satisfacer la totalidad de los siguientes criterios:

1. 100% de los casos de prueba planificados ejecutados y debidamente evidenciados en el tablero de control de Azure Boards.
2. Cero (0) defectos abiertos de Severidad 1 (Bloqueante) y Severidad 2 (Crítico).
3. Tasa de aprobación mínima del 95% en casos de prueba de Severidad 3 y 4, con los defectos residuales documentados y calendarizados para el siguiente sprint de mantenimiento.
4. Confirmación de integridad referencial en PostgreSQL sin registros huérfanos en las tablas citas, mensajes ni movimientos_inventario.
5. Emisión y firma formal del Informe de Resumen de Pruebas (Test Summary Report) por el QA Lead.

---

## 3. ENTREGABLES DE LA PRUEBA

Al concluir el ciclo de aseguramiento de calidad, el equipo de pruebas consolidará y entregará los siguientes artefactos formales:

1. Plan Maestro de Pruebas de Software (STP-SHU-2026-V3): El presente documento formal de planificación, diseño y especificación metodológica.
2. Backlog de Pruebas en Azure DevOps Boards: Registro de trazabilidad vinculado a la Épica maestra #162 en la organización lalafaro6/Testeo, conteniendo las 5 Features, 19 PBIs y 35 Tasks de testing asignadas.
3. Registro de Incidentes y Defectos (Bug Reports): Expedientes técnicos con capturas de pantalla, pasos detallados de reproducción y trazas del sistema (adb logcat y consola DevTools).
4. Informe Formal de Cierre y Resumen de Pruebas (Test Summary Report): Documento final que certifica las métricas de ejecución, el porcentaje de casos superados y el dictamen técnico para la liberación a producción.
```

---

# BLOQUE 2: CRISTIAN JORDAN

**Rol Técnico:** QA Frontend Móvil & Usabilidad Android
**Secciones a Copiar:** Casos de prueba CP-MOB-01 al CP-MOB-06 y especificación de herramientas y entorno de pruebas móviles (ADB, Scrcpy, Hardware Android).

```markdown
#### 2.1.1 Módulo App Móvil Cliente (.NET MAUI) — Responsable: Cristian Jordan

##### CP-MOB-01: Persistencia de Sesión ante Muerte de Proceso Android (Swipe-to-Close)
* Especialista Responsable: Cristian Jordan (QA Frontend Móvil & Usabilidad Android)
* Nivel: Sistema / Aceptación (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Verificar que el token JWT persista en almacenamiento dual (SecureStorage + Preferences) y se recupere sincrónicamente tras el cierre forzoso de la aplicación por el sistema operativo.
* Precondición: Cliente autenticado exitosamente en el dispositivo móvil con sesión activa.
* Procedimiento:
  1. Abrir la aplicación y verificar que se visualiza la pantalla principal (ClientHomePage).
  2. Presionar el botón de inicio de Android para enviar la aplicación a segundo plano.
  3. Abrir la bandeja de aplicaciones recientes del sistema operativo.
  4. Deslizar la tarjeta de la aplicación Shushine Studio hacia afuera (Swipe-to-Close) finalizando el proceso en memoria.
  5. Pulsar nuevamente el ícono de la aplicación en el launcher de Android.
* Resultado Esperado Formal: La aplicación inicia directamente en ClientHomePage. La validación sincrónica HasValidTokenSync() en el constructor de AppShell determina la existencia del token en menos de 10 ms. No se muestra la pantalla de inicio de sesión ni se le solicita la contraseña al usuario.

---

##### CP-MOB-02: Cierre de Sesión Voluntario y Purga de Credenciales Locales
* Especialista Responsable: Cristian Jordan (QA Frontend Móvil & Usabilidad Android)
* Nivel: Sistema / Seguridad (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Garantizar que el cierre de sesión voluntario elimine todos los identificadores de sesión en almacenamiento local y permita ingresar inmediatamente con otra cuenta sin fuga de datos.
* Precondición: Sesión activa en el dispositivo con Usuario_A.
* Procedimiento:
  1. Navegar a la pestaña "Perfil" en la barra de navegación inferior.
  2. Pulsar el botón "Cerrar sesión".
  3. En el cuadro de diálogo de confirmación, pulsar "Confirmar".
  4. Verificar la transición inmediata a la pantalla de inicio de sesión (LoginPage).
  5. Ingresar credenciales válidas de Usuario_B y pulsar "Iniciar sesión".
* Resultado Esperado Formal: El método ClearAsync() purga por completo SecureStorage y Preferences. No se detecta fuga de tokens entre cuentas. La aplicación inicia sesión con los datos específicos de Usuario_B.

---

##### CP-MOB-03: Navegación Shell y Prevención de Excepciones de Enrutamiento
* Especialista Responsable: Cristian Jordan (QA Frontend Móvil & Usabilidad Android)
* Nivel: Sistema / Usabilidad (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Comprobar que los botones de acción rápida en la pantalla de inicio transicionen hacia sus respectivas rutas sin arrojar ArgumentException ni cierres forzosos.
* Precondición: Usuario posicionado en ClientHomePage.
* Procedimiento:
  1. Pulsar el botón "Agendar una cita". Verificar navegación a StylistSelectionPage y retroceder.
  2. Pulsar el botón "Servicios". Verificar navegación a ServicesPage y retroceder.
  3. Pulsar el botón "Solicitudes". Verificar navegación a RequestsPage y retroceder.
  4. Pulsar el botón "Chat Atelier". Verificar navegación a ClientChatPage y retroceder.
* Resultado Esperado Formal: Cada botón redirige a su ruta Shell registrada. La pila de navegación opera limpiamente sin arrojar excepciones de ruta inexistente y la aplicación permanece estable.

---

##### CP-MOB-04: Exclusión Mutua en Renderizado de 'Tu Próxima Cita'
* Especialista Responsable: Cristian Jordan (QA Frontend Móvil & Usabilidad Android)
* Nivel: UI / Estabilidad (Caja Negra) | Prioridad: Media (P2)
* Objetivo Técnico: Validar que la sección de próxima cita no presente solapamiento visual entre la tarjeta de datos de la cita y el indicador de ausencia de citas.
* Precondición: Usuario cliente con exactamente 1 cita confirmada registrada en base de datos.
* Procedimiento:
  1. Abrir la pantalla principal de la aplicación móvil.
  2. Inspeccionar la sección superior rotulada "Tu Próxima Cita".
  3. Verificar la propiedad IsVisible del contenedor de la cita y del contenedor de estado vacío (EmptyStateView).
* Resultado Esperado Formal: Se exhibe únicamente la tarjeta con los datos de la cita confirmada (servicio, estilista, fecha, hora). La tarjeta "Sin citas pendientes" permanece con IsVisible = False sin ocupar espacio en pantalla. Ambas vistas son mutuamente excluyentes.

---

##### CP-MOB-05: Transición al Detalle de Cita sin Fallos de Casteo XAML
* Especialista Responsable: Cristian Jordan (QA Frontend Móvil & Usabilidad Android)
* Nivel: Estabilidad (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Validar que la navegación hacia AppointmentDetailPage resuelva correctamente los convertidores de enlaces compilados sin disparar InvalidCastException.
* Precondición: Tarjeta de próxima cita visible en ClientHomePage.
* Procedimiento:
  1. Pulsar directamente sobre el cuerpo de la tarjeta de cita o en el enlace "Ver detalle".
  2. Esperar la carga completa de AppointmentDetailPage.
  3. Inspeccionar la salida de adb logcat en busca de excepciones no capturadas durante la fase de layout.
* Resultado Esperado Formal: La vista AppointmentDetailPage se renderiza de forma inmediata mostrando el resumen de la reserva, estilista y badge de estado. Cero excepciones de casteo XAML detectadas en consola.

---

##### CP-MOB-06: Gestión de Pestaña 'Mis Citas' (Próximas vs Historial)
* Especialista Responsable: Cristian Jordan (QA Frontend Móvil & Usabilidad Android)
* Nivel: Sistema / Funcional (Caja Negra) | Prioridad: Media (P2)
* Objetivo Técnico: Comprobar la alternancia de filtros entre citas programadas y pasadas con soporte de gesto Pull-to-Refresh.
* Precondición: Cliente con historial de citas activas y pasadas en base de datos.
* Procedimiento:
  1. Seleccionar la pestaña "Citas" en la barra de navegación inferior.
  2. Verificar la lista bajo el selector predeterminado "Próximas".
  3. Pulsar el selector "Historial".
  4. Realizar el gesto de deslizamiento vertical hacia abajo (Pull-to-Refresh).
* Resultado Esperado Formal: La lista conmuta mostrando las citas correspondientes a cada segmento con sus insignias de estado (Confirmada, Completada, Cancelada). El gesto de refresco sincroniza la lista con el backend y oculta el indicador de carga.

---

#### Aportes de Cristian Jordan a la Sección 4 (Recursos y Entorno Móvil)

* Herramientas de Prueba Móvil:
  * Android Debug Bridge (ADB) (Platform-Tools 34+): Captura de logs de sistema en tiempo real (adb logcat), inyección de eventos de ciclo de vida de procesos y despliegue de paquetes APK.
  * Scrcpy (v2.4+): Proyección, visualización e interacción en espejo con baja latencia del dispositivo móvil Android físico.
* Plataforma de Hardware Móvil:
  * Dispositivo Móvil de Prueba: Smartphone físico Android (HyperOS / MIUI sobre Android 14), 6 GB de memoria RAM, procesador octa-core, pantalla FHD+ (1080x2400 px), conectado a red Wi-Fi de 5 GHz.
```

---

# BLOQUE 3: RODRIGO CORTEZ

**Rol Técnico:** QA Panel Web & Operación de Salón
**Secciones a Copiar:** Casos de prueba CP-WEB-01 al CP-WEB-06 y especificación de herramientas y entorno de pruebas web (DevTools, Playwright, Estación de Trabajo).

```markdown
#### 2.1.2 Módulo Panel Administrativo Web (Alpine.js) — Responsable: Rodrigo Cortez

##### CP-WEB-01: Módulo de Chat & Soporte en Tiempo Real
* Especialista Responsable: Rodrigo Cortez (QA Panel Web & Operación de Salón)
* Nivel: UAT / Integración (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Validar que el módulo de Chat y Soporte se encuentre disponible en el menú de navegación y permita la inserción de respuestas en Supabase.
* Precondición: Administrador autenticado en el panel web.
* Procedimiento:
  1. Localizar el menú de navegación lateral izquierdo.
  2. Hacer clic sobre la opción "Chat & Soporte".
  3. Seleccionar una conversación de cliente en el listado lateral.
  4. Redactar un mensaje de prueba ("Estimada cliente, su cita está confirmada").
  5. Presionar el botón "Enviar" o la tecla Enter.
* Resultado Esperado Formal: El mensaje se renderiza inmediatamente en el hilo visual como emitido por el administrador. Se inserta un registro en la tabla mensajes de Supabase con el remitente correspondiente y timestamp actual.

---

##### CP-WEB-02: Filtro Predeterminado de Agenda: Citas de Hoy
* Especialista Responsable: Rodrigo Cortez (QA Panel Web & Operación de Salón)
* Nivel: Operativo (Caja Negra) | Prioridad: Media (P2)
* Objetivo Técnico: Asegurar que la vista inicial de la agenda filtre únicamente las citas de la jornada actual para optimizar la operación del recepcionista.
* Precondición: Existen citas registradas para hoy, días anteriores y días posteriores.
* Procedimiento:
  1. Navegar a la sección "Agenda & Citas" desde el menú lateral.
  2. Inspeccionar la tabla principal de citas y el encabezado de estado.
* Resultado Esperado Formal: La tabla filtra automáticamente y expone únicamente las citas cuya fecha coincida con la fecha actual del sistema. Se visualiza un badge de resumen que cuantifica las citas del día.

---

##### CP-WEB-03: Filtro de Agenda: Ver Todo (Historial) y Filtro por Fecha
* Especialista Responsable: Rodrigo Cortez (QA Panel Web & Operación de Salón)
* Nivel: UAT (Caja Negra) | Prioridad: Media (P2)
* Objetivo Técnico: Permitir al operador consultar el historial completo o filtrar por fecha mediante datepicker y filtros de estado.
* Mecanismo Técnico: Petición HTTP a Supabase REST con parámetros .select('*') y combinación condicional: .eq('fecha', fechaSeleccionada) y .eq('estado', estadoFiltro) en servidor, combinada con renderizado reactivo Alpine.js en memoria.
* Precondición: Módulo "Agenda & Citas" desplegado.
* Procedimiento:
  1. Pulsar el botón "Ver Todo (Historial)". Verificar el contenido de la tabla.
  2. Hacer clic en el campo "Filtrar por Día" y seleccionar una fecha diferente en el calendario.
  3. Alternar el filtro de estado entre "Todas", "Confirmadas" y "Pendientes".
* Resultado Esperado Formal: Al pulsar "Ver Todo", la tabla muestra todas las citas históricas sin filtro de fecha. Al escoger una fecha en el datepicker, la tabla conmuta en vivo mostrando exclusivamente los registros coincidentes mediante el parámetro .eq('fecha', ...).

---

##### CP-WEB-04: Creación de Nueva Cita desde el Panel Web
* Especialista Responsable: Rodrigo Cortez (QA Panel Web & Operación de Salón)
* Nivel: Transaccional (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Comprobar la inserción de una reserva manual desde el modal administrativo con asignación de folio único.
* Precondición: Módulo de Agenda abierto en el panel web.
* Procedimiento:
  1. Pulsar el botón "+ Nueva Cita".
  2. Completar los campos del modal: Cliente, Servicio, Estilista, Fecha y Hora.
  3. Pulsar el botón "Guardar Cita".
* Resultado Esperado Formal: Se genera un código de cita único con prefijo #SHU-2026-XXXX. El registro se inserta exitosamente en la tabla citas de Supabase, se presenta una notificación flotante (Toast) y la tabla de agenda se actualiza automáticamente.

---

##### CP-WEB-05: Registro de Entrada de Stock en Inventario del Salón
* Especialista Responsable: Rodrigo Cortez (QA Panel Web & Operación de Salón)
* Nivel: Gestión Inventario (Caja Negra) | Prioridad: Media (P2)
* Objetivo Técnico: Validar que el ingreso de insumos incremente el stock físico y registre el movimiento de auditoría correspondiente.
* Precondición: Módulo "Inventario del Salón" abierto.
* Procedimiento:
  1. Pulsar el botón "Entrada de Stock".
  2. Seleccionar un producto del catálogo de insumos.
  3. Digitar una cantidad de ingreso (ej. 10 unidades) y motivo ("Reabastecimiento de proveedor").
  4. Presionar "Guardar Entrada".
* Resultado Esperado Formal: Se crea un registro en la tabla movimientos_inventario de tipo entrada. La cantidad en existencia del producto aumenta en exactamente 10 unidades y el badge de estado se recalcula automáticamente a 'Óptimo'.

---

##### CP-WEB-06: Métricas Ejecutivas y KPIs 100% Reales
* Especialista Responsable: Rodrigo Cortez (QA Panel Web & Operación de Salón)
* Nivel: Integridad de Datos (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Confirmar que el Dashboard Ejecutivo no contenga valores fijos ni simulados y responda únicamente a data viva de Supabase.
* Precondición: Dashboard principal abierto.
* Procedimiento:
  1. Inspeccionar las 4 tarjetas métricas superiores (Ingresos, Citas del Día, Clientes VIP, Productos Críticos).
  2. Abrir la consola de red de DevTools (F12) y constatar las peticiones REST hacia Supabase.
  3. Hacer clic en la campana de notificaciones.
* Resultado Esperado Formal: Los montos y contadores se derivan de funciones de agregación reales (SUM, COUNT). Las alertas emitidas corresponden estrictamente a productos bajo el stock mínimo o citas pendientes reales.

---

#### Aportes de Rodrigo Cortez a la Sección 4 (Recursos y Entorno Web)

* Herramientas de Prueba Web:
  * Google Chrome / Chromium DevTools (v120+): Inspección del DOM, depuración de estado reactivo Alpine.js, auditoría de peticiones REST de red y tiempos de respuesta.
  * Playwright Engine (Chromium Headless v1.57+): Automatización y captura de evidencias web de extremo a extremo y generación de reportes PDF de alta fidelidad.
* Estación de Trabajo QA:
  * Equipo PC Linux (Lubuntu / Ubuntu x86_64), 16 GB memoria RAM, terminal de comandos bash y conexión de red de alta velocidad.
  * Panel Web: Servidor local HTTP y CDN Vercel para pruebas de carga en red.
```

---

# BLOQUE 4: CORNELIO MARTINEZ

**Rol Técnico:** QA Backend, Integración & Seguridad RLS
**Secciones a Copiar:** Casos de prueba CP-INT-01 al CP-INT-03, Caso CP-SEC-01, Sección 2.2 (Niveles de prueba), Sección 4.2 (Infraestructura Backend y Datos de Prueba), Términos/Acrónimos, Alineación ISO 29119 y Firmas.

```markdown
#### 2.1.3 Módulo Integración Omnicanal End-to-End (E2E) — Responsable: Cornelio Martinez

##### CP-INT-01: Reflejo en Panel Web de Cita Agendada desde App Móvil
* Especialista Responsable: Cornelio Martinez (QA Backend, Integración & Seguridad RLS)
* Nivel: Integración E2E (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Verificar la propagación en tiempo real de una reserva creada desde el smartphone del cliente hacia la agenda web del operador.
* Precondición: App móvil abierta en el celular y Panel Web abierto simultáneamente en la computadora.
* Procedimiento:
  1. En el teléfono móvil, completar una reserva de servicio ("Corte & Brushing") y confirmar.
  2. Medir el tiempo transcurrido hasta que el registro se visualice en el Panel Web sin recargar la página (F5).
* Resultado Esperado Formal: La cita aparece en la tabla de la agenda web en un lapso inferior a 5 segundos con el badge de origen "App Móvil" y se emite la notificación visual correspondiente.

---

##### CP-INT-02: Reflejo en App Móvil de Cita Agendada desde Panel Web
* Especialista Responsable: Cornelio Martinez (QA Backend, Integración & Seguridad RLS)
* Nivel: Integración E2E (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Confirmar que una cita asignada por el recepcionista aparezca de inmediato en la aplicación del cliente.
* Precondición: Cliente con sesión abierta en la aplicación móvil.
* Procedimiento:
  1. En el Panel Web, el recepcionista crea una nueva cita para el cliente especificado.
  2. El cliente abre su app móvil o refresca la vista de inicio.
* Resultado Esperado Formal: La sección "Tu Próxima Cita" y la pestaña "Mis Citas" del cliente muestran la nueva reserva con todos los datos sincronizados.

---

##### CP-INT-03: Mensajería Bidireccional en Tiempo Real (Chat Móvil <-> Soporte Web)
* Especialista Responsable: Cornelio Martinez (QA Backend, Integración & Seguridad RLS)
* Nivel: Integración E2E (Caja Negra) | Prioridad: Alta (P1)
* Objetivo Técnico: Validar la comunicación instantánea mediante el canal de WebSockets de Supabase Realtime.
* Precondición: Cliente en vista Chat de la app móvil; Administrador en módulo Chat & Soporte del panel web.
* Procedimiento:
  1. El cliente envía el mensaje: "¿Tienen disponibilidad de cambio de horario?".
  2. El administrador visualiza el mensaje y responde: "Con gusto, disponemos de las 4:00 PM".
* Resultado Esperado Formal: Ambos mensajes se reciben y despliegan en sus respectivas interfaces en tiempo real sin requerir acciones manuales de recarga.

---

#### 2.1.4 Módulo Seguridad y Políticas Row Level Security (RLS) — Responsable: Cornelio Martinez

##### CP-SEC-01: Verificación de Aislamiento y Violación de Políticas RLS en Supabase
* Especialista Responsable: Cornelio Martinez (QA Backend, Integración & Seguridad RLS)
* Nivel: Seguridad / Integración (Caja Gris) | Prioridad: Alta (P1)
* Norma Asociada: ISO 9001:2015 Cláusula 8.2 / OWASP Top 10 API Security (BOLA — Broken Object Level Authorization).
* Objetivo Técnico: Garantizar que las directivas RLS en PostgreSQL impidan que un usuario consulte, modifique o elimine reservas pertenecientes a otro usuario utilizando tokens ajenos o la clave pública anon_key.
* Precondición: Usuario_A posee citas registradas con su ID. Usuario_B posee token JWT válido propio.
* Procedimiento:
  1. Desde un cliente REST (Postman), interceptar el token de autenticación de Usuario_B.
  2. Emitir petición GET https://acikahicfjtojuvqcvxv.supabase.co/rest/v1/citas?cliente_id=eq.{ID_Usuario_A} portando el Bearer token de Usuario_B.
  3. Emitir petición PATCH intentando alterar el estado de la cita de Usuario_A a "Cancelada".
  4. Emitir petición GET /citas utilizando únicamente la anon_key pública sin cabecera de autorización de usuario.
* Resultado Esperado Formal: La consulta GET de Usuario_B retorna un arreglo vacío [] o HTTP 403 Forbidden. La mutación PATCH no altera ninguna fila (0 filas afectadas). La consulta con anon_key sin sesión es bloqueada por las directivas de seguridad a nivel de fila (auth.uid() = cliente_id).

---

### 2.2 Niveles de prueba

El plan operacionaliza tres niveles formales conforme al ciclo de vida del software:

1. Nivel 1 — Pruebas Unitarias (Caja Blanca):
   * Objetivo: Verificar la lógica aislada de clases, servicios, viewmodels y convertidores sin dependencias de red externas.
   * Herramientas: xUnit, Moq, FluentAssertions en C#; Jest para funciones frontend.
   * Técnica: Análisis de cobertura de ramas y caminos lógicos de ejecución.
2. Nivel 2 — Pruebas de Integración (Caja Gris):
   * Objetivo: Verificar contratos JSON de endpoints REST, integridad referencial de base de datos relacional y directivas Row Level Security (RLS).
   * Herramientas: Postman, Newman, Supabase SQL Editor.
   * Técnica: Validación de esquemas JSON, inyección controlada de headers JWT y verificación de transacciones ACID.
3. Nivel 3 — Pruebas de Sistema y Aceptación (UAT) (Caja Negra):
   * Objetivo: Certificar los flujos completos de negocio desde la experiencia del cliente (app Android) y del operador del salón (panel web).
   * Herramientas: Dispositivos físicos Android, Google Chrome DevTools, Playwright.
   * Técnica: Ejecución estructurada mediante escenarios BDD Gherkin.
4. Pruebas No Funcionales:
   * Seguridad: Aislamiento de sesiones en disco (Android KeyStore), prevención de ataques por manipulación de objetos directos (BOLA).
   * Resiliencia y Concurrencia: Verificación de políticas ante interrupciones de red, reintentos exponenciales en WebSockets y prevención de sobre-reserva en el mismo bloque horario mediante restricciones de base de datos UNIQUE(estilista_id, fecha, hora).

---

#### Aportes de Cornelio Martinez a la Sección 4 (Infraestructura Backend y Datos de Prueba)

* Infraestructura de Software y Backend:
  * Servidor Backend: Spring Boot 3.x desplegado localmente en puerto 8080.
  * Base de Datos y Motor en la Nube: Supabase PostgreSQL 15 (acikahicfjtojuvqcvxv.supabase.co) con motor Realtime habilitado para canales de suscripción WebSockets.
  * Herramientas API: Postman / REST Assured v10+ para emisión de peticiones HTTP, inyección de tokens JWT y auditoría de directivas RLS.
* Datos de Prueba de Línea Base (Test Data Baseline):
  * Usuario Administrador: admin@shushine.com (Rol: ADMIN, UID asignado en Supabase Auth, acceso irrestricto a agenda y bodega).
  * Usuario Cliente de Prueba 1: cliente1@test.com (Cliente registrado con exactamente 1 cita confirmada para la fecha actual).
  * Usuario Cliente de Prueba 2: cliente2@test.com (Cliente registrado nuevo sin citas previas registradas para validar estado vacío).
  * Estilistas Registrados: 3 profesionales activos en catálogo con horarios asignados y especialidades definidas.
  * Catálogo de Servicios: Corte Dama ($25.00), Balayage ($85.00), Manicura Spa ($18.00), Keratina Premium ($65.00).

---

## TÉRMINOS / ACRÓNIMOS

* SUT (System Under Test): Sistema bajo prueba; se refiere a la solución integral Shushine Studio compuesta por el cliente móvil, el panel web y el backend.
* RLS (Row Level Security): Mecanismo de seguridad nativo de PostgreSQL que restringe la visibilidad y modificación de filas basándose en la identidad del usuario autenticado (auth.uid() = cliente_id).
* BOLA (Broken Object Level Authorization): Vulnerabilidad crítica de seguridad (OWASP API1) donde un usuario puede manipular identificadores de recursos para acceder a información de otros usuarios.
* JWT (JSON Web Token): Estándar abierto RFC 7519 utilizado para la transmisión segura de afirmaciones de identidad entre el cliente y el backend.
* MAUI (.NET Multi-platform App UI): Framework de Microsoft para la construcción de interfaces de usuario nativas multiplataforma utilizando C# y XAML.
* ADB (Android Debug Bridge): Herramienta de línea de comandos para la depuración y gestión de dispositivos con sistema operativo Android.
* BDD (Behavior-Driven Development): Metodología de desarrollo guiada por comportamiento que utiliza el lenguaje Gherkin (Dado, Cuando, Entonces) para definir especificaciones legibles y verificables.
* UAT (User Acceptance Testing): Pruebas de aceptación del usuario orientadas a certificar que el sistema cumple con las necesidades y expectativas del negocio antes de su despliegue formal.
* DoD (Definition of Done): Criterios rigurosos de finalización acordados por el equipo para considerar que una historia de usuario o ciclo de prueba ha finalizado satisfactoriamente.
* SLA (Service Level Agreement): Compromiso formal que define el tiempo límite establecido para la resolución de un defecto clasificado por severidad.
* API REST: Interfaz de programación de aplicaciones fundamentada en los principios arquitectónicos de transferencia de estado representacional mediante el protocolo HTTP.
* RACI (Responsible, Accountable, Consulted, Informed): Matriz de asignación de responsabilidades utilizada para clarificar el rol de cada integrante en los procesos de ingeniería.

---

## ALINEACIÓN CON LA NORMA INTERNACIONAL ISO/IEC/IEEE 29119

El presente plan incorpora de manera formal los lineamientos del estándar internacional ISO/IEC/IEEE 29119, estructurándose en tres dimensiones normativas clave:

1. ISO/IEC/IEEE 29119-1 (Conceptos y Definiciones): Adopción de terminología estandarizada de pruebas de software, diferenciación rigurosa entre diseño de pruebas y ejecución, y categorización sistemática de riesgos técnicos y operacionales.
2. ISO/IEC/IEEE 29119-2 (Procesos de Prueba Dinámicos):
   * Proceso de Diseño y Especificación: Implementación de técnicas formales de caja negra y caja gris con condiciones previas, pasos reproducibles y resultados esperados verificables.
   * Proceso de Gestión de Incidentes: Clasificación cuantitativa de defectos (S1 a S4) vinculada a acuerdos de nivel de servicio (SLAs) para priorizar correcciones.
   * Proceso de Monitoreo y Control: Definición de criterios de entrada, suspensión, reanudación y salida (Definition of Done) para gobernar el flujo de trabajo.
3. ISO/IEC/IEEE 29119-3 (Documentación de Pruebas): El esquema documental satisface los requisitos de integridad estructural del Software Test Plan (STP), garantizando la auditabilidad y trazabilidad completa del aseguramiento de calidad técnico.

---

## FIRMAS Y CONFORMIDAD DEL EQUIPO DE PRUEBAS

En señal de conformidad técnica y validación del diseño, alcance, metodología y responsabilidades estipuladas en el presente Plan Maestro de Pruebas de Software (STP-SHU-2026-V3), los integrantes del equipo de Aseguramiento de Calidad de la Escuela Superior Franciscana Especializada (ESFE AGAPE) suscriben el presente documento:

| Nombre del Integrante | Especialidad Técnica Asignada | Módulos / Casos Diseñados | Estado de Conformidad |
| :--- | :--- | :--- | :---: |
| Alex Fernando Alfaro Diaz | QA Lead / Estrategia & Riesgos | Secciones 1, 2.3, 2.4, 2.5, 3 y Gobernanza | [ APROBADO CONFORME ] |
| Cristian Jordan | QA Frontend Móvil .NET MAUI | Sección 2.1 (CP-MOB-01 al CP-MOB-06) | [ APROBADO CONFORME ] |
| Rodrigo Cortez | QA Panel Web & Operaciones | Sección 2.1 (CP-WEB-01 al CP-WEB-06) | [ APROBADO CONFORME ] |
| Cornelio Martinez | QA Backend, Integración & RLS | Sección 2.1 (CP-INT y CP-SEC) | [ APROBADO CONFORME ] |

---
SHUSHINE STUDIO — EQUIPO DE ASEGURAMIENTO DE CALIDAD DE SOFTWARE (TESTING QA)  
ESFE AGAPE / MEGATEC — Ciclo II - 2026
```
