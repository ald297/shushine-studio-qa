# Documentación integral de pruebas de rendimiento y optimización

**Aplicación:** Shushine Studio · **Documento consolidado:** 20 de octubre de 2026 · **Zona horaria:** America/El_Salvador.

Las ejecuciones documentadas corresponden al trabajo del 16 de octubre en El Salvador; algunas carpetas muestran 17 de octubre porque sus nombres utilizan UTC. Esta consolidación revisa los informes, el código y las evidencias conservadas. No representa una nueva ejecución de carga el 20 de octubre.

## 1. Qué se consiguió

Se preparó un entorno separado de producción, se automatizaron pruebas con Grafana k6, se detectaron problemas de integridad y rendimiento, se corrigió el backend y se repitieron las mediciones para comprobar los cambios.

Los dos últimos objetivos quedaron cumplidos en el entorno ensayado:

- **Lecturas:** 20 solicitudes por segundo durante 10 minutos, p95 global y de cada ruta inferior a un segundo, sin errores ni iteraciones descartadas.
- **Reservas:** tres ráfagas de 50 intentos simultáneos sobre una misma franja, con una cita creada y 49 conflictos esperados en cada ráfaga, p95 inferior a dos segundos y ningún solapamiento. Además, 50 reservas independientes se guardaron correctamente con p95 inferior a dos segundos.

Producción no recibió la carga, no se migró y no se desplegó este backend durante el trabajo. Los resultados corresponden a una API local conectada a Supabase develop. No son una certificación de capacidad productiva ni equivalen a 50 reservas por segundo sostenidas.

## 2. Necesidad inicial y objetivos de aceptación

Se solicitó planear y ejecutar pruebas de estrés, automatizar su configuración y visualizar los resultados con herramientas de Grafana. Al existir solamente un despliegue de producción, primero se necesitó aislar la base de datos y la ejecución.

Los criterios utilizados fueron:

| Aspecto | Criterio |
|---|---|
| Tiempo de lectura | p95 < 1.000 ms, global y por ruta en la validación final |
| Tiempo de escritura | p95 < 2.000 ms |
| Errores inesperados | Umbral general < 1%; las verificaciones finales exigieron respuestas correctas y observaron cero |
| Carga ofrecida | Cero iteraciones descartadas para declarar un objetivo sostenido cumplido |
| Reservas independientes | Todas persistidas, sin conflictos falsos |
| Competencia por una franja | Exactamente una creada; los demás intentos deben devolver conflicto real |
| Integridad | Cero pares de citas activas solapadas; ningún registro parcial |

**p95** significa que el 95% de las solicitudes observadas terminó en ese tiempo o menos. No es el tiempo máximo. Un **409** por una franja realmente ocupada es una respuesta esperada; un timeout o el rechazo de una reserva independiente es un fallo.

Una **iteración descartada** significa que k6 no pudo iniciar parte del trabajo al ritmo ofrecido con los recursos y usuarios virtuales configurados. No es una respuesta del servidor. Se contabiliza aparte de errores HTTP y solicitudes interrumpidas.

## 3. Entorno separado y tratamiento de los datos

### 3.1. Decisión sobre Supabase

Se evaluó crear una rama develop de Supabase. La opción requería un plan de pago, por lo que se eligió un proyecto gratuito independiente. Como el cupo estaba ocupado, se pausó, con autorización expresa, `ald297's Project` (`fbxygbcgnpgtfplwpkxu`). Ese proyecto no se reactivó automáticamente.

El usuario creó **ShushineStudio-develop** y definió su contraseña. Su referencia es `mpmqotozaopmvmesgmgw`, región Canadá Central (`ca-central-1`), plan Free/NANO. Es un proyecto separado, no una rama Git ni una rama administrada de la base productiva.

Se exportó únicamente el esquema público de producción, con **28 tablas**, y se importó transaccionalmente en develop. No se copiaron los registros de clientes de producción. La exportación inicial fue una lectura de estructura; las migraciones y escrituras posteriores se hicieron en develop o en el laboratorio local.

La contraseña permanece en `.performance/develop.local.json`, archivo local con permisos `0600` y excluido de Git. No se incorpora a esta documentación. No deben publicarse ese archivo, tokens, JWT ni cadenas de conexión con contraseña.

