# Informe inicial de rendimiento — ShushineStudio develop

Fecha: 16 de octubre de 2026, America/El_Salvador. Resultado: **entorno preparado y pruebas ejecutadas; objetivos de rendimiento incumplidos**.

## Entorno y aislamiento

- Proyecto Supabase independiente: `ShushineStudio-develop`, referencia `mpmqotozaopmvmesgmgw`, organización `ShushineStudio-develop`, región Canadá Central (`ca-central-1`), plan Free, cómputo NANO.
- Se importó el esquema public de producción: **28 tablas**, sin filas de producción, propietarios ni grants. La importación se ejecutó transaccionalmente y el conteo se verificó mediante SQL y en el dashboard.
- La aplicación creó sus registros de demostración y se generaron diez cuentas ficticias para las pruebas. Las contraseñas solo están en archivos locales excluidos de Git, con permisos 0600.
- API Spring Boot ejecutada localmente en `127.0.0.1:18080`, conectada al session pooler 5432 de develop. Pool Hikari máximo: 10 conexiones. Generador k6 2.3.0 en la misma máquina que la API.
- Producción (`acikahicfjtojuvqcvxv`) solo se leyó para exportar el esquema; no recibió carga ni cambios de datos/esquema.
- Se pausó, con autorización, `ald297's Project` (`fbxygbcgnpgtfplwpkxu`) para liberar el cupo gratuito. No se contrató Pro ni se creó una rama de pago.

La latencia incluye API local, conexión de red y Supabase Canadá. No certifica el rendimiento de Render, ni reproduce completamente el panel web, Auth Supabase, Realtime o Storage. La región difiere de producción (`us-east-1`).

## Objetivos iniciales

p95 de lecturas menor a 1 segundo; p95 de reservas menor a 2 segundos; menos de 1% de errores inesperados; cero solapamientos. Estos son objetivos propuestos, no acuerdos de servicio confirmados. p95 significa que el 95% de las respuestas tarda ese tiempo o menos.

## Resultados

| Prueba | Resultado funcional | p95 | Evaluación |
|---|---|---:|---|
| Smoke inicial | 10 recorridos; cero errores | 1.203 s | Supera objetivo de lectura |
| Reservas independientes | 7 citas guardadas de 10; 3 respuestas 409 inesperadas; cero solapamientos | 2.666 s | Falla funcional y de tiempo |
| Contención, misma franja | 1 cita guardada; 9 respuestas 409; cero solapamientos | 2.626 s | Integridad observada correcta; supera objetivo de tiempo |
| Estrés exploratorio | 1,269 recorridos completados; 97 timeouts, 500 iteraciones descartadas y 300 interrumpidas al abortar | 15.000 s global | Parada automática por exceso de errores |
| Smoke posterior | 10 recorridos; cero errores | 1.500 s | Vuelve a responder sin errores; no cumple objetivo de tiempo |

Las reservas independientes se asignaron a fechas distintas, con clientes ya existentes. El backend convierte excepciones de persistencia/transacción en HTTP 409, por lo que esos rechazos no se excluyen como conflictos esperados. Los logs de la API registran errores PostgreSQL de serialización; se requiere investigación de transacciones y reintentos antes de atribuir una causa definitiva a cada rechazo.

En contención, una sola fila persistida y cero solapamientos son evidencia de integridad para este lote. No prueban que cada 409 se deba exclusivamente a un solapamiento legítimo.

## Rampa exploratoria

Configuración prevista: 5 → 10 → 20 → 40 → 80 recorridos/s, con rampas de 10 segundos y mesetas de 20 segundos; retorno a 5 recorridos/s. Mezcla: 50% catálogo, 30% disponibilidad y 20% citas propias. Cada recorrido medido ejecuta una consulta HTTP; la autenticación se prepara por separado.

La prueba se detuvo alrededor de los 120 segundos de carga, dentro de la meseta de 40 recorridos/s, al superar el umbral acumulado del 5% de errores inesperados. **No se alcanzó el tramo de 80 ni la recuperación programada**; por eso se ejecutó un smoke posterior.

