package com.shushinestudio.servicios;
import com.shushinestudio.dtos.cita.CitaGuardar;
import com.shushinestudio.repositorios.ReservaJdbcRepository;
import com.shushinestudio.repositorios.ReservaJdbcRepository.*;
import com.shushinestudio.servicios.implementaciones.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CitaRapidaTest {
    private ReservaJdbcRepository jdbc;
    private CitaService service;
    private Contexto context;
    private CitaGuardar dto;
    @BeforeEach void setup() {
        jdbc=mock(ReservaJdbcRepository.class);service=new CitaService();
        ReflectionTestUtils.setField(service,"reservaJdbcRepository",jdbc);
        context=new Contexto(1,"Camila","Calderón","7000-1111",2,"Estilista",true,
            List.of(new ServicioDato(3,"Servicio A",new BigDecimal("50.00"),60),
                    new ServicioDato(4,"Servicio B",new BigDecimal("10.05"),30)));
        dto=CitaGuardar.builder().estilistaId(2).fechaCita(LocalDate.of(2030,1,7)).horaInicio(LocalTime.of(10,0))
            .servicioIds(List.of(3,4)).notasCliente("Nota").metodoPagoPreferente("Tarjeta").build();
        when(jdbc.preparar(dto,"camila")).thenReturn(Optional.of(context));
    }
    @Test void preservesFinancialRoundingDurationAndResponse() {
        when(jdbc.insertar(eq(dto),eq(context),eq(LocalTime.of(11,30)),eq(new BigDecimal("60.05")),
            eq(new BigDecimal("7.81")),eq(new BigDecimal("67.86")))).thenReturn(new Persistida(99,"SHU-2030-12345"));
        var result=service.crearCita(dto,"camila");
        assertEquals(new BigDecimal("60.05"),result.getSubtotal());assertEquals(new BigDecimal("7.81"),result.getIva());
        assertEquals(new BigDecimal("67.86"),result.getTotal());assertEquals(LocalTime.of(11,30),result.getHoraFin());
        assertEquals("Camila Calderón",result.getClienteNombre());assertEquals(List.of("Servicio A","Servicio B"),result.getServiciosNombres());
        assertEquals("Tarjeta",result.getMetodoPagoPreferente());assertEquals("Confirmed",result.getEstado());
    }
    @Test void overlapStillComesFromDatabaseAndIsNotRetried() {
        var error=new DataIntegrityViolationException("overlap",new java.sql.SQLException("overlap","23P01"));
        when(jdbc.insertar(any(),any(),any(),any(),any(),any())).thenThrow(error);
        assertSame(error,assertThrows(DataIntegrityViolationException.class,()->service.crearCita(dto,"camila")));
        verify(jdbc,times(1)).insertar(any(),any(),any(),any(),any(),any());
    }
    @Test void inactiveStylistRejectsBeforeAnyInsert() {
        when(jdbc.preparar(dto,"camila")).thenReturn(Optional.of(new Contexto(1,"Camila",null,null,2,"Estilista",false,context.servicios())));
        assertThrows(IllegalArgumentException.class,()->service.crearCita(dto,"camila"));
        verify(jdbc,never()).insertar(any(),any(),any(),any(),any(),any());
    }
    @Test void invalidDurationDoesNotWrite() {
        dto.setHoraInicio(LocalTime.of(23,30));assertThrows(IllegalArgumentException.class,()->service.crearCita(dto,"camila"));
        verify(jdbc,never()).insertar(any(),any(),any(),any(),any(),any());
    }
    @Test void missingServiceAndRequiredFieldsDoNotWrite() {
        dto.setServicioIds(List.of());assertThrows(IllegalArgumentException.class,()->service.crearCita(dto,"camila"));
        verifyNoInteractions(jdbc);
    }
}