### 3.2. Topología utilizada

```text
k6 en el equipo local
        │ HTTP de prueba
        ▼
API Java local: 127.0.0.1:18080
        │ Hikari: mínimo 14 / máximo 14 en la configuración final
        ▼
Supabase ShushineStudio-develop — ca-central-1

k6 → métricas JSON + resumen + informe HTML
API / equipo / BD → telemetría de diagnóstico
```

La API se vinculó a loopback. Los scripts verifican la referencia y conexión del entorno, y comprueban el datasource del proceso Java antes de sembrar datos o enviar carga. Rechazan referencias conocidas de producción y conexiones que no coincidan con develop. Se usa el pooler de sesión en puerto 5432; la configuración de develop rechaza el pooler transaccional 6543.

También se preparó PostgreSQL local aislado en `127.0.0.1:15432/shushine_perf`. Sirve como alternativa y para regresiones desechables; no es el entorno de las cifras finales de Supabase. Su autenticación `trust` se limita al loopback de ese laboratorio.

Storage se desactivó en el perfil de API de pruebas. No se cambiaron las conexiones de producción de la aplicación móvil ni del panel administrativo.

## 4. Herramientas y automatización

| Herramienta | Uso durante el trabajo |
|---|---|
| Grafana k6 2.3.0 | Generar carga HTTP, validar respuestas, medir percentiles y aplicar umbrales |
| Panel web integrado de k6 | Ver la ejecución local y exportar informes HTML independientes |
| Java 21 y Maven 3.9.11 | Construir el backend Spring Boot 3.3.3 y ejecutar sus pruebas |
| PostgreSQL 17 y psql | Preparar el laboratorio, aplicar SQL y comprobar persistencia e integridad |
| Python | Instalar runtimes, orquestar entornos, ejecutar escenarios y analizar resultados |
| Hikari, Micrometer y telemetría propia | Observar conexiones, esperas, JVM, HTTP y operaciones de negocio |
| Estadísticas PostgreSQL | Observar actividad y consultas, sin reiniciar contadores |

**Se utilizó Grafana k6 con su panel integrado. No se instaló Grafana Server ni se configuró Grafana Cloud.** Los informes HTML son el resultado visual disponible, no un servicio remoto de monitoreo permanente.

Los runtimes se instalaron en espacio de usuario, sin sudo ni Docker. El instalador usa descargas oficiales y comprueba checksums de k6, JDK y Maven. La receta corresponde a Debian 13 amd64 con Python 3.12 o posterior y las utilidades indicadas por el instalador.

### Archivos principales

Todos los siguientes archivos están bajo `/home/ministorm/salon-de-belleza/`:

| Archivo o carpeta | Responsabilidad |
|---|---|
| `scripts/performance/install.py` | Instalar herramientas en espacio de usuario |
| `scripts/performance/lab.py` | Gestionar PostgreSQL/API local y datos ficticios |
| `scripts/performance/develop.py` | Comprobar, inicializar, migrar, iniciar y sembrar develop |
| `scripts/performance/run.py` | Ejecutar escenarios y verificar escrituras persistidas |
| `tests/performance/suite.js` | Definir carga k6, respuestas esperadas y umbrales |
| `scripts/performance/sustained.py` | Series en frío/caliente con observación de recursos |
| `scripts/performance/analyze_sustained.py` | Analizar rutas, minutos, conexiones y tiempos |
| `scripts/performance/database_observer.py` | Observar estadísticas de consultas y base |
| `scripts/performance/probe_database_roundtrip.py` | Separar tiempo observado por cliente de ejecución SQL |
| `scripts/performance/live_sustained.py` | Consultar el avance y las métricas recientes |
| `scripts/performance/verify_*.py` | Regresiones funcionales y de integridad |
| `tests/performance/results/` | Resultados históricos locales, excluidos de Git |

## 5. Cómo se diseñaron las pruebas

### 5.1. Datos y autenticación

Se crearon cuentas, clientes, estilistas, servicios, horarios y citas ficticias. Los horarios de referencia son de lunes a sábado, de 09:00 a 17:00, con almuerzo de 12:00 a 13:00. Las franjas para escrituras se renuevan para evitar confundir datos anteriores con el resultado de una ejecución.

