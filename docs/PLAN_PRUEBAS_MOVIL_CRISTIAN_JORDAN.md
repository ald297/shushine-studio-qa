# PLAN DE PRUEBAS MÓVIL Y REPORTE DE EJECUCIÓN FORMAL — SHUSHINE STUDIO (.NET MAUI)
**Organización:** Shushine Studio Quality Assurance Team  
**Responsable QA:** Cristian Jordan Pérez (QA Frontend Móvil & Usabilidad Android)  
**Estándares de Referencia:** ISO/IEC/IEEE 29119-3:2013 / IEEE 829-2008  
**Plataforma Objetivo:** Android 14 (HyperOS / MIUI) sobre Smartphone Físico  
**Estado General de la Suite:** 100% Aprobado (6/6 Casos de Prueba Exitosos)  
**Azure DevOps Epic / Feature:** `#177` - Feature 3: Pruebas de Aceptación UAT - App Móvil Cliente (.NET MAUI)  

---

## 1. Resumen Ejecutivo de Ejecución

Durante la jornada de pruebas de aceptación y estabilidad en dispositivo móvil Android físico, se evaluaron los 6 casos de prueba del módulo cliente desarrollados en .NET MAUI. Todas las pruebas concluyeron satisfactoriamente sin registrar bloqueos, excepciones no controladas ni fugas de estado o tokens de autenticación.

| Caso de Prueba | Descripción | Azure PBI / Tasks | Test Case Azure | Resultado | Evidencia Gráfica Principal |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **CP-MOB-01** | Persistencia de Sesión ante Muerte de Proceso (Swipe-to-Close) | PBI `#178`<br>Tasks `#179`, `#180` | `#265` | **APROBADO** | `evidencias_movil/CP-MOB-01_PBI178_SwipeToClose_TerminacionLimpia.jpeg` |
| **CP-MOB-02** | Cierre de Sesión Voluntario y Cambio de Cuenta Limpio | PBI `#181`<br>Tasks `#182`, `#183` | `#266` | **APROBADO** | `evidencias_movil/CP-MOB-02_PBI181_LoginNuevaCuentaCamila.jpeg` |
| **CP-MOB-03** | Navegación Shell y Prevención de Excepciones de Enrutamiento | PBI `#184`<br>Tasks `#185`, `#186` | `#267` | **APROBADO** | `evidencias_movil/CP-MOB-03_PBI184_ShellNavegacionCatalogoServicios.jpeg` |
| **CP-MOB-04** | Exclusión Mutua en Renderizado de 'Tu Próxima Cita' (EmptyState) | PBI `#187`<br>Tasks `#188`, `#189` | `#268` | **APROBADO** | `evidencias_movil/CP-MOB-04_PBI187_TarjetaCitaActivaExclusionMutua.jpeg` |
| **CP-MOB-05** | Transición al Detalle de Cita sin Fallos de Casteo XAML | PBI `#190`<br>Tasks `#191`, `#192` | `#269` | **APROBADO** | `evidencias_movil/CP-MOB-05_PBI190_DetalleCitaFolioSHU20261341.jpeg` |
| **CP-MOB-06** | Pestaña 'Mis Citas' (Próximas vs Historial) y Pull-to-Refresh | PBI `#193`<br>Tasks `#194`, `#195` | `#270` | **APROBADO** | `evidencias_movil/CP-MOB-06_PBI193_HistorialDetalleTarjetasEstados.jpeg` |

---

## 2. Detalle de Ejecución y Validación por Caso de Prueba

### CP-MOB-01: Persistencia de Sesión ante Muerte de Proceso Android (Swipe-to-Close)
* **Objetivo Técnico:** Verificar que el token JWT persista en almacenamiento dual (`SecureStorage` + `Preferences`) y se recupere sincrónicamente tras el cierre forzoso de la aplicación por el sistema operativo.
* **Precondición:** Cliente autenticado en dispositivo Android con sesión activa.
* **Procedimiento:**
  1. Abrir la aplicación y verificar la vista `HomePage` con sesión activa.
  2. Presionar el botón Inicio de Android y abrir la bandeja de aplicaciones recientes.
  3. Deslizar la tarjeta de la app hacia afuera (Swipe-to-Close) liquidando el proceso.
  4. Reiniciar la app desde el launcher y monitorear `adb logcat`.
