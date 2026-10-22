# Correcciones y nueva medición — ShushineStudio develop

Fecha: 16 de octubre de 2026, America/El_Salvador.

**Se corrigieron los rechazos de reservas independientes y se redujeron consultas de lectura. La aplicación conserva límites de tiempo y saturación: todavía no aprueba todos los objetivos de rendimiento.** Los cambios se ejecutaron en la API local conectada a Supabase develop; no se modificó ni desplegó producción.

## Qué cambió

### Reservas e integridad

- Las reservas normales y Walk-in usan `READ COMMITTED`. La protección frente a carreras queda en una restricción de exclusión PostgreSQL, en vez de depender de una consulta previa y del aislamiento `SERIALIZABLE` sobre operaciones independientes.
- `citas_no_solapamiento_activo` impide intervalos superpuestos del mismo estilista. El rango `[inicio,fin)` permite franjas adyacentes. Las citas `Cancelled` no bloquean, conservando el criterio anterior. La restricción protege también escrituras directas a la BD.
- Se verifica que la duración sea positiva y termine el mismo día. Los horarios invertidos o que crucen medianoche se rechazan.
- Las cuentas antiguas sin perfil Cliente bloquean únicamente su usuario durante la creación de ese perfil y lo vuelven a consultar antes de insertar. Dos primeras reservas simultáneas comparten un solo perfil; las cuentas con perfil existente no necesitan ese bloqueo.
- Los códigos de cita usan una secuencia global inicializada después de los códigos existentes, evitando colisiones aleatorias entre solicitudes simultáneas. Conservan `SHU-año-` y al menos cuatro dígitos; pueden crecer y tener saltos por rollback.
- El backend verifica restricciones y secuencia antes de abrir HTTP. Una prueba de arranque real sin migración terminó con error y no abrió el puerto.

La migración está en `database/migrations/20261016_reservas_sin_solapamientos.sql`. Se aplicó a develop y al laboratorio local. Es repetible y no reinicia una secuencia existente. **Debe aplicarse antes de desplegar este backend a cualquier otro entorno.** Si hay citas inválidas o solapadas, la migración falla; no borra ni corrige registros automáticamente.

### Consultas y errores

- Se cargan usuario/rol y servicio/categoría juntos mediante EntityGraph, reduciendo consultas adicionales.
- El historial de citas del cliente se obtiene con los datos necesarios para su respuesta, evitando consultas por cada cita y cada servicio.
- La disponibilidad obtiene únicamente hora de inicio y fin de las citas, sin materializar clientes y servicios que no necesita.
- La reserva evita consultas repetidas de cliente/usuario y el chequeo de solapamiento previo a la inserción. PostgreSQL decide el conflicto atómicamente.
- HTTP 409 para solapamiento corresponde a SQLSTATE `23P01`; los fallos de conexión/serialización transitorios se distinguen como 503 y otros errores de persistencia como 500. Se preserva 409 para actualización optimista concurrente con un mensaje específico. Se eliminaron capturas genéricas que convertían errores de BD en un falso «horario ocupado».

No se añadieron cachés de cuentas ni se omitió la consulta de autenticación por solicitud.

## Verificación funcional

- **40 pruebas Java:** cero fallos, errores o casos omitidos; incluye lógica, respuestas HTTP y protección de arranque.
- **12 regresiones con PostgreSQL real:** solapamiento parcial, franjas adyacentes, otro estilista, cancelación/reactivación, horarios inválidos, ocho conexiones por una misma franja, fechas distintas concurrentes y secuencia sin duplicados. Todos pasaron. Sus filas propias de prueba se limpiaron.
- **40 reservas independientes:** guardadas de 40 solicitudes, sin rechazos inesperados ni solapamientos. Son tres lotes de diez del perfil inicial y un lote de diez del perfil con horarios completos.
- **Primera reserva de cuenta antigua:** dos solicitudes simultáneas en fechas diferentes se guardaron con un solo perfil Cliente y códigos distintos. Los registros propios de esa regresión se limpiaron.
- **Contención con 50 solicitudes:** una cita guardada, 49 conflictos esperados y cero solapamientos. También se verificó un lote de diez solicitudes.

