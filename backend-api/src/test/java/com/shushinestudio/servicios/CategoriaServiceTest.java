package com.shushinestudio.servicios;

import com.shushinestudio.dtos.categoria.CategoriaGuardar;
import com.shushinestudio.dtos.categoria.CategoriaModificar;
import com.shushinestudio.dtos.categoria.CategoriaSalida;
import com.shushinestudio.modelos.Categoria;
import com.shushinestudio.repositorios.ICategoriaRepository;
import com.shushinestudio.servicios.implementaciones.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoriaServiceTest {

    private ICategoriaRepository categoriaRepository;
    private CategoriaService categoriaService;
    private Categoria categoriaBase;

    @BeforeEach
    void setUp() {
        categoriaRepository = mock(ICategoriaRepository.class);
        categoriaService = new CategoriaService();

        ModelMapper modelMapper = new ModelMapper();
        ReflectionTestUtils.setField(categoriaService, "categoriaRepository", categoriaRepository);
        ReflectionTestUtils.setField(categoriaService, "modelMapper", modelMapper);

        categoriaBase = Categoria.builder()
                .id(1)
                .nombre("Corte y Estilo")
                .descripcion("Cortes modernos y clásicos para dama y caballero")
                .tipo("Servicio")
                .activo(true)
                .build();
    }

    @Test
    void t1_crear() {
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaBase);

        CategoriaSalida salida = categoriaService.crear(new CategoriaGuardar("Corte y Estilo"));
        assertNotEquals(null, salida);
        assertEquals("Corte y Estilo", salida.getNombre());
    }

    @Test
    void t2_obtenerTodos() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaBase));

        int actual = categoriaService.obtenerTodos().size();
        assertNotEquals(0, actual);
        assertEquals(1, actual);
    }

    @Test
    void t3_obtenerTodosPaginados() {
        when(categoriaRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(categoriaBase)));

        Page<CategoriaSalida> salida = categoriaService.obtenerTodosPaginados(PageRequest.of(0, 10));
        assertNotEquals(0, salida.getTotalElements());
    }

    @Test
    void t4_obtenerPorId() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaBase));

        CategoriaSalida salida = categoriaService.obtenerPorId(1);
        assertNotEquals(null, salida);
        assertEquals(1, salida.getId());
    }

    @Test
    void t5_editar() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaBase));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaBase);

        CategoriaSalida salida = categoriaService.editar(
                new CategoriaModificar(1, "Corte y Estilo Actualizado")
        );
        assertNotEquals(null, salida);
    }

    @Test
    void t6_eliminarPorId() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        doNothing().when(categoriaRepository).deleteById(1);

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                categoriaService.eliminarPorId(1);
            }
        });
        verify(categoriaRepository, times(1)).deleteById(1);
    }
}