La preparación obtiene JWT de diez cuentas y los reutiliza por usuario virtual; se renuevan cada 45 minutos. Esto permite medir las operaciones elegidas sin convertir cada solicitud en un login. La autenticación de las solicitudes sigue activa; no se ensayó un login masivo sostenido.

Se detectó que las primeras consultas de disponibilidad devolvían listas vacías porque faltaban horarios. Se corrigió la semilla y se añadió una comprobación previa que exige franjas no vacías. Las mediciones iniciales con disponibilidad vacía se conservaron como antecedente y no se utilizaron como comparación equivalente de capacidad.

### 5.2. Escenarios

La mezcla de lectura envía **50% catálogo**, **30% disponibilidad** y **20% citas propias**:

- `GET /api/servicios/lista`.
- `GET /api/estilistas/{id}/disponibilidad`, con fecha y servicio.
- `GET /api/citas/mis-citas`.

Cada recorrido medido de lectura envía una petición HTTP. Las reservas usan `POST /api/citas` y se miden por separado.

| Perfil | Configuración y finalidad |
|---|---|
| `smoke` | 10 recorridos, un usuario virtual, pausas de un segundo; comprobar funcionamiento |
| `baseline` | 5 recorridos/s durante 10 minutos |
| `stress` | 5 → 10 → 20 → 40 → 80 recorridos/s; rampas de un minuto y mesetas de cinco; recuperación de dos minutos; 32 minutos previstos |
| `stress --quick` | Rampas de 10 s y mesetas de 20 s; descenso de 5 s y observación de 30 s; 185 s previstos |
| `spike` | Cambios de 5 → 15 → 5 recorridos/s |
| `soak` | Tasa y duración configurables; una hora por defecto |
| `booking` | Reservas independientes; `--vus` controla concurrencia y `--iterations` cantidad |
| `contention` | Todos los intentos compiten por una misma franja; hasta 50 usuarios virtuales |
| Serie sostenida | Arranque de API, 10 minutos medidos, calentamiento separado de dos minutos y otros 10 minutos medidos |

Que un perfil exista no significa que haya sido certificado. La validación sostenida final fue de 10 minutos por corrida; no se completó una certificación final de una hora.

La suite valida JSON y respuestas, y aborta cuando el error acumulado supera el 5% después de 30 segundos. Ese control no es una ventana móvil. Un código de salida de umbral fallido conserva el resultado como evidencia de degradación.

“En frío” significa API reiniciada antes de la preparación y los diez logins. La base no se reinicia y no se mide literalmente la primera solicitud del proceso.

## 6. Qué problemas aparecieron y cómo se corrigieron

### 6.1. Reservas rechazadas aunque no competían

Al principio se guardaron solamente **7 de 10 reservas independientes**. El aislamiento `SERIALIZABLE` generaba fallos de serialización y algunas capturas de excepciones los presentaban como si la franja estuviera ocupada.

Se cambió la operación a `READ COMMITTED` y se trasladó la protección frente a carreras a PostgreSQL. Se eliminó la consulta previa de solapamiento: una comprobación seguida de una inserción no garantiza por sí sola la integridad cuando dos solicitudes llegan juntas.

La migración `database/migrations/20261016_reservas_sin_solapamientos.sql` incorpora:

- Extensión `btree_gist` y restricción de exclusión por estilista e intervalo de fecha/hora.
- Rangos `[inicio, fin)`: una cita puede comenzar exactamente cuando termina otra.
- Validación de que la hora final sea posterior a la inicial.
- Exclusión de las citas `Cancelled` del bloqueo de franjas. Se preservó el criterio anterior: `NoShow` sigue bloqueando.
- Secuencia global `cita_codigo_seq`, inicializada después de los códigos existentes y sin reiniciarse al repetir la migración.

Los códigos mantienen `SHU-año-número`, al menos cuatro dígitos y crecimiento sin truncar números largos. Una secuencia puede dejar saltos tras un rollback; eso es normal y no significa citas perdidas.

Si existen datos inválidos o solapados, la migración falla y no borra registros para ocultarlos. `ReservationSchemaGuard` comprueba los objetos necesarios antes de abrir HTTP: un backend sin la migración no acepta tráfico.

### 6.2. Errores de base mal interpretados

Se centralizó su traducción:

