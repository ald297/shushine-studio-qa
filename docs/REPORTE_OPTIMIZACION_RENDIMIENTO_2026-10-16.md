# Optimización de rendimiento — 16 de octubre de 2026

## Objetivo y resultado

Objetivo elegido a partir del diagnóstico: **20 lecturas/s durante 10 minutos, tanto tras reiniciar como en caliente; p95 global y de cada ruta <1.000 ms, cero errores HTTP, cero descartes y respuestas JSON válidas**. Mezcla: 50% catálogo, 30% disponibilidad y 20% mis citas.

**Objetivo cumplido en las dos corridas.** La prueba corresponde a API local + Supabase develop Free/NANO en Canadá, no al despliegue productivo. Producción no fue modificada ni recibió tráfico de pruebas.

| Corrida a 20 rps | p95 global anterior | p95 global nuevo | Reducción | HTTP inesperados | Descartes | Criterios nuevos |
|---|---:|---:|---:|---:|---:|---|
| cold-20 | 1625 ms | 365 ms | 77.5% | 0 | 0 | Cumple |
| warm-20 | 1120 ms | 377 ms | 66.3% | 0 | 0 | Cumple |

La corrida anterior en frío tuvo 34 descartes del generador; la tabla muestra los descartes nuevos. Los criterios nuevos incluyen p95 individual de las tres rutas, más exigente que el objetivo global anterior.

| Corrida | Catálogo p95 | Disponibilidad p95 | Mis citas p95 | Solicitudes medidas |
|---|---:|---:|---:|---:|
| cold-20 | 325 ms | 216 ms | 409 ms | 12000 |
| warm-20 | 379 ms | 281 ms | 401 ms | 12001 |

## Cambios aplicados

- **Una consulta para disponibilidad.** Un `SELECT` parametrizado obtiene estilista, horario, duración del servicio, bloqueos y citas; las agregaciones independientes evitan multiplicar filas al combinar bloqueos y reservas. Se reemplazan cinco consultas de negocio sucesivas por una; la lectura del usuario para autenticar cada solicitud continúa funcionando.

- **Consistencia y datos actuales.** La consulta usa un único snapshot de PostgreSQL, sin caché de disponibilidad ni perfiles de autenticación. Se conservan reglas de almuerzo, bloqueos, citas canceladas, duración explícita y jornadas sin horario. Una cancelación directa en Supabase se refleja en la siguiente lectura.

- **Pool preparado antes de HTTP.** El mínimo de conexiones pasa de 2 a 10; el máximo sigue en 10. La inicialización toma y valida esa capacidad antes de abrir el conector HTTP. Si la base no está preparada, falla el arranque y devuelve los recursos adquiridos. Esto mantiene hasta 8 conexiones inactivas adicionales y puede alargar el arranque; el tamaño sigue siendo configurable por propiedades Spring.

- **Protección del cálculo horario.** El recorrido usa tiempo lineal, conserva segundos y evita que `LocalTime` vuelva a medianoche y genere un bucle infinito. Se rechazan duraciones de 24 h o mayores en jornadas con horario, coherente con las reservas del mismo día.

- **Objetivo automático por ruta.** k6 ahora verifica p95 <1 s para catálogo, disponibilidad y mis citas por separado, además de los criterios anteriores. La serie permite elegir `--rates 20`, conserva métricas y arranca automáticamente el observador SQL. Se añadió un timer para la nueva lectura JDBC.

## Evidencia de conexiones

| Corrida | Cola máxima anterior | Cola máxima nueva | Muestras con cola | Espera media de adquisición | CPU media API |
|---|---:|---:|---:|---:|---:|
| cold-20 | 74 | 0 | 0.0% | 0.1 ms | 9.0% de un núcleo |
| warm-20 | 20 | 0 | 0.0% | 6.4 ms | 5.1% de un núcleo |

Los valores de adquisición incluyen validación de conexiones; no son exclusivamente tiempo en cola. Los máximos muestreados cada 2 s pueden omitir picos. El timer de disponibilidad incluye adquirir conexión, ejecución, transporte y decodificación; PostgreSQL se mide por separado con `pg_stat_statements`.

