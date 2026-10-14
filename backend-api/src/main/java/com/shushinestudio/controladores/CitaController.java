package com.shushinestudio.controladores;

import com.shushinestudio.dtos.cita.CitaCambiarEstado;
import com.shushinestudio.dtos.cita.CitaGuardar;
import com.shushinestudio.dtos.cita.CitaSalida;
import com.shushinestudio.dtos.cita.CitaWalkinGuardar;
import com.shushinestudio.dtos.timeline.TimelineItemSalida;
import com.shushinestudio.servicios.interfaces.ICitaService;
import com.shushinestudio.servicios.interfaces.ITimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Endpoints para el motor transaccional de citas, reservas y gestión de estados de Shushine Studio")
@SecurityRequirement(name = "Bearer Authentication")
public class CitaController {

    @Autowired
    private ICitaService citaService;

    @Autowired
    private ITimelineService timelineService;

    @PostMapping
    @Operation(summary = "Crear nueva reserva de cita", description = "Crea una cita atómicamente con verificación de disponibilidad de estilista y cálculo financiero automático.")
    public ResponseEntity<?> crearCita(@Valid @RequestBody CitaGuardar citaGuardar, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of(
                    "status", 401,
                    "title", "Unauthorized",
                    "detail", "Token de autenticación ausente o inválido."
            ));
        }
        try {
            String login = authentication.getName();
            CitaSalida resultado = citaService.crearCita(citaGuardar, login);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                    "status", 409,
                    "title", "Conflict",
                    "detail", e.getMessage()
            ));
        } catch (org.springframework.dao.DataAccessException | jakarta.persistence.PersistenceException | org.springframework.transaction.TransactionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                    "status", 409,
                    "title", "Conflict",
                    "detail", "El estilista seleccionado ya no tiene disponible la franja horaria solicitada."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "status", 400,
                    "title", "Bad Request",
                    "detail", e.getMessage()
            ));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("serialize") || msg.contains("lock") || msg.contains("concurren") || msg.contains("solapamiento") || msg.contains("conflict") || msg.contains("duplicate")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                        "status", 409,
                        "title", "Conflict",
                        "detail", "El estilista seleccionado ya no tiene disponible la franja horaria solicitada."
                ));
            }
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/walkin")
    @Operation(summary = "Registrar cita presencial Walk-in", description = "Permite a recepción o estilista registrar inmediatamente a un cliente presencial sin usuario previo.")
    public ResponseEntity<?> crearCitaWalkin(@Valid @RequestBody CitaWalkinGuardar walkinGuardar) {
        try {
            CitaSalida resultado = citaService.crearCitaWalkin(walkinGuardar);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                    "status", 409,
                    "title", "Conflict",
                    "detail", e.getMessage()
            ));
        } catch (org.springframework.dao.DataAccessException | jakarta.persistence.PersistenceException | org.springframework.transaction.TransactionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                    "status", 409,
                    "title", "Conflict",
                    "detail", "El estilista seleccionado ya no tiene disponible la franja horaria solicitada."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "status", 400,
                    "title", "Bad Request",
                    "detail", e.getMessage()
            ));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("serialize") || msg.contains("lock") || msg.contains("concurren") || msg.contains("solapamiento") || msg.contains("conflict") || msg.contains("duplicate")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of(
                        "status", 409,
                        "title", "Conflict",
                        "detail", "El estilista seleccionado ya no tiene disponible la franja horaria solicitada."
                ));
            }
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/timeline")
    @Operation(summary = "Obtener agenda timeline diaria", description = "Retorna la agenda cronológica de citas para una fecha y opcionalmente filtrada por estilista.")
    public ResponseEntity<List<TimelineItemSalida>> timeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer estilistaId
    ) {
        return ResponseEntity.ok(timelineService.obtenerTimeline(fecha, estilistaId));
    }

    @GetMapping("/mis-citas")
    @Operation(summary = "Obtener citas del usuario autenticado", description = "Retorna el historial y citas activas del cliente asociado al token de sesión.")
    public ResponseEntity<?> misCitas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of(
                    "status", 401,
                    "title", "Unauthorized",
                    "detail", "Token de autenticación ausente o inválido."
            ));
        }
        String login = authentication.getName();
        return ResponseEntity.ok(citaService.obtenerMisCitas(login));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Listar todas las citas paginadas", description = "Permite a recepción y administración visualizar todas las citas con paginación y ordenamiento.")
    public ResponseEntity<Page<CitaSalida>> listarPaginado(Pageable pageable) {
        return ResponseEntity.ok(citaService.obtenerTodasPaginadas(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cita por ID", description = "Devuelve el detalle completo de una cita específica.")
    public ResponseEntity<CitaSalida> obtenerPorId(@PathVariable Integer id) {
        CitaSalida salida = citaService.obtenerPorId(id);
        if (salida == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(salida);
    }

    @GetMapping("/codigo/{codigoCita}")
    @Operation(summary = "Obtener cita por código único", description = "Devuelve el detalle de la cita a partir de su código de confirmación (ej. SHU-2026-1234).")
    public ResponseEntity<CitaSalida> obtenerPorCodigo(@PathVariable String codigoCita) {
        CitaSalida salida = citaService.obtenerPorCodigo(codigoCita);
        if (salida == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(salida);
    }

    @RequestMapping(value = "/{id}/estado", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @Operation(summary = "Cambiar estado de la cita", description = "Actualiza el estado de la cita (Confirmed, InProgress, Completed, Cancelled).")
    public ResponseEntity<CitaSalida> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody(required = false) CitaCambiarEstado cambio,
            @RequestParam(required = false) String nuevoEstado,
            Authentication authentication) {
        if (cambio == null) {
            cambio = new CitaCambiarEstado();
        }
        cambio.setId(id);
        if (nuevoEstado != null && !nuevoEstado.isBlank()) {
            cambio.setNuevoEstado(nuevoEstado);
        }
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userLogin = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
        return ResponseEntity.ok(citaService.cambiarEstado(cambio, userLogin, isAdmin));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar cita por ID", description = "Cancela una cita activa liberando el horario del estilista.")
    public ResponseEntity<CitaSalida> cancelarCita(
            @PathVariable Integer id,
            @RequestParam(required = false) String motivo,
            @RequestBody(required = false) java.util.Map<String, String> body,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String finalMotivo = (motivo != null && !motivo.isBlank()) 
                ? motivo 
                : (body != null && body.containsKey("motivo") ? body.get("motivo") : "Cancelación por usuario");
        String userLogin = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
        return ResponseEntity.ok(citaService.cancelarCita(id, finalMotivo, userLogin, isAdmin));
    }
}