| Situación | Respuesta |
|---|---|
| Exclusión real de solapamiento, SQLSTATE `23P01` | HTTP 409 |
| Conflicto de versión por actualización optimista | HTTP 409 con explicación específica |
| Serialización, interbloqueo o recursos/conexión transitorios | HTTP 503 |
| Otros fallos de persistencia | HTTP 500 genérico |

Las respuestas no exponen SQL ni mensajes internos. Se eliminaron capturas genéricas que convertían cualquier error de persistencia en “horario ocupado”.

### 6.3. Consultas repetidas y disponibilidad lenta

Se usaron EntityGraph y cargas dirigidas para usuario/rol, cliente, servicio/categoría e historial. Esto evita consultas adicionales por cada entidad o detalle.

Para disponibilidad se implementó `DisponibilidadConsultaRepository`: una sola consulta JDBC parametrizada obtiene estilista, horario, duración y agregados independientes de bloqueos y citas. Evita multiplicar filas al combinar relaciones y entrega una instantánea coherente de la sentencia.

`DisponibilidadService` conserva almuerzos, bloqueos, horarios, razones de indisponibilidad y reglas de duración. También se corrigió el recorrido cerca de medianoche para evitar ciclos por desbordamiento de la hora. No se añadió caché de disponibilidad: una cancelación debe verse inmediatamente.

### 6.4. Esperas para obtener conexiones

Primero se instrumentó, en vez de aumentar recursos sin evidencia. Se añadieron métricas de Hikari, JVM, CPU, memoria, HTTP y estadísticas SQL. Los identificadores de consultas se registran como hashes, sin SQL, parámetros, cuerpos, JWT ni contraseñas. La incorporación de Actuator no habilitó nuevos endpoints de administración: están desactivados por defecto.

Se observaron consultas SQL rápidas en el servidor y tiempos de ida y vuelta relevantes. Una prueba de `SELECT 1` con conexión ya abierta tuvo mediana de aproximadamente **89 ms**, frente a **0,017 ms** de ejecución del SQL en el servidor. Ese tiempo incluye cliente, protocolo, red y pooler; no es una medición pura de latencia de red.

El pool pasó de mínimo 2/máximo 10 a mínimo 10/máximo 10, y después a **mínimo 14/máximo 14** para las reservas finales. `DatabasePoolWarmup` prepara y valida las conexiones mínimas antes de abrir HTTP, después de comprobar el esquema.

El último ajuste se justificó porque, con diez conexiones, la espera media llegó a 451 ms frente a 177 ms de uso y se observaron hasta 40 solicitudes esperando. La base reportó 23 conexiones totales de un máximo de 60 antes del ajuste. Catorce es la configuración ensayada; debe recalcularse si cambian réplicas, plan o conexiones de otros clientes.

### 6.5. Escritura de reservas con demasiados viajes a la base

La ruta normal para clientes existentes utiliza ahora `ReservaJdbcRepository`:

1. Un SELECT obtiene usuario/cliente, estilista y servicios. Los precios proceden de la base en cada solicitud.
2. Java conserva el cálculo de duración, subtotal, IVA del 13% con redondeo `HALF_UP` y total.
3. Una sentencia SQL con CTE coordina la agenda, obtiene el número de secuencia, inserta la cita e inserta sus detalles.
4. Si falla un detalle o una restricción, se revierte toda la sentencia; no queda una cita incompleta.

Se conservan el DTO y los valores iniciales: estado `Confirmed`, pago `Pending`, origen `App`, descuento cero y versión cero. Se preparan y reutilizan metadatos de serialización; no se almacenan en caché precios ni resultados de negocio.

La primera variante de escritura agrupada produjo interbloqueos y timeouts en develop; fue descartada. También se detectó una inserción tardía después de un timeout del cliente y se limpió expresamente. Esto confirmó que una respuesta HTTP fallida no demuestra por sí sola que no haya habido escritura.

Se añadió `pg_advisory_xact_lock` por estilista/día. El bloqueo espera y se libera al terminar la transacción; la restricción de exclusión sigue siendo quien decide el conflicto por solapamiento. Dos franjas distintas del mismo día pueden esperar brevemente sin que eso implique rechazarlas.

