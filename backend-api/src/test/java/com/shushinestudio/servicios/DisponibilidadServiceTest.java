package com.shushinestudio.servicios;

import com.shushinestudio.repositorios.DisponibilidadConsultaRepository;
import com.shushinestudio.repositorios.DisponibilidadConsultaRepository.*;
import com.shushinestudio.servicios.implementaciones.DisponibilidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisponibilidadServiceTest {
    private DisponibilidadConsultaRepository reader;
    private DisponibilidadService service;
    private final LocalDate date=LocalDate.of(2030,1,7);
    private Contexto context;
    @BeforeEach void setUp() {
        reader=mock(DisponibilidadConsultaRepository.class);service=new DisponibilidadService();
        ReflectionTestUtils.setField(service,"disponibilidadConsultaRepository",reader);
        context=new Contexto(1,"Prueba",60,LocalTime.of(9,0),LocalTime.of(17,0),LocalTime.NOON,LocalTime.of(13,0),
            List.of(new Bloqueo(LocalTime.of(15,0),LocalTime.of(16,0),"Capacitación")),
            List.of(new Ocupacion(LocalTime.of(10,0),LocalTime.of(11,0))));
    }
    private void stub(Integer serviceId) {when(reader.consultar(1,date,serviceId)).thenReturn(Optional.of(context));}
    @Test void preservesLunchBookingAndBlockReasons() {
        stub(null);var result=service.calcularDisponibilidad(1,date,30);
        assertEquals(16,result.getFranjas().size());
        var slots=result.getFranjas();assertTrue(slots.get(0).isDisponible());
        assertEquals("Horario reservado",slots.get(2).getMotivoNoDisponible());
        assertEquals("Horario de almuerzo",slots.get(6).getMotivoNoDisponible());
        assertEquals("Capacitación",slots.get(12).getMotivoNoDisponible());
    }
    @Test void usesServiceDurationWhenExplicitDurationAbsent() {
        stub(7);var slots=service.calcularDisponibilidad(1,date,null,7).getFranjas();
        assertEquals(15,slots.size());assertEquals(LocalTime.of(10,0),slots.get(0).getHoraFin());
    }
    @Test void explicitDurationOverridesService() {
        stub(7);assertEquals(16,service.calcularDisponibilidad(1,date,30,7).getFranjas().size());
    }
    @Test void invalidExplicitDurationUsesDefaultRatherThanService() {
        stub(7);assertEquals(16,service.calcularDisponibilidad(1,date,0,7).getFranjas().size());
    }
    @Test void absentServiceUsesThirtyMinutes() {
        context=new Contexto(1,"Prueba",null,LocalTime.of(9,0),LocalTime.of(10,0),null,null,List.of(),List.of());
        stub(999);assertEquals(2,service.calcularDisponibilidad(1,date,null,999).getFranjas().size());
    }
    @Test void closedDayHasNoSlots() {
        context=new Contexto(1,"Prueba",60,null,null,null,null,List.of(),List.of());
        stub(null);assertTrue(service.calcularDisponibilidad(1,date,30).getFranjas().isEmpty());
    }
    @Test void missingStylistStillRejectsRequest() {
        when(reader.consultar(1,date,null)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,()->service.calcularDisponibilidad(1,date,30));
    }
    @Test void lateDayTerminatesWithoutWrappingToMidnight() {
        context=new Contexto(1,"Prueba",30,LocalTime.of(22,30),LocalTime.of(23,30),null,null,List.of(),List.of());stub(null);
        var result=assertTimeoutPreemptively(Duration.ofSeconds(1),()->service.calcularDisponibilidad(1,date,30));
        assertEquals(2,result.getFranjas().size());assertEquals(LocalTime.of(23,30),result.getFranjas().get(1).getHoraFin());
    }
    @Test void preservesScheduleSeconds() {
        context=new Contexto(1,"Prueba",30,LocalTime.of(9,0,30),LocalTime.of(10,0,30),null,null,List.of(),List.of());stub(null);
        assertEquals(LocalTime.of(9,30,30),service.calcularDisponibilidad(1,date,30).getFranjas().get(0).getHoraFin());
    }
    @Test void durationBeyondSameDayIsRejected() {
        stub(null);assertThrows(IllegalArgumentException.class,()->service.calcularDisponibilidad(1,date,1440));
    }
}
