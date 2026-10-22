# Diagnóstico y carga sostenida — 16 de octubre de 2026

## Resultado medido

API local conectada exclusivamente a Supabase **ShushineStudio-develop**, región Canadá. Generador k6 y API comparten equipo de 8 CPU lógicas. Producción no fue modificada ni recibió carga.

Las dos corridas a 10 rps cumplen los criterios globales; las dos a 20 rps fallan por latencia. Ninguna presentó errores HTTP. A 20 rps en caliente, disponibilidad alcanza p95 de 1.636 ms y el pool tiene cola en el 23% de las muestras. El siguiente ajuste recomendado es reducir los viajes sucesivos a la base en disponibilidad y preparar el pool antes de admitir carga tras un reinicio.

| Corrida | Solicitudes | Errores inesperados | Descartadas | p95 global | Criterios k6 |
|---|---:|---:|---:|---:|---|
| cold-10 | 6000 | 0 | 0 | 948 ms | Cumple |
| warm-10 | 6000 | 0 | 0 | 919 ms | Cumple |
| cold-20 | 11966 | 0 | 34 | 1625 ms | No cumple |
| warm-20 | 12001 | 0 | 0 | 1120 ms | No cumple |

Objetivos existentes: p95 de lecturas <1.000 ms, errores inesperados <1%, ninguna solicitud descartada y todas las comprobaciones funcionales correctas. El p95 global de la mezcla no equivale al p95 individual de cada ruta.

`dropped_iterations` cuenta solicitudes que k6 no pudo iniciar a tiempo, no errores HTTP de la API. La suite preasigna 50 VU y permite crecer hasta 300. En cold-20 los 34 descartes ocurrieron en los primeros segundos (18:39:26–18:39:31 UTC), al aumentar la concurrencia durante la cola inicial. La interpretación de ese dato incluye la asignación dinámica del generador; la latencia elevada y la cola de Hikari se verifican independientemente con métricas de la API.

| Corrida | Catálogo p95 | Disponibilidad p95 | Mis citas p95 |
|---|---:|---:|---:|
| cold-10 | 421 ms | 1010 ms | 498 ms |
| warm-10 | 416 ms | 990 ms | 492 ms |
| cold-20 | 1299 ms | 2005 ms | 1336 ms |
| warm-20 | 950 ms | 1636 ms | 995 ms |

| Corrida | p95 del primer minuto observado | p95 del décimo minuto observado |
|---|---:|---:|
| cold-10 | 956 ms | 958 ms |
| warm-10 | 909 ms | 889 ms |
| cold-20 | 4989 ms | 870 ms |
| warm-20 | 922 ms | 919 ms |

Minutos agrupados por hora de finalización de solicitudes, a partir de la primera respuesta medida. Un p95 global favorable puede ocultar una degradación breve del arranque.

## Dónde se acumula la espera

| Corrida | Espera media para obtener conexión | Uso medio de conexión | Muestras con cola | Máxima cola muestreada | CPU media API | Memoria residente máxima |
|---|---:|---:|---:|---:|---:|---:|
| cold-10 | 14.1 ms | 205.7 ms | 1.0% | 7 | 7.3% de un núcleo | 445 MiB |
| warm-10 | 11.4 ms | 202.0 ms | 0.0% | 0 | 3.4% de un núcleo | 410 MiB |
| cold-20 | 77.8 ms | 201.5 ms | 28.6% | 74 | 10.2% de un núcleo | 480 MiB |
| warm-20 | 31.4 ms | 201.2 ms | 23.0% | 20 | 6.0% de un núcleo | 486 MiB |

Tiempos medios de ejecución **dentro de PostgreSQL**, calculados con diferencias de `pg_stat_statements` durante la ventana observada; excluyen transporte y espera en Hikari. La ventana de la primera corrida es parcial porque el observador comenzó después del arranque.

| Corrida | Ventana SQL observada | Usuarios | Servicios | Citas | Horarios |
|---|---:|---:|---:|---:|---:|
| cold-10 | 407 s | 0.038 ms | 0.057 ms | 0.153 ms | 0.028 ms |
| warm-10 | 594 s | 0.038 ms | 0.063 ms | 0.185 ms | 0.031 ms |
| cold-20 | 563 s | 0.037 ms | 0.066 ms | 0.110 ms | 0.025 ms |
| warm-20 | 597 s | 0.036 ms | 0.045 ms | 0.100 ms | 0.025 ms |

Sonda de 20 `SELECT 1` en una sesión ya conectada: mediana cliente **89.2 ms**, p95 **96.5 ms**; ejecución de `SELECT 1` dentro del servidor **0.017 ms**. La sonda excluye la apertura/TLS inicial pero incluye cliente, protocolo, pooler, transporte y SQL trivial; no es RTT puro. Se ejecutó durante warm-10 y añadió 21 consultas pequeñas; su evidencia está en `database-roundtrip.json`.