* **Resultado Obtenido:** La aplicación inició de manera sincrónica sin presentar parpadeo de LoginPage. Tiempo de lectura de token en `AppShell`: < 8 ms. Cero solicitudes de re-autenticación al usuario.
* **Evidencias:**
  * Bandeja de Recientes con App Shushine Studio: [Ver Evidencia](evidencias_movil/CP-MOB-01_PBI178_BandejaRecientes_Shushine.jpeg)
  * Terminación limpia de proceso (Bandeja Vacía): [Ver Evidencia](evidencias_movil/CP-MOB-01_PBI178_SwipeToClose_TerminacionLimpia.jpeg)

---

### CP-MOB-02: Cierre de Sesión Voluntario y Purga de Credenciales Locales
* **Objetivo Técnico:** Garantizar que el cierre de sesión voluntario elimine todos los identificadores de sesión en almacenamiento local y permita ingresar inmediatamente con otra cuenta sin fuga de datos.
* **Precondición:** Sesión activa con cuenta cliente.
* **Procedimiento:**
  1. Navegar a la pestaña "Perfil" (`ProfilePage`).
  2. Pulsar "Cerrar sesión" y confirmar en el modal de alerta.
  3. Verificar redirección inmediata a `LoginPage`.
  4. Ingresar credenciales de cuenta alterna ("Camila").
* **Resultado Obtenido:** El método `ClearAsync()` purgó por completo `SecureStorage` y `Preferences`. Se validó aislamiento total de tokens y la nueva cuenta inició con su respectiva información aislada.
* **Evidencias:**
  * Modal de Confirmación de Logout: [Ver Evidencia](evidencias_movil/CP-MOB-02_PBI181_DialogoConfirmacionLogout.jpeg)
  * Redirección a LoginPage con campos limpios: [Ver Evidencia](evidencias_movil/CP-MOB-02_PBI181_RedireccionLoginPageLimpia.jpeg)
  * Inicio de sesión exitoso con cuenta alterna: [Ver Evidencia](evidencias_movil/CP-MOB-02_PBI181_LoginNuevaCuentaCamila.jpeg)

---

### CP-MOB-03: Navegación Shell y Prevención de Excepciones de Enrutamiento
* **Objetivo Técnico:** Comprobar que los botones de acción rápida en la barra inferior y home transicionen hacia sus respectivas rutas sin arrojar `ArgumentException` ni cierres forzosos.
* **Precondición:** Usuario posicionado en `HomePage`.
* **Procedimiento:**
  1. Pulsar botón "Catálogo" y explorar servicios.
  2. Navegar a la ruta interna de "Solicitud Personalizada" (`CustomRequestPage`).
  3. Pulsar botón "Chat" (`ChatPage`) y enviar mensaje de prueba.
* **Resultado Obtenido:** Enrutamiento fluido en AppShell. Pila de navegación limpia y reactiva. Sin excepciones en runtime.
* **Evidencias:**
  * Vista de Catálogo de Servicios: [Ver Evidencia](evidencias_movil/CP-MOB-03_PBI184_ShellNavegacionCatalogoServicios.jpeg)
  * Formulario de Solicitud Personalizada: [Ver Evidencia](evidencias_movil/CP-MOB-03_PBI184_ShellNavegacionSolicitudPersonalizada.jpeg)
  * Canal de Chat en Tiempo Real: [Ver Evidencia](evidencias_movil/CP-MOB-03_PBI184_ShellNavegacionChatAtelier.jpeg)

---