Las cuentas antiguas sin Cliente conservan una ruta transaccional que bloquea y vuelve a comprobar el usuario para crear un solo perfil. Esa ruta y Walk-in adquieren el mismo bloqueo de agenda. Los escritores directos de Supabase siguen sujetos a la exclusión, aunque no participan automáticamente en ese bloqueo de la API.

### 6.6. Mejoras de los propios scripts

Se corrigió la detección de procesos Java zombis para permitir reinicios fiables. La serie sostenida guarda avance atómicamente y solo permite reanudar entre bloques completos; no mezcla una corrida incompleta con otra. `booking` respeta `--vus`. El observador SQL se adaptó a los campos de estadísticas de PostgreSQL 17.

## 7. Evolución de los resultados

### 7.1. Exploración inicial y estrés

La primera exploración encontró errores de reservas y saturación. Después de corregir la integridad y completar los horarios ficticios, una rampa rápida ofreció niveles crecientes:

| Meseta ofrecida | Solicitudes completadas en la meseta | p95 | Fallos HTTP |
|---|---:|---:|---:|
| 5/s | 100 | 1.140 ms | 0 |
| 10/s | 200 | 908 ms | 0 |
| 20/s | 400 | 833 ms | 0 |
| 40/s | 622 | 13.381 ms | 15 |
| 80/s | 266 | 15.001 ms | 56 |

La clasificación usa la etapa en que comenzó cada solicitud. En toda la rampa se completaron 2.412 recorridos, con 125 timeouts, 1.785 iteraciones descartadas y 289 interrumpidas. Se abortó alrededor de los 155 segundos, antes de completar la recuperación. El smoke posterior respondió sin errores, pero no permite fijar el instante exacto de recuperación.

Los 833 ms a 20/s eran una meseta de solo veinte segundos. Una comprobación de 30 segundos tras reiniciar obtuvo p95 de 3.205 ms y ocho descartes. Por eso se pasó a medir corridas largas con telemetría, sin declarar capacidad estable por la meseta corta.

**Estos son resultados anteriores a las optimizaciones finales. La rampa 40/80 no se volvió a certificar con el código final y el pool de 14; no determina su límite actual.**

### 7.2. Lecturas sostenidas antes de optimizar

| Corrida de 10 minutos | Completadas | p95 global | Descartadas | Lectura del resultado |
|---|---:|---:|---:|---|
| 10/s en frío | 6.000 | 948 ms | 0 | Disponibilidad individual ~1.010 ms: no cumple el criterio estricto por ruta |
| 10/s caliente | 6.000 | 919 ms | 0 | Mejor comportamiento a esta tasa |
| 20/s en frío | 11.966 | 1.625 ms | 34 | No cumple |
| 20/s caliente | 12.001 | 1.120 ms | 0 | No cumple latencia |

No hubo errores HTTP en esas cuatro corridas. En frío a 20/s se observaron hasta 74 solicitudes esperando conexión; en caliente, hasta 20. La CPU del proceso no mostraba saturación y las consultas observadas tenían medias bajas de ejecución en el servidor. La evidencia orientó a reducir viajes y esperas de conexión.

Limitaciones del diagnóstico: la primera ventana SQL fue parcial; `track_io_timing` estaba desactivado, por lo que no se puede concluir que no hubiera espera de disco. La primera corrida a 10/s coincidió inicialmente con pruebas Maven. API y k6 comparten equipo; las métricas de host y red pueden incluir otras actividades.

### 7.3. Lecturas después de reducir consultas y preparar el pool

| Corrida de 10 minutos a 20/s | Completadas | p95 global | Errores | Descartadas |
|---|---:|---:|---:|---:|
| API reiniciada | 12.000 | 365 ms | 0 | 0 |
| Caliente | 12.001 | 377 ms | 0 | 0 |

Cada una de las tres rutas quedó por debajo de un segundo. En estas corridas el máximo observado de solicitudes pendientes del pool fue cero. El objetivo de lecturas quedó cumplido con esta evidencia prolongada, en lugar de basarse en una meseta breve.

### 7.4. Reservas: resultado final

La referencia inmediata de 50 intentos por una franja tenía p95 de **6.180 ms**. Tras agrupar la escritura y coordinar la agenda, el pool de diez aún presentó variación: 1.498, 1.734 y 2.557 ms en contención, y 2.172 ms para 50 independientes. El ajuste a catorce conexiones produjo:

