/**
 * Script para actualizar y dar formato institucional en Google Docs
 * Autor: Antigravity IDE
 */
const pw = require('/home/ministorm/.cache/ms-playwright-go/1.57.0/package');

const htmlContent = `
<div style="font-family: Arial, sans-serif; font-size: 11pt; line-height: 1.5; color: #000000;">

  <!-- ENCABEZADO Y PORTADA -->
  <p style="text-align: center; font-size: 10pt; font-weight: bold; text-transform: uppercase; letter-spacing: 1.5pt; margin-bottom: 25pt;">
    ESCUELA SUPERIOR FRANCISCANA ESPECIALIZADA / AGAPE
  </p>

  <p style="text-align: center; margin-bottom: 20pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">CARRERA</strong><br>
    <span style="font-size: 10.5pt; color: #222222;">Técnico en Ingeniería de Desarrollo de Software</span>
  </p>

  <p style="text-align: center; margin-bottom: 20pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">MÓDULO</strong><br>
    <span style="font-size: 10.5pt; color: #222222;">Aseguramiento de la Calidad de Software / Formulación del Plan de Pruebas</span>
  </p>

  <p style="text-align: center; margin-bottom: 25pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">TÍTULO</strong><br>
    <span style="font-size: 14pt; font-weight: bold; color: #000000;">Plan Maestro de Pruebas de Software — Shushine Studio</span><br>
    <span style="font-size: 10pt; color: #444444; font-style: italic;">Ecosistema Omnicanal de Gestión y Reservas de Belleza (ISO/IEC/IEEE 29119-3 / IEEE 829)</span>
  </p>

  <p style="text-align: center; margin-bottom: 20pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">ESTUDIANTE(S)</strong><br>
    <span style="font-size: 10.5pt; color: #222222;">
      Alex Fernando Alfaro Diaz<br>
      Cristian Jordan<br>
      Rodrigo Cortez<br>
      Cornelio Martinez
    </span>
  </p>

  <p style="text-align: center; margin-bottom: 20pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">DOCENTE</strong><br>
    <span style="font-size: 10.5pt; color: #222222;">Docente Evaluador / Especialista en Ingeniería y Calidad de Software</span>
  </p>

  <p style="text-align: center; margin-bottom: 20pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">FECHA DE PRESENTACIÓN</strong><br>
    <span style="font-size: 10.5pt; color: #222222;">12 de Octubre de 2026</span>
  </p>

  <p style="text-align: center; margin-bottom: 35pt;">
    <strong style="font-size: 11pt; text-transform: uppercase;">CICLO</strong><br>
    <span style="font-size: 10.5pt; color: #222222;">Ciclo II - 2026</span>
  </p>

  <hr style="border: 0; border-top: 1.5pt solid #000000; margin: 25pt 0;">

  <!-- ÍNDICE GENERAL -->
  <h2 style="font-size: 14pt; font-weight: bold; text-align: center; text-transform: uppercase; letter-spacing: 1pt; margin-top: 20pt; margin-bottom: 15pt;">
    ÍNDICE GENERAL DEL PLAN DE PRUEBAS
  </h2>

  <p style="margin: 4pt 0;"><strong>1. Introducción</strong></p>
  <p style="margin: 4pt 0 4pt 20pt;">1.1 Alcance</p>
  <p style="margin: 4pt 0 4pt 40pt;">1.1.1 Alcance</p>
  <p style="margin: 4pt 0 4pt 40pt;">1.1.2 Fuera de alcance</p>
  <p style="margin: 4pt 0 4pt 20pt;">1.2 Objetivo de Calidad</p>
  <p style="margin: 4pt 0 4pt 20pt;">1.3 Roles y Responsabilidades</p>

  <p style="margin: 4pt 0;"><strong>2. Metodología de prueba</strong></p>
  <p style="margin: 4pt 0 4pt 20pt;">2.1 Compendio</p>
  <p style="margin: 4pt 0 4pt 40pt;">2.1.1 Módulo App Móvil Cliente (.NET MAUI) — Resp: Cristian Jordan</p>
  <p style="margin: 4pt 0 4pt 40pt;">2.1.2 Módulo Panel Administrativo Web (Alpine.js) — Resp: Rodrigo Cortez</p>
  <p style="margin: 4pt 0 4pt 40pt;">2.1.3 Módulo Integración Omnicanal End-to-End — Resp: Cornelio Martinez</p>
  <p style="margin: 4pt 0 4pt 40pt;">2.1.4 Módulo Seguridad y Políticas RLS — Resp: Cornelio Martinez</p>
  <p style="margin: 4pt 0 4pt 20pt;">2.2 Niveles de prueba</p>
  <p style="margin: 4pt 0 4pt 20pt;">2.3 Clasificación de errores</p>
  <p style="margin: 4pt 0 4pt 20pt;">2.4 Criterios de suspensión y requisitos de reanudación</p>
  <p style="margin: 4pt 0 4pt 20pt;">2.5 Completitud de la prueba</p>

  <p style="margin: 4pt 0;"><strong>3. Entregables de la prueba</strong></p>

  <p style="margin: 4pt 0;"><strong>4. Necesidades de recursos y medio ambiente</strong></p>
  <p style="margin: 4pt 0 4pt 20pt;">4.1 Herramientas de prueba</p>
  <p style="margin: 4pt 0 4pt 20pt;">4.2 Entorno de prueba</p>

  <p style="margin: 4pt 0;"><strong>Términos / Acrónimos</strong></p>
  <p style="margin: 4pt 0;"><strong>Alineación con la Norma Internacional ISO/IEC/IEEE 29119</strong></p>
  <p style="margin: 4pt 0;"><strong>Firmas y Conformidad del Equipo de Pruebas</strong></p>

  <hr style="border: 0; border-top: 1.5pt solid #000000; margin: 25pt 0;">

  <!-- SECCIÓN 1: INTRODUCCIÓN -->
  <h1 style="font-size: 15pt; font-weight: bold; color: #000000; margin-top: 25pt; margin-bottom: 10pt; text-transform: uppercase;">
    1. INTRODUCCIÓN
  </h1>

  <p style="text-align: justify; margin-bottom: 10pt;">
    El presente Plan Maestro de Pruebas de Software define de manera estructurada, rigurosa y aplicable la estrategia de aseguramiento de calidad técnico para el sistema <strong>Shushine Studio</strong>, una plataforma tecnológica omnicanal orientada a la gestión integral de reservas, control de inventario en bodega y atención a clientes en salones de belleza y estética profesional.
  </p>

  <p style="text-align: justify; margin-bottom: 15pt;">
    En estricto cumplimiento con las directrices institucionales de la <strong>Escuela Superior Franciscana Especializada (ESFE AGAPE)</strong> y los estándares internacionales <strong>ISO/IEC/IEEE 29119-3:2021</strong> e <strong>IEEE 829-2008</strong>, este documento formaliza la <strong>fase de planificación, diseño y especificación metódica de pruebas</strong>. En consecuencia, este plan <strong>no documenta resultados prematuros de ejecución</strong>; su función ingenieril consiste en fijar el marco metodológico, las condiciones previas, los procedimientos paso a paso y los resultados esperados verificables que certifiquen la calidad del software antes de su liberación formal a producción.
  </p>

  <!-- 1.1 ALCANCE -->
  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 20pt; margin-bottom: 8pt;">
    1.1 Alcance
  </h2>

  <p style="text-align: justify; margin-bottom: 8pt;">
    El sistema bajo prueba (System Under Test — SUT) comprende una arquitectura desacoplada de tres subsistemas interactuantes:
  </p>

  <ul style="margin-bottom: 15pt;">
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Cliente Móvil (.NET MAUI en Android):</strong> Aplicación nativa multiplataforma orientada al cliente final para autenticación persistente, catálogo interactivo de servicios, selección de estilistas, agenda de citas y canal de mensajería con el atelier.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Panel Administrativo Web (Alpine.js + Tailwind CSS):</strong> Plataforma web reactiva de escritorio orientada a recepcionistas, estilistas y gerentes del salón para control de agenda operativa diaria, creación de citas manuales, administración de stock de insumos y métricas en vivo.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Capa Backend &amp; Persistencia (Spring Boot REST API + Supabase PostgreSQL Realtime):</strong> Microservicios de negocio, persistencia relacional transaccional, autenticación JWT, directivas de seguridad a nivel de fila (<em>Row Level Security — RLS</em>) y bus de eventos reactivo mediante WebSockets.
    </li>
  </ul>

  <!-- 1.1.1 ALCANCE -->
  <h3 style="font-size: 11.5pt; font-weight: bold; color: #000000; margin-top: 15pt; margin-bottom: 8pt;">
    1.1.1 Alcance (Elementos Críticos Bajo Prueba)
  </h3>

  <p style="margin-bottom: 8pt;">Se especifica la cobertura formal de pruebas sobre los siguientes módulos críticos del sistema:</p>

  <table border="1" cellpadding="6" cellspacing="0" style="border-collapse: collapse; width: 100%; font-size: 9.5pt; margin-bottom: 15pt; border: 1pt solid #444444;">
    <thead>
      <tr style="background-color: #f0f0f0;">
        <th style="border: 1pt solid #444444; width: 25%; text-align: left; padding: 6pt;">Identificador / Módulo</th>
        <th style="border: 1pt solid #444444; width: 30%; text-align: left; padding: 6pt;">Tecnologías Base</th>
        <th style="border: 1pt solid #444444; width: 45%; text-align: left; padding: 6pt;">Elementos Críticos Específicos Bajo Prueba</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>MÓV-01: Autenticación y Sesión</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">.NET MAUI, Android Keystore, Preferences</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Servicio TokenStorageService, persistencia dual síncrona/asíncrona, interceptor ErrorDelegatingHandler, ciclo de vida del proceso en memoria (Swipe-Close) y purga en logout.</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>MÓV-02: Navegación y Shell</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">.NET MAUI Shell, CommunityToolkit MVVM</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Rutas relativas y absolutas en AppShell.xaml.cs, comandos rápidos (Agendar, Servicios, Solicitudes, Chat), preservación de pila de estados sin ArgumentException.</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>MÓV-03: Vistas Reactivas de Citas</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">XAML Compilado, Converters Fuertemente Tipados</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">ClientHomePage, MyAppointmentsPage, exclusión mutua entre tarjeta de cita activa y estado vacío (EmptyStateView), resolución de enlaces sin InvalidCastException.</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>WEB-01: Monitoreo y Dashboard</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Alpine.js, Tailwind CSS, Vercel Edge</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Cálculo dinámico de KPIs sin valores simulados, banner de jornada, centro reactivo de notificaciones y alertas de stock crítico.</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>WEB-02: Agenda Operativa</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Alpine.js, Supabase JS SDK REST</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Filtro inicial Citas de Hoy, visualización de historial completo (Ver Todo), selector de fecha por datepicker con query params, modal de nueva cita con folio #SHU-2026-XXXX.</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>WEB-03: Control de Inventario</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Alpine.js, PostgreSQL Functions</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Catálogo de insumos de salón, umbrales de stock mínimo/óptimo, modal de entrada de stock, actualización física y registro auditable en movimientos_inventario.</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>WEB-04: Chat &amp; Soporte Atelier</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Supabase Realtime WebSockets</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Canal de atención interactivo cliente-salón, persistencia en tabla mensajes, sincronización bidireccional instantánea.</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>INT-01: API REST &amp; Sincronización</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Spring Boot 3.x, Supabase Realtime</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Contratos JSON, latencia de propagación de eventos móvil &lt;-&gt; web inferior a 5 segundos, persistencia transaccional ACID.</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>SEC-01: Seguridad y Políticas RLS</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">PostgreSQL 15, Supabase Auth</td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Políticas Row Level Security en tabla citas y mensajes, aislamiento de datos por cliente_id = auth.uid(), mitigación de ataques BOLA y bloqueo de anon_key pública.</td>
      </tr>
    </tbody>
  </table>

  <!-- 1.1.2 FUERA DE ALCANCE -->
  <h3 style="font-size: 11.5pt; font-weight: bold; color: #000000; margin-top: 15pt; margin-bottom: 8pt;">
    1.1.2 Fuera de alcance (Exclusiones Formales)
  </h3>

  <ul style="margin-bottom: 15pt;">
    <li style="margin-bottom: 5pt; text-align: justify;">
      <strong>Pasarela de pagos en vivo con tarjeta de crédito/débito:</strong> La validación financiera se realizará mediante el registro manual de cobros en efectivo o transferencias bancarias verificadas por recepción.
    </li>
    <li style="margin-bottom: 5pt; text-align: justify;">
      <strong>Compatibilidad con sistema operativo iOS:</strong> Esta iteración se encuentra delimitada exclusivamente para el entorno Android SDK 34 compilado sobre .NET 9.
    </li>
    <li style="margin-bottom: 5pt; text-align: justify;">
      <strong>Módulos contables externos y nómina salarial:</strong> No forman parte del alcance del MVP de titulación.
    </li>
  </ul>

  <!-- 1.2 OBJETIVO DE CALIDAD -->
  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 20pt; margin-bottom: 8pt;">
    1.2 Objetivo de Calidad
  </h2>

  <p style="text-align: justify; margin-bottom: 8pt;">
    El objetivo de calidad del presente plan es establecer las condiciones de verificación que certifiquen que Shushine Studio alcanza los estándares de robustez, seguridad y rendimiento exigidos para su pase a producción. Se establecen los siguientes objetivos cuantitativos y medibles:
  </p>

  <ol style="margin-bottom: 15pt;">
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Estabilidad del Proceso Móvil:</strong> Cero fallos catastróficos (<em>crashes</em>) o cierres forzosos ante muerte de proceso por el gestor de memoria de Android (MIUI / HyperOS), logrando la recuperación de credenciales en un tiempo inferior a <strong>10 ms</strong> mediante lectura síncrona en AppShell.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Latencia Omnicanal en Vivo:</strong> Garantizar que las reservas creadas desde la app móvil se reflejen en la agenda del panel web en un tiempo inferior a <strong>5 segundos</strong> mediante canales WebSockets de Supabase sin requerir refresco manual (F5).
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Aislamiento Estricto de Datos (RLS):</strong> Cumplimiento del <strong>100%</strong> de denegación ante peticiones REST que intenten acceder o alterar citas de otros clientes portando tokens de terceros o claves anónimas públicas (<em>anon_key</em>), mitigando vulnerabilidades OWASP API1 (BOLA).
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Completitud Funcional de Casos de Prueba:</strong> Ejecución del <strong>100%</strong> de los 16 casos de prueba técnicos especificados en la Sección 2.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Cero Tolerancia a Defectos Críticos:</strong> Registrar <strong>cero (0) defectos abiertos clasificados como Severidad 1 (Bloqueante)</strong> o <strong>Severidad 2 (Crítico)</strong> en el momento de corte de la fase de pruebas.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Tasa de Éxito en Defectos Menores:</strong> Lograr una tasa de aprobación mínima del <strong>95%</strong> en defectos de severidad menor (S3 y S4), documentando los planes de mitigación para el sprint subsecuente.
    </li>
  </ol>

  <!-- 1.3 ROLES Y RESPONSABILIDADES -->
  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 20pt; margin-bottom: 8pt;">
    1.3 Roles y Responsabilidades
  </h2>

  <p style="text-align: justify; margin-bottom: 8pt;">
    En cumplimiento con la cláusula 5.3 de la norma <strong>ISO 9001:2015</strong> (Roles, responsabilidades y autoridades en la organización), el equipo de Aseguramiento de Calidad (QA) distribuyó equitativamente el diseño, especificación y supervisión técnica entre sus 4 integrantes:
  </p>

  <table border="1" cellpadding="6" cellspacing="0" style="border-collapse: collapse; width: 100%; font-size: 9.5pt; margin-bottom: 15pt; border: 1pt solid #444444;">
    <thead>
      <tr style="background-color: #f0f0f0;">
        <th style="border: 1pt solid #444444; width: 25%; text-align: left; padding: 6pt;">Integrante</th>
        <th style="border: 1pt solid #444444; width: 25%; text-align: left; padding: 6pt;">Rol Técnico Asignado</th>
        <th style="border: 1pt solid #444444; width: 35%; text-align: left; padding: 6pt;">Módulos y Secciones a su Cargo</th>
        <th style="border: 1pt solid #444444; width: 15%; text-align: center; padding: 6pt;">Casos</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Alex Fernando Alfaro Diaz</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">QA Lead &amp; Estrategia Metodológica</td>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Secciones 1, 2.3, 2.4, 2.5, 3 y Gobernanza:</strong> Marco normativo ISO 29119, pirámide de pruebas, criterios de entrada/salida (DoD), clasificación de errores (S1-S4), gestión de riesgos cuantitativos con SLAs y aprobación final.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>Gobernanza</strong></td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Cristian Jordan</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">QA Frontend Móvil &amp; Usabilidad Android</td>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Secciones 1.1.1 (MOV), 4 y 2.1 (CP-MOB):</strong> Plataforma móvil Android, herramientas ADB y Scrcpy, y diseño exhaustivo de los 6 casos de prueba del cliente móvil .NET MAUI.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>6 casos</strong><br>(CP-MOB)</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Rodrigo Cortez</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">QA Panel Web &amp; Operación de Salón</td>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Secciones 1.1.1 (WEB), 4 y 2.1 (CP-WEB):</strong> Plataforma web Chrome DevTools, auditoría de red REST, gestión operativa de agenda, inventario del salón y los 6 casos de prueba del panel administrativo.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>6 casos</strong><br>(CP-WEB)</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Cornelio Martinez</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">QA Backend, Integración &amp; Seguridad RLS</td>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Secciones 1.1.1 (INT/SEC), 4.2 y 2.1 (CP-INT/SEC):</strong> Datos de línea base, pruebas de sincronización omnicanal en tiempo real, canales WebSockets Supabase y auditoría de directivas Row Level Security (RLS).</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>4 casos</strong><br>(INT/SEC)</td>
      </tr>
    </tbody>
  </table>

  <!-- MATRIZ RACI -->
  <h4 style="font-size: 10.5pt; font-weight: bold; color: #000000; margin-top: 15pt; margin-bottom: 6pt;">
    Matriz de Asignación de Responsabilidades (RACI)
  </h4>

  <table border="1" cellpadding="6" cellspacing="0" style="border-collapse: collapse; width: 100%; font-size: 9pt; margin-bottom: 15pt; border: 1pt solid #444444;">
    <thead>
      <tr style="background-color: #f0f0f0;">
        <th style="border: 1pt solid #444444; width: 35%; text-align: left; padding: 5pt;">Rol / Integrante</th>
        <th style="border: 1pt solid #444444; width: 11%; text-align: center; padding: 5pt;">Planif.</th>
        <th style="border: 1pt solid #444444; width: 11%; text-align: center; padding: 5pt;">Diseño Casos</th>
        <th style="border: 1pt solid #444444; width: 11%; text-align: center; padding: 5pt;">Entorno</th>
        <th style="border: 1pt solid #444444; width: 11%; text-align: center; padding: 5pt;">Corrección</th>
        <th style="border: 1pt solid #444444; width: 11%; text-align: center; padding: 5pt;">Auditoría RLS</th>
        <th style="border: 1pt solid #444444; width: 10%; text-align: center; padding: 5pt;">Sign-off</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Alex Fernando Alfaro Diaz</strong> (QA Lead)</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">A / R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">A</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;">C</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">A / R</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Cristian Jordan</strong> (QA Frontend Móvil)</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;">C</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;">I</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Rodrigo Cortez</strong> (QA Panel Web)</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;">C</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;">I</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>Cornelio Martinez</strong> (QA Backend &amp; RLS)</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;">C</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center; font-weight: bold;">R</td>
      </tr>
    </tbody>
  </table>

  <p style="font-size: 8.5pt; color: #555555; margin-bottom: 20pt;">
    <em>Referencias: <strong>R</strong> = Responsable de ejecución/diseño (Responsible), <strong>A</strong> = Aprobador final (Accountable), <strong>C</strong> = Consultado técnico (Consulted), <strong>I</strong> = Informado (Informed).</em>
  </p>

  <hr style="border: 0; border-top: 1.5pt solid #000000; margin: 25pt 0;">

  <!-- SECCIÓN 2: METODOLOGÍA -->
  <h1 style="font-size: 15pt; font-weight: bold; color: #000000; margin-top: 25pt; margin-bottom: 10pt; text-transform: uppercase;">
    2. METODOLOGÍA DE PRUEBA
  </h1>

  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 15pt; margin-bottom: 8pt;">
    2.1 Compendio
  </h2>

  <p style="text-align: justify; margin-bottom: 10pt;">
    La estrategia de pruebas adopta el modelo de la <strong>Pirámide de Pruebas de Mike Cohn</strong> en articulación con el estándar internacional <strong>ISO/IEC/IEEE 29119-3</strong>, distribuyendo el esfuerzo en tres niveles complementarios:
  </p>

  <ul style="margin-bottom: 15pt;">
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Nivel 3 — Aceptación y Sistema E2E (Caja Negra):</strong> Flujos completos de negocio desde la experiencia del cliente en la aplicación móvil Android (.NET MAUI), flujos operativos en el panel administrativo web (Alpine.js) y sincronización omnicanal en tiempo real.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Nivel 2 — Integración y Contratos de Servicio (Caja Gris):</strong> Verificación de contratos JSON en endpoints REST Spring Boot mediante Postman/Newman, integridad transaccional ACID, directivas Row Level Security (RLS) y canales reactivos WebSockets de Supabase Realtime.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Nivel 1 — Pruebas Unitarias y Componentes (Caja Blanca):</strong> Lógica de clases y servicios en C# (.NET MAUI) con xUnit/Moq, controladores de estado reactivos en Alpine.js y converters fuertemente tipados.
    </li>
  </ul>

  <!-- 2.3 CLASIFICACIÓN DE ERRORES -->
  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 20pt; margin-bottom: 8pt;">
    2.3 Clasificación de errores
  </h2>

  <p style="text-align: justify; margin-bottom: 8pt;">
    Todo desvío detectado durante la ejecución futura del plan se registrará formalmente en el sistema de incidentes de Azure DevOps bajo la siguiente escala técnica de severidad:
  </p>

  <table border="1" cellpadding="6" cellspacing="0" style="border-collapse: collapse; width: 100%; font-size: 9.5pt; margin-bottom: 15pt; border: 1pt solid #444444;">
    <thead>
      <tr style="background-color: #f0f0f0;">
        <th style="border: 1pt solid #444444; width: 22%; text-align: left; padding: 6pt;">Severidad</th>
        <th style="border: 1pt solid #444444; width: 33%; text-align: left; padding: 6pt;">Definición Técnica</th>
        <th style="border: 1pt solid #444444; width: 15%; text-align: center; padding: 6pt;">SLA Resolución</th>
        <th style="border: 1pt solid #444444; width: 30%; text-align: left; padding: 6pt;">Ejemplo Práctico en Shushine Studio</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>S1: Bloqueante (Blocker)</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">El sistema o módulo queda completamente inoperativo; crash de la aplicación móvil, bloqueo total de base de datos o fallo insubsanable al iniciar sesión.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>Inmediato<br>(&lt; 4 horas)</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Cierre forzoso de la aplicación Android al pulsar una tarjeta de cita o fallo catastrófico en el arranque (AppShell).</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>S2: Crítico (Critical)</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Pérdida de funcionalidad principal del negocio sin procedimiento alternativo viable; fallo en la inserción transaccional de reservas o corrupción en inventario.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>&lt; 24 horas</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">El botón de guardar cita en el modal web no inserta el registro en Supabase.</td>
      </tr>
      <tr>
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>S3: Mayor (Major)</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Funcionalidad relevante afectada pero existe un procedimiento alternativo o manual para completar la operación.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>&lt; 48 horas</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">El filtro por fecha del datepicker requiere presionar dos veces para refrescar la tabla de citas.</td>
      </tr>
      <tr style="background-color: #fafafa;">
        <td style="border: 1pt solid #444444; padding: 5pt;"><strong>S4: Menor (Cosmetic)</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Detalle cosmético, error tipográfico, espaciado visual o badge desalineado que no afecta la lógica ni la integridad operativa.</td>
        <td style="border: 1pt solid #444444; padding: 5pt; text-align: center;"><strong>Próximo Sprint</strong></td>
        <td style="border: 1pt solid #444444; padding: 5pt;">Desalineación de un icono en la barra de navegación lateral del panel administrativo web.</td>
      </tr>
    </tbody>
  </table>

  <!-- 2.4 CRITERIOS DE SUSPENSIÓN Y REANUDACIÓN -->
  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 20pt; margin-bottom: 8pt;">
    2.4 Criterios de suspensión y requisitos de reanudación
  </h2>

  <p style="margin-bottom: 6pt;"><strong>Criterios de Suspensión:</strong></p>
  <p style="text-align: justify; margin-bottom: 6pt;">La ejecución de pruebas se suspenderá formalmente si se presenta cualquiera de las siguientes condiciones bloqueantes:</p>
  <ol style="margin-bottom: 12pt;">
    <li style="margin-bottom: 4pt; text-align: justify;">Detección de un defecto de <strong>Severidad 1 (Bloqueante)</strong> que imposibilite la navegación básica o genere un cierre forzoso de la aplicación (<em>Crash on Launch</em>).</li>
    <li style="margin-bottom: 4pt; text-align: justify;">Indisponibilidad no planificada del servicio de base de datos Supabase o caída total de la API REST backend.</li>
    <li style="margin-bottom: 4pt; text-align: justify;">Corrupción masiva en los datos de prueba de línea base que invalide la reproducibilidad de los casos.</li>
  </ol>

  <p style="margin-bottom: 6pt;"><strong>Requisitos de Reanudación:</strong></p>
  <p style="text-align: justify; margin-bottom: 6pt;">Las actividades de prueba se reanudarán una vez satisfechos los siguientes requisitos:</p>
  <ol style="margin-bottom: 15pt;">
    <li style="margin-bottom: 4pt; text-align: justify;">Despliegue de un parche correctivo (<em>Hotfix</em>) por parte del equipo de desarrollo en el repositorio central.</li>
    <li style="margin-bottom: 4pt; text-align: justify;">Ejecución y aprobación de pruebas unitarias de no-regresión sobre el componente subsanado.</li>
    <li style="margin-bottom: 4pt; text-align: justify;">Confirmación de operatividad de los servicios de backend y restauración de los datos de línea base.</li>
  </ol>

  <!-- 2.5 COMPLETITUD DE LA PRUEBA -->
  <h2 style="font-size: 13pt; font-weight: bold; color: #000000; margin-top: 20pt; margin-bottom: 8pt;">
    2.5 Completitud de la prueba (Definition of Done)
  </h2>

  <p style="text-align: justify; margin-bottom: 8pt;">
    Para certificar el cierre formal de la fase de pruebas y emitir la recomendación de pase a producción (<em>Definition of Done — DoD</em>), se debe satisfacer la totalidad de los siguientes criterios:
  </p>

  <ol style="margin-bottom: 15pt;">
    <li style="margin-bottom: 5pt; text-align: justify;"><strong>100% de los casos de prueba planificados ejecutados</strong> y debidamente evidenciados en el tablero de control de Azure Boards.</li>
    <li style="margin-bottom: 5pt; text-align: justify;"><strong>Cero (0) defectos abiertos de Severidad 1 (Bloqueante) y Severidad 2 (Crítico)</strong>.</li>
    <li style="margin-bottom: 5pt; text-align: justify;">Tasa de aprobación mínima del <strong>95%</strong> en casos de prueba de Severidad 3 y 4, con los defectos residuales documentados y calendarizados para el siguiente sprint de mantenimiento.</li>
    <li style="margin-bottom: 5pt; text-align: justify;">Confirmación de integridad referencial en PostgreSQL sin registros huérfanos en las tablas <code>citas</code>, <code>mensajes</code> ni <code>movimientos_inventario</code>.</li>
    <li style="margin-bottom: 5pt; text-align: justify;">Emisión y firma formal del Informe de Resumen de Pruebas (<em>Test Summary Report</em>) por el QA Lead.</li>
  </ol>

  <hr style="border: 0; border-top: 1.5pt solid #000000; margin: 25pt 0;">

  <!-- SECCIÓN 3: ENTREGABLES -->
  <h1 style="font-size: 15pt; font-weight: bold; color: #000000; margin-top: 25pt; margin-bottom: 10pt; text-transform: uppercase;">
    3. ENTREGABLES DE LA PRUEBA
  </h1>

  <p style="text-align: justify; margin-bottom: 8pt;">
    Al concluir el ciclo de aseguramiento de calidad, el equipo de pruebas consolidará y entregará los siguientes artefactos formales:
  </p>

  <ol style="margin-bottom: 25pt;">
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Plan Maestro de Pruebas de Software (STP-SHU-2026-V3):</strong> El presente documento formal de planificación, diseño y especificación metodológica.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Backlog de Pruebas en Azure DevOps Boards:</strong> Registro de trazabilidad vinculado a la Épica maestra <code>#162</code> en la organización <code>lalafaro6/Testeo</code>, conteniendo las 5 Features, 19 PBIs y 35 Tasks de testing asignadas.
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Registro de Incidentes y Defectos (Bug Reports):</strong> Expedientes técnicos con capturas de pantalla, pasos detallados de reproducción y trazas del sistema (<code>adb logcat</code> y consola DevTools).
    </li>
    <li style="margin-bottom: 6pt; text-align: justify;">
      <strong>Informe Formal de Cierre y Resumen de Pruebas (Test Summary Report):</strong> Documento final que certifica las métricas de ejecución, el porcentaje de casos superados y el dictamen técnico para la liberación a producción.
    </li>
  </ol>

</div>
`;

