package com.shushinestudio.controladores;

import com.shushinestudio.dtos.categoria.CategoriaGuardar;
import com.shushinestudio.dtos.categoria.CategoriaModificar;
import com.shushinestudio.dtos.categoria.CategoriaSalida;
import com.shushinestudio.servicios.interfaces.ICategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Endpoints para la gestión y consulta de categorías de belleza en Shushine Studio")
public class CategoriaController {

    @Autowired
    private ICategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorías paginadas", description = "Retorna una página de categorías de servicios.")
    public ResponseEntity<Page<CategoriaSalida>> mostrarTodosPaginados(@ParameterObject Pageable pageable) {
        Page<CategoriaSalida> categorias = categoriaService.obtenerTodosPaginados(pageable);
        if (categorias.hasContent()) {
            return ResponseEntity.ok(categorias);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/lista")
    @Operation(summary = "Listar todas las categorías", description = "Retorna el listado completo de categorías para selectores móviles.")
    public ResponseEntity<List<CategoriaSalida>> mostrarTodos() {
        List<CategoriaSalida> categorias = categoriaService.obtenerTodos();
        if (!categorias.isEmpty()) {
            return ResponseEntity.ok(categorias);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoría por ID", description = "Retorna el detalle de una categoría específica.")
    public ResponseEntity<CategoriaSalida> buscarPorId(@PathVariable Integer id) {
        CategoriaSalida categoria = categoriaService.obtenerPorId(id);
        if (categoria != null) {
            return ResponseEntity.ok(categoria);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear nueva categoría", description = "Requiere rol de ADMIN.")
    public ResponseEntity<CategoriaSalida> crear(@RequestBody CategoriaGuardar categoriaGuardar) {
        CategoriaSalida categoria = categoriaService.crear(categoriaGuardar);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Modificar categoría", description = "Requiere rol de ADMIN.")
    public ResponseEntity<CategoriaSalida> editar(@PathVariable Integer id, @RequestBody CategoriaModificar categoriaModificar) {
        categoriaModificar.setId(id);
        CategoriaSalida categoria = categoriaService.editar(categoriaModificar);
        return ResponseEntity.ok(categoria);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar categoría", description = "Requiere rol de ADMIN.")
    public ResponseEntity<String> eliminar(@PathVariable Integer id) {
        categoriaService.eliminarPorId(id);
        return ResponseEntity.ok("Categoría eliminada correctamente");
    }
}
