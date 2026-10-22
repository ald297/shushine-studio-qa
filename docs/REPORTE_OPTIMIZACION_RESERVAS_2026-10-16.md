# Reservas simultáneas: objetivo cumplido — 16 de octubre de 2026

## Resultado

Objetivo: **50 intentos simultáneos por la misma franja, p95 <2 s, una cita creada y 49 conflictos reales, sin errores inesperados ni solapamientos**. Las tres corridas finales cumplen, incluida una tras reiniciar la API. También se guardan las 50 reservas en fechas distintas.

| Prueba final | p95 | Citas creadas | Conflictos esperados | Errores inesperados | Pares solapados |
|---|---:|---:|---:|---:|---:|
| cold-50 | 1202 ms | 1 | 49 | 0 | 0 |
| repeat-50-a | 1112 ms | 1 | 49 | 0 | 0 |
| repeat-50-b | 1136 ms | 1 | 49 | 0 | 0 |
| independent-50 | 1591 ms | 50 | 0 | 0 | 0 |

La referencia anterior de esta sesión fue **6180 ms**; la medición histórica anterior fue aproximadamente 5.554 ms. Los resultados finales de disputa quedan entre 1.112 y 1.202 ms. Son bursts repetidos de 50 llamadas a `POST /api/citas`, no una prueba de 50 reservas por segundo sostenidas.

## Qué cambió y por qué

1. **Preparación en una lectura.** Para perfiles con Cliente existente, se obtiene cliente, estilista y servicios en un SELECT parametrizado. Se conservan la validación de estilista activo, la duración del mismo día y el cálculo de subtotal/IVA 13% en Java. Los precios siguen procediendo de la base en cada solicitud; solo se prepara y reutiliza metadata de serialización.

2. **Escritura atómica en una sentencia.** Un CTE obtiene el siguiente código de la secuencia, inserta la cita y todos sus detalles. El código conserva al menos cuatro dígitos sin truncar números más largos. Si falla un detalle o una restricción, PostgreSQL revierte la sentencia completa. No se guardan citas parciales. Se conservan versión inicial 0, estado Confirmed, pagos Pending, origen App y los mismos importes.

3. **Orden transaccional por agenda.** Antes de insertar se adquiere `pg_advisory_xact_lock` por estilista/día. El bloqueo espera y se libera al confirmar o revertir; no convierte una agenda ocupada en un rechazo. La restricción de exclusión sigue determinando el 409 por solapamiento real. La creación de perfiles antiguos y Walk-in adquiere el mismo bloqueo dentro de su transacción, antes de otras escrituras. Las franjas distintas del mismo día pueden esperar brevemente su turno, pero no se rechazan por ese motivo.

4. **Pool dimensionado con evidencia.** Con 10 conexiones, aun con menos viajes, hubo corridas de 1.498, 1.734 y 2.557 ms, y hasta 40 solicitudes esperando conexión. La espera media de adquisición llegó a 451 ms frente a 177 ms de uso. Se ajustó mínimo/máximo a **14**, preparadas antes de abrir HTTP. La base reportó 23 conexiones totales de un máximo de 60 antes del ajuste. Mantener cuatro conexiones adicionales consume recursos; el tamaño es configurable y no se extrapola a un despliegue con varias réplicas.

Los perfiles antiguos sin Cliente conservan el bloqueo del usuario y su creación transaccional para evitar perfiles duplicados. Los escritores directos de Supabase continúan protegidos por la restricción de exclusión; no participan automáticamente en el bloqueo de agenda de la API y no están incluidos en esta prueba de rendimiento.

## Verificación

