package com.shushinestudio.controladores;

import com.shushinestudio.dtos.producto.ProductoSalidaDto;
import com.shushinestudio.servicios.interfaces.IProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos y Catálogo", description = "Endpoints de consulta de productos de belleza disponibles para clientes")
public class ProductoController {

    @Autowired
    private IProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos activos paginados", description = "Retorna el catálogo de productos disponibles en el salón con paginación.")
    public ResponseEntity<Page<ProductoSalidaDto>> obtenerProductos(Pageable pageable) {
        return ResponseEntity.ok(productoService.obtenerProductosActivosPaginados(pageable));
    }

    @GetMapping({"/lista", "/activos"})
    @Operation(summary = "Listado completo de productos activos", description = "Retorna todos los productos activos para vistas móviles y selectores.")
    public ResponseEntity<List<ProductoSalidaDto>> obtenerProductosLista() {
        return ResponseEntity.ok(productoService.obtenerProductosActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de producto", description = "Retorna la ficha técnica y precio de un producto.")
    public ResponseEntity<ProductoSalidaDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }
}