La integridad funcional mejoró respecto del informe inicial, donde solo se guardaron 7 de 10 reservas independientes.

## Datos de prueba más representativos

Se detectó que el perfil inicial de disponibilidad devolvía una lista vacía porque los estilistas ficticios no tenían horarios. La semilla ahora crea horarios de lunes a sábado (09:00–17:00, almuerzo 12:00–13:00), utiliza fechas de esos días y no sobrescribe horarios ya existentes.

La disponibilidad usada en la medición final devolvió **15 franjas, 9 disponibles y 3 afectadas por la cita reservada**; las restantes se excluyen por almuerzo. La suite ya no permite iniciar con disponibilidad sin franjas. Las escrituras usan franjas exclusivas de prueba y la contención ocupa una franja del mismo día consultado por el escenario de lectura.

Las mediciones anteriores con disponibilidad vacía se conservan como antecedente. No son una comparación estadística exacta con este perfil: cambió la preparación de los datos y aumentó el trabajo real de disponibilidad.

## Medición final con horarios y citas

Objetivos iniciales propuestos: p95 lecturas <1 s; p95 escrituras <2 s; errores inesperados <1%; cero iteraciones descartadas. No se relajaron para hacer pasar las pruebas.

| Caso | Resultado funcional | p95 | Evaluación de tiempo |
|---|---|---:|---|
| Smoke, 10 recorridos | Cero errores | 1.206 s | No cumple <1 s |
| 10 reservas independientes | 10 guardadas; cero rechazos/solapamientos | 2.510 s | No cumple <2 s |
| 50 solicitudes por una franja | 1 guardada, 49 conflictos esperados; cero solapamientos | 5.779 s | No cumple <2 s |
| Smoke posterior al estrés | Cero errores en 10 recorridos | 1.514 s | Disponible; no cumple <1 s |

En tres repeticiones anteriores de reservas independientes se observaron p95 de 2.418, 1.992 y 1.099 s, siempre con diez citas guardadas. Esa variación impide declarar el objetivo de escritura cumplido de forma consistente.

### Rampa de lectura final

Mezcla: 50% catálogo, 30% disponibilidad y 20% citas propias. La secuencia prevista fue 5 → 10 → 20 → 40 → 80 recorridos/s, con rampas de diez segundos y mesetas de veinte segundos. Cada recorrido medido envía una consulta HTTP; la preparación de login se registra aparte.

La tabla siguiente agrupa solicitudes por la etapa en la que **se iniciaron**, usando las etiquetas HTTP. Solo incluye solicitudes completadas; las descartadas e interrumpidas se contabilizan por separado.

| Meseta ofrecida | Solicitudes completadas | p95 | Fallos HTTP |
|---|---:|---:|---:|
| 5 recorridos/s | 100 | 1.140 s | 0 |
| 10 recorridos/s | 200 | 0.908 s | 0 |
| 20 recorridos/s | 400 | 0.833 s | 0 |
| 40 recorridos/s | 622 | 13.381 s | 15 |
| 80 recorridos/s | 266 | 15.001 s | 56 |

El tramo de veinte recorridos/s cumplió los objetivos de lectura en esta meseta breve. **No constituye capacidad sostenida certificada**: duró veinte segundos, el generador comparte máquina con la API y no se hicieron tres repeticiones prolongadas.

Resultados globales de esta rampa: **2,412 recorridos completados**, **125 timeouts** (5.18% de los recorridos completados), **1,785 iteraciones descartadas** y **289 interrumpidas**. La prueba abortó alrededor de los 155 segundos de carga al cruzar el umbral acumulado de errores del 5%, antes de completar la recuperación programada.

Las iteraciones descartadas indican que no se pudo sostener la carga ofrecida con el máximo configurado de 300 usuarios virtuales. No son respuestas HTTP del servidor y no deben atribuirse automáticamente a PostgreSQL. El timeout de quince segundos limita los percentiles altos observados.