### CP-MOB-04: Exclusión Mutua en Renderizado de 'Tu Próxima Cita'
* **Objetivo Técnico:** Validar que la sección de próxima cita no presente solapamiento visual entre la tarjeta de datos de la cita y el indicador de ausencia de citas (`EmptyStateView`).
* **Precondición:** Comparación entre usuario sin citas y usuario con cita confirmada.
* **Procedimiento:**
  1. Ingresar con cuenta sin citas programadas y registrar estado de `EmptyStateView`.
  2. Ingresar con cuenta con cita activa (`SHU-2026-1341`) y verificar exclusión mutua.
* **Resultado Obtenido:** Las propiedades `IsVisible` operan con estricta exclusión mutua: cuando hay cita activa, el contenedor de EmptyState no se renderiza en el árbol visual; cuando no hay cita, se muestra limpiamente el botón "Explorar Servicios".
* **Evidencias:**
  * EmptyState ("Sin citas pendientes"): [Ver Evidencia](evidencias_movil/CP-MOB-04_PBI187_EmptyStateSinCitasExclusionMutua.jpeg)
  * Tarjeta Activa (Folio SHU-2026-1341): [Ver Evidencia](evidencias_movil/CP-MOB-04_PBI187_TarjetaCitaActivaExclusionMutua.jpeg)

---

### CP-MOB-05: Transición al Detalle de Cita sin Fallos de Casteo XAML
* **Objetivo Técnico:** Validar que la navegación hacia `AppointmentDetailPage` resuelva correctamente los convertidores de enlaces compilados sin disparar `InvalidCastException`.
* **Precondición:** Tarjeta de próxima cita visible en `HomePage`.
* **Procedimiento:**
  1. Pulsar botón "Ver detalle" en la tarjeta de cita.
  2. Monitorear carga completa de `AppointmentDetailPage.xaml`.
  3. Inspeccionar logs de Android en busca de excepciones de casteo de tipos.
* **Resultado Obtenido:** Transición instantánea en menos de 120 ms. Desglose financiero completo ($24.86, Subtotal, IVA, Total), especialista Valeria Morales, fecha y hora formateadas correctamente.
* **Evidencias:**
  * Detalle de Cita Completo: [Ver Evidencia](evidencias_movil/CP-MOB-05_PBI190_DetalleCitaFolioSHU20261341.jpeg)

---

### CP-MOB-06: Gestión de Pestaña 'Mis Citas' (Próximas vs Historial) y Pull-to-Refresh
* **Objetivo Técnico:** Comprobar la alternancia de filtros entre citas programadas y pasadas con soporte de gesto Pull-to-Refresh.
* **Precondición:** Cliente con historial en base de datos.
* **Procedimiento:**
  1. Seleccionar pestaña "Citas" en la barra de navegación.
  2. Ejecutar gesto vertical de deslizamiento (Pull-to-Refresh) en la pestaña "Próximas".
  3. Conmutar a la pestaña "Historial (39)".
  4. Desplazarse por el listado verificando estado de insignias (Confirmed, Cancelled).
* **Resultado Obtenido:** El `RefreshView` activó el spinner de sincronización y cerró adecuadamente al resolver la API. El selector de segmento alternó inmediatamente mostrando las 39 citas del historial con badges consistentes.
* **Evidencias:**
  * Spinner de Pull-to-Refresh: [Ver Evidencia](evidencias_movil/CP-MOB-06_PBI193_PullToRefreshSpinnerProximas.jpeg)
  * Segmento Historial (39 citas): [Ver Evidencia](evidencias_movil/CP-MOB-06_PBI193_SegmentoHistorial39Citas.jpeg)
  * Detalle de Citas y Badges de Estado: [Ver Evidencia](evidencias_movil/CP-MOB-06_PBI193_HistorialDetalleTarjetasEstados.jpeg)

---

## 3. Entorno de Pruebas Móvil y Herramientas
* **Dispositivo Físico:** Smartphone Android (HyperOS / Android 14), 6 GB RAM, Pantalla FHD+ (1080x2400).
* **Herramientas de Diagnóstico:**
  * `adb logcat -s ShushineStudio:V *:E`
  * `adb shell am kill com.shushinestudio.clientapp`
  * `scrcpy` (proyección y captura de evidencias en tiempo real).