- **56 pruebas Java**, cero fallos y errores.
- **5 comparaciones** antes/después: un servicio, varios servicios, IDs repetidos, combinación de ID válido/inexistente y todos inexistentes. Precios, IVA, duración, DTO y detalles coinciden; los IDs y códigos nuevos se validan por separado.
- **4 verificaciones SQL** sobre la sentencia real: FK de detalle fallida no deja cita, confirmación de cita/detalle, rechazo por exclusión y cancelación que libera la franja.
- **4 verificaciones HTTP**: adyacencia, otro estilista a la misma hora, cancelación por propietario con incremento de versión y solapamiento parcial con horas diferentes.
- **2 primeras reservas de perfil antiguo:** ambas creadas, un Cliente, dos códigos distintos.
- Falta de migración de integridad: arranque rechazado y puerto HTTP nunca abierto.
- Los fixtures de estas regresiones y de los bursts se eliminaron después de comprobar persistencia.

## Objetivo de lecturas conservado

Una corrida adicional de 10 minutos a 20 lecturas/s, con el pool nuevo, verifica los objetivos global y por ruta. Mezcla: 50% catálogo, 30% disponibilidad y 20% mis citas.

| Métrica | Resultado |
|---|---:|
| p95 global | 364 ms |
| Catálogo p95 | 309 ms |
| Disponibilidad p95 | 207 ms |
| Mis citas p95 | 389 ms |
| Errores inesperados | 0 |
| Descartes | 0 |

## Alcance y límites

API local y k6 comparten equipo; Supabase develop Free/NANO está en Canadá. Datos ficticios y esquema separado; **producción no se modificó ni recibió tráfico de prueba**. “Tras reiniciar” incluye el preflight y diez logins de preparación antes de medir, no la primera solicitud literal. Son tres bursts de disputa y uno independiente; no garantizan capacidad continua ni incluyen 50 perfiles antiguos, 50 Walk-in, administración directa, chat o Realtime.

La primera variante de escritura agrupada produjo interbloqueos durante la prueba y fue descartada. El bloqueo de agenda resolvió ese problema; las primeras repeticiones con pool 10 todavía variaban por encima de 2 s, motivo del ajuste a 14. Esas variantes no se desplegaron a producción. Las ventanas se ejecutaron en momentos distintos y la red puede variar.

La siguiente validación para capacidad productiva sería una carga mixta sostenida de reservas y lecturas, con API y base en la misma región y un generador separado. La integridad debe seguir comprobándose por los registros persistidos, no solo por HTTP.

Fuentes técnicas: [CTE de modificación de datos](https://www.postgresql.org/docs/17/queries-with.html#QUERIES-WITH-MODIFYING) y [bloqueos transaccionales de PostgreSQL](https://www.postgresql.org/docs/17/explicit-locking.html#ADVISORY-LOCKS).

## Evidencias

- [Corridas y verificaciones](/home/ministorm/salon-de-belleza/tests/performance/results/booking-optimized-20261017T052936Z/runs.json)
- [Hash del JAR](/home/ministorm/salon-de-belleza/tests/performance/results/booking-optimized-20261017T052936Z/metadata.json)
- [Referencia anterior](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T050823Z-contention/summary.json)
- [Lecturas 20 rps](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053233Z-soak/report.html)
- cold-50: [informe k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053003Z-contention/report.html), [persistencia e integridad](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053003Z-contention/verification.json)
- repeat-50-a: [informe k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053016Z-contention/report.html), [persistencia e integridad](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053016Z-contention/verification.json)
- repeat-50-b: [informe k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053029Z-contention/report.html), [persistencia e integridad](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053029Z-contention/verification.json)
- independent-50: [informe k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053042Z-booking/report.html), [persistencia e integridad](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T053042Z-booking/verification.json)
- [booking-contract-verification.json](/home/ministorm/salon-de-belleza/.performance/booking-contract-verification.json)
- [booking-atomicity-verification.json](/home/ministorm/salon-de-belleza/.performance/booking-atomicity-verification.json)
- [booking-api-races-verification.json](/home/ministorm/salon-de-belleza/.performance/booking-api-races-verification.json)
- [legacy-client-verification.json](/home/ministorm/salon-de-belleza/.performance/legacy-client-verification.json)
- [guard-startup-verification.json](/home/ministorm/salon-de-belleza/.performance/guard-startup-verification.json)
