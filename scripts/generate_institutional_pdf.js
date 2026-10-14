/**
 * Script de Generación de PDF Oficial Institucional ESFE AGAPE / MEGATEC
 * Proyecto: Shushine Studio — Plan Maestro de Pruebas de Software (STP-SHU-2026-V3)
 * Normas: ISO/IEC/IEEE 29119-3:2021 / IEEE 829-2008 / ISO 9001:2015
 * Formato: Tipografía Arial exclusivamente, sin emojis, estilo formal institucional,
 * portada idéntica a la plantilla institucional, paginado profesional con pdfunite.
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const pw = require('/home/ministorm/.cache/ms-playwright-go/1.57.0/package');

// 1. Cargar logos oficiales en base64
const imgEsfeAgape = fs.readFileSync('/home/ministorm/salon-de-belleza/docs/assets/logo_esfe_agape.png').toString('base64');
const imgBannerMin = fs.readFileSync('/home/ministorm/salon-de-belleza/docs/assets/banner_ministerio_megatec.png').toString('base64');
const imgHeader = fs.readFileSync('/home/ministorm/salon-de-belleza/docs/assets/header_institucional.png').toString('base64');

// 2. HTML de Portada Institucional (Página 1 exacta)
const coverHtml = `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Portada Institucional - ESFE AGAPE</title>
<style>
  @page {
    size: A4 portrait;
    margin: 20mm 20mm 20mm 20mm;
  }
  *, *:before, *:after { box-sizing: border-box; }
  body {
    font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
    color: #000000;
    background-color: #ffffff;
    margin: 0;
    padding: 0;
    text-align: center;
    text-rendering: optimizeLegibility;
  }
  .inst-header {
    font-size: 11pt;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1.5pt;
    margin-top: 15pt;
    margin-bottom: 25pt;
    color: #000000;
  }
  .logo-esfe {
    width: 105px;
    height: auto;
    display: block;
    margin: 0 auto 15pt auto;
  }
  .logo-banner {
    width: 255px;
    height: auto;
    display: block;
    margin: 0 auto 40pt auto;
  }
  .cover-block {
    margin-bottom: 22pt;
  }
  .block-label {
    font-size: 10.5pt;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 0.8pt;
    color: #000000;
    margin-bottom: 3pt;
  }
  .block-val {
    font-size: 10pt;
    color: #222222;
    line-height: 1.4;
  }
  .block-title-val {
    font-size: 13pt;
    font-weight: bold;
    color: #000000;
    line-height: 1.3;
  }
  .block-norma {
    font-size: 9pt;
    color: #444444;
    font-style: italic;
    margin-top: 3pt;
  }
</style>
</head>
<body>
  <div class="inst-header">ESCUELA SUPERIOR FRANCISCANA ESPECIALIZADA / AGAPE</div>
  
  <img src="data:image/png;base64,${imgEsfeAgape}" class="logo-esfe" alt="Logo ESFE Agape">
  <img src="data:image/png;base64,${imgBannerMin}" class="logo-banner" alt="Logos Ministerio Megatec Agape">

  <div class="cover-block">
    <div class="block-label">CARRERA</div>
    <div class="block-val">T&eacute;cnico en Ingenier&iacute;a de Desarrollo de Software</div>
  </div>

  <div class="cover-block">
    <div class="block-label">M&Oacute;DULO</div>
    <div class="block-val">Aseguramiento de la Calidad de Software / Formulaci&oacute;n del Plan de Pruebas</div>
  </div>

  <div class="cover-block">
    <div class="block-label">T&Iacute;TULO</div>
    <div class="block-title-val">Plan Maestro de Pruebas de Software &mdash; Shushine Studio</div>
    <div class="block-norma">Ecosistema Omnicanal de Gesti&oacute;n y Reservas de Belleza (ISO/IEC/IEEE 29119-3 / IEEE 829)</div>
  </div>

  <div class="cover-block">
    <div class="block-label">ESTUDIANTE(S)</div>
    <div class="block-val">
      Alex Fernando Alfaro Diaz<br>
      Cristian Jordan<br>
      Rodrigo Cortez<br>
      Cornelio Martinez
    </div>
  </div>

  <div class="cover-block">
    <div class="block-label">DOCENTE</div>
    <div class="block-val">Docente Evaluador / Especialista en Ingenier&iacute;a y Calidad de Software</div>
  </div>

  <div class="cover-block">
    <div class="block-label">FECHA DE PRESENTACI&Oacute;N</div>
    <div class="block-val">12 de Octubre de 2026</div>
  </div>

  <div class="cover-block" style="margin-bottom: 0;">
    <div class="block-label">CICLO</div>
    <div class="block-val">Ciclo II - 2026</div>
  </div>
</body>
</html>
`;

// 3. HTML del Contenido General (Páginas 2 a N)
const contentHtml = `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Plan de Pruebas - ESFE AGAPE</title>
<style>
  @page {
    size: A4 portrait;
    margin: 28mm 18mm 20mm 18mm;
  }

  *, *:before, *:after { box-sizing: border-box; }

  body {
    font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
    font-size: 9.5pt;
    line-height: 1.5;
    color: #000000;
    background-color: #ffffff;
    margin: 0;
    padding: 0;
    text-rendering: optimizeLegibility;
  }

  .page-break { page-break-before: always; }
  .avoid-break { page-break-inside: avoid; break-inside: avoid; }

  h1, h2, h3, h4, h5 {
    font-family: Arial, "Helvetica Neue", Helvetica, sans-serif;
    color: #000000;
    font-weight: bold;
    margin-top: 14pt;
    margin-bottom: 5pt;
    page-break-after: avoid;
    break-after: avoid;
  }

  h1 {
    font-size: 14pt;
    border-bottom: 1.5pt solid #000000;
    padding-bottom: 3.5pt;
    margin-top: 0;
    text-transform: uppercase;
    letter-spacing: 0.5pt;
  }

  h2 {
    font-size: 12pt;
    border-bottom: 1pt solid #444444;
    padding-bottom: 2.5pt;
    margin-top: 14pt;
  }

  h3 {
    font-size: 10.5pt;
    margin-top: 11pt;
    text-transform: uppercase;
    letter-spacing: 0.3pt;
  }

  h4 {
    font-size: 9.5pt;
    margin-top: 9pt;
    font-weight: bold;
  }

  p {
    margin-top: 0;
    margin-bottom: 6pt;
    text-align: justify;
    text-justify: inter-word;
  }

  ul, ol {
    margin-top: 0;
    margin-bottom: 7pt;
    padding-left: 18pt;
  }

  li {
    margin-bottom: 2.5pt;
    text-align: justify;
  }

  /* Tabla de Contenido / Índice */
  .toc-container {
    page-break-inside: avoid;
    break-inside: avoid;
  }

  .toc-title {
    text-align: center;
    font-size: 14pt;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1.2pt;
    margin-top: 0;
    margin-bottom: 12pt;
    border-bottom: 1.5pt solid #000000;
    padding-bottom: 5pt;
  }

  .toc-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 8.2pt;
    margin-bottom: 0;
  }

  .toc-table td {
    padding: 2pt 0;
    vertical-align: bottom;
  }

  .toc-table td.toc-item {
    padding-right: 5pt;
    white-space: nowrap;
  }

  .toc-table td.toc-dots {
    border-bottom: 1pt dotted #777777;
    width: 100%;
  }

  .toc-table td.toc-num {
    padding-left: 8pt;
    text-align: right;
    font-weight: bold;
    white-space: nowrap;
  }

  .toc-table tr.toc-main td {
    font-weight: bold;
    padding-top: 4.5pt;
    font-size: 8.8pt;
  }

  .toc-table tr.toc-sub td.toc-item {
    padding-left: 12pt;
  }

  .toc-table tr.toc-subsub td.toc-item {
    padding-left: 22pt;
    color: #222222;
  }

  /* Tablas Formales */
  table.formal-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 6pt;
    margin-bottom: 10pt;
    font-size: 8.5pt;
    page-break-inside: avoid;
    break-inside: avoid;
  }

  table.formal-table th, table.formal-table td {
    border: 1pt solid #444444;
    padding: 4.5pt 6pt;
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
    margin-top: 8pt;
    margin-bottom: 11pt;
    page-break-inside: avoid;
    break-inside: avoid;
    background-color: #ffffff;
  }

  .test-case-header {
    background-color: #e8e8e8;
    border-bottom: 1pt solid #000000;
    padding: 4pt 7pt;
    font-weight: bold;
    font-size: 9pt;
    display: flex;
    justify-content: space-between;
  }

  .test-case-body {
    padding: 5.5pt 7.5pt;
    font-size: 8.5pt;
  }

  .field-row {
    margin-bottom: 3pt;
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
    margin-top: 2.5pt;
    margin-bottom: 2.5pt;
    padding-left: 14pt;
  }

  .steps-list li {
    margin-bottom: 1.5pt;
  }

  .expected-box {
    margin-top: 4.5pt;
    padding: 4.5pt 6.5pt;
    background-color: #f7f7f7;
    border-left: 2.5pt solid #000000;
    font-size: 8pt;
  }

  /* Pirámide de Pruebas */
  .pyramid-container {
    width: 100%;
    margin: 8pt auto;
    border: 1pt solid #000000;
    page-break-inside: avoid;
    break-inside: avoid;
  }

  .pyramid-tier {
    padding: 5pt 8pt;
    border-bottom: 1pt solid #444444;
    text-align: center;
  }

  .pyramid-tier:last-child {
    border-bottom: none;
  }

  .tier-1 { background-color: #ffffff; }
  .tier-2 { background-color: #f2f2f2; }
  .tier-3 { background-color: #e4e4e4; }

  .tier-title {
    font-weight: bold;
    font-size: 8.5pt;
    text-transform: uppercase;
    margin-bottom: 1.5pt;
  }

  .tier-desc {
    font-size: 8pt;
    color: #222222;
  }

  /* Cuadros Formales */
  .formal-box {
    border: 1pt solid #444444;
    background-color: #fbfbfb;
    padding: 5.5pt 8pt;
    margin: 6pt 0;
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
    padding-bottom: 1.5pt;
  }

  code {
    font-family: "Courier New", Courier, monospace;
    font-size: 8pt;
    background-color: #f0f0f0;
    padding: 0.5pt 2.5pt;
    border: 0.5pt solid #cccccc;
  }
</style>
</head>
<body>

  <!-- ==================== CONTENIDO / ÍNDICE (Página 2) ==================== -->
  <div class="toc-container">
    <div class="toc-title">CONTENIDO</div>

  <table class="toc-table">
    <tr class="toc-main">
      <td class="toc-item">1. INTRODUCCI&Oacute;N</td>
      <td class="toc-dots"></td>
      <td class="toc-num">2</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">1.1 Alcance</td>
      <td class="toc-dots"></td>
      <td class="toc-num">2</td>
    </tr>
    <tr class="toc-subsub">
      <td class="toc-item">1.1.1 Alcance (Elementos Cr&iacute;ticos Bajo Prueba)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">3</td>
    </tr>
    <tr class="toc-subsub">
      <td class="toc-item">1.1.2 Fuera de alcance (Exclusiones Formales)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">3</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">1.2 Objetivo de Calidad</td>
      <td class="toc-dots"></td>
      <td class="toc-num">4</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">1.3 Roles y Responsabilidades (Equipo de Testing y Matriz RACI)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">5</td>
    </tr>

    <tr class="toc-main">
      <td class="toc-item">2. METODOLOG&Iacute;A DE PRUEBA</td>
      <td class="toc-dots"></td>
      <td class="toc-num">6</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">2.1 Compendio (Matriz de Casos de Prueba Gherkin)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">6</td>
    </tr>
    <tr class="toc-subsub">
      <td class="toc-item">2.1.1 M&oacute;dulo App M&oacute;vil Cliente (.NET MAUI) &mdash; Resp: Cristian Jordan</td>
      <td class="toc-dots"></td>
      <td class="toc-num">6</td>
    </tr>
    <tr class="toc-subsub">
      <td class="toc-item">2.1.2 M&oacute;dulo Panel Administrativo Web (Alpine.js) &mdash; Resp: Rodrigo Cortez</td>
      <td class="toc-dots"></td>
      <td class="toc-num">10</td>
    </tr>
    <tr class="toc-subsub">
      <td class="toc-item">2.1.3 M&oacute;dulo Integraci&oacute;n Omnicanal End-to-End &mdash; Resp: Cornelio Martinez</td>
      <td class="toc-dots"></td>
      <td class="toc-num">13</td>
    </tr>
    <tr class="toc-subsub">
      <td class="toc-item">2.1.4 M&oacute;dulo Seguridad y Pol&iacute;ticas RLS &mdash; Resp: Cornelio Martinez</td>
      <td class="toc-dots"></td>
      <td class="toc-num">14</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">2.2 Niveles de prueba (Unitarias, Integraci&oacute;n, UAT y No Funcionales)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">14</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">2.3 Clasificaci&oacute;n de errores (Escala S1-S4 y SLAs Cuantitativos)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">14</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">2.4 Criterios de suspensi&oacute;n y requisitos de reanudaci&oacute;n</td>
      <td class="toc-dots"></td>
      <td class="toc-num">15</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">2.5 Completitud de la prueba (Criterios de Salida / DoD)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">15</td>
    </tr>

    <tr class="toc-main">
      <td class="toc-item">3. ENTREGABLES DE LA PRUEBA</td>
      <td class="toc-dots"></td>
      <td class="toc-num">16</td>
    </tr>

    <tr class="toc-main">
      <td class="toc-item">4. NECESIDADES DE RECURSOS Y MEDIO AMBIENTE</td>
      <td class="toc-dots"></td>
      <td class="toc-num">16</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">4.1 Herramientas de prueba (ADB, Scrcpy, DevTools, Playwright, Azure Boards)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">16</td>
    </tr>
    <tr class="toc-sub">
      <td class="toc-item">4.2 Entorno de prueba (Hardware, Servidores y Datos de L&iacute;nea Base)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">16</td>
    </tr>

    <tr class="toc-main">
      <td class="toc-item">T&Eacute;RMINOS / ACR&Oacute;NIMOS (GLOSARIO T&Eacute;CNICO)</td>
      <td class="toc-dots"></td>
      <td class="toc-num">18</td>
    </tr>
    <tr class="toc-main">
      <td class="toc-item">ALINEACI&Oacute;N CON LA NORMA INTERNACIONAL ISO/IEC/IEEE 29119</td>
      <td class="toc-dots"></td>
      <td class="toc-num">18</td>
    </tr>
    <tr class="toc-main">
      <td class="toc-item">FIRMAS Y CONFORMIDAD DEL EQUIPO DE PRUEBAS</td>
      <td class="toc-dots"></td>
      <td class="toc-num">19</td>
    </tr>
  </table>
  </div>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 1 ==================== -->
  <h1>1. INTRODUCCI&Oacute;N</h1>
  <p>
    El presente Plan Maestro de Pruebas de Software define de manera estructurada, rigurosa y aplicable la estrategia de aseguramiento de calidad t&eacute;cnico para el sistema <strong>Shushine Studio</strong>, una plataforma tecnol&oacute;gica omnicanal orientada a la gesti&oacute;n integral de reservas, control de inventario en bodega y atenci&oacute;n a clientes en salones de belleza y est&eacute;tica profesional.
  </p>
  <p>
    En estricto cumplimiento con las directrices institucionales de la <strong>Escuela Superior Franciscana Especializada (ESFE AGAPE)</strong> y los est&aacute;ndares internacionales <strong>ISO/IEC/IEEE 29119-3:2021</strong> e <strong>IEEE 829-2008</strong>, este documento formaliza la <strong>fase de planificaci&oacute;n, dise&ntilde;o y especificaci&oacute;n met&oacute;dica de pruebas</strong>. En consecuencia, este plan <strong>no documenta resultados prematuros de ejecuci&oacute;n</strong>; su funci&oacute;n ingenieril consiste en fijar el marco metodol&oacute;gico, las condiciones previas, los procedimientos paso a paso y los resultados esperados verificables que certifiquen la calidad del software antes de su liberaci&oacute;n formal a producci&oacute;n.
  </p>

  <h2>1.1 Alcance</h2>
  <p>
    El sistema bajo prueba (System Under Test &mdash; SUT) comprende una arquitectura desacoplada de tres subsistemas interactuantes:
  </p>
  <ul>
    <li><strong>Cliente M&oacute;vil (.NET MAUI en Android):</strong> Aplicaci&oacute;n nativa multiplataforma orientada al cliente final para autenticaci&oacute;n persistente, cat&aacute;logo interactivo de servicios, selecci&oacute;n de estilistas, agenda de citas y canal de mensajer&iacute;a con el atelier.</li>
    <li><strong>Panel Administrativo Web (Alpine.js + Tailwind CSS):</strong> Plataforma web reactiva de escritorio orientada a recepcionistas, estilistas y gerentes del sal&oacute;n para control de agenda operativa diaria, creaci&oacute;n de citas manuales, administraci&oacute;n de stock de insumos y m&eacute;tricas en vivo.</li>
    <li><strong>Capa Backend & Persistencia (Spring Boot REST API + Supabase PostgreSQL Realtime):</strong> Microservicios de negocio, persistencia relacional transaccional, autenticaci&oacute;n JWT, directivas de seguridad a nivel de fila (<em>Row Level Security &mdash; RLS</em>) y bus de eventos reactivo mediante WebSockets.</li>
  </ul>

  <h3>1.1.1 Alcance (Elementos Cr&iacute;ticos Bajo Prueba)</h3>
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 25%;">Identificador / M&oacute;dulo</th>
        <th style="width: 30%;">Tecnolog&iacute;as Base</th>
        <th style="width: 45%;">Elementos Cr&iacute;ticos Espec&iacute;ficos Bajo Prueba</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>M&Oacute;V-01: Autenticaci&oacute;n y Sesi&oacute;n</strong></td>
        <td>.NET MAUI, Android Keystore, Preferences</td>
        <td>Servicio TokenStorageService, persistencia dual s&iacute;ncrona/as&iacute;ncrona, interceptor ErrorDelegatingHandler, ciclo de vida del proceso en memoria (Swipe-Close) y purga en logout.</td>
      </tr>
      <tr>
        <td><strong>M&Oacute;V-02: Navegaci&oacute;n y Shell</strong></td>
        <td>.NET MAUI Shell, CommunityToolkit MVVM</td>
        <td>Rutas relativas y absolutas en AppShell.xaml.cs, comandos r&aacute;pidos (Agendar, Servicios, Solicitudes, Chat), preservaci&oacute;n de pila de estados sin ArgumentException.</td>
      </tr>
      <tr>
        <td><strong>M&Oacute;V-03: Vistas Reactivas de Citas</strong></td>
        <td>XAML Compilado, Converters Fuertemente Tipados</td>
        <td>ClientHomePage, MyAppointmentsPage, exclusi&oacute;n mutua entre tarjeta de cita activa y estado vac&iacute;o (EmptyStateView), resoluci&oacute;n de enlaces sin InvalidCastException.</td>
      </tr>
      <tr>
        <td><strong>WEB-01: Monitoreo y Dashboard</strong></td>
        <td>Alpine.js, Tailwind CSS, Vercel Edge</td>
        <td>C&aacute;lculo din&aacute;mico de KPIs sin valores simulados, banner de jornada, centro reactivo de notificaciones y alertas de stock cr&iacute;tico.</td>
      </tr>
      <tr>
        <td><strong>WEB-02: Agenda Operativa</strong></td>
        <td>Alpine.js, Supabase JS SDK REST</td>
        <td>Filtro inicial Citas de Hoy, visualizaci&oacute;n de historial completo (Ver Todo), selector de fecha por datepicker con query params, modal de nueva cita con folio #SHU-2026-XXXX.</td>
      </tr>
      <tr>
        <td><strong>WEB-03: Control de Inventario</strong></td>
        <td>Alpine.js, PostgreSQL Functions</td>
        <td>Cat&aacute;logo de insumos de sal&oacute;n, umbrales de stock m&iacute;nimo/&oacute;ptimo, modal de entrada de stock, actualizaci&oacute;n f&iacute;sica y registro auditable en movimientos_inventario.</td>
      </tr>
      <tr>
        <td><strong>WEB-04: Chat & Soporte</strong></td>
        <td>Supabase Realtime (WebSockets)</td>
        <td>Canal de atenci&oacute;n al cliente bidireccional, persistencia en tabla mensajes, sincronizaci&oacute;n en vivo.</td>
      </tr>
      <tr>
        <td><strong>INT-01: API REST & Integraci&oacute;n</strong></td>
        <td>Spring Boot 3.x, Supabase Realtime</td>
        <td>Contratos JSON, latencia de sincronizaci&oacute;n m&oacute;vil &lt;-&gt; web inferior a 5 segundos, integridad transaccional ACID.</td>
      </tr>
      <tr>
        <td><strong>SEC-01: Seguridad y Pol&iacute;ticas RLS</strong></td>
        <td>PostgreSQL 15, Supabase Auth</td>
        <td>Pol&iacute;ticas Row Level Security en tabla citas y mensajes, aislamiento por cliente_id = auth.uid(), mitigaci&oacute;n de ataques BOLA y bloqueo de clave anon_key p&uacute;blica.</td>
      </tr>
    </tbody>
  </table>

  <h3>1.1.2 Fuera de alcance (Exclusiones Formales)</h3>
  <ul>
    <li><strong>Pasarela de cobro en vivo con tarjeta de cr&eacute;dito/d&eacute;bito:</strong> Se valida la emisi&oacute;n y registro de reservas con simulaci&oacute;n de cobro en efectivo o transferencia verificada en recepci&oacute;n.</li>
    <li><strong>Compatibilidad con sistema operativo iOS:</strong> Esta fase de evaluaci&oacute;n est&aacute; delimitada exclusivamente para la arquitectura Android SDK 34 sobre .NET 9.</li>
    <li><strong>M&oacute;dulos de n&oacute;mina o contabilidad fiscal externa:</strong> No forman parte del alcance de la presente versi&oacute;n acad&eacute;mica de evaluaci&oacute;n.</li>
  </ul>

  <h2>1.2 Objetivo de Calidad</h2>
  <p>
    El objetivo de calidad fundamental es certificar que Shushine Studio alcanza est&aacute;ndares verificables de robustez, seguridad y rendimiento t&eacute;cnico antes de su despliegue en el sal&oacute;n. Se fijan las siguientes metas cuantificables:
  </p>
  <ol>
    <li><strong>Estabilidad del Proceso M&oacute;vil:</strong> Cero cierres forzosos (crashes) ante muerte de proceso por el gestor de memoria de Android (HyperOS/MIUI), logrando la recuperaci&oacute;n de credenciales en un tiempo inferior a <strong>10 ms</strong> mediante lectura s&iacute;ncrona en AppShell.</li>
    <li><strong>Latencia Omnicanal en Vivo:</strong> Sincronizaci&oacute;n de reservas creadas desde la app m&oacute;vil hacia la pantalla de recepci&oacute;n web en un tiempo inferior a <strong>5 segundos</strong> v&iacute;a WebSockets de Supabase sin requerir refresco manual de la p&aacute;gina (F5).</li>
    <li><strong>Aislamiento Estricto de Datos (RLS):</strong> Cumplimiento del <strong>100%</strong> de denegaci&oacute;n ante peticiones REST que intenten acceder o modificar citas de otros clientes portando tokens de terceros o claves an&oacute;nimas (mitigaci&oacute;n OWASP API1 &mdash; BOLA).</li>
    <li><strong>Completitud de Dise&ntilde;o de Pruebas:</strong> Cobertura del <strong>100%</strong> de los 16 casos de prueba t&eacute;cnicos especificados en el presente plan.</li>
    <li><strong>Cero Tolerancia a Defectos Cr&iacute;ticos:</strong> Cero (0) defectos abiertos clasificados en Severidad 1 (Bloqueante) o Severidad 2 (Cr&iacute;tico) al finalizar el ciclo de pruebas.</li>
    <li><strong>Aprobaci&oacute;n en Severidades Menores:</strong> Tasa de &eacute;xito m&iacute;nima del <strong>95%</strong> en casos de Severidad 3 y 4 con planes de correcci&oacute;n calendarizados.</li>
  </ol>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 1.3 ==================== -->
  <h2>1.3 Roles y Responsabilidades</h2>
  <p>
    En apego a la cl&aacute;usula 5.3 de la norma <strong>ISO 9001:2015</strong> (Roles, responsabilidades y autoridades), la autor&iacute;a, dise&ntilde;o y especificaci&oacute;n t&eacute;cnica del plan fue distribuida formalmente entre los 4 integrantes del equipo de aseguramiento de calidad (QA):
  </p>

  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 25%;">Integrante</th>
        <th style="width: 27%;">Rol T&eacute;cnico Asignado</th>
        <th style="width: 33%;">M&oacute;dulos y Secciones a su Cargo</th>
        <th style="width: 15%; text-align: center;">Casos Dise&ntilde;ados</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Alex Fernando Alfaro Diaz</strong></td>
        <td>QA Lead &amp; Estrategia Metodol&oacute;gica</td>
        <td>Secciones 1, 2.3, 2.4, 2.5, 3 y Gobernanza: Marco normativo ISO 29119, pir&aacute;mide de pruebas, criterios DoD, escala de severidades (S1-S4), gesti&oacute;n de riesgos con SLAs cuantitativos y sign-off final.</td>
        <td style="text-align: center;">Gobernanza Global</td>
      </tr>
      <tr>
        <td><strong>Cristian Jordan</strong></td>
        <td>QA Frontend M&oacute;vil &amp; Usabilidad Android</td>
        <td>Secciones 1.1.1 (MOV), 4 y 2.1 (CP-MOB): Plataforma m&oacute;vil Android, herramientas ADB y Scrcpy, y dise&ntilde;o exhaustivo de los 6 casos de prueba del cliente m&oacute;vil .NET MAUI.</td>
        <td style="text-align: center;"><strong>CP-MOB-01 al CP-MOB-06</strong><br>(6 casos)</td>
      </tr>
      <tr>
        <td><strong>Rodrigo Cortez</strong></td>
        <td>QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</td>
        <td>Secciones 1.1.1 (WEB), 4 y 2.1 (CP-WEB): Plataforma web DevTools, auditor&iacute;a de red REST, gesti&oacute;n operativa de agenda, inventario del sal&oacute;n y los 6 casos de prueba del panel administrativo.</td>
        <td style="text-align: center;"><strong>CP-WEB-01 al CP-WEB-06</strong><br>(6 casos)</td>
      </tr>
      <tr>
        <td><strong>Cornelio Martinez</strong></td>
        <td>QA Backend, Integraci&oacute;n &amp; Seguridad RLS</td>
        <td>Secciones 1.1.1 (INT/SEC), 4.2 y 2.1 (CP-INT/SEC): Datos de l&iacute;nea base, pruebas de sincronizaci&oacute;n omnicanal en tiempo real, canales WebSockets Supabase y directivas Row Level Security (RLS).</td>
        <td style="text-align: center;"><strong>CP-INT-01 a 03 y CP-SEC-01</strong><br>(4 casos)</td>
      </tr>
    </tbody>
  </table>

  <h4>Matriz de Asignaci&oacute;n de Responsabilidades (RACI)</h4>
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 30%;">Rol / Integrante</th>
        <th style="width: 14%; text-align: center;">Planificaci&oacute;n</th>
        <th style="width: 14%; text-align: center;">Casos Gherkin</th>
        <th style="width: 14%; text-align: center;">Entornos</th>
        <th style="width: 14%; text-align: center;">Gesti&oacute;n Defectos</th>
        <th style="width: 14%; text-align: center;">Auditor&iacute;a RLS</th>
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
        <td><strong>QA Frontend M&oacute;vil & Usabilidad (Cristian Jordan)</strong></td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">I</td>
      </tr>
      <tr>
        <td><strong>QA Panel Web & Operaci&oacute;n de Sal&oacute;n (Rodrigo Cortez)</strong></td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">I</td>
      </tr>
      <tr>
        <td><strong>QA Backend, Integraci&oacute;n & Seguridad (Cornelio Martinez)</strong></td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">R</td>
        <td style="text-align: center;">C</td>
        <td style="text-align: center;">R</td>
      </tr>
    </tbody>
  </table>
  <p style="font-size: 8pt; color: #444444; margin-top: 2pt;">
    *Referencias: R = Responsable de ejecuci&oacute;n/dise&ntilde;o, A = Aprobador final (Accountable), C = Consultado t&eacute;cnico, I = Informado.
  </p>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 2 ==================== -->
  <h1>2. METODOLOG&Iacute;A DE PRUEBA</h1>

  <h2>2.1 Compendio</h2>
  <p>
    La metodolog&iacute;a adopta el modelo de la Pir&aacute;mide de Pruebas de Mike Cohn estructurado conforme a la norma <strong>ISO/IEC/IEEE 29119-3</strong>:
  </p>

  <div class="pyramid-container">
    <div class="pyramid-tier tier-3">
      <div class="tier-title">Nivel 3: Pruebas de Aceptaci&oacute;n del Sistema y End-to-End (Black-Box)</div>
      <div class="tier-desc">Flujos de usuario en dispositivo Android f&iacute;sico real, consola de recepci&oacute;n web y sincronizaci&oacute;n omnicanal en vivo.</div>
    </div>
    <div class="pyramid-tier tier-2">
      <div class="tier-title">Nivel 2: Pruebas de Integraci&oacute;n y Contratos de Servicio (Gray-Box)</div>
      <div class="tier-desc">Validaci&oacute;n de endpoints REST Spring Boot, pol&iacute;ticas RLS en PostgreSQL, triggers transaccionales y WebSockets Supabase.</div>
    </div>
    <div class="pyramid-tier tier-1">
      <div class="tier-title">Nivel 1: Pruebas Unitarias y Estabilidad de Componentes (White-Box)</div>
      <div class="tier-desc">Servicio de persistencia de tokens, ViewModels .NET MAUI (xUnit), validadores reactivos y converters tipados.</div>
    </div>
  </div>

  <p>
    A continuaci&oacute;n se presenta la especificaci&oacute;n at&oacute;mica de los <strong>16 casos de prueba t&eacute;cnicos</strong> dise&ntilde;ados para certificar los tres subsistemas bajo la plantilla formal BDD Gherkin (<em>Dado... Cuando... Entonces...</em>):
  </p>

  <!-- ==================== CASOS DE PRUEBA: 2.1.1 MÓVIL ==================== -->
  <h3>2.1.1 M&oacute;dulo App M&oacute;vil Cliente (.NET MAUI) &mdash; Dise&ntilde;ado por: Cristian Jordan</h3>

  <!-- CP-MOB-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-01: Persistencia de Sesi&oacute;n ante Muerte de Proceso Android (Swipe-to-Close)</span>
      <span>NIVEL: UAT | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend M&oacute;vil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Verificar que el token JWT persista en almacenamiento dual (SecureStorage + Preferences) y se recupere sincr&oacute;nicamente tras el cierre forzoso de la aplicaci&oacute;n por el sistema operativo.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Cliente autenticado exitosamente en el dispositivo m&oacute;vil con sesi&oacute;n activa.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Abrir la aplicaci&oacute;n y verificar que se visualiza la pantalla principal (ClientHomePage).</li>
          <li>Presionar el bot&oacute;n de inicio de Android para enviar la aplicaci&oacute;n a segundo plano.</li>
          <li>Abrir la bandeja de aplicaciones recientes del sistema operativo.</li>
          <li>Deslizar la tarjeta de la aplicaci&oacute;n Shushine Studio hacia afuera (Swipe-to-Close) finalizando el proceso en memoria.</li>
          <li>Pulsar nuevamente el &iacute;cono de la aplicaci&oacute;n en el launcher de Android.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La aplicaci&oacute;n inicia directamente en ClientHomePage. La validaci&oacute;n sincr&oacute;nica HasValidTokenSync() en el constructor de AppShell determina la existencia del token en menos de 10 ms. No se muestra la pantalla de inicio de sesi&oacute;n ni se le solicita la contrase&ntilde;a al usuario.
      </div>
    </div>
  </div>

  <!-- CP-MOB-02 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-02: Cierre de Sesi&oacute;n Voluntario y Purga de Credenciales Locales</span>
      <span>NIVEL: SEGURIDAD | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend M&oacute;vil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Garantizar que el cierre de sesi&oacute;n voluntario elimine todos los identificadores de sesi&oacute;n en almacenamiento local y permita ingresar inmediatamente con otra cuenta sin fuga de datos.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Sesi&oacute;n activa en el dispositivo con Usuario_A.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Navegar a la pesta&ntilde;a "Perfil" en la barra de navegaci&oacute;n inferior.</li>
          <li>Pulsar el bot&oacute;n "Cerrar sesi&oacute;n".</li>
          <li>En el cuadro de di&aacute;logo de confirmaci&oacute;n, pulsar "Confirmar".</li>
          <li>Verificar la transici&oacute;n inmediata a la pantalla de inicio de sesi&oacute;n (LoginPage).</li>
          <li>Ingresar credenciales v&aacute;lidas de Usuario_B y pulsar "Iniciar sesi&oacute;n".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        El m&eacute;todo ClearAsync() purga por completo SecureStorage y Preferences. No se detecta fuga de tokens entre cuentas. La aplicaci&oacute;n inicia sesi&oacute;n con los datos espec&iacute;ficos de Usuario_B.
      </div>
    </div>
  </div>

  <div class="page-break"></div>

  <!-- CP-MOB-03 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-03: Navegaci&oacute;n Shell y Prevenci&oacute;n de Excepciones de Enrutamiento</span>
      <span>NIVEL: UAT | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend M&oacute;vil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Comprobar que los botones de acci&oacute;n r&aacute;pida en la pantalla de inicio transicionen hacia sus respectivas rutas sin arrojar ArgumentException ni cierres forzosos.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Usuario posicionado en ClientHomePage.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el bot&oacute;n "Agendar una cita". Verificar navegaci&oacute;n a StylistSelectionPage y retroceder.</li>
          <li>Pulsar el bot&oacute;n "Servicios". Verificar navegaci&oacute;n a ServicesPage y retroceder.</li>
          <li>Pulsar el bot&oacute;n "Solicitudes". Verificar navegaci&oacute;n a RequestsPage y retroceder.</li>
          <li>Pulsar el bot&oacute;n "Chat Atelier". Verificar navegaci&oacute;n a ClientChatPage y retroceder.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Cada bot&oacute;n redirige a su ruta Shell registrada. La pila de navegaci&oacute;n opera limpiamente sin arrojar excepciones de ruta inexistente y la aplicaci&oacute;n permanece estable.
      </div>
    </div>
  </div>

  <!-- CP-MOB-04 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-04: Exclusi&oacute;n Mutua en Renderizado de 'Tu Pr&oacute;xima Cita'</span>
      <span>NIVEL: UI / ESTABILIDAD | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend M&oacute;vil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Validar que la secci&oacute;n de pr&oacute;xima cita no presente solapamiento visual entre la tarjeta de datos de la cita y el indicador de ausencia de citas.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Usuario cliente con exactamente 1 cita confirmada registrada en base de datos.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Abrir la pantalla principal de la aplicaci&oacute;n m&oacute;vil.</li>
          <li>Inspeccionar la secci&oacute;n superior rotulada "Tu Pr&oacute;xima Cita".</li>
          <li>Verificar la propiedad IsVisible del contenedor de la cita y del contenedor de estado vac&iacute;o (EmptyStateView).</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Se exhibe &uacute;nicamente la tarjeta con los datos de la cita confirmada (servicio, estilista, fecha, hora). La tarjeta "Sin citas pendientes" permanece con IsVisible = False sin ocupar espacio en pantalla. Ambas vistas son mutuamente excluyentes.
      </div>
    </div>
  </div>

  <!-- CP-MOB-05 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-MOB-05: Transici&oacute;n al Detalle de Cita sin Fallos de Casteo XAML</span>
      <span>NIVEL: ESTABILIDAD | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend M&oacute;vil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Validar que la navegaci&oacute;n hacia AppointmentDetailPage resuelva correctamente los convertidores de enlaces compilados sin disparar InvalidCastException.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Tarjeta de pr&oacute;xima cita visible en ClientHomePage.</span></div>
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
      <span>CASO CP-MOB-06: Gesti&oacute;n de Pesta&ntilde;a 'Mis Citas' (Pr&oacute;ximas vs Historial)</span>
      <span>NIVEL: UAT | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cristian Jordan</strong> &mdash; QA Frontend M&oacute;vil &amp; Usabilidad Android</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Comprobar la alternancia de filtros entre citas programadas y pasadas con soporte de gesto Pull-to-Refresh.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Cliente con historial de citas activas y pasadas en base de datos.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Seleccionar la pesta&ntilde;a "Citas" en la barra de navegaci&oacute;n inferior.</li>
          <li>Verificar la lista bajo el selector predeterminado "Pr&oacute;ximas".</li>
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

  <!-- ==================== CASOS DE PRUEBA: 2.1.2 WEB ==================== -->
  <h3>2.1.2 M&oacute;dulo Panel Administrativo Web (Alpine.js) &mdash; Dise&ntilde;ado por: Rodrigo Cortez</h3>

  <!-- CP-WEB-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-01: M&oacute;dulo de Chat & Soporte en Tiempo Real</span>
      <span>NIVEL: UAT / INTEGRACI&Oacute;N | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Validar que el m&oacute;dulo de Chat y Soporte se encuentre disponible en el men&uacute; de navegaci&oacute;n y permita la inserci&oacute;n de respuestas en Supabase.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Administrador autenticado en el panel web.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Localizar el men&uacute; de navegaci&oacute;n lateral izquierdo.</li>
          <li>Hacer clic sobre la opci&oacute;n "Chat & Soporte".</li>
          <li>Seleccionar una conversaci&oacute;n de cliente en el listado lateral.</li>
          <li>Redactar un mensaje de prueba ("Estimada cliente, su cita est&aacute; confirmada").</li>
          <li>Presionar el bot&oacute;n "Enviar" o la tecla Enter.</li>
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
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Asegurar que la vista inicial de la agenda filtre &uacute;nicamente las citas de la jornada actual para optimizar la operaci&oacute;n del recepcionista.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Existen citas registradas para hoy, d&iacute;as anteriores y d&iacute;as posteriores.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Navegar a la secci&oacute;n "Agenda & Citas" desde el men&uacute; lateral.</li>
          <li>Inspeccionar la tabla principal de citas y el encabezado de estado.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La tabla filtra autom&aacute;ticamente y expone &uacute;nicamente las citas cuya fecha coincida con la fecha actual del sistema. Se visualiza un badge de resumen que cuantifica las citas del d&iacute;a.
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
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Permitir al operador consultar el historial completo o filtrar por fecha mediante datepicker y filtros de estado.</span></div>
      <div class="field-row"><span class="field-label">Mecanismo T&eacute;cnico:</span><span class="field-value">Petici&oacute;n HTTP a Supabase REST con par&aacute;metros .select('*') y combinaci&oacute;n condicional: .eq('fecha', fechaSeleccionada) y .eq('estado', estadoFiltro) en servidor, combinada con renderizado reactivo Alpine.js en memoria.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">M&oacute;dulo "Agenda & Citas" desplegado.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el bot&oacute;n "Ver Todo (Historial)". Verificar el contenido de la tabla.</li>
          <li>Hacer clic en el campo "Filtrar por D&iacute;a" y seleccionar una fecha diferente en el calendario.</li>
          <li>Alternar el filtro de estado entre "Todas", "Confirmadas" y "Pendientes".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Al pulsar "Ver Todo", la tabla muestra todas las citas hist&oacute;ricas sin filtro de fecha. Al escoger una fecha en el datepicker, la tabla conmuta en vivo mostrando exclusivamente los registros coincidentes mediante el par&aacute;metro .eq('fecha', ...).
      </div>
    </div>
  </div>

  <!-- CP-WEB-04 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-04: Creaci&oacute;n de Nueva Cita desde el Panel Web</span>
      <span>NIVEL: TRANSACCIONAL | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Comprobar la inserci&oacute;n de una reserva manual desde el modal administrativo con asignaci&oacute;n de folio &uacute;nico.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">M&oacute;dulo de Agenda abierto en el panel web.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el bot&oacute;n "+ Nueva Cita".</li>
          <li>Completar los campos del modal: Cliente, Servicio, Estilista, Fecha y Hora.</li>
          <li>Pulsar el bot&oacute;n "Guardar Cita".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Se genera un c&oacute;digo de cita &uacute;nico con prefijo #SHU-2026-XXXX. El registro se inserta exitosamente en la tabla citas de Supabase, se presenta una notificaci&oacute;n flotante (Toast) y la tabla de agenda se actualiza autom&aacute;ticamente.
      </div>
    </div>
  </div>

  <div class="page-break"></div>

  <!-- CP-WEB-05 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-05: Registro de Entrada de Stock en Inventario del Sal&oacute;n</span>
      <span>NIVEL: GESTI&Oacute;N INVENTARIO | PRIORIDAD: P2 (MEDIA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Validar que el ingreso de insumos incremente el stock f&iacute;sico y registre el movimiento de auditor&iacute;a correspondiente.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">M&oacute;dulo "Inventario del Sal&oacute;n" abierto.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Pulsar el bot&oacute;n "Entrada de Stock".</li>
          <li>Seleccionar un producto del cat&aacute;logo de insumos.</li>
          <li>Digitar una cantidad de ingreso (ej. 10 unidades) y motivo ("Reabastecimiento de proveedor").</li>
          <li>Presionar "Guardar Entrada".</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Se crea un registro en la tabla movimientos_inventario de tipo 'entrada'. La cantidad en existencia del producto aumenta en exactamente 10 unidades y el badge de estado se recalcula autom&aacute;ticamente a 'Óptimo'.
      </div>
    </div>
  </div>

  <!-- CP-WEB-06 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-WEB-06: M&eacute;tricas Ejecutivas y KPIs 100% Reales</span>
      <span>NIVEL: INTEGRIDAD DATOS | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Rodrigo Cortez</strong> &mdash; QA Panel Web &amp; Operaci&oacute;n de Sal&oacute;n</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Confirmar que el Dashboard Ejecutivo no contenga valores fijos ni simulados y responda &uacute;nicamente a data viva de Supabase.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Dashboard principal abierto.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Inspeccionar las 4 tarjetas m&eacute;tricas superiores (Ingresos, Citas del D&iacute;a, Clientes VIP, Productos Cr&iacute;ticos).</li>
          <li>Abrir la consola de red de DevTools (F12) y constatar las peticiones REST hacia Supabase.</li>
          <li>Hacer clic en la campana de notificaciones.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        Los montos y contadores se derivan de funciones de agregaci&oacute;n reales (SUM, COUNT). Las alertas emitidas corresponden estrictamente a productos bajo el stock m&iacute;nimo o citas pendientes reales.
      </div>
    </div>
  </div>

  <!-- ==================== CASOS DE PRUEBA: 2.1.3 E2E ==================== -->
  <h3>2.1.3 M&oacute;dulo Integraci&oacute;n Omnicanal End-to-End (E2E) &mdash; Dise&ntilde;ado por: Cornelio Martinez</h3>

  <!-- CP-INT-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-INT-01: Reflejo en Panel Web de Cita Agendada desde App M&oacute;vil</span>
      <span>NIVEL: INTEGRACI&Oacute;N E2E | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integraci&oacute;n &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Verificar la propagaci&oacute;n en tiempo real de una reserva creada desde el smartphone del cliente hacia la agenda web del operador.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">App m&oacute;vil abierta en el celular y Panel Web abierto simult&aacute;neamente en la computadora.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>En el tel&eacute;fono m&oacute;vil, completar una reserva de servicio ("Corte & Brushing") y confirmar.</li>
          <li>Medir el tiempo transcurrido hasta que el registro se visualice en el Panel Web sin recargar la p&aacute;gina (F5).</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La cita aparece en la tabla de la agenda web en un lapso inferior a 5 segundos con el badge de origen "App M&oacute;vil" y se emite la notificaci&oacute;n visual correspondiente.
      </div>
    </div>
  </div>

  <!-- CP-INT-02 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-INT-02: Reflejo en App M&oacute;vil de Cita Agendada desde Panel Web</span>
      <span>NIVEL: INTEGRACI&Oacute;N E2E | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integraci&oacute;n &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Confirmar que una cita asignada por el recepcionista aparezca de inmediato en la aplicaci&oacute;n del cliente.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Cliente con sesi&oacute;n abierta en la aplicaci&oacute;n m&oacute;vil.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>En el Panel Web, el recepcionista crea una nueva cita para el cliente especificado.</li>
          <li>El cliente abre su app m&oacute;vil o refresca la vista de inicio.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La secci&oacute;n "Tu Pr&oacute;xima Cita" y la pesta&ntilde;a "Mis Citas" del cliente muestran la nueva reserva con todos los datos sincronizados.
      </div>
    </div>
  </div>

  <!-- CP-INT-03 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-INT-03: Mensajer&iacute;a Bidireccional en Tiempo Real (Chat M&oacute;vil &lt;-&gt; Soporte Web)</span>
      <span>NIVEL: INTEGRACI&Oacute;N E2E | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integraci&oacute;n &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Validar la comunicaci&oacute;n instant&aacute;nea mediante el canal de WebSockets de Supabase Realtime.</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Cliente en vista Chat de la app m&oacute;vil; Administrador en m&oacute;dulo Chat & Soporte del panel web.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>El cliente env&iacute;a el mensaje: "&iquest;Tienen disponibilidad de cambio de horario?".</li>
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

  <!-- ==================== CASOS DE PRUEBA: 2.1.4 SEGURIDAD ==================== -->
  <h3>2.1.4 M&oacute;dulo Seguridad y Pol&iacute;ticas Row Level Security (RLS) &mdash; Dise&ntilde;ado por: Cornelio Martinez</h3>

  <!-- CP-SEC-01 -->
  <div class="test-case-card">
    <div class="test-case-header">
      <span>CASO CP-SEC-01: Verificaci&oacute;n de Aislamiento y Violaci&oacute;n de Pol&iacute;ticas RLS en Supabase</span>
      <span>NIVEL: SEGURIDAD | PRIORIDAD: P1 (ALTA)</span>
    </div>
    <div class="test-case-body">
      <div class="field-row"><span class="field-label">Especialista Responsable:</span><span class="field-value"><strong>Cornelio Martinez</strong> &mdash; QA Backend, Integraci&oacute;n &amp; Seguridad RLS</span></div>
      <div class="field-row"><span class="field-label">Objetivo T&eacute;cnico:</span><span class="field-value">Garantizar que las directivas RLS en PostgreSQL impidan que un usuario consulte, modifique o elimine reservas pertenecientes a otro usuario utilizando tokens ajenos o la clave p&uacute;blica anon_key.</span></div>
      <div class="field-row"><span class="field-label">Norma Asociada:</span><span class="field-value">ISO 9001:2015 Cl&aacute;usula 8.2 / OWASP Top 10 API Security (BOLA &mdash; Broken Object Level Authorization).</span></div>
      <div class="field-row"><span class="field-label">Precondici&oacute;n:</span><span class="field-value">Usuario_A posee citas registradas con su ID. Usuario_B posee token JWT v&aacute;lido propio.</span></div>
      <div class="field-row"><span class="field-label">Procedimiento:</span>
        <ol class="steps-list">
          <li>Desde un cliente REST (Postman), interceptar el token de autenticaci&oacute;n de Usuario_B.</li>
          <li>Emitir petici&oacute;n GET /rest/v1/citas?cliente_id=eq.{ID_Usuario_A} portando el Bearer token de Usuario_B.</li>
          <li>Emitir petici&oacute;n PATCH intentando alterar el estado de la cita de Usuario_A a "Cancelada".</li>
          <li>Emitir petici&oacute;n GET /citas utilizando &uacute;nicamente la anon_key p&uacute;blica sin cabecera de autorizaci&oacute;n de usuario.</li>
        </ol>
      </div>
      <div class="expected-box">
        <strong>RESULTADO ESPERADO FORMAL:</strong><br>
        La consulta GET de Usuario_B retorna un arreglo vac&iacute;o [] o HTTP 403 Forbidden. La mutaci&oacute;n PATCH no altera ninguna fila (0 filas afectadas). La consulta con anon_key sin sesi&oacute;n es bloqueada por las directivas de seguridad a nivel de fila (auth.uid() = cliente_id).
      </div>
    </div>
  </div>

  <!-- ==================== SECCIÓN 2.2 ==================== -->
  <h2>2.2 Niveles de prueba</h2>
  <p>
    El proceso de aseguramiento de calidad del software estructura las pruebas en tres niveles operacionales:
  </p>
  <ul>
    <li><strong>Nivel 1 &mdash; Pruebas Unitarias (Caja Blanca):</strong> Validaci&oacute;n aislada de servicios, convertidores de enlaces XAML compilados y l&oacute;gica de ViewModels sin dependencias de red vivas. Herramientas: xUnit, Moq, FluentAssertions.</li>
    <li><strong>Nivel 2 &mdash; Pruebas de Integraci&oacute;n (Caja Gris):</strong> Verificaci&oacute;n de contratos JSON REST, inyecci&oacute;n de tokens JWT, integridad referencial de base de datos relacional y directivas Row Level Security (RLS) en Supabase. Herramientas: Postman, Newman, Supabase SQL Editor.</li>
    <li><strong>Nivel 3 &mdash; Pruebas de Sistema y Aceptaci&oacute;n UAT (Caja Negra):</strong> Ejecuci&oacute;n de escenarios completos basados en la perspectiva del usuario final seg&uacute;n especificaciones formales Gherkin (Dado / Cuando / Entonces). Herramientas: Dispositivos f&iacute;sicos Android, Google Chrome DevTools, Playwright.</li>
    <li><strong>Pruebas No Funcionales:</strong> Seguridad y cifrado de tokens en reposo (Android KeyStore), mitigaci&oacute;n de accesos indebidos mediante RLS, y resiliencia ante p&eacute;rdida de conexi&oacute;n y concurrencia de reservas sobre estilistas (UNIQUE(estilista_id, fecha, hora)).</li>
  </ul>

  <!-- ==================== SECCIÓN 2.3 ==================== -->
  <h2>2.3 Clasificaci&oacute;n de errores &mdash; Alex Fernando Alfaro Diaz</h2>
  <p><em>Especialista Responsable: Alex Fernando Alfaro Diaz (QA Lead &amp; Gobernanza de Calidad)</em></p>
  <p>
    Todo desv&iacute;o detectado durante la ejecuci&oacute;n futura del plan se registrar&aacute; en el gestor de incidentes de Azure DevOps bajo la siguiente escala formal:
  </p>

  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 20%;">Severidad</th>
        <th style="width: 35%;">Definici&oacute;n T&eacute;cnica</th>
        <th style="width: 15%; text-align: center;">SLA de Resoluci&oacute;n</th>
        <th style="width: 30%;">Ejemplo Pr&aacute;ctico en Shushine Studio</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>S1: Bloqueante (Blocker)</strong></td>
        <td>El sistema queda inoperativo; crash de la aplicaci&oacute;n, bloqueo de base de datos o fallo en el inicio de sesi&oacute;n.</td>
        <td style="text-align: center;">Inmediato (&lt; 4h)</td>
        <td>Cierre forzoso de la app al pulsar una cita o fallo al arrancar.</td>
      </tr>
      <tr>
        <td><strong>S2: Cr&iacute;tico (Critical)</strong></td>
        <td>P&eacute;rdida de funcionalidad principal sin alternativa; fallo al insertar citas o inconsistencia de stock.</td>
        <td style="text-align: center;">&lt; 24h</td>
        <td>El bot&oacute;n de agendar no inserta el registro en Supabase.</td>
      </tr>
      <tr>
        <td><strong>S3: Mayor (Major)</strong></td>
        <td>Funcionalidad relevante afectada pero con procedimiento alternativo o manual disponible.</td>
        <td style="text-align: center;">&lt; 48h</td>
        <td>El datepicker de agenda requiere presionar dos veces para refrescar.</td>
      </tr>
      <tr>
        <td><strong>S4: Menor (Cosmetic)</strong></td>
        <td>Detalle cosm&eacute;tico, error tipogr&aacute;fico o espaciado visual que no afecta la l&oacute;gica ni la integridad.</td>
        <td style="text-align: center;">Pr&oacute;ximo Sprint</td>
        <td>Desalineaci&oacute;n de un icono en la barra lateral del panel web.</td>
      </tr>
    </tbody>
  </table>

  <!-- ==================== SECCIÓN 2.4 ==================== -->
  <h2>2.4 Criterios de suspensi&oacute;n y requisitos de reanudaci&oacute;n</h2>
  <div class="formal-box">
    <div class="formal-box-title">Criterios de Suspensi&oacute;n de Pruebas</div>
    <p>
      La ejecuci&oacute;n de pruebas se suspender&aacute; de inmediato si se detecta un defecto de <strong>Severidad 1 (Bloqueante)</strong> que impida el arranque de la aplicaci&oacute;n (Crash on Launch), la indisponibilidad total de la base de datos Supabase o la corrupci&oacute;n masiva de los datos de l&iacute;nea base.
    </p>
  </div>
  <div class="formal-box">
    <div class="formal-box-title">Requisitos de Reanudaci&oacute;n</div>
    <p>
      Las actividades de prueba se reanudar&aacute;n cuando el equipo de desarrollo libere un hotfix con validaci&oacute;n unitaria de no-regresi&oacute;n y se certifique el restablecimiento completo de los servicios de base de datos.
    </p>
  </div>

  <!-- ==================== SECCIÓN 2.5 ==================== -->
  <h2>2.5 Completitud de la prueba (Definition of Done)</h2>
  <p>Para certificar la completitud de la prueba y autorizar el pase a producci&oacute;n se exige:</p>
  <ol>
    <li>100% de los 16 casos de prueba planificados ejecutados y documentados en Azure Boards.</li>
    <li>Cero (0) defectos abiertos clasificados en Severidad 1 (Bloqueante) y Severidad 2 (Cr&iacute;tico).</li>
    <li>Tasa de aprobaci&oacute;n m&iacute;nima del 95% en defectos menores (Severidad 3 y 4) con plan de mitigaci&oacute;n calendarizado.</li>
    <li>Consistencia transaccional confirmada en PostgreSQL sin registros hu&eacute;rfanos en tablas de citas e inventario.</li>
  </ol>

  <div class="page-break"></div>

  <!-- ==================== SECCIÓN 3 ==================== -->
  <h1>3. ENTREGABLES DE LA PRUEBA</h1>
  <p>Al concluir el ciclo de aseguramiento de calidad, el equipo de testing entregar&aacute; los siguientes artefactos formales:</p>
  <ol>
    <li><strong>Plan Maestro de Pruebas de Software (STP-SHU-2026-V3):</strong> El presente documento de planificaci&oacute;n, dise&ntilde;o y especificaci&oacute;n metodol&oacute;gica bajo est&aacute;ndar ISO/IEC/IEEE 29119-3 e IEEE 829.</li>
    <li><strong>Registro de Trazabilidad en Azure DevOps Boards:</strong> Casos de prueba vinculados con la &Eacute;pica maestra #162 en el proyecto Testeo (5 Features, 19 PBIs y 35 Tasks).</li>
    <li><strong>Registro de Incidentes y Defectos (Bug Reports):</strong> Expedientes t&eacute;cnicos documentados con trazas de adb logcat, consola DevTools y pasos de reproducci&oacute;n.</li>
    <li><strong>Informe Formal de Cierre de Pruebas (Test Summary Report):</strong> Certificaci&oacute;n final que valida el cumplimiento de los criterios de salida para pase a producci&oacute;n.</li>
  </ol>

  <!-- ==================== SECCIÓN 4 ==================== -->
  <h1>4. NECESIDADES DE RECURSOS Y MEDIO AMBIENTE</h1>

  <h2>4.1 Herramientas de prueba</h2>
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 28%;">Herramienta</th>
        <th style="width: 24%;">Versi&oacute;n / Tipo</th>
        <th style="width: 48%;">Prop&oacute;sito en el Proceso de Pruebas</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Android Debug Bridge (ADB)</strong></td>
        <td>Android SDK Tools 34+</td>
        <td>Captura de trazas del sistema en vivo (adb logcat), diagn&oacute;stico de procesos y despliegue de paquetes APK.</td>
      </tr>
      <tr>
        <td><strong>Scrcpy</strong></td>
        <td>v2.4+</td>
        <td>Control e inspecci&oacute;n en espejo del dispositivo Android f&iacute;sico para registro de interacciones visuales.</td>
      </tr>
      <tr>
        <td><strong>Google Chrome DevTools</strong></td>
        <td>v120+</td>
        <td>Inspecci&oacute;n reactiva del DOM Alpine.js, auditor&iacute;a de peticiones REST de red y tiempos de respuesta.</td>
      </tr>
      <tr>
        <td><strong>Playwright Engine</strong></td>
        <td>Chromium Headless v1.57+</td>
        <td>Automatizaci&oacute;n web de extremo a extremo y generaci&oacute;n de reportes PDF de alta fidelidad.</td>
      </tr>
      <tr>
        <td><strong>Azure DevOps Boards</strong></td>
        <td>Cloud (lalafaro6/Testeo)</td>
        <td>Gesti&oacute;n del backlog de pruebas, trazabilidad de requerimientos y control de incidentes.</td>
      </tr>
      <tr>
        <td><strong>Postman / REST Assured</strong></td>
        <td>v10+</td>
        <td>Validaci&oacute;n de contratos JSON, inyecci&oacute;n de tokens JWT y auditor&iacute;a de directivas Row Level Security.</td>
      </tr>
    </tbody>
  </table>

  <h2>4.2 Entorno de prueba</h2>
  <ul>
    <li><strong>Dispositivo M&oacute;vil de Prueba:</strong> Tel&eacute;fono f&iacute;sico Android (HyperOS / MIUI sobre Android 14), 6 GB RAM, pantalla FHD+ (1080x2400 px), conexi&oacute;n Wi-Fi 5 GHz.</li>
    <li><strong>Estaci&oacute;n de Control QA:</strong> Equipo PC Linux Lubuntu / Ubuntu x86_64, 16 GB RAM, terminal de comandos bash y red de alta velocidad.</li>
    <li><strong>Servidor Backend:</strong> Spring Boot 3.x desplegado localmente en puerto 8080.</li>
    <li><strong>Base de Datos en la Nube:</strong> Instancia Supabase PostgreSQL 15 con motor Realtime habilitado para suscripciones WebSockets.</li>
  </ul>

  <h4>Datos de Prueba de L&iacute;nea Base (Test Data Baseline) &mdash; Cornelio Martinez</h4>
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
        <td>Cliente nuevo sin citas previas ni reservas en curso (estado vac&iacute;o).</td>
      </tr>
      <tr>
        <td>Estilistas Activos (3)</td>
        <td>Personal Operativo</td>
        <td>Registrados en cat&aacute;logo con horarios laborales asignados.</td>
      </tr>
      <tr>
        <td>Cat&aacute;logo de Servicios</td>
        <td>Servicios de Belleza</td>
        <td>Corte Dama ($25.00), Balayage ($85.00), Manicura Spa ($18.00), Keratina ($65.00).</td>
      </tr>
    </tbody>
  </table>

  <div class="page-break"></div>

  <!-- ==================== TÉRMINOS / ACRÓNIMOS ==================== -->
  <h1>T&Eacute;RMINOS / ACR&Oacute;NIMOS (GLOSARIO T&Eacute;CNICO)</h1>
  <table class="formal-table">
    <thead>
      <tr>
        <th style="width: 22%;">T&eacute;rmino / Acr&oacute;nimo</th>
        <th style="width: 78%;">Definici&oacute;n T&eacute;cnica Aplicada al Sistema Shushine Studio</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>SUT (System Under Test)</strong></td>
        <td>Sistema Bajo Prueba; soluci&oacute;n integral compuesta por la app m&oacute;vil .NET MAUI, el panel administrativo web y la persistencia en Supabase.</td>
      </tr>
      <tr>
        <td><strong>RLS (Row Level Security)</strong></td>
        <td>Seguridad a Nivel de Fila nativa de PostgreSQL que restringe la visibilidad y mutaci&oacute;n de filas seg&uacute;n la identidad del usuario autenticado (auth.uid() = cliente_id).</td>
      </tr>
      <tr>
        <td><strong>BOLA (Broken Object Level Auth)</strong></td>
        <td>Vulnerabilidad cr&iacute;tica (OWASP API1) en la que un cliente manipula par&aacute;metros de petici&oacute;n para acceder o alterar registros pertenecientes a terceros.</td>
      </tr>
      <tr>
        <td><strong>JWT (JSON Web Token)</strong></td>
        <td>Est&aacute;ndar abierto RFC 7519 para transmisi&oacute;n segura de aserciones de identidad y permisos entre el cliente y el servidor backend.</td>
      </tr>
      <tr>
        <td><strong>MAUI (.NET Multi-platform UI)</strong></td>
        <td>Framework de Microsoft para construir interfaces de usuario nativas multiplataforma utilizando C# y lenguaje XAML compilado.</td>
      </tr>
      <tr>
        <td><strong>ADB (Android Debug Bridge)</strong></td>
        <td>Herramienta de consola de depuraci&oacute;n para comunicarse e interactuar directamente con el dispositivo Android f&iacute;sico.</td>
      </tr>
      <tr>
        <td><strong>BDD (Behavior-Driven Development)</strong></td>
        <td>Metodolog&iacute;a de desarrollo guiada por comportamiento que emplea la sintaxis formal Gherkin (Dado / Cuando / Entonces) para redactar casos un&iacute;vocos.</td>
      </tr>
      <tr>
        <td><strong>UAT (User Acceptance Testing)</strong></td>
        <td>Pruebas de aceptaci&oacute;n del usuario para validar que el sistema responde fielmente a los requerimientos del operador del sal&oacute;n y del cliente final.</td>
      </tr>
      <tr>
        <td><strong>DoD (Definition of Done)</strong></td>
        <td>Criterios formales y rigurosos acordados por el equipo para considerar que el ciclo de pruebas ha alcanzado su culminaci&oacute;n satisfactoria.</td>
      </tr>
      <tr>
        <td><strong>SLA (Service Level Agreement)</strong></td>
        <td>Acuerdo formal que define el tiempo m&aacute;ximo de respuesta comprometido para resolver defectos seg&uacute;n su nivel de severidad.</td>
      </tr>
      <tr>
        <td><strong>API REST</strong></td>
        <td>Interfaz de programaci&oacute;n fundamentada en la arquitectura de transferencia de estado representacional sobre peticiones HTTP estandarizadas.</td>
      </tr>
      <tr>
        <td><strong>RACI</strong></td>
        <td>Matriz de asignaci&oacute;n de responsabilidades (Responsible, Accountable, Consulted, Informed) para gobernar las actividades del equipo de calidad.</td>
      </tr>
    </tbody>
  </table>

  <!-- ==================== ALINEACIÓN ISO 29119 ==================== -->
  <h2>ALINEACI&Oacute;N CON LA NORMA INTERNACIONAL ISO/IEC/IEEE 29119</h2>
  <p>
    El presente plan adopta formalmente los procesos de la norma internacional <strong>ISO/IEC/IEEE 29119</strong> en tres capas metodol&oacute;gicas:
  </p>
  <ul>
    <li><strong>ISO/IEC/IEEE 29119-1 (Conceptos y Definiciones):</strong> Estandarizaci&oacute;n rigurosa de vocabulario t&eacute;cnico, separaci&oacute;n metodol&oacute;gica entre dise&ntilde;o de pruebas y ejecuci&oacute;n, y gesti&oacute;n preventiva de riesgos t&eacute;cnicos.</li>
    <li><strong>ISO/IEC/IEEE 29119-2 (Procesos de Prueba Din&aacute;micos):</strong> Estructuraci&oacute;n at&oacute;mica de casos de prueba con precondiciones, pasos secuenciales reproducibles y criterios de aceptaci&oacute;n un&iacute;vocos; gesti&oacute;n de defectos vinculada a SLAs cuantitativos.</li>
    <li><strong>ISO/IEC/IEEE 29119-3 (Documentaci&oacute;n de Pruebas):</strong> Cumplimiento &iacute;ntegro del esquema documental del Software Test Plan (STP), garantizando la auditabilidad completa del proceso de ingenier&iacute;a de software ante jurados evaluadores.</li>
  </ul>

  <!-- ==================== FIRMAS Y CONFORMIDAD ==================== -->
  <div class="page-break"></div>
  <h1>FIRMAS Y CONFORMIDAD DEL EQUIPO DE PRUEBAS</h1>
  <p>
    En se&ntilde;al de conformidad t&eacute;cnica con el dise&ntilde;o, alcance, metodolog&iacute;a y responsabilidades establecidas en el presente Plan Maestro de Pruebas de Software (STP-SHU-2026-V3), los integrantes del equipo de Aseguramiento de Calidad de la Escuela Superior Franciscana Especializada (ESFE AGAPE) suscriben el presente documento:
  </p>

  <table class="formal-table" style="margin-top: 15pt;">
    <thead>
      <tr>
        <th style="width: 25%;">Nombre del Integrante</th>
        <th style="width: 25%;">Especialidad T&eacute;cnica Asignada</th>
        <th style="width: 28%;">M&oacute;dulos / Casos Dise&ntilde;ados</th>
        <th style="width: 22%; text-align: center;">Firma / Conformidad</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Alex Fernando Alfaro Diaz</strong></td>
        <td>QA Lead / Estrategia &amp; Riesgos</td>
        <td>Secciones 1, 2.3, 2.4, 2.5, 3 y Gobernanza</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
      <tr>
        <td><strong>Cristian Jordan</strong></td>
        <td>QA Frontend M&oacute;vil .NET MAUI</td>
        <td>Secci&oacute;n 2.1 (CP-MOB-01 al CP-MOB-06)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
      <tr>
        <td><strong>Rodrigo Cortez</strong></td>
        <td>QA Panel Web &amp; Operaciones</td>
        <td>Secci&oacute;n 2.1 (CP-WEB-01 al CP-WEB-06)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
      <tr>
        <td><strong>Cornelio Martinez</strong></td>
        <td>QA Backend, Integraci&oacute;n &amp; RLS</td>
        <td>Secci&oacute;n 2.1 (CP-INT y CP-SEC)</td>
        <td style="text-align: center; height: 35pt; vertical-align: middle;">[ APROBADO CONFORME ]</td>
      </tr>
    </tbody>
  </table>

  <div style="margin-top: 35pt; border-top: 1pt solid #000000; padding-top: 10pt; text-align: center; font-size: 8.5pt;">
    <strong>SHUSHINE STUDIO &mdash; EQUIPO DE ASEGURAMIENTO DE CALIDAD DE SOFTWARE (TESTING QA)</strong><br>
    Alex Fernando Alfaro Diaz &bull; Cristian Jordan &bull; Rodrigo Cortez &bull; Cornelio Martinez<br>
    Escuela Superior Franciscana Especializada &bull; AGAPE / MEGATEC &bull; Ciclo II - 2026
  </div>

</body>
</html>
`;

async function generateInstitutionalPDF() {
    console.log("Iniciando compilación del PDF Institucional ESFE AGAPE...");
    const browser = await pw.chromium.launch({ headless: true });
    
    // Paso 1: Generar Portada (cover.pdf)
    const coverPage = await browser.newPage();
    await coverPage.setContent(coverHtml, { waitUntil: 'networkidle' });
    const coverPdfPath = '/tmp/cover_esfe.pdf';
    await coverPage.pdf({
        path: coverPdfPath,
        format: 'A4',
        printBackground: true,
        displayHeaderFooter: false,
        margin: { top: '20mm', bottom: '20mm', left: '20mm', right: '20mm' }
    });
    console.log("Portada generada en:", coverPdfPath);

    // Paso 2: Generar Cuerpo del Documento (content.pdf) con encabezado y pie de página institucional
    const contentPage = await browser.newPage();
    await contentPage.setContent(contentHtml, { waitUntil: 'networkidle' });
    const contentPdfPath = '/tmp/content_esfe.pdf';
    
    const headerTpl = `
      <div style="font-family: Arial, sans-serif; width: 100%; margin: 0 18mm; display: flex; justify-content: space-between; align-items: center; border-bottom: 0.75pt solid #03547c; padding-bottom: 4pt;">
        <div style="text-align: left;">
          <div style="font-size: 8pt; font-weight: bold; color: #000000; text-transform: uppercase; letter-spacing: 0.3pt;">
            ESCUELA SUPERIOR FRANCISCANA ESPECIALIZADA &ndash; AGAPE
          </div>
          <div style="font-size: 7pt; color: #555555; margin-top: 1pt;">
            Plan Maestro de Pruebas de Software &mdash; Shushine Studio (ISO 29119-3)
          </div>
        </div>
        <div>
          <img src="data:image/png;base64,` + imgHeader + `" style="height: 22px; display: block;">
        </div>
      </div>
    `;

    const footerTpl = `
      <div style="font-family: Arial, sans-serif; font-size: 7.5pt; color: #444444; width: 100%; margin: 0 18mm; display: flex; justify-content: space-between; align-items: center; border-top: 0.75pt solid #03547c; padding-top: 4pt;">
        <div style="display: flex; align-items: center; gap: 6pt;">
          <svg width="24" height="13" style="display: inline-block; vertical-align: middle;">
            <rect width="24" height="13" fill="#03547c"/>
            <rect width="24" height="2.5" fill="#90b4c5"/>
          </svg>
          <span style="color: #333333; font-weight: 500;">ESFE AGAPE / MEGATEC &mdash; Ingenier&iacute;a de Software &bull; Uso Interno Confidencial</span>
        </div>
        <div>
          <span style="color: #333333;">P&aacute;gina <span class="pageNumber"></span></span>
        </div>
      </div>
    `;

    await contentPage.pdf({
        path: contentPdfPath,
        format: 'A4',
        printBackground: true,
        displayHeaderFooter: true,
        headerTemplate: headerTpl,
        footerTemplate: footerTpl,
        margin: { top: '25mm', bottom: '22mm', left: '18mm', right: '18mm' }
    });
    console.log("Cuerpo del documento generado en:", contentPdfPath);

    await browser.close();

    // Paso 3: Unir portada y cuerpo usando pdfunite
    const targetPath1 = '/home/ministorm/salon-de-belleza/docs/Plan_de_Pruebas_Formal_IEEE829_Arial_Neutral.pdf';
    const targetPath2 = '/home/ministorm/Descargas/Plan_de_Pruebas_Formal_IEEE829_Arial_Neutral.pdf';

    console.log("Uniendo portada y contenido con pdfunite...");
    execSync('pdfunite "' + coverPdfPath + '" "' + contentPdfPath + '" "' + targetPath1 + '"');
    fs.copyFileSync(targetPath1, targetPath2);

    console.log("PDF Oficial Institucional generado exitosamente en:");
    console.log("1.", targetPath1);
    console.log("2.", targetPath2);
}

generateInstitutionalPDF().catch(err => {
    console.error("Error al generar PDF institucional:", err);
    process.exit(1);
});
