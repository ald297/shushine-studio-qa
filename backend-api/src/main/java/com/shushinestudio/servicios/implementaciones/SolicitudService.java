package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.solicitud.*;
import com.shushinestudio.modelos.Cliente;
import com.shushinestudio.modelos.Cotizacion;
import com.shushinestudio.modelos.SolicitudDiseno;
import com.shushinestudio.repositorios.IClienteRepository;
import com.shushinestudio.repositorios.ICotizacionRepository;
import com.shushinestudio.repositorios.ISolicitudDisenoRepository;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import com.shushinestudio.servicios.interfaces.ISolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudService implements ISolicitudService {

    @Autowired
    private ISolicitudDisenoRepository solicitudRepository;

    @Autowired
    private ICotizacionRepository cotizacionRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public SolicitudSalidaDto crearSolicitud(String loginCliente, SolicitudCrearDto dto) {
        Cliente cliente = obtenerOCrearCliente(loginCliente);

        SolicitudDiseno solicitud = SolicitudDiseno.builder()
                .cliente(cliente)
                .servicioDeseado(dto.getServicioDeseado() != null && !dto.getServicioDeseado().isBlank() ? dto.getServicioDeseado().trim() : "Diseño Personalizado")
                .notasCliente(dto.getNotasCliente().trim())
                .imagenesReferenciaUrls(dto.getImagenesReferenciaUrls())
                .estado("Pendiente")
                .fechaSolicitud(LocalDateTime.now())
                .build();

        SolicitudDiseno guardada = solicitudRepository.save(solicitud);
        return mapearADto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudSalidaDto> obtenerMisSolicitudes(String loginCliente) {
        return solicitudRepository.findByClienteLogin(loginCliente).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudSalidaDto obtenerPorIdCliente(String loginCliente, Integer id) {
        SolicitudDiseno solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con ID " + id));

        if (solicitud.getCliente() == null || solicitud.getCliente().getUsuario() == null ||
                !solicitud.getCliente().getUsuario().getLogin().equalsIgnoreCase(loginCliente)) {
            throw new SecurityException("No tiene permisos para consultar esta solicitud.");
        }

        return mapearADto(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudSalidaDto> obtenerTodasAdmin() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudSalidaDto obtenerPorIdAdmin(Integer id) {
        SolicitudDiseno solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con ID " + id));
        return mapearADto(solicitud);
    }

    @Override
    @Transactional
    public SolicitudSalidaDto cotizarSolicitudAdmin(Integer idSolicitud, CotizacionCrearDto dto) {
        SolicitudDiseno solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con ID " + idSolicitud));

        Cotizacion cotizacion = cotizacionRepository.findBySolicitudId(idSolicitud).orElse(null);
        if (cotizacion == null) {
            cotizacion = Cotizacion.builder()
                    .solicitud(solicitud)
                    .precioPropuesto(dto.getPrecioPropuesto())
                    .descripcionTrabajo(dto.getDescripcionTrabajo().trim())
                    .estado("Propuesta")
                    .fechaCotizacion(LocalDateTime.now())
                    .build();
        } else {
            cotizacion.setPrecioPropuesto(dto.getPrecioPropuesto());
            cotizacion.setDescripcionTrabajo(dto.getDescripcionTrabajo().trim());
            cotizacion.setEstado("Propuesta");
            cotizacion.setFechaCotizacion(LocalDateTime.now());
        }

        cotizacionRepository.save(cotizacion);

        solicitud.setEstado("Cotizada");
        solicitud.setCotizacion(cotizacion);
        SolicitudDiseno guardada = solicitudRepository.save(solicitud);

        return mapearADto(guardada);
    }

    @Override
    @Transactional
    public SolicitudSalidaDto responderCotizacionCliente(String loginCliente, Integer idSolicitud, ResponderCotizacionDto dto) {
        SolicitudDiseno solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con ID " + idSolicitud));

        if (solicitud.getCliente() == null || solicitud.getCliente().getUsuario() == null ||
                !solicitud.getCliente().getUsuario().getLogin().equalsIgnoreCase(loginCliente)) {
            throw new SecurityException("No tiene permisos para responder esta solicitud.");
        }

        Cotizacion cotizacion = cotizacionRepository.findBySolicitudId(idSolicitud)
                .orElseThrow(() -> new IllegalStateException("Esta solicitud aún no cuenta con cotización para responder."));

        if (Boolean.TRUE.equals(dto.getAceptar())) {
            cotizacion.setEstado("Aceptada");
            solicitud.setEstado("Aceptada");
        } else {
            cotizacion.setEstado("Rechazada");
            solicitud.setEstado("Rechazada");
        }

        cotizacion.setFechaRespuesta(LocalDateTime.now());
        cotizacionRepository.save(cotizacion);
        SolicitudDiseno guardada = solicitudRepository.save(solicitud);

        return mapearADto(guardada);
    }

    @Override
    @Transactional
    public void cambiarEstadoAdmin(Integer idSolicitud, String nuevoEstado) {
        SolicitudDiseno solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con ID " + idSolicitud));

        solicitud.setEstado(nuevoEstado);
        solicitudRepository.save(solicitud);
    }

    private Cliente obtenerOCrearCliente(String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con login: " + login));

        return clienteRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Cliente nuevo = Cliente.builder()
                            .usuario(usuario)
                            .nombreWalkin(usuario.getNombre() + (usuario.getApellido() != null ? " " + usuario.getApellido() : ""))
                            .telefonoWalkin(usuario.getTelefono())
                            .nivelFidelidad("Bronce")
                            .puntosAcumulados(0)
                            .esWalkin(false)
                            .build();
                    return clienteRepository.save(nuevo);
                });
    }

    private SolicitudSalidaDto mapearADto(SolicitudDiseno s) {
        String clienteNombre = "Cliente";
        String clienteTelefono = "";
        if (s.getCliente() != null && s.getCliente().getUsuario() != null) {
            clienteNombre = (s.getCliente().getUsuario().getNombre() != null ? s.getCliente().getUsuario().getNombre() : "") +
                    (s.getCliente().getUsuario().getApellido() != null ? " " + s.getCliente().getUsuario().getApellido() : "");
            clienteTelefono = s.getCliente().getUsuario().getTelefono() != null ? s.getCliente().getUsuario().getTelefono() : "";
        }

        CotizacionSalidaDto cotDto = null;
        if (s.getCotizacion() != null) {
            cotDto = CotizacionSalidaDto.builder()
                    .id(s.getCotizacion().getId())
                    .idSolicitud(s.getId())
                    .precioPropuesto(s.getCotizacion().getPrecioPropuesto())
                    .descripcionTrabajo(s.getCotizacion().getDescripcionTrabajo())
                    .estado(s.getCotizacion().getEstado())
                    .fechaCotizacion(s.getCotizacion().getFechaCotizacion())
                    .fechaRespuesta(s.getCotizacion().getFechaRespuesta())
                    .build();
        }

        return SolicitudSalidaDto.builder()
                .id(s.getId())
                .idCliente(s.getCliente() != null ? s.getCliente().getId() : null)
                .nombreCliente(clienteNombre.trim())
                .telefonoCliente(clienteTelefono)
                .servicioDeseado(s.getServicioDeseado())
                .notasCliente(s.getNotasCliente())
                .imagenesReferenciaUrls(s.getImagenesReferenciaUrls())
                .estado(s.getEstado())
                .fechaSolicitud(s.getFechaSolicitud())
                .cotizacion(cotDto)
                .build();
    }
}
