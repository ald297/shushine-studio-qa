package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.servicio.ServicioGuardar;
import com.shushinestudio.dtos.servicio.ServicioModificar;
import com.shushinestudio.dtos.servicio.ServicioSalida;
import com.shushinestudio.modelos.Categoria;
import com.shushinestudio.modelos.Servicio;
import com.shushinestudio.repositorios.ICategoriaRepository;
import com.shushinestudio.repositorios.IServicioRepository;
import com.shushinestudio.servicios.interfaces.IServicioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioService implements IServicioService {

    @Autowired
    private IServicioRepository servicioRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> obtenerTodos() {
        return servicioRepository.findByActivoTrue().stream()
                .map(this::mapToSalida)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioSalida> obtenerTodosPaginados(Pageable pageable) {
        Page<Servicio> page = servicioRepository.findAll(pageable);
        List<ServicioSalida> dtos = page.stream()
                .map(this::mapToSalida)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> obtenerPorCategoria(Integer categoriaId) {
        return servicioRepository.findByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(this::mapToSalida)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> buscarPorNombre(String query) {
        return servicioRepository.findByNombreContainingIgnoreCaseAndActivoTrue(query).stream()
                .map(this::mapToSalida)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioSalida obtenerPorId(Integer id) {
        return servicioRepository.findById(id)
                .map(this::mapToSalida)
                .orElse(null);
    }

    @Override
    @Transactional
    public ServicioSalida crear(ServicioGuardar servicioGuardar) {
        Categoria categoria = categoriaRepository.findById(servicioGuardar.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + servicioGuardar.getCategoriaId()));

        Servicio servicio = Servicio.builder()
                .codigoServicio(servicioGuardar.getCodigoServicio())
                .categoria(categoria)
                .nombre(servicioGuardar.getNombre())
                .descripcion(servicioGuardar.getDescripcion())
                .precioBase(servicioGuardar.getPrecioBase())
                .esPrecioVariable(servicioGuardar.getEsPrecioVariable() != null ? servicioGuardar.getEsPrecioVariable() : false)
                .duracionMinutos(servicioGuardar.getDuracionMinutos())
                .intervaloSeguimientoDias(servicioGuardar.getIntervaloSeguimientoDias() != null ? servicioGuardar.getIntervaloSeguimientoDias() : 21)
                .imagenUrl(validarYSanitizarImagenUrl(servicioGuardar.getImagenUrl()))
                .costoInsumos(servicioGuardar.getCostoInsumos() != null ? servicioGuardar.getCostoInsumos() : BigDecimal.ZERO)
                .activo(true)
                .build();

        Servicio guardado = servicioRepository.save(servicio);
        return mapToSalida(guardado);
    }

    @Override
    @Transactional
    public ServicioSalida editar(ServicioModificar servicioModificar) {
        Servicio existente = servicioRepository.findById(servicioModificar.getId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + servicioModificar.getId()));

        if (servicioModificar.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(servicioModificar.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + servicioModificar.getCategoriaId()));
            existente.setCategoria(categoria);
        }

        if (servicioModificar.getCodigoServicio() != null) existente.setCodigoServicio(servicioModificar.getCodigoServicio());
        if (servicioModificar.getNombre() != null) existente.setNombre(servicioModificar.getNombre());
        if (servicioModificar.getDescripcion() != null) existente.setDescripcion(servicioModificar.getDescripcion());
        if (servicioModificar.getPrecioBase() != null) existente.setPrecioBase(servicioModificar.getPrecioBase());
        if (servicioModificar.getEsPrecioVariable() != null) existente.setEsPrecioVariable(servicioModificar.getEsPrecioVariable());
        if (servicioModificar.getDuracionMinutos() != null) existente.setDuracionMinutos(servicioModificar.getDuracionMinutos());
        if (servicioModificar.getIntervaloSeguimientoDias() != null) existente.setIntervaloSeguimientoDias(servicioModificar.getIntervaloSeguimientoDias());
        if (servicioModificar.getImagenUrl() != null) existente.setImagenUrl(validarYSanitizarImagenUrl(servicioModificar.getImagenUrl()));
        if (servicioModificar.getCostoInsumos() != null) existente.setCostoInsumos(servicioModificar.getCostoInsumos());
        if (servicioModificar.getActivo() != null) existente.setActivo(servicioModificar.getActivo());

        Servicio actualizado = servicioRepository.save(existente);
        return mapToSalida(actualizado);
    }

    private String validarYSanitizarImagenUrl(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return null;
        }
        String lower = imagenUrl.trim().toLowerCase();
        if (lower.startsWith("/data/") || lower.startsWith("file:") || lower.contains("/cache/")
                || lower.startsWith("c:\\") || lower.startsWith("/storage/emulated/") || lower.startsWith("/sdcard/")) {
            throw new IllegalArgumentException("La imagen debe ser una URL HTTP/HTTPS válida, no una ruta local del dispositivo.");
        }
        return imagenUrl.trim();
    }

    @Override
    @Transactional
    public void eliminarPorId(Integer id) {
        if (servicioRepository.existsById(id)) {
            servicioRepository.deleteById(id);
        }
    }

    private ServicioSalida mapToSalida(Servicio servicio) {
        ServicioSalida salida = modelMapper.map(servicio, ServicioSalida.class);
        if (servicio.getCategoria() != null) {
            salida.setCategoriaId(servicio.getCategoria().getId());
            salida.setCategoriaNombre(servicio.getCategoria().getNombre());
        }
        return salida;
    }
}