Al terminar la serie, una sonda de 20 `SELECT 1` en una conexión ya abierta tuvo mediana cliente de **100.1 ms**, frente a 89,2 ms en la sonda anterior. PostgreSQL ejecutó el SQL trivial en **0.019 ms**. El coste del recorrido sigue presente; la sonda incluye cliente, protocolo y pooler, y no es RTT puro. [Evidencia](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261017T043606Z/database-roundtrip-after.json).

## Regresiones y límite pendiente

- Java: **51 pruebas**, 0 fallos, 0 errores.
- Contrato/frescura: **9 verificaciones** con fixtures desechables en develop; limpiados al terminar.
- Falta de migración: arranque rechazado `True`, puerto HTTP abierto `False`.
- 10 reservas independientes: 10 citas persistidas, 0 pares solapados, p95 1846 ms; criterios k6 cumplidos.
- 50 reservas simultáneas por la misma franja: 1 citas persistidas, 0 pares solapados, p95 5554 ms; integridad correcta, objetivo de latencia de escritura no cumplido.

**Pendiente:** la disputa de 50 reservas por una sola franja sigue por encima del objetivo de escritura de 2 s. Los 49 conflictos esperados preservan la integridad, pero no prueban rapidez. El objetivo logrado aquí es la mezcla de lecturas a 20 rps; no se ha demostrado 20 reservas/s ni capacidad de login masivo, administración, chat o Realtime.

El siguiente trabajo sobre escrituras debe medir qué consultas y bloqueos consumen tiempo dentro de `crearCita`, reducir viajes conservando reglas de precio/puntos y repetir pruebas de reservas independientes y en conflicto. Para validar capacidad real también conviene ejecutar la API y la base en la misma región y medir con un generador separado.

## Método y límites

Dos corridas de 10 minutos a 20 rps, con reinicio de la API para la primera y calentamiento de 2 minutos antes de la segunda. Preflight y 10 logins preceden a la medición; “frío” no significa la primera solicitud literal ni reiniciar Supabase. No se hicieron compilaciones ni regresiones en paralelo con las corridas medidas. API y k6 comparten equipo. Los resultados comparan el conjunto de cambios, sin aislar cuantitativamente la contribución de cada uno. Las ventanas anteriores y nuevas ocurrieron en momentos diferentes; las condiciones de red/plataforma también pueden variar.

Se conserva la configuración y fecha del fixture de carga anterior. Las citas nuevas de las pruebas de integridad se eliminaron por su identificador exclusivo tras verificar persistencia. Los cambios están preparados en el repositorio y activos en la API del laboratorio; no se desplegaron a producción.

Fuentes de diseño: [HikariCP: configuración del pool](https://github.com/brettwooldridge/HikariCP) y [PostgreSQL: snapshots por sentencia](https://www.postgresql.org/docs/17/transaction-iso.html).

## Reproducción y archivos

```bash
python3 scripts/performance/develop.py start
python3 scripts/performance/sustained.py --rates 20
python3 scripts/performance/analyze_sustained.py tests/performance/results/sustained-FECHA
```

Compilar antes el JAR con el runtime Maven/Java del README. Para una prueba diferencial de contrato, capturar `verify_availability_contract.py before` en la versión anterior y ejecutar `after` en la nueva.

- [Análisis nuevo](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261017T043606Z/analysis.json)
- [Metadatos y hash del JAR](/home/ministorm/salon-de-belleza/tests/performance/results/sustained-20261017T043606Z/metadata.json)
- [Contrato/frescura](/home/ministorm/salon-de-belleza/.performance/availability-contract-verification.json)
- [Reservas e integridad](/home/ministorm/salon-de-belleza/.performance/optimization-integrity.json)
- cold-20: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T043629Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T043629Z-soak/summary.json)
- warmup-20: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T044635Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T044635Z-soak/summary.json)
- warm-20: [HTML k6](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T044842Z-soak/report.html), [resumen](/home/ministorm/salon-de-belleza/tests/performance/results/20261017T044842Z-soak/summary.json)