El smoke posterior confirmó que la API volvió a responder sin errores, pero no permite fijar el instante exacto de recuperación y continuó por encima del objetivo de un segundo.

### Comprobación desde arranque en frío

Se reinició la API y se ofrecieron veinte recorridos/s durante treinta segundos, con horarios y citas completos. Se completaron **592 recorridos**, con **cero errores HTTP**, **8 iteraciones descartadas** y p95 de **3.205 s**. Esta comprobación no cumplió los objetivos.

El resultado de 0.833 s de la meseta de la rampa no se debe extrapolar al arranque en frío ni considerarse estable en todos los casos. La variación requiere instrumentación y repeticiones comparables; no se atribuye una causa definitiva sin medir conexiones, consultas, CPU y red. No se modificaron umbrales para ocultar esta diferencia.

## Entorno y límites

Proyecto develop `mpmqotozaopmvmesgmgw`, Supabase Free/NANO, región Canadá Central (`ca-central-1`), pool Hikari de diez conexiones. API Java y k6 ejecutados localmente en la misma máquina. Estas cifras incluyen red entre la API y Supabase; no certifican el rendimiento de Render ni de producción en `us-east-1`.

No se midieron todavía UI móvil, tráfico directo Supabase del panel web, login sostenido, consultas administrativas, Realtime o Storage. No se atribuye un cuello de botella definitivo sin métricas continuas de pool, consultas, CPU y red.

## Evidencias y reproducción

Archivos del repositorio:

- `database/migrations/20261016_reservas_sin_solapamientos.sql`: requisito de despliegue.
- `scripts/performance/verify_reservations.py`: regresiones con PostgreSQL real.
- `scripts/performance/verify_startup_guard.py`: rechazo de arranque sin migración.
- `tests/performance/README.md`: comandos y configuración completa.
- `.performance/reservation-db-regression.json`, `.performance/guard-startup-verification.json`: comprobaciones de integridad/arranque.
- `.performance/final-regression-runs.json`: tres lotes de reservas y contención anteriores.
- `.performance/realistic-regression-runs.json`: mediciones finales con horarios.
- `.performance/availability-fixture-verification.json`: franjas disponibles y ocupadas.
- `.performance/realistic-benchmark-metadata.json`: SHA256 del JAR medido y carpeta de estrés; el JAR de esa rampa se conserva en `.performance/realistic-benchmark.jar`.
- `.performance/legacy-client-verification.json`: dos primeras reservas concurrentes de una cuenta antigua.
- `tests/performance/results/20261016T173529Z-soak/`: comprobación de treinta segundos desde arranque en frío, no prueba prolongada de duración.
- `tests/performance/results/20261016T172306Z-stress/`: rampa final; incluye informe HTML, métricas y `request-stages.json`.
- `tests/performance/results/20261016T172742Z-smoke/`: disponibilidad posterior.

Los secretos y resultados están excluidos de Git. La contraseña de develop permanece únicamente en el archivo local protegido. Los informes HTML corresponden al panel integrado de Grafana k6; no se configuró Grafana Cloud ni Grafana Server.

```bash
python3 scripts/performance/develop.py migrate
python3 scripts/performance/develop.py start
python3 scripts/performance/develop.py seed
python3 scripts/performance/verify_legacy_client.py
python3 scripts/performance/run.py booking --iterations 10
python3 scripts/performance/run.py contention --vus 50
python3 scripts/performance/run.py stress --quick
```

Antes de una certificación o despliegue productivo: instrumentar pool/consultas/red, repetir cargas sostenidas con infraestructura comparable y verificar la migración sobre datos reales mediante un proceso de despliegue revisado. Los rechazos funcionales detectados quedaron corregidos en desarrollo; los límites de latencia y carga siguen explícitos.

Referencias de implementación: [rangos y exclusión PostgreSQL 17](https://www.postgresql.org/docs/17/rangetypes.html), [EntityGraph en Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html), [proyecciones JPQL](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html).
