package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.categoria.CategoriaGuardar;
import com.shushinestudio.dtos.categoria.CategoriaModificar;
import com.shushinestudio.dtos.categoria.CategoriaSalida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoriaService {
    List<CategoriaSalida> obtenerTodos();
    Page<CategoriaSalida> obtenerTodosPaginados(Pageable pageable);
    CategoriaSalida obtenerPorId(Integer id);
    CategoriaSalida crear(CategoriaGuardar categoriaGuardar);
    CategoriaSalida editar(CategoriaModificar categoriaModificar);
    void eliminarPorId(Integer id);
}