| Corrida final | p95 | Creadas | Conflictos esperados | Errores inesperados | Pares solapados |
|---|---:|---:|---:|---:|---:|
| 50 sobre una franja, API reiniciada | 1.202 ms | 1 | 49 | 0 | 0 |
| Repetición A, misma competencia | 1.112 ms | 1 | 49 | 0 | 0 |
| Repetición B, misma competencia | 1.136 ms | 1 | 49 | 0 | 0 |
| 50 reservas independientes | 1.591 ms | 50 | 0 | 0 | 0 |

Son 200 intentos de los cuatro lotes finales: 150 en disputa y 50 independientes. Se comprobaron los registros en PostgreSQL, no solamente los códigos HTTP. Todas las corridas finales de esta tabla cumplieron el objetivo de escritura.

### 7.5. Lecturas conservadas tras el último ajuste

Se repitieron 10 minutos a 20 lecturas/s con la configuración final de reservas:

| Métrica | Resultado |
|---|---:|
| Solicitudes completadas | 12.001 |
| p95 global | 364 ms |
| p95 catálogo | 309 ms |
| p95 disponibilidad | 207 ms |
| p95 citas propias | 389 ms |
| Errores inesperados / descartes | 0 / 0 |

Lecturas y reservas se verificaron en corridas separadas. No se ha demostrado todavía que ambas cargas simultáneas sostengan esos mismos resultados.

## 8. Verificación funcional y limpieza

El último build verificado contiene **56 pruebas Java, cero fallos, cero errores y cero omitidas**. Los recuentos de 40 o 51 que aparecen en informes anteriores pertenecen a etapas previas.

Además se ejecutaron comprobaciones contra PostgreSQL y HTTP:

| Grupo | Qué se comprobó |
|---|---|
| 12 regresiones de reservas en BD | Solapamientos, adyacencia, cancelación/reactivación, horarios inválidos, concurrencia y secuencia |
| Contrato de disponibilidad | Siete variantes de consulta y actualización inmediata al cancelar; evidencia conservada de nueve comprobaciones |
| 5 comparaciones de reservas antes/después | Uno o varios servicios, IDs duplicados, combinación válido/inexistente y todos inexistentes; importes, duración, DTO y detalles |
| 4 verificaciones SQL de atomicidad | Fallo de FK sin cita residual, inserción completa, exclusión sin detalles parciales y cancelación que libera |
| 4 carreras verificadas por HTTP | Adyacencia, otro estilista, cancelación del propietario con versión 0→1 y solapamiento parcial |
| Perfil antiguo | Dos primeras reservas simultáneas, un solo Cliente y códigos distintos |
| Arranque sin migración | Proceso termina con error y el puerto HTTP nunca llega a abrirse |

Se limpiaron los registros propios de las regresiones y de las ráfagas finales después de verificar la persistencia. También se retiraron registros piloto identificados durante la investigación. Esto no significa que se haya vaciado toda la base develop: se conservaron datos ficticios de preparación e históricos para investigación. El runner general guarda evidencia y no elimina automáticamente todos los datos de cada ejecución.

## 9. Cómo reproducir el trabajo

Estos comandos describen la operación del laboratorio. **No se ejecutaron de nuevo para redactar este documento.** Parten de la configuración local existente; no requieren pegar contraseñas en el chat. Ejecutarlos desde la raíz del proyecto.

### 9.1. Instalar y construir si hace falta

```bash
cd /home/ministorm/salon-de-belleza
python3 scripts/performance/install.py
JAVA_HOME="$(cat .performance/runtime/java-home.txt)" .performance/runtime/apache-maven-3.9.11/bin/mvn -f backend-api/pom.xml package
```

El comando de Maven incluye pruebas. No se usa `-DskipTests` para declarar un build validado. Los requisitos del instalador deben cumplirse en el equipo destino.

### 9.2. Preparar develop y comprobar funcionamiento

```bash
python3 scripts/performance/develop.py check
python3 scripts/performance/develop.py migrate
python3 scripts/performance/develop.py start
python3 scripts/performance/develop.py seed
python3 scripts/performance/run.py smoke
```

`migrate` aplica la migración al develop configurado. `start` reinicia únicamente la API del laboratorio reconocida por PID y JAR. `seed` escribe datos ficticios. **No repetir `initialize` sobre el proyecto ya preparado**: se reservó para importar el esquema inicial y rechaza una base con tablas.