async function updateGoogleDoc() {
    console.log("Iniciando navegador Chromium...");
    const browser = await pw.chromium.launch({ headless: true });
    const context = await browser.newContext({
        permissions: ['clipboard-read', 'clipboard-write']
    });
    const page = await context.newPage();

    console.log("Navegando a Google Docs...");
    await page.goto("https://docs.google.com/document/d/1cBT0GtPDxXR3qKdte1ZqrSnjaao0rSgnPrwycchdwaU/edit?usp=sharing", {
        waitUntil: "domcontentloaded",
        timeout: 45000
    });

    console.log("Esperando inicialización del lienzo de Google Docs...");
    await page.waitForTimeout(6000);

    // Obtener iframe del editor
    const iframeEl = await page.$(".docs-texteventtarget-iframe");
    if (!iframeEl) {
        throw new Error("No se encontró el iframe del editor en Google Docs");
    }
    const frame = await iframeEl.contentFrame();
    const textbox = await frame.$("[role=textbox]");
    if (!textbox) {
        throw new Error("No se encontró el textbox interactivo de Google Docs");
    }

    console.log("Enfocando textbox y seleccionando todo (Ctrl+A)...");
    await textbox.focus();
    await page.waitForTimeout(500);

    // Seleccionar todo el documento para reemplazar el markdown desordenado
    await page.keyboard.press("Control+a");
    await page.waitForTimeout(500);
    await page.keyboard.press("Backspace");
    await page.waitForTimeout(1000);

    console.log("Escribiendo contenido enriquecido (HTML + Tablas) en el portapapeles...");
    await page.evaluate(async (html) => {
        const text = "Plan Maestro de Pruebas de Software - Shushine Studio";
        await navigator.clipboard.write([
            new ClipboardItem({
                "text/plain": new Blob([text], { type: "text/plain" }),
                "text/html": new Blob([html], { type: "text/html" })
            })
        ]);
    }, htmlContent);

    console.log("Pegando contenido formateado con Control+v...");
    await page.keyboard.press("Control+v");
    
    // Esperar a que Google Docs procese la estructura de tablas y guarde en Drive
    console.log("Esperando que Google Docs guarde los cambios en Drive...");
    await page.waitForTimeout(7000);

    const screenshotPath = "/tmp/gdoc_formatted_result.png";
    await page.screenshot({ path: screenshotPath });
    console.log("Captura de pantalla guardada en:", screenshotPath);

    await browser.close();
    console.log("Proceso completado con éxito.");
}

updateGoogleDoc().catch(err => {
    console.error("Error al actualizar Google Docs:", err);
    process.exit(1);
});
