package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.producto.ProductoGuardarDto;
import com.shushinestudio.dtos.producto.ProductoSalidaDto;
import com.shushinestudio.dtos.producto.ProductoStockDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductoService {

    List<ProductoSalidaDto> obtenerProductosActivos();

    Page<ProductoSalidaDto> obtenerProductosActivosPaginados(Pageable pageable);

    List<ProductoSalidaDto> obtenerTodosAdmin();

    Page<ProductoSalidaDto> obtenerTodosAdminPaginados(Pageable pageable);

    ProductoSalidaDto obtenerPorId(Integer id);

    ProductoSalidaDto crearProducto(ProductoGuardarDto dto);

    ProductoSalidaDto modificarProducto(Integer id, ProductoGuardarDto dto);

    ProductoSalidaDto actualizarStock(Integer id, ProductoStockDto dto);

    void cambiarEstado(Integer id, Boolean activo);

    void eliminarProducto(Integer id);
}
