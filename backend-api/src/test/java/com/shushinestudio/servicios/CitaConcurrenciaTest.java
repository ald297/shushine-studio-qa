package com.shushinestudio.servicios;

import com.shushinestudio.dtos.cita.CitaGuardar;
import com.shushinestudio.dtos.cita.CitaSalida;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.modelos.Cliente;
import com.shushinestudio.modelos.Estilista;
import com.shushinestudio.modelos.Servicio;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IClienteRepository;
import com.shushinestudio.repositorios.IEstilistaRepository;
import com.shushinestudio.repositorios.IServicioRepository;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import com.shushinestudio.servicios.implementaciones.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CitaConcurrenciaTest {

    private ICitaRepository citaRepository;
    private IClienteRepository clienteRepository;
    private IEstilistaRepository estilistaRepository;
    private IServicioRepository servicioRepository;
    private UsuarioRepository usuarioRepository;
    private CitaService citaService;

    private final UUID testUserId = UUID.randomUUID();
    private Usuario usuarioCliente;
    private Cliente cliente;
    private Estilista estilista;
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        citaRepository = mock(ICitaRepository.class);
        clienteRepository = mock(IClienteRepository.class);
        estilistaRepository = mock(IEstilistaRepository.class);
        servicioRepository = mock(IServicioRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        citaService = new CitaService();
        ReflectionTestUtils.setField(citaService, "citaRepository", citaRepository);
        ReflectionTestUtils.setField(citaService, "clienteRepository", clienteRepository);
        ReflectionTestUtils.setField(citaService, "estilistaRepository", estilistaRepository);
        ReflectionTestUtils.setField(citaService, "servicioRepository", servicioRepository);
        ReflectionTestUtils.setField(citaService, "usuarioRepository", usuarioRepository);

        usuarioCliente = Usuario.builder()
                .id(testUserId)
                .login("cliente")
                .nombre("Camila")
                .apellido("Calderón")
                .telefono("7000-1111")
                .build();

        cliente = Cliente.builder()
                .id(1)
                .usuario(usuarioCliente)
                .nivelFidelidad("Bronce")
                .puntosAcumulados(0)
                .esWalkin(false)
                .build();

        estilista = Estilista.builder()
                .id(1)
                .nombreCompleto("Valeria Rivas")
                .activo(true)
                .build();

        servicio = Servicio.builder()
                .id(1)
                .nombre("Balayage & Styling")
                .duracionMinutos(60)
                .precioBase(new BigDecimal("50.00"))
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Crear cita exitosa calcula IVA 13%, genera código y persiste")
    void testCrearCitaExitosa() {
        LocalDate fecha = LocalDate.of(2026, 9, 20);
        LocalTime horaInicio = LocalTime.of(10, 0);

        CitaGuardar dto = CitaGuardar.builder()
                .estilistaId(1)
                .fechaCita(fecha)
                .horaInicio(horaInicio)
                .servicioIds(List.of(1))
                .metodoPagoPreferente("Tarjeta")
                .notasCliente("Sin amoníaco por favor")
                .build();

        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(testUserId)).thenReturn(Optional.of(cliente));
        when(estilistaRepository.findById(1)).thenReturn(Optional.of(estilista));
        when(servicioRepository.findAllById(List.of(1))).thenReturn(List.of(servicio));
        // No hay solapamiento previo
        when(citaRepository.existeSolapamiento(eq(1), eq(fecha), eq(horaInicio), eq(LocalTime.of(11, 0)))).thenReturn(false);

        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> {
            Cita c = invocation.getArgument(0);
            c.setId(100);
            return c;
        });

        CitaSalida resultado = citaService.crearCita(dto, "cliente");

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigoCita());
        assertTrue(resultado.getCodigoCita().startsWith("SHU-2026-"));
        assertEquals(new BigDecimal("50.00"), resultado.getSubtotal());
        assertEquals(new BigDecimal("6.50"), resultado.getIva()); // 50 * 0.13 = 6.50
        assertEquals(new BigDecimal("56.50"), resultado.getTotal());
        assertEquals("Confirmed", resultado.getEstado());

        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("Prevención de doble reserva concurrente: lanza IllegalStateException si ya existe solapamiento")
    void testPrevencionDobleReservaConcurrente() {
        LocalDate fecha = LocalDate.of(2026, 9, 20);
        LocalTime horaInicio = LocalTime.of(10, 0);

        CitaGuardar dto = CitaGuardar.builder()
                .estilistaId(1)
                .fechaCita(fecha)
                .horaInicio(horaInicio)
                .servicioIds(List.of(1))
                .build();

        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(testUserId)).thenReturn(Optional.of(cliente));
        when(estilistaRepository.findById(1)).thenReturn(Optional.of(estilista));
        when(servicioRepository.findAllById(List.of(1))).thenReturn(List.of(servicio));

        // Simula colisión concurrente detectada en base de datos
        when(citaRepository.existeSolapamiento(eq(1), eq(fecha), eq(horaInicio), eq(LocalTime.of(11, 0)))).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                citaService.crearCita(dto, "cliente")
        );

        assertTrue(ex.getMessage().contains("no tiene disponible la franja horaria"));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Falla si el estilista seleccionado está inactivo")
    void testEstilistaInactivo() {
        estilista.setActivo(false);

        CitaGuardar dto = CitaGuardar.builder()
                .estilistaId(1)
                .fechaCita(LocalDate.of(2026, 9, 20))
                .horaInicio(LocalTime.of(10, 0))
                .servicioIds(List.of(1))
                .build();

        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(testUserId)).thenReturn(Optional.of(cliente));
        when(estilistaRepository.findById(1)).thenReturn(Optional.of(estilista));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                citaService.crearCita(dto, "cliente")
        );

        assertTrue(ex.getMessage().contains("no se encuentra activo"));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Seguridad IDOR: CLIENTE A puede cancelar su propia cita")
    void testClienteCancelaCitaPropia() {
        Cita citaPropia = Cita.builder()
                .id(101)
                .cliente(cliente)
                .estado("Confirmed")
                .build();

        when(citaRepository.findById(101)).thenReturn(Optional.of(citaPropia));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaSalida salida = citaService.cancelarCita(101, "Imprevisto", "cliente", false);

        assertNotNull(salida);
        assertEquals("Cancelled", citaPropia.getEstado());
        verify(citaRepository).save(citaPropia);
    }

    @Test
    @DisplayName("Seguridad IDOR: CLIENTE A no puede cancelar la cita de CLIENTE B (Rechazado con AccessDeniedException)")
    void testClienteNoPuedeCancelarCitaAjena() {
        Usuario usuarioB = Usuario.builder()
                .id(UUID.randomUUID())
                .login("cliente_b")
                .nombre("Otro")
                .build();
        Cliente clienteB = Cliente.builder()
                .id(202)
                .usuario(usuarioB)
                .build();
        Cita citaDeB = Cita.builder()
                .id(200)
                .cliente(clienteB)
                .estado("Confirmed")
                .build();

        when(citaRepository.findById(200)).thenReturn(Optional.of(citaDeB));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                citaService.cancelarCita(200, "Intento no autorizado", "cliente", false)
        );

        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Seguridad: ADMIN puede cancelar cualquier cita por motivos operativos")
    void testAdminPuedeCancelarCualquierCita() {
        Usuario usuarioB = Usuario.builder()
                .id(UUID.randomUUID())
                .login("cliente_b")
                .nombre("Otro")
                .build();
        Cliente clienteB = Cliente.builder()
                .id(202)
                .usuario(usuarioB)
                .build();
        Cita citaDeB = Cita.builder()
                .id(200)
                .cliente(clienteB)
                .estado("Confirmed")
                .build();

        when(citaRepository.findById(200)).thenReturn(Optional.of(citaDeB));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaSalida salida = citaService.cancelarCita(200, "Cierre por emergencia", "admin", true);

        assertNotNull(salida);
        assertEquals("Cancelled", citaDeB.getEstado());
        verify(citaRepository).save(citaDeB);
    }
}

