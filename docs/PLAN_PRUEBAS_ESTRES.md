# Plan de pruebas de estrés — Shushine Studio

## Primary goal
Determinar cuánta carga puede atender la aplicación con tiempos aceptables, sin perder datos ni duplicar reservas, y comprobar su recuperación después de una sobrecarga.

Estado: planificación; no se ejecutaron pruebas ni se modificó producción. Las cifras siguientes son objetivos iniciales propuestos, no capacidades demostradas.

## Fase 0: Descubrimiento de documentación
**How this serves the primary goal:** asegura que las pruebas reproduzcan operaciones reales.

Evidencia revisada:
- `backend-api/pom.xml:8`: Spring Boot 3.3.3; Java 21, JPA, PostgreSQL y JWT.
- `backend-api/src/main/resources/application.properties:16`: pool Hikari máximo de 10 conexiones; investigar esperas y saturación durante la prueba. JWT con duración configurada de una hora: renovar durante pruebas largas.
- `backend-api/src/main/java/com/shushinestudio/controladores/AuthController.java:25`: POST `/api/auth/login`; cuerpo `{login,clave}`, respuesta con `token` según DTO UsuarioToken.
- `backend-api/src/main/java/com/shushinestudio/controladores/EstilistaController.java:79`: GET `/api/estilistas/{id}/disponibilidad`; copiar parámetros del controlador al preparar el script.
- `backend-api/src/main/java/com/shushinestudio/controladores/CitaController.java:39`: POST `/api/citas`; copiar estructura exacta del DTO de entrada y autenticación Bearer.
- `backend-api/src/main/java/com/shushinestudio/servicios/implementaciones/CitaService.java:49`: transacciones SERIALIZABLE y validación de solapamiento.
- `docs/REPORTE_CONCURRENCIA_RNF02.md`: antecedente declarado de 10 solicitudes, una reserva y nueve conflictos; no demuestra capacidad general ni garantiza simultaneidad exacta.

Herramienta: Grafana k6 CLI, con scripts JavaScript versionados. Genera tráfico HTTP contra la API usada por móvil y web; no mide fluidez de la interfaz MAUI. Documentación permitida:
- https://grafana.com/docs/k6/latest/using-k6/scenarios/executors/ramping-arrival-rate/
- https://grafana.com/docs/k6/latest/using-k6/scenarios/executors/constant-arrival-rate/
- https://grafana.com/docs/k6/latest/using-k6/thresholds/
- https://grafana.com/docs/k6/latest/using-k6/metrics/

Usar `k6/http`, escenarios documentados, checks y thresholds. Verificar contratos en código/OpenAPI del despliegue antes de implementar. No inventar parámetros ni interpretar usuarios virtuales como solicitudes por segundo.

## Fase 1: Preparar entorno, datos y monitoreo
**How this serves the primary goal:** permite medir sin contaminar las reservas reales y explicar los límites observados.

Preparar una instancia de pruebas con recursos equivalentes al despliegue objetivo y una base PostgreSQL separada. Registrar versión del código, CPU/RAM, límites del hosting, tamaño de datos y pool de conexiones. Ejecutar k6 desde otra máquina y observar también su CPU/red.

Crear clientes y administradores de prueba, servicios activos, estilistas con horarios válidos y suficientes fechas/franjas libres. Identificar cada registro creado para su limpieza posterior. Desactivar efectos externos reales. Pasar URL, usuarios y claves mediante variables de entorno; no guardar secretos en scripts ni evidencias.

Observar tiempos HTTP por operación, errores inesperados, operaciones completadas/segundo, iteraciones descartadas, CPU, memoria, reinicios, conexiones/esperas de BD, consultas lentas y bloqueos. Usar métricas del hosting y BD; agregar instrumentación si esas métricas no están disponibles.

Verificación: una sesión cliente y una administrativa completan sus flujos; la limpieza funciona; no hay llamadas al entorno productivo. Separar calentamiento de medición, incluyendo arranque en frío si aplica.

## Fase 2: Construir escenarios reales
**How this serves the primary goal:** mide las operaciones que afectan a clientes y personal.

Preparar scripts separados para catálogo/disponibilidad, login, reservas válidas, contención de reservas y consultas administrativas. Copiar contratos de los controladores/DTO y adaptar ejemplos HTTP oficiales de k6.

Mezcla inicial orientativa: 50% catálogo, 25% disponibilidad, 15% consultas autenticadas y 10% creación de citas. Ajustarla con tráfico real cuando exista. Medir login por separado para no simular que cada consulta vuelve a iniciar sesión. Los usuarios deben tener cuentas y tokens adecuados a su rol.

Para reservas válidas asignar franjas independientes; para contención usar deliberadamente una misma franja inicialmente libre. No prometer simultaneidad exacta: medir solicitudes superpuestas. Repetir lotes de 10, 25 y 50 competidores, cada lote con una franja nueva; debe quedar una sola cita activa y cero solapamientos. Confirmarlo en BD además de las respuestas HTTP.

Atención: `CitaController.java:59-64` convierte varias excepciones de persistencia/transacción en 409. Un 409 por sí solo no prueba un conflicto legítimo; correlacionar logs, respuesta y BD. En carga normal, los 409 inesperados son fallos; en contención, solo excluir conflictos verificados de las métricas de error. No excluir todos los 4xx globalmente.

