package com.shushinestudio.controladores;

import com.shushinestudio.dtos.producto.ProductoGuardarDto;
import com.shushinestudio.dtos.producto.ProductoSalidaDto;
import com.shushinestudio.dtos.producto.ProductoStockDto;
import com.shushinestudio.servicios.interfaces.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/productos")
@Tag(name = "Productos (Administración e Inventario)", description = "Endpoints de gestión y control de inventario de productos para administradores")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminProductoController {

    @Autowired
    private IProductoService productoService;

    @GetMapping
    @Operation(summary = "Listado paginado de inventario", description = "Retorna todos los productos (activos e inactivos) con stock y estado.")
    public ResponseEntity<Page<ProductoSalidaDto>> obtenerProductos(Pageable pageable) {
        return ResponseEntity.ok(productoService.obtenerTodosAdminPaginados(pageable));
    }

    @GetMapping("/lista")
    @Operation(summary = "Listado completo de inventario", description = "Retorna todos los productos para la gestión en la app móvil.")
    public ResponseEntity<List<ProductoSalidaDto>> obtenerProductosLista() {
        return ResponseEntity.ok(productoService.obtenerTodosAdmin());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de producto para administración", description = "Retorna la información completa de un producto por ID.")
    public ResponseEntity<ProductoSalidaDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo producto", description = "Crea un nuevo producto en el catálogo e inicializa su stock.")
    public ResponseEntity<ProductoSalidaDto> crearProducto(@Valid @RequestBody ProductoGuardarDto dto) {
        ProductoSalidaDto nuevo = productoService.crearProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar producto existente", description = "Actualiza los datos, precio o categorías de un producto.")
    public ResponseEntity<ProductoSalidaDto> modificarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoGuardarDto dto) {
        return ResponseEntity.ok(productoService.modificarProducto(id, dto));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Actualizar existencia/stock de producto", description = "Ajusta las existencias en inventario de un producto específico.")
    public ResponseEntity<ProductoSalidaDto> actualizarStock(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoStockDto dto) {
        return ResponseEntity.ok(productoService.actualizarStock(id, dto));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar producto", description = "Habilita o deshabilita la disponibilidad del producto en el catálogo.")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam Boolean activo) {
        productoService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja producto", description = "Desactiva lógicamente el producto del inventario.")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
