package com.shushinestudio.controladores;

import com.shushinestudio.dtos.solicitud.CotizacionCrearDto;
import com.shushinestudio.dtos.solicitud.SolicitudSalidaDto;
import com.shushinestudio.servicios.interfaces.ISolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/solicitudes")
@Tag(name = "Solicitudes (Administración)", description = "Endpoints para que el administrador revise solicitudes, fotos y registre cotizaciones")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminSolicitudController {

    @Autowired
    private ISolicitudService solicitudService;

    @GetMapping
    @Operation(summary = "Bandeja de solicitudes de clientes", description = "Retorna todas las solicitudes personalizadas registradas con su estado.")
    public ResponseEntity<List<SolicitudSalidaDto>> obtenerTodas() {
        return ResponseEntity.ok(solicitudService.obtenerTodasAdmin());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de solicitud para administración", description = "Retorna la información completa de una solicitud, incluyendo imágenes.")
    public ResponseEntity<SolicitudSalidaDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.obtenerPorIdAdmin(id));
    }

    @PostMapping("/{id}/cotizar")
    @Operation(summary = "Enviar cotización al cliente", description = "Registra una propuesta de precio y descripción del trabajo para la solicitud.")
    public ResponseEntity<SolicitudSalidaDto> cotizar(
            @PathVariable Integer id,
            @Valid @RequestBody CotizacionCrearDto dto) {
        return ResponseEntity.ok(solicitudService.cotizarSolicitudAdmin(id, dto));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de solicitud", description = "Modifica manualmente el estado de la solicitud (Pendiente, Cancelada, etc.).")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado) {
        solicitudService.cambiarEstadoAdmin(id, estado);
        return ResponseEntity.ok().build();
    }
}
