package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.categoria.CategoriaGuardar;
import com.shushinestudio.dtos.categoria.CategoriaModificar;
import com.shushinestudio.dtos.categoria.CategoriaSalida;
import com.shushinestudio.modelos.Categoria;
import com.shushinestudio.repositorios.ICategoriaRepository;
import com.shushinestudio.servicios.interfaces.ICategoriaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService implements ICategoriaService {

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaSalida> obtenerTodos() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(categoria -> modelMapper.map(categoria, CategoriaSalida.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaSalida> obtenerTodosPaginados(Pageable pageable) {
        Page<Categoria> page = categoriaRepository.findAll(pageable);
        List<CategoriaSalida> categoriasDto = page.stream()
                .map(categoria -> modelMapper.map(categoria, CategoriaSalida.class))
                .collect(Collectors.toList());
        return new PageImpl<>(categoriasDto, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaSalida obtenerPorId(Integer id) {
        return categoriaRepository.findById(id)
                .map(categoria -> modelMapper.map(categoria, CategoriaSalida.class))
                .orElse(null);
    }

    @Override
    @Transactional
    public CategoriaSalida crear(CategoriaGuardar categoriaGuardar) {
        Categoria categoria = modelMapper.map(categoriaGuardar, Categoria.class);
        categoria.setId(null);
        if (categoria.getTipo() == null || categoria.getTipo().isBlank()) {
            categoria.setTipo("Servicio");
        }
        if (categoria.getActivo() == null) {
            categoria.setActivo(true);
        }
        Categoria guardada = categoriaRepository.save(categoria);
        return modelMapper.map(guardada, CategoriaSalida.class);
    }

    @Override
    @Transactional
    public CategoriaSalida editar(CategoriaModificar categoriaModificar) {
        Categoria existente = categoriaRepository.findById(categoriaModificar.getId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la categoría con ID: " + categoriaModificar.getId()));

        if (categoriaModificar.getNombre() != null) {
            existente.setNombre(categoriaModificar.getNombre());
        }
        if (categoriaModificar.getDescripcion() != null) {
            existente.setDescripcion(categoriaModificar.getDescripcion());
        }
        if (categoriaModificar.getIconoUrl() != null) {
            existente.setIconoUrl(categoriaModificar.getIconoUrl());
        }
        if (categoriaModificar.getTipo() != null) {
            existente.setTipo(categoriaModificar.getTipo());
        }
        if (categoriaModificar.getActivo() != null) {
            existente.setActivo(categoriaModificar.getActivo());
        }

        Categoria actualizada = categoriaRepository.save(existente);
        return modelMapper.map(actualizada, CategoriaSalida.class);
    }

    @Override
    @Transactional
    public void eliminarPorId(Integer id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
        }
    }
}