### 9.3. Repetir los objetivos concretos

```bash
# Lecturas en frío y caliente, con calentamiento y telemetría separados.
python3 scripts/performance/sustained.py --rates 20

# Una ráfaga por la misma franja. Repetir por separado para comparar.
python3 scripts/performance/run.py contention --vus 50

# Cincuenta reservas en franjas independientes.
python3 scripts/performance/run.py booking --vus 50 --iterations 50

# Regresión de lecturas tras cambios de reservas.
python3 scripts/performance/run.py soak --rate 20 --duration 10m
```

La serie sostenida crea una carpeta y muestra su ubicación. Analizar la carpeta real indicada por el comando:

```bash
python3 scripts/performance/analyze_sustained.py tests/performance/results/sustained-FECHA
```

`FECHA` es un marcador que se sustituye por el nombre generado. Las corridas consumen recursos del proyecto develop. Comparar código, JAR, datos, configuración y condiciones de red antes de atribuir diferencias a una optimización.

### 9.4. Ver el panel y revisar evidencias

```bash
python3 scripts/performance/run.py baseline --dashboard
```

Durante esa ejecución se puede abrir `http://127.0.0.1:5665`. Al terminar, cerrar la pestaña para permitir la salida de k6. Sin `--dashboard` también se exportan resultados; ejecuciones demasiado breves pueden no producir HTML, aunque sí resumen y métricas.

Cada carpeta de corrida conserva `summary.json`, `metrics.json`, `report.html` cuando hay datos suficientes y `verification.json`. Las escrituras añaden cantidades persistidas y solapamientos. El resumen permite ver umbrales fallidos; el HTML facilita explorar tiempos y evolución.

Para investigar degradación se dispone de `stress --quick`. Esa rampa busca límites y puede fallar deliberadamente: no sustituye la validación de 10 minutos ni declara por sí misma capacidad productiva.

### 9.5. Entorno completamente local

```bash
python3 scripts/performance/lab.py start
python3 scripts/performance/lab.py seed
python3 scripts/performance/run.py smoke
python3 scripts/performance/lab.py stop
```

Los dos perfiles utilizan el puerto 18080; detener la API del laboratorio anterior antes de cambiar de entorno. `lab.py stop` gestiona los procesos locales; no pausa ni elimina Supabase. Las cifras del laboratorio local no se deben mezclar con las del proyecto remoto.

## 10. Requisitos antes de desplegar estos cambios

La migración de reservas debe aplicarse al entorno destino **antes** de iniciar este backend. No se incluyó una migración automática de producción. Hay que comprobar los datos existentes: los solapamientos o intervalos inválidos impiden crear las restricciones y requieren resolución explícita.

El tamaño de pool debe dimensionarse considerando todas las réplicas y clientes de la base. Preparar catorce conexiones en cada réplica multiplica el consumo total. El perfil de telemetría se activa expresamente para diagnóstico y no publica secretos ni endpoints administrativos.

Los cambios permanecen en los archivos del workspace: durante este trabajo no se creó un commit, una PR ni un despliegue. Los resultados y secretos locales están excluidos de Git; trasladar únicamente el código no traslada automáticamente el laboratorio, sus contraseñas o sus informes históricos.

## 11. Alcance pendiente y siguiente objetivo

La siguiente validación útil es **una carga mixta sostenida de lecturas y reservas**, manteniendo los criterios de latencia e integridad, con generador separado y API/base en una región comparable al despliegue real. Permitiría comprobar cómo compiten las operaciones por las conexiones y definir una capacidad operativa con margen.

Queda pendiente:

- Repetir la rampa alta de 40/80 con la versión final, observando saturación y recuperación completa.
- Ejecutar pruebas prolongadas y repeticiones suficientes para evaluar estabilidad, fugas y variación.
- Medir login masivo, perfiles antiguos en volumen, Walk-in, administración y tráfico directo del panel a Supabase.
- Evaluar experiencia móvil, consultas administrativas, chat, Supabase Auth, Realtime y Storage.
- Preparar un entorno comparable a producción antes de emitir conclusiones sobre Render o usuarios reales.
- Configurar monitoreo remoto, alertas o integración CI si se decide operarlos. No quedaron configurados Grafana Cloud/Server ni ejecuciones programadas.