| Meseta | Respuestas observadas en el período | p95 | Errores observados |
|---|---:|---:|---:|
| 5 recorridos/s | 100 | 0.860 s | 0 |
| 10 recorridos/s | 200 | 0.829 s | 0 |
| 20 recorridos/s | 279 | 6.690 s | 0 |
| 40 recorridos/s | 309 | 15.001 s | 97 |

Las etiquetas de etapa de las métricas personalizadas se asignan al completar la respuesta: una respuesta puede haberse iniciado en la rampa anterior. Las muestras de la tabla no equivalen a tráfico ofrecido en esa meseta. Los timeouts se fijaron en 15 segundos, por lo que el percentil de la última etapa refleja ese límite.

El error inesperado global fue 97/1,269 = **7.64%**. Las iteraciones descartadas indican que se agotó el presupuesto configurado de usuarios virtuales (máximo 300) al aumentar las latencias; no son respuestas HTTP del servidor. Se deben analizar junto con el generador y el servicio, y no tratarse automáticamente como fallos de la BD.

El tramo de 10 recorridos/s es el último tramo breve observado que cumplió los objetivos de lectura. **No es una capacidad sostenida certificada**: las mesetas duraron solo veinte segundos y no se hicieron tres repeticiones. El smoke inicial ya mostró variación alrededor del objetivo de un segundo.

El smoke de recuperación empezó a las 10:41:21, después de la parada a las 10:40:32, y terminó a las 10:41:52. Respondió sin errores con p95 de 1.500 s. Esto confirma disponibilidad en esa ventana, no un instante exacto de recuperación ni cumplimiento del objetivo de latencia.

## Evidencias

Rutas relativas al repositorio:

- `tests/performance/results/20261016T163651Z-smoke/`: smoke inicial.
- `tests/performance/results/20261016T163736Z-booking/`: reservas independientes.
- `tests/performance/results/20261016T163802Z-contention/`: contención.
- `tests/performance/results/20261016T163826Z-stress/`: estrés exploratorio; `stages.json` contiene agregados por etapa.
- `tests/performance/results/20261016T164121Z-smoke/`: comprobación posterior.
- `.performance/api-develop.log`: logs de la API durante las pruebas.
- `.performance/develop-schema-verification.json`: verificación del esquema; los dos usuarios registrados en ese momento eran los de demostración del inicializador.
- `.performance/supabase-develop-28-tables.png`: evidencia visual del conteo.
- `.performance/stress-resource-samples.jsonl`: dos muestras puntuales de memoria/carga y actividad PostgreSQL; no constituyen monitoreo continuo. La segunda muestra se tomó después del aborto. Las esperas `ClientRead` no se deben interpretar como saturación de CPU de PostgreSQL.

Los resultados están excluidos de Git para conservar métricas/evidencia local sin publicar datos de ejecución. Los archivos HTML generados son los informes del panel integrado de Grafana k6; no se configuró Grafana Cloud ni Grafana Server.

## Reproducir

Con la API de develop activa:

```bash
python3 scripts/performance/run.py smoke
python3 scripts/performance/run.py booking --iterations 10
python3 scripts/performance/run.py contention --vus 10
python3 scripts/performance/run.py stress --quick
```

Para iniciar de nuevo:

```bash
python3 scripts/performance/develop.py check
python3 scripts/performance/develop.py start
python3 scripts/performance/develop.py seed
```

No repetir `initialize`: develop ya tiene el esquema y el comando rechaza bases no vacías. Los perfiles largos siguen disponibles: `baseline`, `stress` sin `--quick`, `spike` y `soak`. No se ejecutaron aún como certificación prolongada.

## Trabajo siguiente

1. Investigar y corregir los rechazos de reservas independientes; conservar la protección frente a dobles reservas.
2. Instrumentar pool Hikari, consultas, CPU, memoria y red para localizar el origen de las latencias y los timeouts.
3. Repetir mesetas largas y al menos tres ejecuciones comparables, usando un generador separado y un backend de pruebas con recursos equivalentes al despliegue objetivo.
4. Incorporar escenarios de login, consultas administrativas y tráfico directo Supabase/Realtime del panel web, actualmente fuera de la mezcla.

El entorno de pruebas y sus comandos quedaron operativos. La aplicación todavía no aprueba los objetivos iniciales de rendimiento e integridad funcional de reservas independientes.
