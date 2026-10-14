# Reporte Oficial de Certificación de Concurrencia y Transaccionalidad (RNF02) — Shushine Studio

> **Proyecto:** Shushine Studio — Sistema Móvil de Gestión y Reservas para Salón de Belleza  
> **Institución:** Escuela Superior Franciscana Especializada – AGAPE (ESFE AGAPE / MEGATEC)  
> **Responsable:** Alex Fernando Alfaro Diaz (Backend Lead & Scrum Master)  
> **Fecha de Certificación:** 25 de Septiembre de 2026  
> **Entorno de Prueba:** Render Cloud (`https://shushine-studio.onrender.com`) enlazado a Supabase PostgreSQL en AWS  
> **Requisito No Funcional Validado:** **RNF02 - Integridad y Concurrencia Transaccional en Reservas (Double-Booking Prevention)**

---

## 1. Resumen Ejecutivo de la Prueba

Se ejecutó una prueba de estrés y contención transaccional sobre el endpoint transaccional crítico `POST /api/citas` de la Web API de Shushine Studio desplegada en producción en Render Cloud.

El objetivo fue someter al sistema a condiciones extremas de carrera (*race condition*), simulando **10 peticiones HTTP concurrentes sincronizadas en el mismo milisegundo exacto**, compitiendo agresivamente por el **mismo estilista (`ID: 1`), misma fecha (`2026-12-01`) y mismo slot de inicio (`12:00`)**.

| Métrica Evaluada | Resultado Obtenido | Estado |
| :--- | :--- | :--- |
| **Peticiones Concurrentes Disparadas** | 10 hilos simultáneos | ✅ Ejecutado |
| **Sincronización de Hilos** | Barrera temporal a nivel de microsegundos | ✅ Sincronizado |
| **Reservas Aprobadas (201 Created)** | **1** (Cita `SHU-2026-3476`) | ✅ Éxito Esperado |
| **Reservas Bloqueadas (409 Conflict)** | **9** (Mensaje RFC 7807) | ✅ Bloqueo Seguro |
| **Tasa de Doble Reserva (Overbooking)** | **0.00%** (Cero reservas duplicadas) | 🏆 **Cumplimiento Total** |
| **Errores No Controlados (HTTP 500)** | **0** | 🛡️ **Blindaje Completo** |

---

## 2. Detalle de Ejecución por Hilo Concurrente

```text
=================================================================
⚡ PRUEBA DE ESTRÉS Y CONCURRENCIA TRANSACCIONAL (RNF02)
Objetivo: https://shushine-studio.onrender.com/api/citas
=================================================================

1. Obteniendo token JWT para usuario cliente...
✅ Token obtenido exitosamente.

2. Preparando 10 peticiones simultáneas...
   - Estilista ID: 1
   - Fecha disputada: 2026-12-01
   - Horario disputado: 12:00

3. Resultados de las peticiones concurrentes:
-----------------------------------------------------------------
Hilo     | Status Code  | Latencia   | Resultado / Detalle
-----------------------------------------------------------------
Hilo #1  | 409          | 3067.22 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #2  | 409          | 2174.55 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #3  | 409          | 3071.25 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #4  | 409          | 3068.43 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #5  | 201          | 1681.22 ms | ✅ CITA CREADA EXITOSA (SHU-2026-3476)
Hilo #6  | 409          | 2368.97 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #7  | 409          | 2088.95 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #8  | 409          | 2168.89 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #9  | 409          | 3066.16 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
Hilo #10 | 409          | 3071.59 ms | 🛡️ BLOQUEO CONCURRENCIA (El estilista seleccionado ya no tiene disponible la franja horaria solicitada.)
-----------------------------------------------------------------

📊 RESUMEN TRANSACCIONAL RNF02:
- Peticiones totales disparadas: 10
- Reservas aprobadas (201 Created): 1 (Esperado: 1)
- Reservas bloqueadas por solapamiento (409 Conflict): 9 (Esperado: 9)

🏆 CERTIFICACIÓN ACID RNF02: ¡PRUEBA SUPERADA CON ÉXITO ABSOLUTO!
El motor transaccional de Shushine Studio garantiza la prevención total de overbooking.
```

---

## 3. Arquitectura y Mecanismos de Protección Implementados

### 3.1 Nivel de Aislamiento Transaccional (`SERIALIZABLE`)
El servicio `CitaService` opera bajo la directiva de aislamiento más estricta del estándar SQL:
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public CitaSalida crearCita(CitaGuardar citaGuardar, String userLogin)
```
Esto instruye al motor relacional PostgreSQL a aplicar el algoritmo **SSI (Serializable Snapshot Isolation)**, garantizando que el resultado de ejecutar transacciones concurrentes sea indistinguible de haberlas ejecutado una por una de manera secuencial.

### 3.2 Verificación de Solapamiento Atómica en Repositorio
Antes de persistir la entidad, el backend valida la no existencia de citas activas en conflicto:
```sql
SELECT COUNT(c) > 0 FROM Cita c 
WHERE c.estilista.id = :estilistaId 
  AND c.fechaCita = :fecha 
  AND c.estadoCita NOT IN ('CANCELADA', 'NO_ASISTIO')
  AND (:horaInicio < c.horaFin AND :horaFin > c.horaInicio)
```

### 3.3 Adherencia al Estándar RFC 7807 (Problem Details)
Toda contención de bloqueo o serialización es capturada y estandarizada mediante `GlobalExceptionHandler` y los controladores REST, retornando `application/problem+json`:
```json
{
  "type": "https://httpstatuses.io/409",
  "title": "Conflict",
  "status": 409,
  "detail": "El estilista seleccionado ya no tiene disponible la franja horaria solicitada.",
  "instance": "/api/citas"
}
```
Esto permite al cliente móvil en .NET MAUI interceptar el error a través de su `ErrorDelegatingHandler` y mostrar un diálogo amigable en español al usuario sin que la aplicación sufra cierres inesperados.

---

## 4. Conclusión

La Web API de **Shushine Studio** cumple formalmente al 100% con los requerimientos de fiabilidad transaccional, tolerancia a fallos concurrentes y prevención de doble reserva (**RNF02**), quedando lista para su integración directa y consumo desde los ViewModels de la aplicación móvil .NET MAUI.
