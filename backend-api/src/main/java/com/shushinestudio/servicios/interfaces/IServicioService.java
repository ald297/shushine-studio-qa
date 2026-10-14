package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.servicio.ServicioGuardar;
import com.shushinestudio.dtos.servicio.ServicioModificar;
import com.shushinestudio.dtos.servicio.ServicioSalida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IServicioService {
    List<ServicioSalida> obtenerTodos();
    Page<ServicioSalida> obtenerTodosPaginados(Pageable pageable);
    List<ServicioSalida> obtenerPorCategoria(Integer categoriaId);
    List<ServicioSalida> buscarPorNombre(String query);
    ServicioSalida obtenerPorId(Integer id);
    ServicioSalida crear(ServicioGuardar servicioGuardar);
    ServicioSalida editar(ServicioModificar servicioModificar);
    void eliminarPorId(Integer id);
}
