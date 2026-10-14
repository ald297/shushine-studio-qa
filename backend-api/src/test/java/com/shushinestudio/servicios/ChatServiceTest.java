package com.shushinestudio.servicios;

import com.shushinestudio.dtos.chat.ConversacionDetalleDto;
import com.shushinestudio.dtos.chat.ConversacionSalidaDto;
import com.shushinestudio.dtos.chat.MensajeEnviarDto;
import com.shushinestudio.dtos.chat.MensajeSalidaDto;
import com.shushinestudio.modelos.Cliente;
import com.shushinestudio.modelos.Conversacion;
import com.shushinestudio.modelos.Mensaje;
import com.shushinestudio.repositorios.IClienteRepository;
import com.shushinestudio.repositorios.IConversacionRepository;
import com.shushinestudio.repositorios.IMensajeRepository;
import com.shushinestudio.seguridad.modelos.Rol;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import com.shushinestudio.servicios.implementaciones.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private IConversacionRepository conversacionRepository;
    private IMensajeRepository mensajeRepository;
    private IClienteRepository clienteRepository;
    private UsuarioRepository usuarioRepository;
    private ChatService chatService;

    private Usuario usuarioCliente;
    private Usuario usuarioAdmin;
    private Cliente cliente;
    private Conversacion conversacion;
    private Mensaje mensaje;

    @BeforeEach
    void setUp() {
        conversacionRepository = mock(IConversacionRepository.class);
        mensajeRepository = mock(IMensajeRepository.class);
        clienteRepository = mock(IClienteRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        chatService = new ChatService();
        ReflectionTestUtils.setField(chatService, "conversacionRepository", conversacionRepository);
        ReflectionTestUtils.setField(chatService, "mensajeRepository", mensajeRepository);
        ReflectionTestUtils.setField(chatService, "clienteRepository", clienteRepository);
        ReflectionTestUtils.setField(chatService, "usuarioRepository", usuarioRepository);

        Rol rolCliente = Rol.builder().id(2).nombre("CLIENTE").build();
        Rol rolAdmin = Rol.builder().id(1).nombre("ADMIN").build();

        usuarioCliente = Usuario.builder()
                .id(UUID.randomUUID())
                .login("cliente")
                .nombre("Camila")
                .apellido("Calderon")
                .nombreCompleto("Camila Calderon")
                .telefono("70003344")
                .rol(rolCliente)
                .activo(true)
                .build();

        usuarioAdmin = Usuario.builder()
                .id(UUID.randomUUID())
                .login("admin")
                .nombre("Administrador")
                .apellido("Shushine")
                .nombreCompleto("Administrador Shushine")
                .telefono("70001122")
                .rol(rolAdmin)
                .activo(true)
                .build();

        cliente = Cliente.builder()
                .id(1)
                .usuario(usuarioCliente)
                .nombreWalkin("Camila Calderon")
                .telefonoWalkin("70003344")
                .nivelFidelidad("Bronce")
                .puntosAcumulados(0)
                .build();

        conversacion = Conversacion.builder()
                .id(100)
                .cliente(cliente)
                .admin(usuarioAdmin)
                .fechaCreacion(LocalDateTime.now())
                .fechaUltimoMensaje(LocalDateTime.now())
                .activa(true)
                .build();

        mensaje = Mensaje.builder()
                .id(500)
                .conversacion(conversacion)
                .remitente(usuarioCliente)
                .contenido("Hola, quisiera consultar disponibilidad.")
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Cliente obtiene o crea su conversación exitosamente")
    void testObtenerOCrearConversacionCliente() {
        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(conversacionRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(conversacion));

        ConversacionSalidaDto salida = chatService.obtenerOCrearConversacionCliente("cliente");

        assertNotNull(salida);
        assertEquals(100, salida.getId());
        assertEquals("Camila Calderon", salida.getNombreCliente());
    }

    @Test
    @DisplayName("Cliente envía un mensaje y actualiza fecha de último mensaje en la conversación")
    void testEnviarMensajeCliente() {
        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(conversacionRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(conversacion));
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        MensajeEnviarDto enviarDto = MensajeEnviarDto.builder()
                .contenido("Hola, quisiera consultar disponibilidad.")
                .build();

        MensajeSalidaDto salida = chatService.enviarMensajeCliente("cliente", enviarDto);

        assertNotNull(salida);
        assertEquals(500, salida.getId());
        assertEquals("Hola, quisiera consultar disponibilidad.", salida.getContenido());
        assertTrue(salida.getEsMio());
        verify(conversacionRepository).save(any(Conversacion.class));
    }

    @Test
    @DisplayName("Rechazar envío de mensaje vacío o con solo espacios")
    void testEnviarMensajeVacioFalla() {
        MensajeEnviarDto enviarDto = MensajeEnviarDto.builder().contenido("   ").build();
        assertThrows(IllegalArgumentException.class, () ->
                chatService.enviarMensajeCliente("cliente", enviarDto));
    }

    @Test
    @DisplayName("Rechazar envío de mensaje que supere los 1000 caracteres")
    void testEnviarMensajeDemasiadoLargoFalla() {
        String textoLargo = "A".repeat(1001);
        MensajeEnviarDto enviarDto = MensajeEnviarDto.builder().contenido(textoLargo).build();
        assertThrows(IllegalArgumentException.class, () ->
                chatService.enviarMensajeCliente("cliente", enviarDto));
    }

    @Test
    @DisplayName("Cliente consulta historial de mensajes con paginación")
    void testObtenerMensajesCliente() {
        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(conversacionRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(conversacion));
        when(mensajeRepository.findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(eq(100), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mensaje)));

        Page<MensajeSalidaDto> pagina = chatService.obtenerMensajesCliente("cliente", PageRequest.of(0, 30));

        assertNotNull(pagina);
        assertEquals(1, pagina.getTotalElements());
        assertEquals("Hola, quisiera consultar disponibilidad.", pagina.getContent().get(0).getContenido());
    }

    @Test
    @DisplayName("Administrador consulta bandeja de conversaciones activas")
    void testObtenerConversacionesAdmin() {
        when(conversacionRepository.findAllByActivaTrueOrderByFechaUltimoMensajeDesc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(conversacion)));

        Page<ConversacionSalidaDto> bandeja = chatService.obtenerConversacionesAdmin(PageRequest.of(0, 30));

        assertNotNull(bandeja);
        assertEquals(1, bandeja.getTotalElements());
        assertEquals("Camila Calderon", bandeja.getContent().get(0).getNombreCliente());
    }

    @Test
    @DisplayName("Administrador abre y consulta detalle de conversación")
    void testObtenerConversacionPorIdAdmin() {
        when(conversacionRepository.findById(100)).thenReturn(Optional.of(conversacion));
        when(mensajeRepository.findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(100))
                .thenReturn(List.of(mensaje));

        ConversacionDetalleDto detalle = chatService.obtenerConversacionPorIdAdmin(100);

        assertNotNull(detalle);
        assertEquals(100, detalle.getId());
        assertEquals(1, detalle.getMensajes().size());
        assertEquals("Hola, quisiera consultar disponibilidad.", detalle.getMensajes().get(0).getContenido());
    }

    @Test
    @DisplayName("Administrador responde mensaje en conversación existente")
    void testEnviarMensajeAdmin() {
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuarioAdmin));
        when(conversacionRepository.findById(100)).thenReturn(Optional.of(conversacion));

        Mensaje respuesta = Mensaje.builder()
                .id(501)
                .conversacion(conversacion)
                .remitente(usuarioAdmin)
                .contenido("Hola Camila, claro que sí. ¿Qué día deseas agendar?")
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .activo(true)
                .build();

        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(respuesta);

        MensajeEnviarDto enviarDto = MensajeEnviarDto.builder()
                .contenido("Hola Camila, claro que sí. ¿Qué día deseas agendar?")
                .build();

        MensajeSalidaDto salida = chatService.enviarMensajeAdmin(100, "admin", enviarDto);

        assertNotNull(salida);
        assertEquals(501, salida.getId());
        assertEquals("ADMIN", salida.getRolRemitente());
        assertTrue(salida.getEsMio());
    }

    @Test
    @DisplayName("Marcar mensajes como leídos")
    void testMarcarMensajesComoLeidos() {
        when(usuarioRepository.findByLogin("cliente")).thenReturn(Optional.of(usuarioCliente));
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(conversacionRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(conversacion));
        when(mensajeRepository.marcarComoLeidos(100, usuarioCliente.getId())).thenReturn(3);

        int leidos = chatService.marcarMensajesComoLeidosCliente("cliente");
        assertEquals(3, leidos);
    }
}
