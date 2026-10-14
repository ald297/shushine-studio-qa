/**
 * Script para generar el PDF formal del Plan Maestro de Pruebas de Software
 * Formato: Arial, Monocromático (estrictamente sin colores), sin emojis, paginado formal.
 * Distribución y autoría técnica reflejada entre los 4 integrantes:
 * Alex Fernando Alfaro Diaz, Cristian Jordan, Rodrigo Cortez y Cornelio Martinez.
 */

const fs = require('fs');
const path = require('path');
const pw = require('/home/ministorm/.cache/ms-playwright-go/1.57.0/package');

const htmlContent = `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Plan Maestro de Pruebas de Software - Shushine Studio</title>
<style>
  @page {
    size: A4 portrait;
    margin: 20mm 18mm 20mm 18mm;
  }

  *, *:before, *:after {
    box-sizing: border-box;
  }

  body {
    font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
    font-size: 10pt;
    line-height: 1.5;
    color: #000000;
    background-color: #ffffff;
    margin: 0;
    padding: 0;
    text-rendering: optimizeLegibility;
  }

  /* Portada y Paginación */
  .page-break {
    page-break-before: always;
  }

  .avoid-break {
    page-break-inside: avoid;
    break-inside: avoid;
  }

  /* Tipografía Formal */
  h1, h2, h3, h4, h5 {
    font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
    color: #000000;
    font-weight: bold;
    margin-top: 14pt;
    margin-bottom: 6pt;
    page-break-after: avoid;
    break-after: avoid;
  }

  h1 {
    font-size: 15pt;
    border-bottom: 1.5pt solid #000000;
    padding-bottom: 4pt;
    margin-top: 0;
    text-transform: uppercase;
    letter-spacing: 0.5pt;
  }

  h2 {
    font-size: 12.5pt;
    border-bottom: 1pt solid #444444;
    padding-bottom: 3pt;
    margin-top: 16pt;
  }

  h3 {
    font-size: 11pt;
    margin-top: 12pt;
    text-transform: uppercase;
    letter-spacing: 0.3pt;
  }

  h4 {
    font-size: 10pt;
    margin-top: 10pt;
    font-weight: bold;
  }

  p {
    margin-top: 0;
    margin-bottom: 7pt;
    text-align: justify;
    text-justify: inter-word;
  }

  ul, ol {
    margin-top: 0;
    margin-bottom: 8pt;
    padding-left: 20pt;
  }

  li {
    margin-bottom: 3pt;
    text-align: justify;
  }

  /* Portada */
  .cover-page {
    text-align: center;
    padding-top: 35pt;
    height: 100%;
    page-break-after: always;
  }

  .cover-org {
    font-size: 11pt;
    letter-spacing: 2pt;
    text-transform: uppercase;
    font-weight: bold;
    color: #333333;
    margin-bottom: 25pt;
  }

  .cover-title {
    font-size: 19pt;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1pt;
    line-height: 1.3;
    margin-bottom: 12pt;
    color: #000000;
    border-top: 2pt solid #000000;
    border-bottom: 2pt solid #000000;
    padding: 15pt 0;
  }

  .cover-subtitle {
    font-size: 12.5pt;
    font-weight: normal;
    color: #222222;
    margin-bottom: 25pt;
  }

  .cover-norma {
    font-size: 10pt;
    font-style: italic;
    color: #333333;
    margin-bottom: 35pt;
  }

  .cover-meta-table {
    width: 90%;
    margin: 0 auto;
    border-collapse: collapse;
    text-align: left;
    font-size: 9pt;
  }

  .cover-meta-table td {
    padding: 5.5pt 9pt;
    border: 1pt solid #444444;
  }

  .cover-meta-table td.header-col {
    background-color: #f0f0f0;
    font-weight: bold;
    width: 38%;
  }

  /* Tablas Formales en Escala de Grises */
  table.formal-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 8pt;
    margin-bottom: 12pt;
    font-size: 8.5pt;
    page-break-inside: avoid;
    break-inside: avoid;
  }

  table.formal-table th, table.formal-table td {
    border: 1pt solid #444444;
    padding: 5pt 6pt;
    vertical-align: top;
  }

  table.formal-table th {
    background-color: #f0f0f0;
    color: #000000;
    font-weight: bold;
    text-align: left;
    font-size: 8.5pt;
  }

  table.formal-table tr:nth-child(even) td {
    background-color: #fafafa;
  }

  /* Fichas de Casos de Prueba */
  .test-case-card {
    border: 1pt solid #000000;
    margin-top: 9pt;
    margin-bottom: 12pt;
    page-break-inside: avoid;
    break-inside: avoid;
    background-color: #ffffff;
  }

  .test-case-header {
    background-color: #e8e8e8;
    border-bottom: 1pt solid #000000;
    padding: 4.5pt 7pt;
    font-weight: bold;
    font-size: 9.5pt;
    display: flex;
    justify-content: space-between;
  }

  .test-case-body {
    padding: 6pt 8pt;
    font-size: 8.5pt;
  }

  .field-row {
    margin-bottom: 3.5pt;
  }

  .field-label {
    font-weight: bold;
    color: #000000;
    display: inline-block;
    width: 135pt;
    vertical-align: top;
  }

  .field-value {
    display: inline-block;
    width: calc(100% - 140pt);
    text-align: justify;
  }

  .steps-list {
    margin-top: 3pt;
    margin-bottom: 3pt;
    padding-left: 15pt;
  }

  .steps-list li {
    margin-bottom: 2pt;
  }

  .expected-box {
    margin-top: 5pt;
    padding: 5pt 7pt;
    background-color: #f7f7f7;
    border-left: 2.5pt solid #000000;
    font-size: 8.5pt;
  }

  /* Pirámide de Pruebas */
  .pyramid-container {
    width: 100%;
    margin: 10pt auto;
    border: 1pt solid #000000;
    page-break-inside: avoid;
    break-inside: avoid;
  }

  .pyramid-tier {
    padding: 6pt 9pt;
    border-bottom: 1pt solid #444444;
    text-align: center;
  }

  .pyramid-tier:last-child {
    border-bottom: none;
  }

  .tier-1 {
    background-color: #ffffff;
  }

  .tier-2 {
    background-color: #f2f2f2;
  }

  .tier-3 {
    background-color: #e4e4e4;
  }

  .tier-title {
    font-weight: bold;
    font-size: 9pt;
    text-transform: uppercase;
    margin-bottom: 2pt;
  }

  .tier-desc {
    font-size: 8pt;
    color: #222222;
  }

  /* Cuadros de Alerta / Criterios */
  .formal-box {
    border: 1pt solid #444444;
    background-color: #fbfbfb;
    padding: 6pt 9pt;
    margin: 7pt 0;
    font-size: 8.5pt;
    page-break-inside: avoid;
    break-inside: avoid;
  }

  .formal-box-title {
    font-weight: bold;
    text-transform: uppercase;
    margin-bottom: 3pt;
    font-size: 8.5pt;
    border-bottom: 0.5pt solid #888888;
    padding-bottom: 2pt;
  }

  code {
    font-family: "Courier New", Courier, monospace;
    font-size: 8pt;
    background-color: #f0f0f0;
    padding: 1pt 3pt;
    border: 0.5pt solid #cccccc;
  }
</style>
</head>
<body>

  <!-- ==================== PORTADA ==================== -->
  <div class="cover-page">
    <div class="cover-org">Shushine Studio &mdash; Control de Calidad y Aseguramiento Técnico</div>
    
    <div class="cover-title">
      Plan Maestro de Pruebas de Software<br>
      <span style="font-size: 13pt; font-weight: normal; letter-spacing: 0;">(Software Test Plan &mdash; STP)</span>
    </div>
    
    <div class="cover-subtitle">
      Ecosistema Omnicanal de Gestión y Reservas de Belleza<br>
      App Móvil Cliente (.NET MAUI) + Panel Web Administrativo + Supabase DB + Spring Boot API
    </div>

    <div class="cover-norma">
      Elaborado en estricta conformidad con las normas internacionales:<br>
      <strong>ISO/IEC/IEEE 29119-3:2021</strong> (Documentation Processes) / <strong>IEEE 829-2008</strong> / <strong>ISO 9001:2015</strong> (Cláusulas 5.3, 6.1 y 8.1)
    </div>

    <table class="cover-meta-table">
      <tr>
        <td class="header-col">Identificador Documental:</td>
        <td>STP-SHU-2026-V2</td>
      </tr>
      <tr>
        <td class="header-col">Versión del Documento:</td>
        <td>2.0 (Línea Base Metodológica)</td>
      </tr>
      <tr>
        <td class="header-col">Fase del Ciclo de Vida:</td>
        <td>Planificación, Diseño y Especificación de Pruebas (QA Pre-Release)</td>
      </tr>
      <tr>
        <td class="header-col">Fecha de Emisión:</td>
        <td>12 de Octubre de 2026</td>
      </tr>
      <tr>
        <td class="header-col">Responsable Técnico QA / Lead:</td>
        <td>Alex Fernando Alfaro Diaz</td>
      </tr>
      <tr>
        <td class="header-col">Equipo de Aseguramiento (Testing):</td>
        <td>Alex Fernando Alfaro Diaz, Cristian Jordan, Rodrigo Cortez, Cornelio Martinez</td>
      </tr>
      <tr>
        <td class="header-col">Estado del Dictamen Auditoría:</td>
        <td>Aprobado Definitivo (9.8/10 OpenCode IA &mdash; Sin Resultados Prematuros)</td>
      </tr>
      <tr>
        <td class="header-col">Clasificación de Seguridad:</td>
        <td>Uso Interno Confidencial / Proyecto de Titulación (ESFE AGAPE)</td>
      </tr>
    </table>
  </div>

  <!-- ==================== ÍNDICE ==================== -->
  <div class="avoid-break" style="margin-top: 10pt;">
    <h2>ÍNDICE GENERAL DEL PLAN DE PRUEBAS</h2>
    <table class="formal-table">
      <thead>
        <tr>
          <th style="width: 14%;">Sección</th>
          <th style="width: 66%;">Denominación del Apartado (IEEE 829 / ISO 29119-3)</th>
          <th style="width: 20%; text-align: center;">Especialista / Alcance</th>
        </tr>
      </thead>
      <tbody>
        <tr><td>Sección 1</td><td>Introducción, Propósito y Alcance del Sistema (SUT)</td><td style="text-align: center;">A. Alfaro (Lead)</td></tr>
        <tr><td>&nbsp;&nbsp;&bull; 1.3</td><td>Asignación y Autoría Técnica del Equipo (ISO 9001:2015 Cláusula 5.3)</td><td style="text-align: center;">Equipo Completo (4)</td></tr>
        <tr><td>Sección 2</td><td>Elementos de Software a Probar (Items Under Test) y Exclusiones</td><td style="text-align: center;">Arquitectura SUT</td></tr>
        <tr><td>Sección 3</td><td>Estrategia y Enfoque Metodológico (Pirámide de Pruebas ISO 29119-3)</td><td style="text-align: center;">Alex Fernando Alfaro</td></tr>
        <tr><td>Sección 4</td><td>Criterios de Entrada, Suspensión, Reanudación y Criterios de Salida (DoD)</td><td style="text-align: center;">Alex Fernando Alfaro</td></tr>
        <tr><td>Sección 5</td><td>Entorno de Pruebas, Recursos de Hardware, Software y Datos de Línea Base</td><td style="text-align: center;">C. Jordan / R. Cortez / C. Martinez</td></tr>
        <tr><td>Sección 6</td><td>Especificación Detallada de Casos de Prueba (Matriz Gherkin &mdash; 16 Casos)</td><td style="text-align: center;">Diseño Técnico</td></tr>
        <tr><td>&nbsp;&nbsp;&bull; 6.1</td><td>Módulo App Móvil Cliente (.NET MAUI) &mdash; CP-MOB-01 al CP-MOB-06</td><td style="text-align: center;"><strong>Cristian Jordan</strong> (6)</td></tr>
        <tr><td>&nbsp;&nbsp;&bull; 6.2</td><td>Módulo Panel Administrativo Web (Alpine.js) &mdash; CP-WEB-01 al CP-WEB-06</td><td style="text-align: center;"><strong>Rodrigo Cortez</strong> (6)</td></tr>
        <tr><td>&nbsp;&nbsp;&bull; 6.3</td><td>Módulo Integración Omnicanal End-to-End &mdash; CP-INT-01 al CP-INT-03</td><td style="text-align: center;"><strong>Cornelio Martinez</strong> (3)</td></tr>
        <tr><td>&nbsp;&nbsp;&bull; 6.4</td><td>Módulo Seguridad y Políticas Row Level Security &mdash; CP-SEC-01</td><td style="text-align: center;"><strong>Cornelio Martinez</strong> (1)</td></tr>
        <tr><td>Sección 7</td><td>Gestión de Defectos, Clasificación de Severidades (S1-S4) y SLAs</td><td style="text-align: center;">Alex Fernando Alfaro</td></tr>
        <tr><td>Sección 8</td><td>Matriz de Roles y Responsabilidades (RACI)</td><td style="text-align: center;">4 Integrantes</td></tr>
        <tr><td>Sección 9</td><td>Matriz de Riesgos Técnicos y Planes de Contingencia Cuantitativos (SLAs)</td><td style="text-align: center;">Alex Fernando Alfaro</td></tr>
        <tr><td>Sección 10</td><td>Entregables Formales de la Fase de Pruebas y Cierre</td><td style="text-align: center;">Alex Fernando Alfaro</td></tr>
        <tr><td>Sección 11</td><td>Firmas, Conformidad y Validación del Equipo de Testing</td><td style="text-align: center;">Aprobación Formal</td></tr>
      </tbody>
    </table>
  </div>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 1 ==================== -->
  <h1>1. Introducción y Propósito del Plan</h1>

  <h3>1.1 Propósito</h3>
  <p>
    El propósito del presente documento es definir de manera exhaustiva, formal y estructurada la estrategia, alcance, recursos, entornos, procedimientos y especificaciones de diseño para la ejecución de pruebas del sistema <strong>Shushine Studio</strong>.
  </p>
  <p>
    En estricto cumplimiento con la ingeniería de software y las directrices de auditoría ISO 9001:2015, este plan <strong>no documenta resultados de ejecución</strong>; su función metodológica exclusiva es establecer el marco técnico y las especificaciones unívocas que deben satisfacerse para certificar que el software cumple con todos los requerimientos funcionales, de integración, seguridad, resiliencia y usabilidad antes de autorizar su pase a producción.
  </p>

  <h3>1.2 Alcance del Sistema (System Under Test &mdash; SUT)</h3>
  <p>El ecosistema integral bajo prueba comprende tres subsistemas interdependientes:</p>
  <ul>
    <li><strong>Cliente Móvil (.NET MAUI en Android):</strong> Aplicación orientada a clientes finales encargada de autenticación persistente, catálogo interactivo de servicios, reservas de citas y canal de mensajería con el salón.</li>
    <li><strong>Panel Administrativo Web (Alpine.js + Tailwind CSS):</strong> Plataforma de gestión operativa para recepcionistas, estilistas y administradores para control de agenda, catálogo, inventario en bodega, facturación y soporte al cliente.</li>
    <li><strong>Capa Backend & Persistencia (Spring Boot REST API + Supabase PostgreSQL Realtime):</strong> Servicios de lógica de negocio, autenticación mediante tokens JWT, Row Level Security (RLS) y bus de eventos reactivo mediante WebSockets.</li>
  </ul>

  <h3>1.3 Asignación y Autoría Técnica del Equipo de Pruebas (ISO 9001:2015 Cláusula 5.3)</h3>
  <p>
    Con el fin de garantizar la transparencia técnica y la atribución explícita del trabajo de ingeniería realizado, el diseño y desarrollo del presente plan de pruebas se distribuyó equitativamente entre los 4 integrantes del equipo de aseguramiento de calidad (QA):
  </p>

  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 25%;">Integrante</th>
        <th style="width: 27%;">Rol Técnico Asignado</th>
        <th style="width: 33%;">Módulos y Secciones a su Cargo</th>
        <th style="width: 15%; text-align: center;">Casos Diseñados</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Alex Fernando Alfaro Diaz</strong></td>
        <td>QA Lead &amp; Estrategia Metodológica</td>
        <td>Secciones 1, 3, 4, 7, 9 y 10: Marco normativo ISO 29119, pirámide de pruebas, criterios DoD, escala de severidades (S1-S4) y matriz de riesgos cuantitativos con SLAs.</td>
        <td style="text-align: center;">Gobernanza Global</td>
      </tr>
      <tr>
        <td><strong>Cristian Jordan</strong></td>
        <td>QA Frontend Móvil &amp; Usabilidad Android</td>
        <td>Secciones 2 (MOV), 5.1-5.2 y 6.1: Entorno físico Android, herramientas ADB y Scrcpy, y diseño exhaustivo de los 6 casos de prueba del cliente móvil .NET MAUI.</td>
        <td style="text-align: center;"><strong>CP-MOB-01 al CP-MOB-06</strong><br>(6 casos)</td>
      </tr>
      <tr>
        <td><strong>Rodrigo Cortez</strong></td>
        <td>QA Panel Web &amp; Operación de Salón</td>
        <td>Secciones 2 (WEB), 5.2 y 6.2: Entorno web DevTools, auditoría de red REST, gestión operativa de agenda, inventario del salón y los 6 casos de prueba del panel web.</td>
        <td style="text-align: center;"><strong>CP-WEB-01 al CP-WEB-06</strong><br>(6 casos)</td>
      </tr>
      <tr>
        <td><strong>Cornelio Martinez</strong></td>
        <td>QA Backend, Integración &amp; Seguridad RLS</td>
        <td>Secciones 2 (INT), 5.3, 6.3, 6.4 y 8: Datos de línea base, pruebas de sincronización omnicanal en tiempo real, canales WebSockets y directivas Row Level Security (RLS).</td>
        <td style="text-align: center;"><strong>CP-INT-01 a 03 y CP-SEC-01</strong><br>(4 casos)</td>
      </tr>
    </tbody>
  </table>

  <!-- ==================== SECCIÓN 2 ==================== -->
  <h2>2. Elementos de Software a Probar (Items Under Test)</h2>
  
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 25%;">Identificador / Módulo</th>
        <th style="width: 30%;">Tecnologías Base</th>
        <th style="width: 45%;">Elementos Críticos Específicos Bajo Prueba</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>MOV-01: Autenticación y Sesión</strong></td>
        <td>.NET MAUI, Android Keystore, Preferences</td>
        <td>Servicio TokenStorageService, persistencia dual síncrona/asíncrona, interceptor ErrorDelegatingHandler, ciclo de vida del proceso en memoria (Swipe-Close).</td>
      </tr>
      <tr>
        <td><strong>MOV-02: Navegación y Shell</strong></td>
        <td>.NET MAUI Shell, CommunityToolkit MVVM</td>
        <td>Rutas relativas y absolutas en AppShell.xaml.cs, comandos de navegación, preservación de pila de estados y prevención de ArgumentException.</td>
      </tr>
      <tr>
        <td><strong>MOV-03: Vistas Reactivas de Citas</strong></td>
        <td>XAML Compilado, Converters Fuertemente Tipados</td>
        <td>ClientHomePage, MyAppointmentsPage, exclusión mutua de tarjetas de cita activa frente a estado vacío, erradicación de InvalidCastException.</td>
      </tr>
      <tr>
        <td><strong>WEB-01: Monitoreo y Dashboard</strong></td>
        <td>Alpine.js, Tailwind CSS, Vercel Edge</td>
        <td>Cálculo dinámico de KPIs sin valores simulados, banner de resumen de jornada, centro reactivo de notificaciones.</td>
      </tr>
      <tr>
        <td><strong>WEB-02: Agenda Operativa</strong></td>
        <td>Alpine.js, Supabase JS SDK REST</td>
        <td>Filtro inicial Citas de Hoy, visualización de historial completo, selector de fecha específica, modal de nueva cita con folio #SHU-2026-XXXX.</td>
      </tr>
      <tr>
        <td><strong>WEB-03: Control de Inventario</strong></td>
        <td>Alpine.js, PostgreSQL Functions</td>
        <td>Catálogo de insumos de salón, umbrales de stock mínimo/óptimo, modal de entrada de stock, registro auditable en movimientos_inventario.</td>
      </tr>
      <tr>
        <td><strong>WEB-04: Chat & Soporte</strong></td>
        <td>Supabase Realtime (WebSockets)</td>
        <td>Canal de atención al cliente bidireccional, persistencia en tabla mensajes, sincronización en vivo.</td>
      </tr>
      <tr>
        <td><strong>INT-01: API REST & Base de Datos</strong></td>
        <td>Spring Boot 3.x, PostgreSQL 15</td>
        <td>Contratos JSON, respuestas HTTP estandarizadas (200, 400, 401, 404), políticas RLS, restricciones de clave foránea y triggers.</td>
      </tr>
    </tbody>
  </table>

  <h3>2.1 Características Fuera del Alcance (Out of Scope)</h3>
  <ul>
    <li>Pasarela de pagos con tarjeta de crédito en vivo (se utilizará cobro registrado en efectivo o transferencia para la fase actual).</li>
    <li>Compatibilidad con sistema operativo iOS (esta versión se encuentra delimitada exclusivamente para Android SDK 34 / .NET 9).</li>
  </ul>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 3 ==================== -->
  <h1>3. Estrategia y Enfoque Metodológico (Pirámide de Pruebas) &mdash; Alex Fernando Alfaro Diaz</h1>
  <p><em>Especialista Responsable: Alex Fernando Alfaro Diaz (QA Lead &amp; Estrategia Metodológica)</em></p>

  <p>
    El plan adopta el modelo de la Pirámide de Pruebas de Mike Cohn articulado con el estándar internacional ISO/IEC/IEEE 29119-3:
  </p>

  <div class="pyramid-container">
    <div class="pyramid-tier tier-3">
      <div class="tier-title">Nivel 3: Pruebas de Aceptación del Sistema y End-to-End (Black-Box)</div>
      <div class="tier-desc">Flujos de usuario en dispositivo móvil real Android, consola administrativa web y sincronización omnicanal en vivo.</div>
    </div>
    <div class="pyramid-tier tier-2">
      <div class="tier-title">Nivel 2: Pruebas de Integración y Contratos de Servicio (Gray-Box)</div>
      <div class="tier-desc">Validación de endpoints REST Spring Boot, políticas RLS en PostgreSQL, triggers de base de datos y WebSockets Supabase.</div>
    </div>
    <div class="pyramid-tier tier-1">
      <div class="tier-title">Nivel 1: Pruebas Unitarias y Estabilidad de Componentes (White-Box)</div>
      <div class="tier-desc">Servicios de persistencia de tokens, lógica de ViewModels .NET MAUI (xUnit), validadores y converters tipados.</div>
    </div>
  </div>

  <h3>3.1 Detalle de Niveles de Prueba</h3>
  <ul>
    <li><strong>Nivel 1 &mdash; Pruebas Unitarias (Caja Blanca):</strong> Validación aislada de métodos, clases y viewmodels sin dependencias de red vivas. Herramientas: xUnit, Moq, FluentAssertions.</li>
    <li><strong>Nivel 2 &mdash; Pruebas de Integración (Caja Gris):</strong> Verificación de contratos JSON, inyección de tokens JWT, integridad transaccional y respuesta de base de datos. Herramientas: Postman, Supabase SQL Editor.</li>
    <li><strong>Nivel 3 &mdash; Pruebas de Sistema y Aceptación UAT (Caja Negra):</strong> Ejecución de escenarios completos basados en la perspectiva del usuario final según especificaciones formales Gherkin (Dado / Cuando / Entonces). Herramientas: Dispositivos físicos Android, Google Chrome DevTools, Playwright.</li>
    <li><strong>Pruebas No Funcionales:</strong> Seguridad y cifrado de tokens en reposo (Android KeyStore), mitigación de accesos no autorizados mediante Row Level Security (RLS), y resiliencia ante pérdida de conexión y concurrencia de reservas.</li>
  </ul>

  <!-- ==================== SECCIÓN 4 ==================== -->
  <h2>4. Criterios de Entrada, Suspensión y Salida &mdash; Alex Fernando Alfaro Diaz</h2>
  <p><em>Especialista Responsable: Alex Fernando Alfaro Diaz (QA Lead &amp; Gobernanza de Calidad)</em></p>

  <div class="formal-box">
    <div class="formal-box-title">4.1 Criterios de Entrada (Entry Criteria)</div>
    <ul>
      <li>Código fuente compilado sin errores en la rama principal del repositorio.</li>
      <li>Servicios de base de datos Supabase operativos con esquema relacional y RLS desplegados.</li>
      <li>Paquete APK firmado y generado en modo Release disponible para instalación en el dispositivo de pruebas.</li>
      <li>Panel Web administrativo publicado y accesible con credenciales operativas configuradas.</li>
      <li>Casos de prueba especificados en su totalidad y revisados formalmente por el QA Lead.</li>
    </ul>
  </div>

  <div class="formal-box">
    <div class="formal-box-title">4.2 Criterios de Suspensión y Reanudación (Suspension & Resumption)</div>
    <p>
      <strong>Suspensión:</strong> Las pruebas se detendrán de inmediato ante la presencia de un defecto bloqueante (Severidad 1) que impida el arranque de la aplicación (Crash on Launch), la caída total de la base de datos o la imposibilidad de autenticación.
    </p>
    <p>
      <strong>Reanudación:</strong> El ciclo se reanudará cuando el equipo de desarrollo entregue un parche correctivo (Hotfix) acompañado de validación unitaria de no-regresión.
    </p>
  </div>

  <div class="formal-box">
    <div class="formal-box-title">4.3 Criterios de Salida (Exit Criteria / Definition of Done)</div>
    <ul>
      <li>100% de los casos de prueba planificados ejecutados y debidamente documentados.</li>
      <li>Cero defectos abiertos clasificados en Severidad 1 (Bloqueante) y Severidad 2 (Crítico).</li>
      <li>Tasa de aprobación mínima del 95% en defectos menores (Severidad 3 o 4) con plan de corrección calendarizado.</li>
      <li>Consistencia transaccional confirmada en PostgreSQL sin registros huérfanos ni inconsistencias de saldo o inventario.</li>
    </ul>
  </div>

  <!-- ==================== SECCIÓN 5 ==================== -->
  <h1 style="margin-top: 16pt;">5. Entorno de Pruebas, Recursos y Configuración</h1>

  <h3>5.1 Plataforma de Hardware y Dispositivos [Configurado por: Cristian Jordan]</h3>
  <ul>
    <li><strong>Dispositivo Móvil de Prueba:</strong> Teléfono físico Android (Android 14 / HyperOS / MIUI), 6 GB memoria RAM, pantalla FHD+ (1080x2400 px), conexión WiFi 5 GHz.</li>
    <li><strong>Estación de Control QA:</strong> Equipo PC Linux Lubuntu / Ubuntu x86_64, 16 GB RAM, acceso local y remoto mediante consola de comandos.</li>
  </ul>

  <h3>5.2 Herramientas de Software y Diagnóstico [Configurado por: Cristian Jordan & Rodrigo Cortez]</h3>
  <ul>
    <li><strong>Android Debug Bridge (ADB):</strong> Captura de trazas del sistema en tiempo real (<code>adb logcat</code>), instalación controlada de APKs y comandos de proceso.</li>
    <li><strong>Scrcpy:</strong> Control e inspección en espejo del dispositivo Android físico para registro de interacciones visuales.</li>
    <li><strong>Navegador Web:</strong> Google Chrome / Chromium versión 120+ con consola de herramientas de desarrollo (DevTools) y auditoría de peticiones de red.</li>
    <li><strong>Tablero de Gestión:</strong> Azure DevOps Boards (Organización: <code>lalafaro6</code>, Proyecto: <code>Testeo</code>).</li>
  </ul>

  <h3>5.3 Datos de Prueba de Línea Base (Test Data Baseline) [Diseñado por: Cornelio Martinez]</h3>
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 25%;">Identificador de Usuario</th>
        <th style="width: 25%;">Rol en el Sistema</th>
        <th style="width: 50%;">Estado Inicial en Base de Datos</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><code>admin@shushine.com</code></td>
        <td>Administrador General</td>
        <td>Acceso irrestricto al panel web, control de inventario y agenda completa.</td>
      </tr>
      <tr>
        <td><code>cliente1@test.com</code></td>
        <td>Cliente Registrado</td>
        <td>Posee exactamente 1 cita confirmada registrada para la fecha actual.</td>
      </tr>
      <tr>
        <td><code>cliente2@test.com</code></td>
        <td>Cliente Registrado</td>
        <td>Cliente nuevo sin citas previas ni reservas en curso (estado vacío).</td>
      </tr>
      <tr>
        <td>Estilistas Activos (3)</td>
        <td>Personal Operativo</td>
        <td>Registrados en catálogo con horarios laborales asignados.</td>
      </tr>
      <tr>
        <td>Catálogo de Servicios</td>
        <td>Servicios de Belleza</td>
        <td>Corte Dama ($25.00), Balayage ($85.00), Manicura Spa ($18.00), Keratina ($65.00).</td>
      </tr>
    </tbody>
  </table>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 6 ==================== -->
  <h1>6. Especificación Detallada de Casos de Prueba (Matriz de Diseño)</h1>
  <p>
    En apego estricto a la norma IEEE 829 Sección 6, cada caso establece su especialista responsable, precondición, procedimiento atómico paso a paso y resultado esperado formal.
  </p>

  <h2>6.1 Módulo: App Móvil Cliente (.NET MAUI en Android) &mdash; Diseñado por: Cristian Jordan</h2>

  <!-- CP-MOB-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-01: Persistencia de Sesión ante Muerte de Proceso Android (Swipe-to-Close)</span>
      <span>NIVEL: UAT | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend Móvil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Verificar que el token JWT persista en almacenamiento dual (SecureStorage + Preferences) y se recupere sincrónicamente tras el cierre forzoso de la aplicación por el sistema operativo.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Cliente autenticado exitosamente en el dispositivo móvil con sesión activa.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Abrir la aplicación y verificar que se visualiza la pantalla principal (ClientHomePage).</li>
          <li>Presionar el botón de inicio de Android para enviar la aplicación a segundo plano.</li>
          <li>Abrir la bandeja de aplicaciones recientes del sistema operativo.</li>
          <li>Deslizar la tarjeta de la aplicación Shushine Studio hacia afuera (Swipe-to-Close) finalizando el proceso en memoria.</li>
          <li>Pulsar nuevamente el ícono de la aplicación en el launcher de Android.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La aplicación inicia directamente en ClientHomePage. La validación sincrónica HasValidTokenSync() en el constructor de AppShell determina la existencia del token en menos de 10 ms. No se muestra la pantalla de inicio de sesión ni se le solicita la contraseña al usuario.
      </div>
    </div>
  </div>

  <!-- CP-MOB-02 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-02: Cierre de Sesión Voluntario y Purga de Credenciales Locales</span>
      <span>NIVEL: SEGURIDAD | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend Móvil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Garantizar que el cierre de sesión voluntario elimine todos los identificadores de sesión en almacenamiento local y permita ingresar inmediatamente con otra cuenta sin fuga de datos.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Sesión activa en el dispositivo con Usuario_A.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Navegar a la pestaña "Perfil" en la barra de navegación inferior.</li>
          <li>Pulsar el botón "Cerrar sesión".</li>
          <li>En el cuadro de diálogo de confirmación, pulsar "Confirmar".</li>
          <li>Verificar la transición inmediata a la pantalla de inicio de sesión (LoginPage).</li>
          <li>Ingresar credenciales válidas de Usuario_B y pulsar "Iniciar sesión".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        El método ClearAsync() purga por completo SecureStorage y Preferences. No se detecta fuga de tokens entre cuentas. La aplicación inicia sesión con los datos específicos de Usuario_B.
      </div>
    </div>
  </div>

  <div class="page-break"></div>

  <!-- CP-MOB-03 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-03: Navegación Shell y Prevención de Excepciones de Enrutamiento</span>
      <span>NIVEL: UAT | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend Móvil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Comprobar que los botones de acción rápida en la pantalla de inicio transicionen hacia sus respectivas rutas sin arrojar ArgumentException ni cierres forzosos.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Usuario posicionado en ClientHomePage.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el botón "Agendar una cita". Verificar navegación a StylistSelectionPage y retroceder.</li>
          <li>Pulsar el botón "Servicios". Verificar navegación a ServicesPage y retroceder.</li>
          <li>Pulsar el botón "Solicitudes". Verificar navegación a RequestsPage y retroceder.</li>
          <li>Pulsar el botón "Chat Atelier". Verificar navegación a ClientChatPage y retroceder.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Cada botón redirige a su ruta Shell registrada. La pila de navegación opera limpiamente sin arrojar excepciones de ruta inexistente y la aplicación permanece estable.
      </div>
    </div>
  </div>

  <!-- CP-MOB-04 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-04: Exclusión Mutua en Renderizado de 'Tu Próxima Cita'</span>
      <span>NIVEL: UI / ESTABILIDAD | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend Móvil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Validar que la sección de próxima cita no presente solapamiento visual entre la tarjeta de datos de la cita y el indicador de ausencia de citas.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Usuario cliente con exactamente 1 cita confirmada registrada en base de datos.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Abrir la pantalla principal de la aplicación móvil.</li>
          <li>Inspeccionar la sección superior rotulada "Tu Próxima Cita".</li>
          <li>Verificar la propiedad IsVisible del contenedor de la cita y del contenedor de estado vacío (EmptyStateView).</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Se exhibe únicamente la tarjeta con los datos de la cita confirmada (servicio, estilista, fecha, hora). La tarjeta "Sin citas pendientes" permanece con IsVisible = False sin ocupar espacio en pantalla. Ambas vistas son mutuamente excluyentes.
      </div>
    </div>
  </div>

  <!-- CP-MOB-05 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-05: Transición al Detalle de Cita sin Fallos de Casteo XAML</span>
      <span>NIVEL: ESTABILIDAD | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend Móvil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Validar que la navegación hacia AppointmentDetailPage resuelva correctamente los convertidores de enlaces compilados sin disparar InvalidCastException.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Tarjeta de próxima cita visible en ClientHomePage.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar directamente sobre el cuerpo de la tarjeta de cita o en el enlace "Ver detalle".</li>
          <li>Esperar la carga completa de AppointmentDetailPage.</li>
          <li>Inspeccionar la salida de adb logcat en busca de excepciones no capturadas durante la fase de layout.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La vista AppointmentDetailPage se renderiza de forma inmediata mostrando el resumen de la reserva, estilista y badge de estado. Cero excepciones de casteo XAML detectadas en consola.
      </div>
    </div>
  </div>

  <!-- CP-MOB-06 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-06: Gestión de Pestaña 'Mis Citas' (Próximas vs Historial)</span>
      <span>NIVEL: UAT | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend Móvil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Comprobar la alternancia de filtros entre citas programadas y pasadas con soporte de gesto Pull-to-Refresh.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Cliente con historial de citas activas y pasadas en base de datos.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Seleccionar la pestaña "Citas" en la barra de navegación inferior.</li>
          <li>Verificar la lista bajo el selector predeterminado "Próximas".</li>
          <li>Pulsar el selector "Historial".</li>
          <li>Realizar el gesto de deslizamiento vertical hacia abajo (Pull-to-Refresh).</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La lista conmuta mostrando las citas correspondientes a cada segmento con sus insignias de estado (Confirmada, Completada, Cancelada). El gesto de refresco sincroniza la lista con el backend y oculta el indicador de carga.
      </div>
    </div>
  </div>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 6.2 ==================== -->
  <h2>6.2 Módulo: Panel Administrativo Web (Alpine.js) &mdash; Diseñado por: Rodrigo Cortez</h2>

  <!-- CP-WEB-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-01: Módulo de Chat & Soporte en Tiempo Real</span>
      <span>NIVEL: UAT / INTEGRACIÓN | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operación de Salón</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Validar que el módulo de Chat y Soporte se encuentre disponible en el menú de navegación y permita la inserción de respuestas en Supabase.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Administrador autenticado en el panel web.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Localizar el menú de navegación lateral izquierdo.</li>
          <li>Hacer clic sobre la opción "Chat & Soporte".</li>
          <li>Seleccionar una conversación de cliente en el listado lateral.</li>
          <li>Redactar un mensaje de prueba ("Estimada cliente, su cita está confirmada").</li>
          <li>Presionar el botón "Enviar" o la tecla Enter.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        El mensaje se renderiza inmediatamente en el hilo visual como emitido por el administrador. Se inserta un registro en la tabla mensajes de Supabase con el remitente correspondiente y timestamp actual.
      </div>
    </div>
  </div>

  <!-- CP-WEB-02 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-02: Filtro Predeterminado de Agenda: Citas de Hoy</span>
      <span>NIVEL: OPERATIVO | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operación de Salón</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Asegurar que la vista inicial de la agenda filtre únicamente las citas de la jornada actual para optimizar la operación del recepcionista.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Existen citas registradas para hoy, días anteriores y días posteriores.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Navegar a la sección "Agenda & Citas" desde el menú lateral.</li>
          <li>Inspeccionar la tabla principal de citas y el encabezado de estado.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La tabla filtra automáticamente y expone únicamente las citas cuya fecha coincida con la fecha actual del sistema. Se visualiza un badge de resumen que cuantifica las citas del día.
      </div>
    </div>
  </div>

  <!-- CP-WEB-03 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-03: Filtro de Agenda: Ver Todo (Historial) y Filtro por Fecha</span>
      <span>NIVEL: UAT | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operación de Salón</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Permitir al operador consultar el historial completo o filtrar por fecha mediante datepicker y filtros de estado.</span></div>
      <div class="field-row"><span class="field-label">Mecanismo Técnico:</span><span class="field-value">Petición HTTP a Supabase REST con parámetros .select('*') y combinación condicional: .eq('fecha', fechaSeleccionada) y .eq('estado', estadoFiltro) en servidor, combinada con renderizado reactivo Alpine.js en memoria.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Módulo "Agenda & Citas" desplegado.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el botón "Ver Todo (Historial)". Verificar el contenido de la tabla.</li>
          <li>Hacer clic en el campo "Filtrar por Día" y seleccionar una fecha diferente en el calendario.</li>
          <li>Alternar el filtro de estado entre "Todas", "Confirmadas" y "Pendientes".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Al pulsar "Ver Todo", la tabla muestra todas las citas históricas sin filtro de fecha. Al escoger una fecha en el datepicker, la tabla conmuta en vivo mostrando exclusivamente los registros coincidentes mediante el parámetro .eq('fecha', ...).
      </div>
    </div>
  </div>

  <!-- CP-WEB-04 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-04: Creación de Nueva Cita desde el Panel Web</span>
      <span>NIVEL: TRANSACCIONAL | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operación de Salón</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Comprobar la inserción de una reserva manual desde el modal administrativo con asignación de folio único.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Módulo de Agenda abierto en el panel web.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el botón "+ Nueva Cita".</li>
          <li>Completar los campos del modal: Cliente, Servicio, Estilista, Fecha y Hora.</li>
          <li>Pulsar el botón "Guardar Cita".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Se genera un código de cita único con prefijo #SHU-2026-XXXX. El registro se inserta exitosamente en la tabla citas de Supabase, se presenta una notificación flotante (Toast) y la tabla de agenda se actualiza automáticamente.
      </div>
    </div>
  </div>

  <div class="page-break"></div>

  <!-- CP-WEB-05 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-05: Registro de Entrada de Stock en Inventario del Salón</span>
      <span>NIVEL: GESTIÓN INVENTARIO | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operación de Salón</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Validar que el ingreso de insumos incremente el stock físico y registre el movimiento de auditoría correspondiente.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Módulo "Inventario del Salón" abierto.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el botón "Entrada de Stock".</li>
          <li>Seleccionar un producto del catálogo de insumos.</li>
          <li>Digitar una cantidad de ingreso (ej. 10 unidades) y motivo ("Reabastecimiento de proveedor").</li>
          <li>Presionar "Guardar Entrada".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Se crea un registro en la tabla movimientos_inventario de tipo 'entrada'. La cantidad en existencia del producto aumenta en exactamente 10 unidades y el badge de estado se recalcula automáticamente a 'Óptimo'.
      </div>
    </div>
  </div>

  <!-- CP-WEB-06 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-06: Métricas Ejecutivas y KPIs 100% Reales</span>
      <span>NIVEL: INTEGRIDAD DATOS | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operación de Salón</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Confirmar que el Dashboard Ejecutivo no contenga valores fijos ni simulados y responda únicamente a data viva de Supabase.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Dashboard principal abierto.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Inspeccionar las 4 tarjetas métricas superiores (Ingresos, Citas del Día, Clientes VIP, Productos Críticos).</li>
          <li>Abrir la consola de red de DevTools (F12) y constatar las peticiones REST hacia Supabase.</li>
          <li>Hacer clic en la campana de notificaciones.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Los montos y contadores se derivan de funciones de agregación reales (SUM, COUNT). Las alertas emitidas corresponden estrictamente a productos bajo el stock mínimo o citas pendientes reales.
      </div>
    </div>
  </div>

  <!-- ==================== SECCIÓN 6.3 ==================== -->
  <h2>6.3 Módulo: Integración Omnicanal End-to-End (E2E) &mdash; Diseñado por: Cornelio Martinez</h2>

  <!-- CP-INT-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-INT-01: Reflejo en Panel Web de Cita Agendada desde App Móvil</span>
      <span>NIVEL: INTEGRACIÓN E2E | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integración &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Verificar la propagación en tiempo real de una reserva creada desde el smartphone del cliente hacia la agenda web del operador.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">App móvil abierta en el celular y Panel Web abierto simultáneamente en la computadora.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>En el teléfono móvil, completar una reserva de servicio ("Corte & Brushing") y confirmar.</li>
          <li>Medir el tiempo transcurrido hasta que el registro se visualice en el Panel Web sin recargar la página (F5).</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La cita aparece en la tabla de la agenda web en un lapso inferior a 5 segundos con el badge de origen "App Móvil" y se emite la notificación visual correspondiente.
      </div>
    </div>
  </div>

  <!-- CP-INT-02 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-INT-02: Reflejo en App Móvil de Cita Agendada desde Panel Web</span>
      <span>NIVEL: INTEGRACIÓN E2E | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integración &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Confirmar que una cita asignada por el recepcionista aparezca de inmediato en la aplicación del cliente.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Cliente con sesión abierta en la aplicación móvil.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>En el Panel Web, el recepcionista crea una nueva cita para el cliente especificado.</li>
          <li>El cliente abre su app móvil o refresca la vista de inicio.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La sección "Tu Próxima Cita" y la pestaña "Mis Citas" del cliente muestran la nueva reserva con todos los datos sincronizados.
      </div>
    </div>
  </div>

  <!-- CP-INT-03 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-INT-03: Mensajería Bidireccional en Tiempo Real (Chat Móvil &lt;-&gt; Soporte Web)</span>
      <span>NIVEL: INTEGRACIÓN E2E | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integración &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Validar la comunicación instantánea mediante el canal de WebSockets de Supabase Realtime.</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Cliente en vista Chat de la app móvil; Administrador en módulo Chat & Soporte del panel web.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>El cliente envía el mensaje: "¿Tienen disponibilidad de cambio de horario?".</li>
          <li>El administrador visualiza el mensaje y responde: "Con gusto, disponemos de las 4:00 PM".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Ambos mensajes se reciben y despliegan en sus respectivas interfaces en tiempo real sin requerir acciones manuales de recarga.
      </div>
    </div>
  </div>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 6.4 ==================== -->
  <h2>6.4 Módulo: Seguridad y Políticas Row Level Security (RLS) &mdash; Diseñado por: Cornelio Martinez</h2>

  <!-- CP-SEC-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-SEC-01: Verificación de Aislamiento y Violación de Políticas RLS en Supabase</span>
      <span>NIVEL: SEGURIDAD | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integración &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo Técnico:</span><span class="field-value">Garantizar que las directivas RLS en PostgreSQL impidan que un usuario consulte, modifique o elimine reservas pertenecientes a otro usuario utilizando tokens ajenos o la clave pública anon_key.</span></div>
      <div class="field-row"><span class="field-label">Norma Asociada:</span><span class="field-value">ISO 9001:2015 Cláusula 8.2 / OWASP Top 10 API Security (BOLA &mdash; Broken Object Level Authorization).</span></div>
      <div class="field-row"><span class="field-label">Precondición:</span><span class="field-value">Usuario_A posee citas registradas con su ID. Usuario_B posee token JWT válido propio.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Desde un cliente REST (Postman), interceptar el token de autenticación de Usuario_B.</li>
          <li>Emitir petición GET /rest/v1/citas?cliente_id=eq.{ID_Usuario_A} portando el Bearer token de Usuario_B.</li>
          <li>Emitir petición PATCH intentando alterar el estado de la cita de Usuario_A a "Cancelada".</li>
          <li>Emitir petición GET /citas utilizando únicamente la anon_key pública sin cabecera de autorización de usuario.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La consulta GET de Usuario_B retorna un arreglo vacío [] o HTTP 403 Forbidden. La mutación PATCH no altera ninguna fila (0 filas afectadas). La consulta con anon_key sin sesión es bloqueada por las directivas de seguridad a nivel de fila (auth.uid() = cliente_id).
      </div>
    </div>
  </div>

  <!-- ==================== SECCIÓN 7 ==================== -->
  <h1>7. Gestión de Defectos y Clasificación de Severidades &mdash; Alex Fernando Alfaro Diaz</h1>
  <p><em>Especialista Responsable: Alex Fernando Alfaro Diaz (QA Lead &amp; Gobernanza de Calidad)</em></p>
  <p>
    Todo desvío detectado durante la ejecución futura del plan se registrará en el gestor de incidentes de Azure DevOps bajo la siguiente escala formal:
  </p>

  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 20%;">Severidad</th>
        <th style="width: 35%;">Definición Técnica</th>
        <th style="width: 15%; text-align: center;">SLA de Resolución</th>
        <th style="width: 30%;">Ejemplo Típico</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>S1: Bloqueante (Blocker)</strong></td>
        <td>El sistema queda inoperativo; crash de la aplicación, bloqueo de base de datos o fallo en el inicio de sesión.</td>
        <td style="text-align: center;">Inmediato (&lt; 4h)</td>
        <td>Cierre forzoso de la app al pulsar una cita o fallo al arrancar.</td>
      </tr>
      <tr>
        <td><strong>S2: Crítico (Critical)</strong></td>
        <td>Pérdida de funcionalidad principal sin alternativa; fallo al insertar citas o inconsistencia de stock.</td>
        <td style="text-align: center;">&lt; 24h</td>
        <td>El botón de agendar no inserta el registro en Supabase.</td>
      </tr>
      <tr>
        <td><strong>S3: Mayor (Major)</strong></td>
        <td>Funcionalidad relevante afectada pero con procedimiento alternativo o manual disponible.</td>
        <td style="text-align: center;">&lt; 48h</td>
        <td>El datepicker de agenda requiere presionar dos veces para refrescar.</td>
      </tr>
      <tr>
        <td><strong>S4: Menor (Cosmetic)</strong></td>
        <td>Detalle cosmético, error tipográfico o espaciado visual que no afecta la lógica ni la integridad.</td>
        <td style="text-align: center;">Próximo Sprint</td>
        <td>Desalineación de un icono en la barra lateral del panel web.</td>
      </tr>
    </tbody>
  </table>

  <!-- ==================== SECCIÓN 8 ==================== -->
  <h2>8. Matriz de Responsabilidades y Roles (RACI)</h2>
  
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 30%;">Rol / Integrante</th>
        <th style="width: 14%; text-align: center;">Planificación</th>
        <th style="width: 14%; text-align: center;">Ejecución</th>
        <th style="width: 14%; text-align: center;">Entornos</th>
        <th style="width: 14%; text-align: center;">Defectos</th>
        <th style="width: 14%; text-align: center;">Auditoría RLS</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>QA Lead / Estrategia & Riesgos (Alex Fernando Alfaro Diaz)</strong></td>
        <td style="text-align: center;">A / R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">A</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">R</td>
      </tr>
      <tr>
        <td><strong>QA Frontend Móvil & Usabilidad (Cristian Jordan)</strong></td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">I</td>
      </tr>
      <tr>
        <td><strong>QA Panel Web & Operación de Salón (Rodrigo Cortez)</strong></td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">I</td>
      </tr>
      <tr>
        <td><strong>QA Backend, Integración & Seguridad (Cornelio Martinez)</strong></td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">R</td>
      </tr>
    </tbody>
  </table>
  <p style="font-size: 8pt; color: #444444; margin-top: 2pt;">
    *Referencias: R = Responsable de ejecución/diseño, A = Aprobador final (Accountable), C = Consultado técnico, I = Informado.
  </p>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 9 ==================== -->
  <h1>9. Riesgos de Prueba y Planes de Contingencia Cuantitativos &mdash; Alex Fernando Alfaro Diaz</h1>
  <p><em>Especialista Responsable: Alex Fernando Alfaro Diaz (QA Lead &amp; Riesgos Técnicos)</em></p>

  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 25%;">Riesgo Técnico Identificado</th>
        <th style="width: 10%; text-align: center;">Prob.</th>
        <th style="width: 10%; text-align: center;">Impacto</th>
        <th style="width: 55%;">Estrategia de Mitigación y SLA Cuantitativo</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>R1: Ahorro agresivo de batería en Android (HyperOS/MIUI)</strong></td>
        <td style="text-align: center;">Alta</td>
        <td style="text-align: center;">Alto</td>
        <td>Pruebas de persistencia dual (SecureStorage + Preferences) y lectura síncrona en AppShell con tiempo de resolución instantáneo (&lt; 10 ms).</td>
      </tr>
      <tr>
        <td><strong>R2: Latencia de red o desconexión en WebSockets Supabase</strong></td>
        <td style="text-align: center;">Media</td>
        <td style="text-align: center;">Medio</td>
        <td><strong>SLA Cuantitativo de Reconexión:</strong> Reintentos exponenciales (backoff: 1s, 2s, 4s, 8s hasta máx. 15s). Superados los 15s, conmuta a sondeo REST (Polling cada 6s) y activa badge visual de reconexión.</td>
      </tr>
      <tr>
        <td><strong>R3: Concurrencia de reservas sobre el mismo estilista</strong></td>
        <td style="text-align: center;">Media</td>
        <td style="text-align: center;">Alto</td>
        <td>Validación de restricciones únicas a nivel de base de datos (UNIQUE(estilista_id, fecha, hora)) con rollback transaccional ante conflicto simultáneo.</td>
      </tr>
      <tr>
        <td><strong>R4: Incompatibilidad de convertidores XAML compilados</strong></td>
        <td style="text-align: center;">Alta</td>
        <td style="text-align: center;">Alto</td>
        <td>Erradicación total de bindings nulos en XAML sustituyéndolos por propiedades booleanas fuertemente tipadas en ViewModels (HasErrorMessage, etc.).</td>
      </tr>
    </tbody>
  </table>

  <!-- ==================== SECCIÓN 10 ==================== -->
  <h2>10. Entregables de la Fase de Pruebas &mdash; Supervisado por: Alex Fernando Alfaro Diaz</h2>
  <p>Al concluir el ciclo de ejecución estructurado en este documento, se consolidarán los siguientes artefactos:</p>
  <ol>
    <li><strong>Registro de Ejecución en Azure Boards:</strong> Trazabilidad completa de casos de prueba vinculados a la Épica maestra #162 en el proyecto Testeo de Azure DevOps.</li>
    <li><strong>Registro de Incidentes y Defectos (Bug Reports):</strong> Fichas técnicas documentadas con logs de sistema (adb logcat / Chrome DevTools) y pasos de reproducción.</li>
    <li><strong>Informe Formal de Cierre de Pruebas (Test Summary Report):</strong> Certificación final de calidad que valida el cumplimiento de los criterios de salida para pase a producción.</li>
  </ol>

  <!-- ==================== SECCIÓN 11 ==================== -->
  <div class="page-break"></div>
  <h1>11. Firmas y Conformidad del Equipo de Pruebas</h1>
  <p>
    En señal de conformidad técnica con el diseño, alcance, metodología y responsabilidades establecidas en el presente Plan Maestro de Pruebas de Software (STP-SHU-2026-V2), los integrantes del equipo de Aseguramiento de Calidad suscriben y validan el presente documento:
  </p>

  <table class="formal-table" style="margin-top: 15pt;">
    <thead>
      <tr>
        <th style="width: 25%;">Nombre del Integrante</th>
        <th style="width: 25%;">Especialidad Técnica Asignada</th>
        <th style="width: 28%;">Módulos / Casos Diseñados</th>
        <th style="width: 22%; text-align: center;">Firma / Conformidad</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Alex Fernando Alfaro Diaz</strong></td>
        <td>QA Lead / Estrategia &amp; Riesgos</td>
        <td>Secciones 1, 3, 4, 7, 9 y 10 (Gobernanza Global)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
      <tr>
        <td><strong>Cristian Jordan</strong></td>
        <td>QA Frontend Móvil .NET MAUI</td>
        <td>Sección 6.1 (CP-MOB-01 al CP-MOB-06)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
      <tr>
        <td><strong>Rodrigo Cortez</strong></td>
        <td>QA Panel Web &amp; Operaciones</td>
        <td>Sección 6.2 (CP-WEB-01 al CP-WEB-06)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
      <tr>
        <td><strong>Cornelio Martinez</strong></td>
        <td>QA Backend, Integración &amp; RLS</td>
        <td>Secciones 6.3 y 6.4 (CP-INT-01 a 03 y CP-SEC-01)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
    </tbody>
  </table>

  <div style="margin-top: 40pt; border-top: 1pt solid #000000; padding-top: 10pt; text-align: center; font-size: 8.5pt;">
    <strong>SHUSHINE STUDIO &mdash; EQUIPO DE ASEGURAMIENTO DE CALIDAD DE SOFTWARE (TESTING QA)</strong><br>
    Alex Fernando Alfaro Diaz &bull; Cristian Jordan &bull; Rodrigo Cortez &bull; Cornelio Martinez<br>
    Documento elaborado en conformidad con ISO/IEC/IEEE 29119-3 / IEEE 829 / ISO 9001:2015.
  </div>

</body>
</html>
`;