Verificación: ejecutar cada flujo con carga mínima y revisar códigos, contenido y efectos persistidos. No usar la rampa de llegada como barrera sincronizada.

## Fase 3: Ejecutar carga, estrés, picos y duración
**How this serves the primary goal:** encuentra el límite sostenido y comprueba estabilidad y recuperación.

| Prueba | Carga inicial propuesta | Duración | Propósito |
|---|---|---|---|
| Validación del script | 1–2 usuarios virtuales | 2 min | Detectar fallos de datos, tokens y contratos |
| Línea base | 5 recorridos iniciados/segundo | 10 min | Medir rendimiento con carga baja |
| Estrés escalonado | 5 → 10 → 20 → 40 → 80 recorridos/segundo | Rampa 1 min + meseta 5 min por nivel | Encontrar primer nivel que incumple objetivos |
| Pico | Última carga estable → triple → carga estable | 5 min + 1 min + 5 min | Medir impacto y recuperación |
| Duración | 60–70% de última carga estable | 60 min inicialmente; luego 2 h | Detectar degradación, fugas y agotamiento |
| Contención de citas | Lotes de 10, 25 y 50 solicitudes superpuestas | Al menos 5 lotes por nivel | Validar integridad de reservas |

Un recorrido puede enviar varias solicitudes: registrar ambas tasas. Usar `ramping-arrival-rate` para estrés y `constant-arrival-rate` para mesetas/duración, según documentación oficial; dimensionar usuarios virtuales para sostener la tasa. Las iteraciones descartadas y la saturación del generador impiden atribuir automáticamente el límite al servidor.

Objetivos iniciales a confirmar: p95 de lecturas menor a 1 s; p95 de login/reserva menor a 2 s; errores inesperados inferiores al 1%; cero reservas duplicadas o pérdida de datos. p95 significa que 95% de las respuestas cumple ese tiempo. Medir por operación y meseta; no ocultar fallos con un promedio global.

En estrés es esperable cruzar estos objetivos: el resultado es el último nivel estable y el primer nivel degradado. Detener aumento de carga ante errores inesperados superiores al 5% durante 30 s, p95 superior a 5 s durante 60 s, reinicios repetidos o cualquier problema de integridad. Registrar el motivo, bajar a línea base y observar 5 min; proponer recuperación en menos de 2 min.

Comando previsto, una vez implementados los scripts e instalado k6:

```bash
k6 run -e BASE_URL=https://URL-DEL-ENTORNO-DE-PRUEBAS tests/performance/stress.js
```

Ese archivo aún no existe: esta entrega es el plan. El límite de 80 recorridos/s es un techo exploratorio inicial, no un requisito ni una garantía. Si no hay degradación, planear una extensión controlada.

Verificación: guardar métricas por etapa, logs correlacionados y resultados de integridad; registrar si una parada fue por servidor, BD, generador o datos agotados.

## Fase 4: Verificar y entregar evidencias
**How this serves the primary goal:** convierte las mediciones en una capacidad demostrable y acciones concretas.

Revisar scripts contra contratos y documentación, comprobar filtros de errores y renovación de tokens, y repetir al menos tres veces la carga máxima estable para evaluar variación. Después de cambios correctivos repetir línea base y escenario afectado con el mismo entorno y datos equivalentes.

Entregar informe con fecha, versión, entorno, mezcla de tráfico, carga ofrecida/completada, p50/p95/p99 por operación, errores por causa, gráficas, integridad de reservas, tiempo de recuperación y cuello de botella con evidencia. Incluir evidencia en Azure DevOps si se incorpora al proceso del equipo. Limpiar exclusivamente registros identificados de prueba y confirmar la limpieza.

Resultado esperado: capacidad estable para la mezcla probada, primer punto de degradación, comportamiento de recuperación y mejoras priorizadas. No extrapolarlo a usuarios reales sin un modelo de frecuencia de uso.

Fuera de alcance inicial: estrés en producción, pruebas visuales de rendimiento móvil, cambios de infraestructura e implementación de scripts durante esta planificación.

### Límite de cobertura y comprobación omnicanal
El panel `admin-web/dashboard.html:1211-1212` también usa Supabase directamente y Realtime. Medir únicamente Spring Boot no cubre toda la aplicación. Inventariar esas consultas durante la fase 0 y reproducir su tráfico autorizado en el entorno aislado; mantener un cliente web suscrito durante la carga para medir propagación de citas. El plan formal `docs/PLAN_DE_PRUEBAS_FORMAL_IEEE829.md:90` exige propagación inferior a 5 s: registrarla como criterio independiente. La estabilidad de Android con ADB Monkey queda como complemento posterior, distinto de capacidad del backend.

Contrato confirmado para disponibilidad: `GET /api/estilistas/{id}/disponibilidad?fecha=YYYY-MM-DD&duracionMinutos=N` o `servicioId=N`. Reserva según `dtos/cita/CitaGuardar.java:18-31`: `estilistaId`, `fechaCita`, `horaInicio`, `servicioIds`, `notasCliente`, `metodoPagoPreferente`. Reutilizar ejemplos de `postman/Shushine_Studio_API.postman_collection.json` tras validar contra el código actual.