No se midieron directamente todos los recursos internos del servidor Supabase. Las medias SQL bajas no descartan colas o picos; el sondeo de red incluye trabajo del cliente y pooler. Tampoco puede deducirse capacidad continua de las ráfagas de reservas.

## 12. Evidencias y trazabilidad

La explicación principal está en este documento. Los siguientes archivos permiten auditarla y consultar el detalle original. Los informes anteriores describen el estado de cada etapa; frases como “todavía no cumple” no sustituyen los resultados finales de las secciones 7 y 8.

### Informes de las etapas

- [Plan original](/home/ministorm/salon-de-belleza/docs/PLAN_PRUEBAS_ESTRES.md).
- [Primera ejecución en develop](/home/ministorm/salon-de-belleza/docs/REPORTE_ESTRES_DEVELOP_2026-10-16.md).
- [Correcciones de integridad y rampa con horarios](/home/ministorm/salon-de-belleza/docs/REPORTE_CORRECCIONES_ESTRES_2026-10-16.md).
- [Diagnóstico sostenido](/home/ministorm/salon-de-belleza/docs/REPORTE_DIAGNOSTICO_SOSTENIDO_2026-10-16.md).
- [Optimización de lecturas](/home/ministorm/salon-de-belleza/docs/REPORTE_OPTIMIZACION_RENDIMIENTO_2026-10-16.md).
- [Optimización final de reservas](/home/ministorm/salon-de-belleza/docs/REPORTE_OPTIMIZACION_RESERVAS_2026-10-16.md).

### Evidencia final verificable

| Evidencia | Archivo |
|---|---|
| Consolidación del objetivo, pruebas Java, integridad y hash | [verification-final.json](/home/ministorm/salon-de-belleza/tests/performance/results/booking-optimized-20261017T052936Z/verification-final.json) |
| Metadatos del artefacto ensayado | [metadata.json](/home/ministorm/salon-de-belleza/tests/performance/results/booking-optimized-20261017T052936Z/metadata.json) |
| Tres ráfagas y reservas independientes | [runs.json](/home/ministorm/salon-de-belleza/tests/performance/results/booking-optimized-20261017T052936Z/runs.json) |
| Primera ráfaga final | [Informe HTML](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053003Z-contention/report.html) |
| Segunda ráfaga final | [Informe HTML](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053016Z-contention/report.html) |
| Tercera ráfaga final | [Informe HTML](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053029Z-contention/report.html) |
| Cincuenta reservas independientes | [Informe HTML](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053042Z-booking/report.html) |
| Lecturas finales a 20/s | [Informe HTML](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053233Z-soak/report.html) |
| Contrato de reservas | [Verificación](/home/ministorm/salon-de-belleza/.performance/booking-contract-verification.json) |
| Atomicidad de la escritura | [Verificación](/home/ministorm/salon-de-belleza/.performance/booking-atomicity-verification.json) |
| Carreras por HTTP | [Verificación](/home/ministorm/salon-de-belleza/.performance/booking-api-races-verification.json) |
| Contrato de disponibilidad | [Verificación](/home/ministorm/salon-de-belleza/.performance/availability-contract-verification.json) |
| Perfil antiguo sin duplicación | [Verificación](/home/ministorm/salon-de-belleza/.performance/legacy-client-verification.json) |
| Arranque protegido | [Verificación](/home/ministorm/salon-de-belleza/.performance/guard-startup-verification.json) |
| Regresiones en PostgreSQL | [Verificación](/home/ministorm/salon-de-belleza/.performance/reservation-db-regression.json) |
| Migración necesaria | [SQL](/home/ministorm/salon-de-belleza/database/migrations/20261016_reservas_sin_solapamientos.sql) |

SHA-256 del JAR de la verificación final:

```text
65b557376dcbb685b6ab7f99d592cc0c50db75f8c0bcbf61fc6eca81cb2735d3
```

Ese hash identifica el artefacto medido. Una compilación posterior o cambios futuros requieren su propia identificación y validación. Los enlaces a resultados funcionan mientras se conserven los archivos locales ignorados por Git; el documento conserva las conclusiones principales aunque esos archivos no se distribuyan.