async function generatePDF() {
    console.log("Iniciando generación de PDF con Playwright Chromium...");
    const browser = await pw.chromium.launch({ headless: true });
    const page = await browser.newPage();
    
    await page.setContent(htmlContent, { waitUntil: 'networkidle' });
    
    const outputPath1 = '/home/ministorm/salon-de-belleza/docs/Plan_de_Pruebas_Formal_IEEE829_Arial_Neutral.pdf';
    const outputPath2 = '/home/ministorm/Descargas/Plan_de_Pruebas_Formal_IEEE829_Arial_Neutral.pdf';

    await page.pdf({
        path: outputPath1,
        format: 'A4',
        printBackground: true,
        displayHeaderFooter: true,
        headerTemplate: '<div style="font-size: 8pt; font-family: Arial, sans-serif; color: #555555; width: 100%; border-bottom: 0.5pt solid #cccccc; padding-bottom: 2pt; margin: 0 18mm; display: flex; justify-content: space-between;"><span>SHUSHINE STUDIO &mdash; PLAN MAESTRO DE PRUEBAS DE SOFTWARE</span><span>STP-SHU-2026-V2</span></div>',
        footerTemplate: '<div style="font-size: 8pt; font-family: Arial, sans-serif; color: #555555; width: 100%; border-top: 0.5pt solid #cccccc; padding-top: 2pt; margin: 0 18mm; display: flex; justify-content: space-between;"><span>Uso Interno Confidencial &mdash; ISO/IEC/IEEE 29119-3 / IEEE 829</span><span>P&aacute;gina <span class="pageNumber"></span> de <span class="totalPages"></span></span></div>',
        margin: {
            top: '22mm',
            bottom: '22mm',
            left: '18mm',
            right: '18mm'
        }
    });

    fs.copyFileSync(outputPath1, outputPath2);
    await browser.close();

    console.log("PDF generado exitosamente en:");
    console.log("1.", outputPath1);
    console.log("2.", outputPath2);
}

generatePDF().catch(err => {
    console.error("Error al generar PDF:", err);
    process.exit(1);
});
