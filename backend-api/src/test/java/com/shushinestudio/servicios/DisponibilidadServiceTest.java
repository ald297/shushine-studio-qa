package com.shushinestudio.servicios;

import com.shushinestudio.dtos.disponibilidad.DisponibilidadSalida;
import com.shushinestudio.dtos.disponibilidad.FranjaHorariaDto;
import com.shushinestudio.modelos.BloqueoHorario;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.modelos.Estilista;
import com.shushinestudio.modelos.HorarioEstilista;
import com.shushinestudio.repositorios.IBloqueoHorarioRepository;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IEstilistaRepository;
import com.shushinestudio.repositorios.IHorarioEstilistaRepository;
import com.shushinestudio.servicios.implementaciones.DisponibilidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisponibilidadServiceTest {

    private IEstilistaRepository estilistaRepository;
    private IHorarioEstilistaRepository horarioEstilistaRepository;
    private IBloqueoHorarioRepository bloqueoHorarioRepository;
    private ICitaRepository citaRepository;
    private DisponibilidadService disponibilidadService;

    private Estilista estilista;
    private HorarioEstilista horario;

    @BeforeEach
    void setUp() {
        estilistaRepository = mock(IEstilistaRepository.class);
        horarioEstilistaRepository = mock(IHorarioEstilistaRepository.class);
        bloqueoHorarioRepository = mock(IBloqueoHorarioRepository.class);
        citaRepository = mock(ICitaRepository.class);

        disponibilidadService = new DisponibilidadService();
        ReflectionTestUtils.setField(disponibilidadService, "estilistaRepository", estilistaRepository);
        ReflectionTestUtils.setField(disponibilidadService, "horarioEstilistaRepository", horarioEstilistaRepository);
        ReflectionTestUtils.setField(disponibilidadService, "bloqueoHorarioRepository", bloqueoHorarioRepository);
        ReflectionTestUtils.setField(disponibilidadService, "citaRepository", citaRepository);
        ReflectionTestUtils.setField(disponibilidadService, "modelMapper", new ModelMapper());

        estilista = Estilista.builder()
                .id(1)
                .nombreCompleto("Valeria Rivas")
                .especialidadPrincipal("Colorista Master")
                .activo(true)
                .build();

        horario = HorarioEstilista.builder()
                .id(10)
                .estilista(estilista)
                .diaSemana(1) // Lunes
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(17, 0))
                .horaInicioAlmuerzo(LocalTime.of(12, 0))
                .horaFinAlmuerzo(LocalTime.of(13, 0))
                .activo(true)
                .build();
    }

    @Test
    void testCalcularDisponibilidad_ExcluyeAlmuerzoYCitas() {
        LocalDate fecha = LocalDate.of(2026, 9, 21); // Un lunes
        int diaSemana = fecha.getDayOfWeek().getValue();

        when(estilistaRepository.findById(1)).thenReturn(Optional.of(estilista));
        when(horarioEstilistaRepository.findByEstilistaIdAndDiaSemanaAndActivoTrue(1, diaSemana))
                .thenReturn(Optional.of(horario));

        // Cita existente de 10:00 a 11:00
        Cita citaAgendada = Cita.builder()
                .id(100)
                .estilista(estilista)
                .fechaCita(fecha)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(11, 0))
                .estado("Confirmed")
                .build();
        when(citaRepository.findByEstilistaIdAndFechaCitaAndEstadoNot(1, fecha, "Cancelled"))
                .thenReturn(List.of(citaAgendada));

        // Bloqueo por capacitación de 15:00 a 16:00
        BloqueoHorario bloqueo = BloqueoHorario.builder()
                .id(200)
                .estilista(estilista)
                .fecha(fecha)
                .horaInicio(LocalTime.of(15, 0))
                .horaFin(LocalTime.of(16, 0))
                .motivo("Capacitación L'Oréal")
                .build();
        when(bloqueoHorarioRepository.findByEstilistaIdAndFecha(1, fecha))
                .thenReturn(List.of(bloqueo));

        DisponibilidadSalida salida = disponibilidadService.calcularDisponibilidad(1, fecha, 30);

        assertNotNull(salida);
        assertEquals(1, salida.getEstilistaId());
        assertFalse(salida.getFranjas().isEmpty());

        // Slot de las 09:00 debe estar disponible
        FranjaHorariaDto slot9am = salida.getFranjas().stream()
                .filter(f -> f.getHoraInicio().equals(LocalTime.of(9, 0)))
                .findFirst().orElseThrow();
        assertTrue(slot9am.isDisponible());

        // Slot de las 10:00 debe estar ocupado por cita agendada
        FranjaHorariaDto slot10am = salida.getFranjas().stream()
                .filter(f -> f.getHoraInicio().equals(LocalTime.of(10, 0)))
                .findFirst().orElseThrow();
        assertFalse(slot10am.isDisponible());
        assertEquals("Horario reservado", slot10am.getMotivoNoDisponible());

        // Slot de las 12:00 debe estar ocupado por almuerzo
        FranjaHorariaDto slot12pm = salida.getFranjas().stream()
                .filter(f -> f.getHoraInicio().equals(LocalTime.of(12, 0)))
                .findFirst().orElseThrow();
        assertFalse(slot12pm.isDisponible());
        assertEquals("Horario de almuerzo", slot12pm.getMotivoNoDisponible());

        // Slot de las 15:00 debe estar ocupado por capacitación
        FranjaHorariaDto slot3pm = salida.getFranjas().stream()
                .filter(f -> f.getHoraInicio().equals(LocalTime.of(15, 0)))
                .findFirst().orElseThrow();
        assertFalse(slot3pm.isDisponible());
        assertEquals("Capacitación L'Oréal", slot3pm.getMotivoNoDisponible());
    }

    @Test
    void testCalcularDisponibilidad_DiaSinHorario() {
        LocalDate domingo = LocalDate.of(2026, 9, 27); // Domingo (7)
        when(estilistaRepository.findById(1)).thenReturn(Optional.of(estilista));
        when(horarioEstilistaRepository.findByEstilistaIdAndDiaSemanaAndActivoTrue(1, 7))
                .thenReturn(Optional.empty());

        DisponibilidadSalida salida = disponibilidadService.calcularDisponibilidad(1, domingo, 30);

        assertNotNull(salida);
        assertTrue(salida.getFranjas().isEmpty(), "No deben haber franjas en días que no labora el estilista");
    }
}
