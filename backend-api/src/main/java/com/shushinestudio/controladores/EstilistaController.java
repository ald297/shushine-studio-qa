package com.shushinestudio.controladores;

import com.shushinestudio.dtos.disponibilidad.DisponibilidadSalida;
import com.shushinestudio.dtos.disponibilidad.EstilistaGuardarDto;
import com.shushinestudio.dtos.disponibilidad.EstilistaSalida;
import com.shushinestudio.servicios.interfaces.IDisponibilidadService;
import com.shushinestudio.servicios.interfaces.IServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/estilistas")
@Tag(name = "Estilistas y Disponibilidad", description = "Endpoints de personal profesional y cálculo de franjas horarias libres")
public class EstilistaController {

    @Autowired
    private IDisponibilidadService disponibilidadService;

    @Autowired(required = false)
    private IServicioService servicioService;

    @GetMapping({"", "/activos", "/lista"})
    @Operation(summary = "Listar estilistas", description = "Retorna el listado del equipo profesional del salón.")
    public ResponseEntity<List<EstilistaSalida>> obtenerEstilistas(@RequestParam(required = false) Boolean soloActivos) {
        boolean activos = (soloActivos == null || soloActivos);
        List<EstilistaSalida> estilistas = activos
                ? disponibilidadService.obtenerEstilistasActivos()
                : disponibilidadService.obtenerTodosEstilistas();
        return ResponseEntity.ok(estilistas);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear nuevo estilista", description = "Registra un nuevo miembro del equipo profesional del salón.")
    public ResponseEntity<EstilistaSalida> crearEstilista(@RequestBody EstilistaGuardarDto dto) {
        EstilistaSalida nuevo = disponibilidadService.crearEstilista(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar disponibilidad de estilista", description = "Activa o inactiva a un estilista en la agenda.")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Integer id, @RequestParam Boolean activo) {
        disponibilidadService.actualizarEstadoEstilista(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar estilista", description = "Elimina permanentemente a un estilista del sistema.")
    public ResponseEntity<Void> eliminarEstilista(@PathVariable Integer id) {
        disponibilidadService.eliminarEstilista(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un estilista", description = "Retorna el perfil y especialidad del estilista.")
    public ResponseEntity<EstilistaSalida> obtenerEstilistaPorId(@PathVariable Integer id) {
        EstilistaSalida estilista = disponibilidadService.obtenerEstilistaPorId(id);
        if (estilista != null) {
            return ResponseEntity.ok(estilista);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(
            summary = "Calcular disponibilidad horaria en tiempo real",
            description = "Calcula los bloques libres excluyendo jornada no laboral, almuerzo, bloqueos y citas existentes."
    )
    public ResponseEntity<DisponibilidadSalida> calcularDisponibilidad(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer duracionMinutos,
            @RequestParam(required = false) Integer servicioId
    ) {
        int duracion = (duracionMinutos != null && duracionMinutos > 0) ? duracionMinutos : 30;
        if (duracionMinutos == null && servicioId != null && servicioService != null) {
            try {
                var serv = servicioService.obtenerPorId(servicioId);
                if (serv != null && serv.getDuracionMinutos() != null && serv.getDuracionMinutos() > 0) {
                    duracion = serv.getDuracionMinutos();
                }
            } catch (Exception ignored) {}
        }
        DisponibilidadSalida disponibilidad = disponibilidadService.calcularDisponibilidad(id, fecha, duracion);
        return ResponseEntity.ok(disponibilidad);
    }

    @GetMapping("/{id}/horarios")
    @Operation(summary = "Obtener horarios de trabajo semanales del estilista")
    public ResponseEntity<List<com.shushinestudio.modelos.HorarioEstilista>> obtenerHorarios(@PathVariable Integer id) {
        return ResponseEntity.ok(disponibilidadService.obtenerHorariosEstilista(id));
    }

    @PutMapping("/{id}/horarios")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Guardar o actualizar horario de trabajo del estilista")
    public ResponseEntity<com.shushinestudio.modelos.HorarioEstilista> guardarHorario(
            @PathVariable Integer id,
            @RequestBody com.shushinestudio.modelos.HorarioEstilista horario) {
        return ResponseEntity.ok(disponibilidadService.guardarHorarioEstilista(id, horario));
    }

    @GetMapping("/{id}/bloqueos")
    @Operation(summary = "Obtener bloqueos de agenda del estilista")
    public ResponseEntity<List<com.shushinestudio.modelos.BloqueoHorario>> obtenerBloqueos(@PathVariable Integer id) {
        return ResponseEntity.ok(disponibilidadService.obtenerBloqueosEstilista(id));
    }

    @PostMapping("/{id}/bloqueos")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear bloqueo de horario para estilista")
    public ResponseEntity<com.shushinestudio.modelos.BloqueoHorario> crearBloqueo(
            @PathVariable Integer id,
            @RequestBody com.shushinestudio.modelos.BloqueoHorario bloqueo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadService.crearBloqueo(id, bloqueo));
    }

    @DeleteMapping("/bloqueos/{bloqueoId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar bloqueo de horario")
    public ResponseEntity<Void> eliminarBloqueo(@PathVariable Integer bloqueoId) {
        disponibilidadService.eliminarBloqueo(bloqueoId);
        return ResponseEntity.noContent().build();
    }
}

