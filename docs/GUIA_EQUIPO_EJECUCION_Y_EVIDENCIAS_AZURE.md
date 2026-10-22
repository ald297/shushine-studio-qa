
# Guía de Ejecución de Pruebas y Evidencias en Azure DevOps

### Proyecto: Shushine Studio — Ecosistema Omnicanal

**Equipo:** Alex Alfaro, Cristian Jordan, Rodrigo Cortez, Cornelio Martinez
**Plataforma del Proyecto:** [https://dev.azure.com/lalafaro6/Testeo](https://dev.azure.com/lalafaro6/Testeo)

---

## 1. El Concepto Clave: ¿Dónde se hace la prueba y qué es Azure DevOps?

Para no perderse, hay que entender una regla fundamental:

> **Azure DevOps NO ejecuta tu aplicación ni tu código por arte de magia.**
> Azure DevOps es la **libreta de registro o panel de control** donde el equipo anota los resultados, adjunta las fotos de comprobación y donde el docente revisa la calidad del proyecto.

La prueba **la haces tú en tu computadora o teléfono**, y luego registras el resultado en Azure DevOps:

```
        DONDE HACES LA PRUEBA                           AZURE DEVOPS
┌───────────────────────────────────────┐       ┌────────────────────────────────┐
│ • En tu IDE corriendo JUnit (Java)    │       │ 1. Abres la tarjeta en Boards  │
│ • En Postman enviando petición HTTP   │ Foto  │ 2. Abres el Test Runner        │
│ • En el celular/emulador con la App   │ ────> │ 3. Subes la captura (Evidencia)│
│ • En Chrome con el Panel Web          │       │ 4. Marcas ✔️ (Pass) o ❌ (Fail) │
└───────────────────────────────────────┘       └────────────────────────────────┘
```

---

## 2. ¿Qué es exactamente la "Evidencia"?

La evidencia es simplemente **una captura de pantalla (screenshot)** o archivo que demuestra que la prueba se realizó y funcionó (o falló).

| Tipo de Prueba                      | ¿Dónde se ejecuta?                          | ¿Qué captura de pantalla se toma como evidencia?                                                          |
| :---------------------------------- | :-------------------------------------------- | :---------------------------------------------------------------------------------------------------------- |
| **Pruebas Unitarias Backend** | En el IDE (VS Code / IntelliJ) o terminal     | La consola con `BUILD SUCCESS` o la barra verde de JUnit con los tests pasados.                           |
| **Pruebas de la App Móvil**  | En el emulador de Android o teléfono físico | Pantallazos del celular mostrando la app funcionando (login, cita agendada, navegación).                   |
| **Pruebas del Panel Web**     | En Google Chrome / Firefox                    | Captura del navegador mostrando la pantalla administrativa correspondiente (agenda, inventario, métricas). |
| **Pruebas de API REST**       | En **Postman** o Thunder Client         | Captura de Postman mostrando la URL, el `Status: 200 OK` (o 201) y la respuesta JSON devuelta.            |

---

## 3. Paso a Paso: Cómo Ejecutar un Test y Subir Evidencias (Como en el Video 3)

Cada vez que vayas a documentar una prueba en Azure DevOps, sigue estos 5 sencillos pasos:

### Paso 1: Haz la prueba en tu máquina

Ejecuta la prueba en tu entorno (corre el test, haz la petición en Postman o usa la app). Toma una captura de pantalla y guárdala en tu máquina (ejemplo: `evidencia_login.png`).

### Paso 2: Entra a Azure DevOps

1. Entra a [https://dev.azure.com/lalafaro6/Testeo](https://dev.azure.com/lalafaro6/Testeo).
2. En el menú de la izquierda ve a **`Boards -> Boards`**.
3. Busca la tarjeta de la Historia de Usuario (PBI) que te corresponde. Verás que tiene un icono de matraz (`⚗️`) y abajo el listado de los casos de prueba.

### Paso 3: Abre el Test Runner

1. En la prueba que realizaste, haz clic sobre los tres puntos `...` de la tarjeta y selecciona **`Run`** (o ve a `Test Plans -> Test plans`, marca la casilla de la prueba y pulsa **`Run for web application`**).
2. Se abrirá la ventanita emergente del **Test Runner** (la misma ventana gris que mostró el docente en el video).

### Paso 4: Evalúa el Paso y Adjunta la Evidencia

1. Lee lo que dice en pantalla:
   * **`Action`:** La acción o datos de entrada que debías probar.
   * **`Expected result`:** El resultado que se esperaba obtener.
2. Si el sistema se comportó como decía el resultado esperado:
   * Haz clic en el **check verde** ✔️ (**Pass step**).
3. Si falló:
   * Haz clic en la **cruz roja** ❌ (**Fail step**) y escribe brevemente en el recuadro de comentarios qué fue lo que falló.
4. **Sube la Evidencia:**
   * En la barra superior de la ventanita, haz clic en el **icono del clip o cámara** (*Add attachment*).
   * Selecciona y sube tu captura de pantalla (`evidencia_login.png`).
5. **Si la prueba falló (Reportar Defecto):**
   * Haz clic en el botón **`Create bug`** de la barra superior. Azure DevOps creará un error vinculado automáticamente con la foto y los pasos.

### Paso 5: Guardar y Finalizar

* Haz clic en **`Save and Close`** (arriba a la izquierda del Test Runner).
* Al regresar al tablero de `Boards`, verás que el caso de prueba ya no está azul, sino que tiene un **check verde** o una **marca roja**.

---

## 4. Los Pipelines: ¿Qué Pruebas Abarcan y Qué Hacen?

Muchos se confunden pensando que el pipeline hace todas las pruebas del proyecto. No es así.

### ¿Qué es un Pipeline?

Es un **robot automático en la nube de Azure**. Cada vez que alguien sube cambios al repositorio con `git push`, un servidor de Microsoft en la nube clona el proyecto y compila/prueba el código automáticamente.

### ¿Qué pruebas del Backlog abarca nuestro Pipeline (`azure-pipelines.yml`)?

* **Abarca las Pruebas Unitarias de Backend (PBI #228 - Alex):**
  * En el archivo `azure-pipelines.yml`, la etapa `UnitTesting` levanta un entorno con **Java 21 LTS** y ejecuta:
    ```bash
    mvn clean test
    ```
  * Esto corre automáticamente en la nube todas las clases de prueba en `backend-api/src/test/java/`:
    * `JwtServiceTest` (Generación y validación de tokens).
    * `CitaConcurrenciaTest` (Prevención de doble reserva simultánea).
    * `DisponibilidadServiceTest` (Cálculo de huecos libres y horarios).
    * `FacturaServiceTest` (Cálculo automático de IVA al 13%).
    * `CategoriaServiceTest` (Validación de nombres únicos).
    * `ChatServiceTest` (Mensajería y reglas de longitud).
  * Los resultados se publican automáticamente en la pestaña **`Pipelines -> Tests`** con gráficos de pastel y porcentaje de éxito.

### ¿Por qué las demás pruebas NO corren en el pipeline?

* **La App Móvil (.NET MAUI - Cristian):** Requiere un usuario tocando la pantalla del celular o emulador para validar navegación, gestos y diseño.
* **El Panel Web (Rodrigo):** Requiere interactuar visualmente con el navegador (abrir modales, seleccionar fechas en el calendario).
* **Las APIs REST en Render (Cornelio):** Requieren validar los endpoints en vivo contra la base de datos real en la nube.
* **Por eso estas pruebas son manuales asistidas con el Test Runner:** Cada integrante las prueba, saca su captura y la sube al caso de prueba en Azure DevOps.

---

## 5. Ficha de Trabajo por Integrante

Cada integrante tiene tareas y casos de prueba específicos ya asignados en el tablero de Azure DevOps:

### 👤 Alex Alfaro (Backend y Lógica Móvil)

* **PBIs Asignados:**
  * `#160`: Verificación de Modelos y ViewModels de Cliente (.NET MAUI).
  * `#164`: Verificación Unitaria de TokenStorageService y Persistencia Dual.
  * `#167`: Verificación Unitaria de ClientHomeViewModel y Exclusión Mutua.
  * `#228`: Pruebas Unitarias de Lógica de Negocio y Seguridad (JUnit 5 + Mockito).
* **Herramientas de Trabajo:** IDE (VS Code / IntelliJ) + Terminal de desarrollo.
* **Evidencias a Subir:** Capturas de la terminal con `BUILD SUCCESS` de Maven o el panel de JUnit con todas las pruebas en verde.

### 👤 Cristian Jordan (App Móvil .NET MAUI)

* **PBIs Asignados:**
  * `#178`: Persistencia de sesión al reiniciar la App.
  * `#181`: Cierre de sesión voluntario y cambio de cuenta limpio.
  * `#184`: Navegación Shell entre pestañas sin cierres forzosos.
  * `#187`: Exclusión mutua en la tarjeta "Tu Próxima Cita".
  * `#190`: Transición al detalle de cita sin excepciones XAML.
  * `#193`: Pestaña Mis Citas (Próximas vs Historial) y Pull-to-Refresh.
  * `#222`: Prueba de estrés y saturación de eventos en interfaz móvil (Opcional).
* **Herramientas de Trabajo:** Emulador de Android o teléfono celular físico con la App instalada.
* **Evidencias a Subir:** Capturas de pantalla del teléfono mostrando cada flujo completado exitosamente.

### 👤 Rodrigo Cortez (Panel Web Administrativo)

* **PBIs Asignados:**
  * `#197`: Módulo de Chat & Soporte en tiempo real en panel web.
  * `#200`: Filtrado de agenda: Citas de hoy por defecto.
  * `#203`: Filtrado de agenda: Ver todo (Historial) y filtro por día.
  * `#206`: Creación de cita desde modal del panel web.
  * `#209`: Registro de entrada de stock en inventario del salón.
  * `#212`: KPIs ejecutivos y alertas 100% reales (sin datos falsos).
* **Herramientas de Trabajo:** Navegador web (Google Chrome / Firefox).
* **Evidencias a Subir:** Capturas de pantalla de las vistas del panel web mostrando las tablas, modales y calendarios actualizados.

### 👤 Cornelio Martinez (Integración, Seguridad y API REST)

* **PBIs Asignados:**
  * `#171`: Pruebas de integración de endpoints REST y manejo de errores HTTP.
  * `#174`: Pruebas de integración transaccional en Supabase PostgreSQL.
  * `#216`: Reflejo en panel web de cita agendada desde móvil.
  * `#218`: Reflejo en app móvil de cita agendada desde panel web.
  * `#220`: Mensajería bidireccional Chat Móvil <-> Chat Admin Web.
  * `#225`: Verificación de aislamiento y violación de políticas RLS en Supabase (OWASP BOLA).
  * `#232`: Suite integral de 38 endpoints API REST planificados (`CP-API-01` a `CP-API-38`).
* **Herramientas de Trabajo:** Postman o Thunder Client + Navegador + Emulador.
* **Evidencias a Subir:** Capturas de Postman con respuestas `200 OK` / `201 Created` y JSON devueltos, o capturas mostrando la cita sincronizada entre el móvil y la web.

---

## 6. Resumen en 3 Frases para la Reunión de Equipo

1. **"Alex tiene las pruebas unitarias que se automatizan en el pipeline de Azure."**
2. **"Cristian, Rodrigo y Cornelio prueban la app, la web y las APIs en sus computadoras/teléfonos y le toman captura de pantalla al resultado."**
3. **"Todos entramos a Azure DevOps, abrimos el Test Runner en nuestras tarjetas, marcamos check verde y adjuntamos nuestra captura de pantalla."**
