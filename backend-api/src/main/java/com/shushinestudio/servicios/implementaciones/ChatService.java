package com.shushinestudio.servicios.implementaciones;

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
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import com.shushinestudio.servicios.interfaces.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService implements IChatService {

    @Autowired
    private IConversacionRepository conversacionRepository;

    @Autowired
    private IMensajeRepository mensajeRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // =========================================================================
    // OPERACIONES PARA CLIENTE
    // =========================================================================

    @Override
    @Transactional
    public ConversacionSalidaDto obtenerOCrearConversacionCliente(String userLogin) {
        Usuario usuario = obtenerUsuarioPorLogin(userLogin);
        Cliente cliente = obtenerOCrearClienteParaUsuario(usuario);

        Conversacion conversacion = conversacionRepository.findByClienteId(cliente.getId())
                .orElseGet(() -> {
                    Usuario admin = usuarioRepository.findByLogin("admin").orElse(null);
                    Conversacion nueva = Conversacion.builder()
                            .cliente(cliente)
                            .admin(admin)
                            .fechaCreacion(LocalDateTime.now())
                            .fechaUltimoMensaje(LocalDateTime.now())
                            .activa(true)
                            .build();
                    return conversacionRepository.save(nueva);
                });

        return mapearConversacionSalida(conversacion, usuario.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MensajeSalidaDto> obtenerMensajesCliente(String userLogin, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByLogin(userLogin).orElse(null);
        if (usuario == null) {
            return Page.empty(pageable);
        }

        Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioId(usuario.getId());
        if (clienteOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        Optional<Conversacion> conversacionOpt = conversacionRepository.findByClienteId(clienteOpt.get().getId());
        if (conversacionOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        return mensajeRepository.findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(conversacionOpt.get().getId(), pageable)
                .map(m -> mapearMensajeSalida(m, usuario.getId()));
    }

    @Override
    @Transactional
    public MensajeSalidaDto enviarMensajeCliente(String userLogin, MensajeEnviarDto dto) {
        validarContenido(dto.getContenido(), dto.getImagenUrl());

        Usuario usuario = obtenerUsuarioPorLogin(userLogin);
        Cliente cliente = obtenerOCrearClienteParaUsuario(usuario);

        Conversacion conversacion = conversacionRepository.findByClienteId(cliente.getId())
                .orElseGet(() -> {
                    Usuario admin = usuarioRepository.findByLogin("admin").orElse(null);
                    Conversacion nueva = Conversacion.builder()
                            .cliente(cliente)
                            .admin(admin)
                            .fechaCreacion(LocalDateTime.now())
                            .fechaUltimoMensaje(LocalDateTime.now())
                            .activa(true)
                            .build();
                    return conversacionRepository.save(nueva);
                });

        String texto = (dto.getContenido() != null && !dto.getContenido().isBlank())
                ? dto.getContenido().trim()
                : "[Foto adjunta]";

        LocalDateTime now = LocalDateTime.now();
        Mensaje mensaje = Mensaje.builder()
                .conversacion(conversacion)
                .remitente(usuario)
                .contenido(texto)
                .imagenUrl(dto.getImagenUrl())
                .fechaEnvio(now)
                .leido(false)
                .activo(true)
                .build();

        Mensaje guardado = mensajeRepository.save(mensaje);

        conversacion.setFechaUltimoMensaje(now);
        conversacionRepository.save(conversacion);

        return mapearMensajeSalida(guardado, usuario.getId());
    }

    @Override
    @Transactional
    public int marcarMensajesComoLeidosCliente(String userLogin) {
        Usuario usuario = obtenerUsuarioPorLogin(userLogin);
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil de cliente no encontrado"));

        Optional<Conversacion> conversacionOpt = conversacionRepository.findByClienteId(cliente.getId());
        if (conversacionOpt.isEmpty()) {
            return 0;
        }

        return mensajeRepository.marcarComoLeidos(conversacionOpt.get().getId(), usuario.getId());
    }

    // =========================================================================
    // OPERACIONES PARA ADMINISTRADOR
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ConversacionSalidaDto> obtenerConversacionesAdmin(Pageable pageable) {
        return conversacionRepository.findAllByActivaTrueOrderByFechaUltimoMensajeDesc(pageable)
                .map(c -> mapearConversacionSalida(c, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversacionSalidaDto> obtenerConversacionesAdmin() {
        return conversacionRepository.findAllByActivaTrueOrderByFechaUltimoMensajeDesc()
                .stream()
                .map(c -> mapearConversacionSalida(c, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversacionDetalleDto obtenerConversacionPorIdAdmin(Integer conversacionId) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación con ID " + conversacionId + " no encontrada"));

        List<MensajeSalidaDto> mensajesDto = mensajeRepository
                .findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(conversacion.getId())
                .stream()
                .map(m -> mapearMensajeSalida(m, conversacion.getAdmin() != null ? conversacion.getAdmin().getId() : null))
                .collect(Collectors.toList());

        String nombreCliente = obtenerNombreCompletoCliente(conversacion.getCliente());
        String telefonoCliente = conversacion.getCliente().getUsuario() != null ?
                conversacion.getCliente().getUsuario().getTelefono() : conversacion.getCliente().getTelefonoWalkin();

        Optional<Mensaje> ultimo = mensajeRepository.findTopByConversacionIdAndActivoTrueOrderByFechaEnvioDesc(conversacion.getId());
        String ultimoTexto = ultimo.map(Mensaje::getContenido).orElse("Sin mensajes");
        LocalDateTime fechaUltimo = ultimo.map(Mensaje::getFechaEnvio).orElse(conversacion.getFechaUltimoMensaje());

        UUID adminId = conversacion.getAdmin() != null ? conversacion.getAdmin().getId() : UUID.randomUUID();
        long noLeidos = mensajeRepository.countByConversacionIdAndLeidoFalseAndRemitenteIdNot(conversacion.getId(), adminId);

        return ConversacionDetalleDto.builder()
                .id(conversacion.getId())
                .idCliente(conversacion.getCliente().getId())
                .nombreCliente(nombreCliente)
                .telefonoCliente(telefonoCliente)
                .ultimoMensaje(ultimoTexto)
                .fechaUltimoMensaje(fechaUltimo)
                .mensajesNoLeidos(noLeidos)
                .activa(conversacion.getActiva())
                .mensajes(mensajesDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MensajeSalidaDto> obtenerMensajesAdmin(Integer conversacionId, Pageable pageable) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación con ID " + conversacionId + " no encontrada"));

        UUID adminId = conversacion.getAdmin() != null ? conversacion.getAdmin().getId() : null;

        return mensajeRepository.findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(conversacion.getId(), pageable)
                .map(m -> mapearMensajeSalida(m, adminId));
    }

    @Override
    @Transactional
    public MensajeSalidaDto enviarMensajeAdmin(Integer conversacionId, String adminLogin, MensajeEnviarDto dto) {
        validarContenido(dto.getContenido(), dto.getImagenUrl());

        Usuario admin = obtenerUsuarioPorLogin(adminLogin);
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación con ID " + conversacionId + " no encontrada"));

        String texto = (dto.getContenido() != null && !dto.getContenido().isBlank())
                ? dto.getContenido().trim()
                : "[Foto adjunta]";

        LocalDateTime now = LocalDateTime.now();
        Mensaje mensaje = Mensaje.builder()
                .conversacion(conversacion)
                .remitente(admin)
                .contenido(texto)
                .imagenUrl(dto.getImagenUrl())
                .fechaEnvio(now)
                .leido(false)
                .activo(true)
                .build();

        Mensaje guardado = mensajeRepository.save(mensaje);

        conversacion.setAdmin(admin);
        conversacion.setFechaUltimoMensaje(now);
        conversacionRepository.save(conversacion);

        return mapearMensajeSalida(guardado, admin.getId());
    }

    @Override
    @Transactional
    public int marcarMensajesComoLeidosAdmin(Integer conversacionId, String adminLogin) {
        Usuario admin = obtenerUsuarioPorLogin(adminLogin);
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación con ID " + conversacionId + " no encontrada"));

        return mensajeRepository.marcarComoLeidos(conversacion.getId(), admin.getId());
    }

    // =========================================================================
    // UTILIDADES Y MAPEOS
    // =========================================================================

    private void validarContenido(String contenido, String imagenUrl) {
        boolean sinTexto = contenido == null || contenido.trim().isEmpty();
        boolean sinImagen = imagenUrl == null || imagenUrl.trim().isEmpty();
        if (sinTexto && sinImagen) {
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío ni contener solo espacios.");
        }
        if (contenido != null && contenido.length() > 1000) {
            throw new IllegalArgumentException("El mensaje supera el límite máximo permitido de 1000 caracteres.");
        }
    }

    private Usuario obtenerUsuarioPorLogin(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no autenticado o no encontrado en el sistema"));
    }

    private Cliente obtenerOCrearClienteParaUsuario(Usuario usuario) {
        return clienteRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            String nombreWalkin = usuario.getNombre() + (usuario.getApellido() != null ? " " + usuario.getApellido() : "");
            Cliente nuevoCliente = Cliente.builder()
                    .usuario(usuario)
                    .nombreWalkin(nombreWalkin)
                    .telefonoWalkin(usuario.getTelefono())
                    .nivelFidelidad("Bronce")
                    .puntosAcumulados(0)
                    .esWalkin(false)
                    .build();
            return clienteRepository.save(nuevoCliente);
        });
    }

    private MensajeSalidaDto mapearMensajeSalida(Mensaje m, UUID usuarioActualId) {
        String nombreRemitente = "Usuario";
        String rolRemitente = "CLIENTE";

        if (m.getRemitente() != null) {
            if (m.getRemitente().getNombreCompleto() != null && !m.getRemitente().getNombreCompleto().isBlank()) {
                nombreRemitente = m.getRemitente().getNombreCompleto();
            } else if (m.getRemitente().getNombre() != null) {
                nombreRemitente = m.getRemitente().getNombre() + (m.getRemitente().getApellido() != null ? " " + m.getRemitente().getApellido() : "");
            }
            if (m.getRemitente().getRol() != null && m.getRemitente().getRol().getNombre() != null) {
                rolRemitente = m.getRemitente().getRol().getNombre().toUpperCase();
            }
        }

        boolean esMio = false;
        if (usuarioActualId != null && m.getRemitente() != null && m.getRemitente().getId() != null) {
            esMio = m.getRemitente().getId().equals(usuarioActualId);
        } else if (rolRemitente.contains("ADMIN")) {
            esMio = false;
        }

        return MensajeSalidaDto.builder()
                .id(m.getId())
                .conversacionId(m.getConversacion() != null ? m.getConversacion().getId() : null)
                .contenido(m.getContenido())
                .imagenUrl(m.getImagenUrl())
                .idRemitente(m.getRemitente() != null ? m.getRemitente().getId() : null)
                .nombreRemitente(nombreRemitente)
                .rolRemitente(rolRemitente)
                .esMio(esMio)
                .fechaEnvio(m.getFechaEnvio())
                .leido(m.getLeido())
                .build();
    }

    private ConversacionSalidaDto mapearConversacionSalida(Conversacion c, UUID usuarioActualId) {
        String nombreCliente = obtenerNombreCompletoCliente(c.getCliente());
        String telefonoCliente = c.getCliente().getUsuario() != null ?
                c.getCliente().getUsuario().getTelefono() : c.getCliente().getTelefonoWalkin();

        Optional<Mensaje> ultimo = mensajeRepository.findTopByConversacionIdAndActivoTrueOrderByFechaEnvioDesc(c.getId());
        String ultimoTexto = ultimo.map(Mensaje::getContenido).orElse("Sin mensajes aún");
        LocalDateTime fechaUltimo = ultimo.map(Mensaje::getFechaEnvio).orElse(c.getFechaUltimoMensaje());

        UUID excludeUserId = usuarioActualId;
        if (excludeUserId == null) {
            // Si es vista de admin, contamos los mensajes no leídos enviados por el cliente
            excludeUserId = c.getAdmin() != null ? c.getAdmin().getId() : UUID.randomUUID();
        }
        long noLeidos = mensajeRepository.countByConversacionIdAndLeidoFalseAndRemitenteIdNot(c.getId(), excludeUserId);

        return ConversacionSalidaDto.builder()
                .id(c.getId())
                .idCliente(c.getCliente().getId())
                .nombreCliente(nombreCliente)
                .telefonoCliente(telefonoCliente)
                .ultimoMensaje(ultimoTexto)
                .fechaUltimoMensaje(fechaUltimo)
                .mensajesNoLeidos(noLeidos)
                .activa(c.getActiva())
                .build();
    }

    private String obtenerNombreCompletoCliente(Cliente cliente) {
        if (cliente == null) return "Cliente";
        if (cliente.getUsuario() != null) {
            if (cliente.getUsuario().getNombreCompleto() != null && !cliente.getUsuario().getNombreCompleto().isBlank()) {
                return cliente.getUsuario().getNombreCompleto();
            }
            if (cliente.getUsuario().getNombre() != null) {
                return cliente.getUsuario().getNombre() + (cliente.getUsuario().getApellido() != null ? " " + cliente.getUsuario().getApellido() : "");
            }
        }
        if (cliente.getNombreWalkin() != null && !cliente.getNombreWalkin().isBlank()) {
            return cliente.getNombreWalkin();
        }
        return "Cliente";
    }
}
