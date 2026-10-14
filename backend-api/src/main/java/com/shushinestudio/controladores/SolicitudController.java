package com.shushinestudio.controladores;

import com.shushinestudio.dtos.solicitud.ResponderCotizacionDto;
import com.shushinestudio.dtos.solicitud.SolicitudCrearDto;
import com.shushinestudio.dtos.solicitud.SolicitudSalidaDto;
import com.shushinestudio.servicios.interfaces.ISolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@Tag(name = "Solicitudes Personalizadas", description = "Endpoints para que clientes envíen diseños de referencia y gestionen cotizaciones")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
public class SolicitudController {

    @Autowired
    private ISolicitudService solicitudService;

    @PostMapping
    @Operation(summary = "Crear solicitud personalizada", description = "Envía una nueva solicitud de diseño especial o cotización con notas y fotos.")
    public ResponseEntity<SolicitudSalidaDto> crearSolicitud(
            @Valid @RequestBody SolicitudCrearDto dto,
            Authentication authentication) {
        String login = authentication.getName();
        SolicitudSalidaDto creada = solicitudService.crearSolicitud(login, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/mis-solicitudes")
    @Operation(summary = "Obtener mis solicitudes", description = "Lista las solicitudes personalizadas del cliente autenticado y sus estados.")
    public ResponseEntity<List<SolicitudSalidaDto>> obtenerMisSolicitudes(Authentication authentication) {
        String login = authentication.getName();
        return ResponseEntity.ok(solicitudService.obtenerMisSolicitudes(login));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de solicitud", description = "Consulta el detalle y estado de cotización de una solicitud del cliente.")
    public ResponseEntity<SolicitudSalidaDto> obtenerPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        String login = authentication.getName();
        return ResponseEntity.ok(solicitudService.obtenerPorIdCliente(login, id));
    }

    @PutMapping("/{id}/responder-cotizacion")
    @Operation(summary = "Aceptar o rechazar cotización", description = "Permite al cliente aceptar o declinar la propuesta de cotización enviada por el salón.")
    public ResponseEntity<SolicitudSalidaDto> responderCotizacion(
            @PathVariable Integer id,
            @Valid @RequestBody ResponderCotizacionDto dto,
            Authentication authentication) {
        String login = authentication.getName();
        return ResponseEntity.ok(solicitudService.responderCotizacionCliente(login, id, dto));
    }
}
