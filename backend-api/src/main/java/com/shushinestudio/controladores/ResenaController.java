package com.shushinestudio.controladores;

import com.shushinestudio.dtos.resena.ResenaCrearDto;
import com.shushinestudio.dtos.resena.ResenaSalidaDto;
import com.shushinestudio.servicios.interfaces.IResenaService;
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
@RequestMapping("/api")
@Tag(name = "Reseñas y Calificaciones", description = "Endpoints de evaluación de citas completadas y gestión de testimonios")
public class ResenaController {

    @Autowired
    private IResenaService resenaService;

    @PostMapping("/resenas")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @Operation(summary = "Registrar reseña de una cita", description = "Permite al cliente calificar una cita completada con 1-5 estrellas y comentario.")
    public ResponseEntity<ResenaSalidaDto> crearResena(
            @Valid @RequestBody ResenaCrearDto dto,
            Authentication authentication) {
        String login = authentication.getName();
        ResenaSalidaDto resena = resenaService.crearResena(login, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resena);
    }

    @GetMapping("/resenas/mis-resenas")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @Operation(summary = "Obtener mis reseñas", description = "Retorna el historial de reseñas publicadas por el cliente autenticado.")
    public ResponseEntity<List<ResenaSalidaDto>> obtenerMisResenas(Authentication authentication) {
        String login = authentication.getName();
        return ResponseEntity.ok(resenaService.obtenerMisResenas(login));
    }

    @GetMapping("/resenas/{id}")
    @Operation(summary = "Obtener detalle de una reseña", description = "Retorna los datos de una reseña por su identificador.")
    public ResponseEntity<ResenaSalidaDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(resenaService.obtenerPorId(id));
    }

    @GetMapping("/admin/resenas")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Bandeja administrativa de reseñas", description = "Lista todas las reseñas registradas en el sistema para moderación.")
    public ResponseEntity<List<ResenaSalidaDto>> obtenerTodasAdmin() {
        return ResponseEntity.ok(resenaService.obtenerTodasResenasAdmin());
    }
}
