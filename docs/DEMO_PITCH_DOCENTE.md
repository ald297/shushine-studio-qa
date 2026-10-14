# 🎓 Ficha Oficial de Demostración y Defensa Académica (Demo Pitch) — Shushine Studio

> **Proyecto:** Shushine Studio — Sistema Móvil de Gestión y Reservas para Salón de Belleza  
> **Institución:** Escuela Superior Franciscana Especializada (ESFE AGAPE / MEGATEC)  
> **Equipo de Desarrollo:**  
> - **Alex Fernando Alfaro Diaz** (Backend Lead & Scrum Master)  
> - **Camila Antonia Calderon Cortez** (Frontend Móvil & QA Lead)  
> **Fecha de Defensa:** Convocatoria Oficial 2026  
> **Versión del Sistema:** v1.0.0-Release  

---

## 🌐 1. Ficha Técnica del Entorno y Credenciales Oficiales

### 1.1 Entorno de Ejecución en la Nube
| Componente | Plataforma / Proveedor | URL / Conexión Oficial |
| :--- | :--- | :--- |
| **API RESTful (Java Spring Boot 3.3.3)** | Render Cloud (Dockerized / Linux) | `https://shushine-studio.onrender.com` |
| **Documentación Interactiva Swagger UI** | OpenAPI 3.0 con Bearer Token | `https://shushine-studio.onrender.com/swagger-ui/index.html` |
| **Base de Datos Relacional (PostgreSQL)** | Supabase Cloud en AWS Pooler | Host: `db.acikahicfjtojuvqcvxv.supabase.co` (Puerto: `5432` / `6543`) |
| **Frontend Móvil Multiplataforma** | .NET MAUI (Android 21+ / C# XAML) | Paquete compilado `com.shushinestudio.mobile` |
| **Control de Versiones & Tableros** | Azure DevOps & GitHub | Organización: `cc25003` / Proyecto: `Shunshine Studio` |

### 1.2 Credenciales Sembradas de Prueba (`DataInitializer`)
El sistema cuenta con un semillero transaccional automatizado que detecta si la base de datos está vacía y puebla automáticamente los usuarios con roles y contraseñas cifradas en **BCrypt**:

| Rol | Usuario (`login`) | Contraseña (`clave`) | Permisos y Alcance |
| :--- | :--- | :--- | :--- |
| **ADMINISTRADOR** | `admin` | `admin123` | Control total del sistema, métricas del salón (`/api/dashboard`), gestión de estilistas, bloqueos y auditoría. |
| **CLIENTE** | `cliente` | `cliente123` | Consulta de catálogo, disponibilidad horaria, agendamiento de citas (`POST /api/citas`) y consulta de historial. |
| **RECEPCIONISTA** | `recepcion` | `recepcion123` | Registro de clientes presenciales Walk-in (`/api/citas/walkin`), consulta de timeline diario y cobro en caja. |

---

## ⏱️ 2. Guion de Demostración en Vivo para el Jurado Docente (5 a 7 Minutos)

Este guion está cronometrado para guiar a los evaluadores por los puntos más fuertes y exigentes de la rúbrica académica:

### Minuto 0:00 – 1:00 | Apertura y Arquitectura Pedagógica
* **Orador:** Alex Fernando Alfaro Diaz.
* **Mensaje Clave:**  
  *"Buenos días, estimado jurado evaluador. Shushine Studio es una solución integral de alta disponibilidad diseñada bajo los estándares de **Clean Architecture** y principios **SOLID**. Para garantizar escalabilidad empresarial y desacoplamiento, estructuramos la solución en dos agentes independientes:*
  1. *Un backend en **Java Spring Boot 3.3.3** desplegado en **Render Cloud**, que actúa como la **Única Fuente de Verdad**, ejecutando cálculos financieros, auditoría y control de concurrencia.*
  2. *Una base de datos **PostgreSQL relacional en Supabase** con políticas Row Level Security (RLS).*
  3. *Un cliente móvil nativo en **.NET MAUI** liderado por Camila Calderon, diseñado con **MVVM CommunityToolkit** y consumo asíncrono.*"

### Minuto 1:00 – 2:30 | Demostración en Swagger UI: Seguridad JWT y Catálogo
* **Acción en Pantalla:** Abrir [Swagger UI en Vivo](https://shushine-studio.onrender.com/swagger-ui/index.html).
* **Paso 1:** Ejecutar `POST /api/auth/login` con `{"login": "cliente", "clave": "cliente123"}`.
  * *Mostrar el token JWT emitido y copiarlo al botón verde **Authorize** de Swagger.*
* **Paso 2:** Explicar el **Patrón de Endpoints Duales (Regla 3.4 de AGENTS.md)**:
  * Mostrar `GET /api/servicios` (Paginado con `Pageable` para servidores con miles de registros).
  * Mostrar `GET /api/servicios/lista` (Respuesta JSON directa optimizada para selectores móviles en .NET MAUI).

### Minuto 2:30 – 4:00 | El Núcleo de Negocio: Motor de Disponibilidad en Tiempo Real
* **Acción en Pantalla:** Ejecutar `GET /api/estilistas/1/disponibilidad?fecha=2026-12-01&servicioId=1`.
* **Explicación Técnica:**
  * *"La API no guarda franjas estáticas en tablas rígidas. El motor `DisponibilidadService` cruza dinámicamente el horario de apertura del salón (`09:00 a 18:00`), el horario de almuerzo del estilista (`13:00 a 14:00`), la duración del servicio seleccionado (`45 minutos`) y las citas ya reservadas en PostgreSQL.*
  * *Observen cómo las franjas de almuerzo y las franjas ya ocupadas retornan automáticamente `disponible: false` con su motivo legible."*

### Minuto 4:00 – 5:30 | La Joya de la Corona: Certificación de Concurrencia ACID (RNF02)
* **Objetivo:** Demostrar que el sistema previene al 100% el fenómeno de sobreventa o doble reserva (*Double-Booking*).
* **Acción:** Abrir terminal o mostrar el reporte [docs/REPORTE_CONCURRENCIA_RNF02.md](file:///home/alex/Desktop/Shushine_Studio/docs/REPORTE_CONCURRENCIA_RNF02.md).
* **Demostración:**
  * *"Sometimos la API en Render a una prueba de estrés disparando **10 hilos HTTP en el mismo milisegundo exacto**, intentando reservar la misma estilista (Valeria Morales) en la misma fecha y hora.*
  * *Gracias a la anotación `@Transactional(isolation = Isolation.SERIALIZABLE)` y el algoritmo SSI de PostgreSQL:*
    * *Exactamente **1 petición ganó la carrera** y obtuvo código `201 Created` (Cita `#SHU-2026-3476`).*
    * *Las otras **9 peticiones fueron bloqueadas de inmediato** con código `409 Conflict` bajo el estándar de la industria **RFC 7807 (`application/problem+json`)**.*
    * *Tasa de sobre-reserva: **0.00%**.*"

### Minuto 5:30 – 7:00 | Integración Móvil (.NET MAUI) y Manejo Resiliente de Errores
* **Orador:** Camila Antonia Calderon Cortez / Alex Alfaro.
* **Mensaje Clave:**
  * *"En el frontend .NET MAUI, desacoplamos totalmente la interfaz visual de las llamadas de red mediante Casos de Uso (`GetServiciosCatalogUseCase`, `CreateAppointmentUseCase`).*
  * *Contamos con un interceptor centralizado (`ErrorDelegatingHandler`) que inyecta el token Bearer en hardware seguro (`SecureStorage`) y captura los errores RFC 7807 para transformarlos en alertas amigables en español sin que la app sufra un crash.*
  * *Todo el ciclo de vida del Sprint fue gestionado en **Azure Boards** con tableros Kanban, ramas `feature/*` y control de calidad estricto."*

---

## 💡 3. Banco de Preguntas Frecuentes del Jurado y Respuestas Maestras

### P1: "¿Cómo garantizan a nivel de base de datos que dos estilistas o clientes no colisionen en el mismo horario?"
> **Respuesta:**  
> *"Implementamos una doble barrera de seguridad:  
> 1. **Nivel Aplicación/Transaccional:** El método de creación opera con `@Transactional(isolation = Isolation.SERIALIZABLE)`. Esto activa el aislamiento de instantáneas serializables (SSI) en PostgreSQL, detectando dependencias cruzadas de lectura y escritura.  
> 2. **Nivel Consulta Atómica:** Antes de insertar, el repositorio ejecuta una consulta de solapamiento temporal matemático: `(:horaInicio < c.horaFin AND :horaFin > c.horaInicio)`. Si existe solapamiento o colisión de concurrencia, lanzamos una excepción traducida por nuestro `GlobalExceptionHandler` al código estándar `409 Conflict`."*

### P2: "¿Por qué la aplicación móvil no consulta directamente las tablas de Supabase utilizando el SDK de cliente?"
> **Respuesta (Regla 3.3 de AGENTS.md):**  
> *"Por seguridad y arquitectura limpia. En entornos de producción empresarial, permitir que un cliente móvil ejecute consultas directas de base de datos expone la lógica de negocio y debilita la auditoría.  
> La **Web API es la única autoridad de negocio**: calcula subtotales, IVA del 13%, reglas de fidelidad y slots de estilistas. La app móvil consume contratos REST tipados, y en Supabase activamos **Row Level Security (RLS)** en el esquema público para denegar accesos anónimos directos."*

### P3: "¿Qué sucede si el usuario pierde la conexión a internet en medio del flujo de reserva?"
> **Respuesta:**  
> *"El manejador `ErrorDelegatingHandler` de .NET MAUI captura excepciones de tipo `HttpRequestException` o `SocketException` en el cliente. En lugar de cerrarse de manera abrupta, notifica al hilo principal mediante `MainThread.InvokeOnMainThreadAsync` y despliega un diálogo informativo: 'Sin Conexión con el Salón', permitiendo reintentar la operación en cuanto se recupere la señal sin perder el estado del formulario."*

### P4: "¿Cómo cumple la API con las convenciones de commit y autoría de código?"
> **Respuesta:**  
> *"Seguimos el estándar **Conventional Commits** en idioma español con alcance granular (`feat(citas):`, `fix(interceptor):`, `docs(concurrencia):`). Además, bajo nuestras reglas de equipo, el 100% de los commits y tareas de Azure Boards están formalmente atribuidos a los integrantes humanos del proyecto (**Alex Alfaro** y **Camila Calderon**)."*

---

## 📋 4. Lista de Verificación Previa a la Presentación (Checklist)

- [x] Web API de Render levantada y respondiendo (`HTTP 200` en Swagger UI).
- [x] Base de datos Supabase PostgreSQL activa con tablas y datos semilla cargados.
- [x] Token Bearer probado y funcional para usuario `admin` y `cliente`.
- [x] Reportes de certificación disponibles en el repositorio (`REPORTE_CONCURRENCIA_RNF02.md` y `REPORTE_PRUEBAS_API_EN_VIVO.md`).
- [x] Tareas de Azure Boards en estado `Done` y sincronizadas en Azure Repos.
- [x] Entidades, DTOs y Repositorios de .NET MAUI compilados sin errores de dependencias.
