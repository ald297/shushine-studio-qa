package com.shushinestudio.controladores;

import com.shushinestudio.dtos.servicio.ServicioGuardar;
import com.shushinestudio.dtos.servicio.ServicioModificar;
import com.shushinestudio.dtos.servicio.ServicioSalida;
import com.shushinestudio.servicios.interfaces.IServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@Tag(name = "Servicios", description = "Endpoints para la consulta y administración de servicios de Shushine Studio")
public class ServicioController {

    @Autowired
    private IServicioService servicioService;

    @GetMapping
    @Operation(summary = "Listar servicios paginados", description = "Retorna una página de servicios de belleza activos.")
    public ResponseEntity<Page<ServicioSalida>> mostrarTodosPaginados(@ParameterObject Pageable pageable) {
        Page<ServicioSalida> servicios = servicioService.obtenerTodosPaginados(pageable);
        if (servicios.hasContent()) {
            return ResponseEntity.ok(servicios);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping({"/lista", "/activos"})
    @Operation(summary = "Listar todos los servicios", description = "Retorna la lista completa de servicios activos para la app móvil.")
    public ResponseEntity<List<ServicioSalida>> mostrarTodos() {
        List<ServicioSalida> servicios = servicioService.obtenerTodos();
        if (!servicios.isEmpty()) {
            return ResponseEntity.ok(servicios);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Filtrar servicios por categoría", description = "Retorna los servicios asociados a la categoría solicitada.")
    public ResponseEntity<List<ServicioSalida>> buscarPorCategoria(@PathVariable Integer categoriaId) {
        List<ServicioSalida> servicios = servicioService.obtenerPorCategoria(categoriaId);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar servicios por nombre", description = "Búsqueda textual reactiva para la barra de búsqueda móvil.")
    public ResponseEntity<List<ServicioSalida>> buscarPorNombre(@RequestParam String query) {
        List<ServicioSalida> servicios = servicioService.buscarPorNombre(query);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar servicio por ID", description = "Retorna la ficha de detalle completa de un servicio.")
    public ResponseEntity<ServicioSalida> buscarPorId(@PathVariable Integer id) {
        ServicioSalida servicio = servicioService.obtenerPorId(id);
        if (servicio != null) {
            return ResponseEntity.ok(servicio);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear nuevo servicio", description = "Requiere rol de ADMIN.")
    public ResponseEntity<ServicioSalida> crear(@RequestBody ServicioGuardar servicioGuardar) {
        ServicioSalida servicio = servicioService.crear(servicioGuardar);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Modificar servicio existente", description = "Requiere rol de ADMIN.")
    public ResponseEntity<ServicioSalida> editar(@PathVariable Integer id, @RequestBody ServicioModificar servicioModificar) {
        servicioModificar.setId(id);
        ServicioSalida servicio = servicioService.editar(servicioModificar);
        return ResponseEntity.ok(servicio);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar servicio", description = "Requiere rol de ADMIN.")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        servicioService.eliminarPorId(id);
        return ResponseEntity.ok("Servicio eliminado correctamente");
    }
}