Los tiempos Hibernate/driver, los contadores de conexiones y los tiempos de SQL se conservan en `analysis.json`. Hibernate contabiliza consultas seleccionadas; el número de sentencias preparadas es más amplio. Ninguno de estos datos permite deducir por sí solo CPU/RAM del servidor Supabase ni latencia pura de red.

## Interpretación y siguiente cambio propuesto

El pool está configurado con mínimo de 2 conexiones inactivas y máximo de 10. Las muestras de cold-20 registran su crecimiento inicial y la cola asociada; conviene evaluar calentamiento y readiness antes de aceptar esa tasa tras un reinicio.

La comparación entre ejecución SQL en el servidor y tiempos del driver permite detectar el coste de los viajes entre API y base. En disponibilidad, el código obtiene por separado el usuario autenticado, la duración del servicio, el estilista, el horario, los bloqueos y las citas. Esas consultas sucesivas amplifican la latencia del recorrido. Esto es una inferencia sustentada en los tiempos y el código, no una medición aislada de RTT.

Conviene reducir los viajes de disponibilidad y repetir la misma serie; también medir la API y la base en la misma región. El pool debe ajustarse solamente si la cola medida y los límites de Supabase lo justifican. En esta entrega se conservó el máximo de 10 conexiones para mantener comparable el diagnóstico.

El resultado distingue cumplimiento global de cumplimiento por ruta. Una corrida con errores cero pero p95 superior al objetivo no se considera aprobada. Dos repeticiones no establecen capacidad garantizada y estas tasas no equivalen a cantidad de usuarios.

## Procedimiento y límites

1. Reinicio únicamente de la API local; corrida de 10 minutos a 10 rps.
2. Calentamiento explícito de 2 minutos y corrida de 10 minutos a 10 rps.
3. Reinicio de la API y repetición del procedimiento a 20 rps.

“Frío” significa API recién reiniciada. El preflight y los 10 logins de preparación ocurren antes de medir; la base Supabase no fue reiniciada. Mezcla: 50% catálogo, 30% disponibilidad, 20% mis citas. Son lecturas de datos ficticios; no son pruebas de login masivo, reservas, administración, chat o Realtime.

Muestreo Java/host cada 2 s; PostgreSQL cada 10 s y estadísticas de sentencias cada 30 s. Las colas y máximos muestreados pueden omitir picos entre muestras. Los contadores Java se comparan entre la primera y última muestra, con un margen de aproximadamente 2 s. Las consultas de diagnóstico añaden carga pequeña. No se reiniciaron estadísticas ni se habilitaron extensiones. `track_io_timing=off`; tiempos I/O cero no prueban ausencia de espera.

La sonda psql incluye crear un proceso, conectar/TLS y consultar; no debe confundirse con RTT. La red del host incluye tráfico ajeno a k6/API. Una ejecución Maven de pruebas coincidió con el primer minuto de `cold-10`; se conserva en metadatos y muestras de CPU. Las demás corridas no incluyen compilaciones. El orden fijo frío/caliente y la variación temporal de red/plataforma también pueden influir; las diferencias no se atribuyen exclusivamente al calentamiento. Develop Free/NANO en Canadá y API local no reproducen el despliegue productivo.

## Implementación y verificación

Actuator/Micrometer recoge métricas internas; **0 endpoints de gestión expuestos**. La escritura de telemetría solo se activa por configuración al arrancar develop. Se exportan identificadores de consultas SHA-256, sin SQL/HQL, parámetros, tokens, contraseñas ni cuerpos. **41 pruebas Java pasan**, incluida la comprobación de captura de tiempos y exclusión del texto SQL. Python compila y `git diff --check` pasa.

Fuentes técnicas: [métricas Spring Boot](https://docs.spring.io/spring-boot/reference/actuator/metrics.html) y [estadísticas Hibernate 6.5](https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html).

## Evidencia local

- [Serie y metadatos](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/metadata.json)
- [Análisis numérico](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/analysis.json)
- [Estadísticas SQL del servidor](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/sql-server.ndjson)
- cold-10: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T181445Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T181445Z-soak/summary.json), [telemetría](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/cold-10-telemetry.ndjson)
- warmup-10: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T182453Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T182453Z-soak/summary.json), [telemetría](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/warmup-10-telemetry.ndjson)
- warm-10: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T182702Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T182702Z-soak/summary.json), [telemetría](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/warm-10-telemetry.ndjson)
- cold-20: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T183915Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T183915Z-soak/summary.json), [telemetría](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/cold-20-telemetry.ndjson)
- warmup-20: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T184925Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T184925Z-soak/summary.json), [telemetría](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/warmup-20-telemetry.ndjson)
- warm-20: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T185134Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261016T185134Z-soak/summary.json), [telemetría](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261016T181434Z/warm-20-telemetry.ndjson)
