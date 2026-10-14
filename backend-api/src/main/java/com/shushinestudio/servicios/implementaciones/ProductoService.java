package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.producto.ProductoGuardarDto;
import com.shushinestudio.dtos.producto.ProductoSalidaDto;
import com.shushinestudio.dtos.producto.ProductoStockDto;
import com.shushinestudio.modelos.Categoria;
import com.shushinestudio.modelos.Producto;
import com.shushinestudio.repositorios.ICategoriaRepository;
import com.shushinestudio.repositorios.IProductoRepository;
import com.shushinestudio.servicios.interfaces.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductoService implements IProductoService {

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoSalidaDto> obtenerProductosActivos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoSalidaDto> obtenerProductosActivosPaginados(Pageable pageable) {
        return productoRepository.findByActivoTrue(pageable).map(this::mapearADto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoSalidaDto> obtenerTodosAdmin() {
        return productoRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoSalidaDto> obtenerTodosAdminPaginados(Pageable pageable) {
        return productoRepository.findAll(pageable).map(this::mapearADto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoSalidaDto obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID " + id));
        return mapearADto(producto);
    }

    @Override
    @Transactional
    public ProductoSalidaDto crearProducto(ProductoGuardarDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID " + dto.getIdCategoria()));

        String codigo = dto.getCodigoProducto();
        if (codigo == null || codigo.isBlank()) {
            codigo = "PROD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        if (productoRepository.existsByCodigoProducto(codigo)) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + codigo);
        }

        Producto producto = Producto.builder()
                .codigoProducto(codigo)
                .categoria(categoria)
                .nombre(dto.getNombre().trim())
                .marca(dto.getMarca().trim())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stockActual(dto.getStockActual() != null ? dto.getStockActual() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5)
                .imagenUrl(dto.getImagenUrl())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        Producto guardado = productoRepository.save(producto);
        return mapearADto(guardado);
    }

    @Override
    @Transactional
    public ProductoSalidaDto modificarProducto(Integer id, ProductoGuardarDto dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID " + id));

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID " + dto.getIdCategoria()));

        if (dto.getCodigoProducto() != null && !dto.getCodigoProducto().isBlank() &&
                !dto.getCodigoProducto().equalsIgnoreCase(producto.getCodigoProducto())) {
            if (productoRepository.existsByCodigoProducto(dto.getCodigoProducto())) {
                throw new IllegalArgumentException("Ya existe un producto con el código " + dto.getCodigoProducto());
            }
            producto.setCodigoProducto(dto.getCodigoProducto().trim().toUpperCase());
        }

        producto.setCategoria(categoria);
        producto.setNombre(dto.getNombre().trim());
        producto.setMarca(dto.getMarca().trim());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        if (dto.getStockActual() != null) {
            producto.setStockActual(dto.getStockActual());
        }
        if (dto.getStockMinimo() != null) {
            producto.setStockMinimo(dto.getStockMinimo());
        }
        if (dto.getImagenUrl() != null) {
            producto.setImagenUrl(dto.getImagenUrl());
        }
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }

        Producto actualizado = productoRepository.save(producto);
        return mapearADto(actualizado);
    }

    @Override
    @Transactional
    public ProductoSalidaDto actualizarStock(Integer id, ProductoStockDto dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID " + id));

        producto.setStockActual(dto.getStock());
        Producto actualizado = productoRepository.save(producto);
        return mapearADto(actualizado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, Boolean activo) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID " + id));

        producto.setActivo(activo);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID " + id));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoSalidaDto mapearADto(Producto producto) {
        boolean stockBajo = producto.getStockActual() != null && producto.getStockMinimo() != null &&
                producto.getStockActual() <= producto.getStockMinimo();

        return ProductoSalidaDto.builder()
                .id(producto.getId())
                .codigoProducto(producto.getCodigoProducto())
                .idCategoria(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .nombreCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                .nombre(producto.getNombre())
                .marca(producto.getMarca())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .stockBajo(stockBajo)
                .build();
    }
}
