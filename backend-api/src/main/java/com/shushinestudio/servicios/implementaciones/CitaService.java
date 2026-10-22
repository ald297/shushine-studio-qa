package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.cita.CitaCambiarEstado;
import com.shushinestudio.dtos.cita.CitaGuardar;
import com.shushinestudio.dtos.cita.CitaSalida;
import com.shushinestudio.modelos.*;
import com.shushinestudio.repositorios.*;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import com.shushinestudio.servicios.interfaces.ICitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaService implements ICitaService {

    @Autowired
    private ICitaRepository citaRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IEstilistaRepository estilistaRepository;

    @Autowired
    private IServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaJdbcRepository reservaJdbcRepository;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Override
    public CitaSalida crearCita(CitaGuardar dto, String userLogin) {
        if (dto.getServicioIds() == null || dto.getServicioIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un servicio para la cita");
        }
        if (dto.getEstilistaId() == null || dto.getFechaCita() == null || dto.getHoraInicio() == null) {
            throw new IllegalArgumentException("Debe indicar estilista, fecha y hora de inicio");
        }
        var contexto = reservaJdbcRepository.preparar(dto, userLogin)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no autenticado"));
        // Rare legacy profiles retain the guarded, transactional creation path.
        if (contexto.clienteId() == null) {
            var tx = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
            tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
            return tx.execute(status -> crearCitaConPerfilLegado(dto, userLogin));
        }
        if (contexto.estilistaId() == null) throw new IllegalArgumentException("Estilista no encontrado");
        if (!Boolean.TRUE.equals(contexto.estilistaActivo())) {
            throw new IllegalArgumentException("El estilista seleccionado no se encuentra activo");
        }
        var servicios = contexto.servicios();
        if (servicios.isEmpty()) throw new IllegalArgumentException("Los servicios seleccionados no existen");
        int duracion = servicios.stream().mapToInt(ReservaJdbcRepository.ServicioDato::duracionMinutos).sum();
        LocalTime fin = calcularHoraFin(dto.getHoraInicio(), duracion);
        BigDecimal subtotal = servicios.stream().map(ReservaJdbcRepository.ServicioDato::precioBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);
        var guardada = reservaJdbcRepository.insertar(dto, contexto, fin, subtotal, iva, total);
        return CitaSalida.builder().id(guardada.id()).codigoCita(guardada.codigo()).clienteId(contexto.clienteId())
                .clienteNombre(contexto.nombre() + " " + (contexto.apellido() != null ? contexto.apellido() : ""))
                .clienteTelefono(contexto.telefono()).estilistaId(contexto.estilistaId()).estilistaNombre(contexto.estilistaNombre())
                .fechaCita(dto.getFechaCita()).horaInicio(dto.getHoraInicio()).horaFin(fin).estado("Confirmed")
                .subtotal(subtotal).iva(iva).total(total)
                .metodoPagoPreferente(dto.getMetodoPagoPreferente() != null ? dto.getMetodoPagoPreferente() : "Efectivo")
                .estadoPago("Pending").notasCliente(dto.getNotasCliente())
                .serviciosNombres(servicios.stream().map(ReservaJdbcRepository.ServicioDato::nombre).toList()).build();
    }

    private CitaSalida crearCitaConPerfilLegado(CitaGuardar citaGuardar, String userLogin) {
        reservaJdbcRepository.bloquearAgenda(citaGuardar.getEstilistaId(), citaGuardar.getFechaCita());
        // 1. Obtener o crear entidad Cliente para el usuario autenticado
        Cliente cliente = clienteRepository.findByUsuarioLogin(userLogin).orElseGet(() -> {
            // Solo los perfiles antiguos sin Cliente necesitan serializar su creación.
            Usuario usuario = usuarioRepository.findByLoginForUpdate(userLogin)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no autenticado"));
            Cliente existente = clienteRepository.findByUsuarioLogin(userLogin).orElse(null);
            if (existente != null) return existente;
            Cliente nuevoCliente = Cliente.builder()
                    .usuario(usuario)
                    .nombreWalkin(usuario.getNombre() + (usuario.getApellido() != null ? " " + usuario.getApellido() : ""))
                    .telefonoWalkin(usuario.getTelefono())
                    .nivelFidelidad("Bronce")
                    .puntosAcumulados(0)
                    .esWalkin(false)
                    .build();
            return clienteRepository.save(nuevoCliente);
        });

        // 2. Validar Estilista
        Estilista estilista = estilistaRepository.findById(citaGuardar.getEstilistaId())
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado"));
        if (!Boolean.TRUE.equals(estilista.getActivo())) {
            throw new IllegalArgumentException("El estilista seleccionado no se encuentra activo");
        }

        // 3. Obtener Servicios y calcular duración y subtotal
        if (citaGuardar.getServicioIds() == null || citaGuardar.getServicioIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un servicio para la cita");
        }

        List<Servicio> servicios = servicioRepository.findAllById(citaGuardar.getServicioIds());
        if (servicios.isEmpty()) {
            throw new IllegalArgumentException("Los servicios seleccionados no existen");
        }

        int duracionTotalMinutos = servicios.stream()
                .mapToInt(Servicio::getDuracionMinutos)
                .sum();
        LocalTime horaFinCalculada = calcularHoraFin(citaGuardar.getHoraInicio(), duracionTotalMinutos);

        // PostgreSQL comprueba el solapamiento atómicamente al insertar (SQLSTATE 23P01).
        // Un SELECT previo no evita carreras y añade un viaje de red a cada reserva.

        // 5. Cálculo Financiero (Subtotal, IVA 13%, Total)
        BigDecimal subtotal = servicios.stream()
                .map(Servicio::getPrecioBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);

        // 6. Generar Código Único de Cita (SHU-YYYY-NNNN)
        String codigoCita = generarCodigoCita(citaGuardar.getFechaCita());

        // 7. Persistir Cita
        Cita nuevaCita = Cita.builder()
                .codigoCita(codigoCita)
                .cliente(cliente)
                .estilista(estilista)
                .fechaCita(citaGuardar.getFechaCita())
                .horaInicio(citaGuardar.getHoraInicio())
                .horaFin(horaFinCalculada)
                .estado("Confirmed")
                .subtotal(subtotal)
                .descuentoPuntos(BigDecimal.ZERO)
                .iva(iva)
                .total(total)
                .metodoPagoPreferente(citaGuardar.getMetodoPagoPreferente() != null ? citaGuardar.getMetodoPagoPreferente() : "Efectivo")
                .estadoPago("Pending")
                .esWalkin(false)
                .notasCliente(citaGuardar.getNotasCliente())
                .origen("App")
                .servicios(new ArrayList<>())
                .build();

        for (Servicio s : servicios) {
            CitaServicio detalle = CitaServicio.builder()
                    .cita(nuevaCita)
                    .servicio(s)
                    .precioAplicado(s.getPrecioBase())
                    .duracionMinutos(s.getDuracionMinutos())
                    .build();
            nuevaCita.getServicios().add(detalle);
        }

        Cita guardada = citaRepository.save(nuevaCita);
        return mapToSalida(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaSalida> obtenerMisCitas(String userLogin) {
        return citaRepository.findByClienteUsuarioLoginOrderByFechaCitaDescHoraInicioDesc(userLogin).stream()
                .map(this::mapToSalida)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CitaSalida> obtenerTodasPaginadas(Pageable pageable) {
        Page<Cita> page = citaRepository.findAll(pageable);
        List<CitaSalida> list = page.stream().map(this::mapToSalida).collect(Collectors.toList());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public CitaSalida obtenerPorId(Integer id) {
        return citaRepository.findById(id).map(this::mapToSalida).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaSalida obtenerPorCodigo(String codigoCita) {
        return citaRepository.findByCodigoCita(codigoCita).map(this::mapToSalida).orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CitaSalida crearCitaWalkin(com.shushinestudio.dtos.cita.CitaWalkinGuardar walkinGuardar) {
        if (walkinGuardar.getEstilistaId() == null || walkinGuardar.getFechaCita() == null || walkinGuardar.getHoraInicio() == null) {
            throw new IllegalArgumentException("Debe indicar estilista, fecha y hora de inicio");
        }
        reservaJdbcRepository.bloquearAgenda(walkinGuardar.getEstilistaId(), walkinGuardar.getFechaCita());
        // 1. Crear cliente Walk-in
        Cliente clienteWalkin = Cliente.builder()
                .nombreWalkin(walkinGuardar.getNombreCliente())
                .telefonoWalkin(walkinGuardar.getTelefonoCliente())
                .nivelFidelidad("Walk-in")
                .puntosAcumulados(0)
                .esWalkin(true)
                .build();
        Cliente clienteGuardado = clienteRepository.save(clienteWalkin);

        // 2. Validar estilista
        Estilista estilista = estilistaRepository.findById(walkinGuardar.getEstilistaId())
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado"));
        if (!Boolean.TRUE.equals(estilista.getActivo())) {
            throw new IllegalArgumentException("El estilista seleccionado no se encuentra activo");
        }

        // 3. Validar servicios
        List<Servicio> servicios = servicioRepository.findAllById(walkinGuardar.getServicioIds());
        if (servicios.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un servicio válido");
        }
        int duracion = servicios.stream().mapToInt(Servicio::getDuracionMinutos).sum();
        LocalTime horaFin = calcularHoraFin(walkinGuardar.getHoraInicio(), duracion);

        // La restricción de exclusión de PostgreSQL también protege reservas Walk-in.

        // 5. Cálculos financieros
        BigDecimal subtotal = servicios.stream()
                .map(Servicio::getPrecioBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);

        String codigoCita = generarCodigoCita(walkinGuardar.getFechaCita());

        Cita cita = Cita.builder()
                .codigoCita(codigoCita)
                .cliente(clienteGuardado)
                .estilista(estilista)
                .fechaCita(walkinGuardar.getFechaCita())
                .horaInicio(walkinGuardar.getHoraInicio())
                .horaFin(horaFin)
                .estado("Confirmed")
                .subtotal(subtotal)
                .descuentoPuntos(BigDecimal.ZERO)
                .iva(iva)
                .total(total)
                .metodoPagoPreferente(walkinGuardar.getMetodoPagoPreferente() != null ? walkinGuardar.getMetodoPagoPreferente() : "Efectivo")
                .estadoPago("Pending")
                .esWalkin(true)
                .notasCliente(walkinGuardar.getNotas())
                .origen("Walk-in")
                .servicios(new ArrayList<>())
                .build();

        for (Servicio s : servicios) {
            CitaServicio detalle = CitaServicio.builder()
                    .cita(cita)
                    .servicio(s)
                    .precioAplicado(s.getPrecioBase())
                    .duracionMinutos(s.getDuracionMinutos())
                    .build();
            cita.getServicios().add(detalle);
        }

        Cita guardada = citaRepository.save(cita);
        return mapToSalida(guardada);
    }

    @Override
    @Transactional
    public CitaSalida cambiarEstado(CitaCambiarEstado cambio) {
        return cambiarEstado(cambio, null, true);
    }

    @Override
    @Transactional
    public CitaSalida cambiarEstado(CitaCambiarEstado cambio, String userLogin, boolean isAdmin) {
        Cita cita = citaRepository.findById(cambio.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + cambio.getId()));

        if (!isAdmin) {
            boolean esDueno = cita.getCliente() != null
                    && cita.getCliente().getUsuario() != null
                    && userLogin != null
                    && userLogin.equalsIgnoreCase(cita.getCliente().getUsuario().getLogin());
            if (!esDueno) {
                throw new org.springframework.security.access.AccessDeniedException("No posee privilegios para modificar una cita que no le pertenece.");
            }
            String targetVerif = normalizarEstado(cambio.getNuevoEstado());
            if (!"Cancelled".equalsIgnoreCase(targetVerif)) {
                throw new org.springframework.security.access.AccessDeniedException("Solo los administradores pueden cambiar el estado a " + targetVerif);
            }
        }

        String estadoActual = cita.getEstado();
        if ("Completed".equalsIgnoreCase(estadoActual) || "Cancelled".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("No se puede cambiar el estado de una cita finalizada o cancelada.");
        }

        String target = normalizarEstado(cambio.getNuevoEstado());
        cita.setEstado(target);
        if (cambio.getMotivoCancelacion() != null) {
            cita.setMotivoCancelacion(cambio.getMotivoCancelacion());
        }

        Cita actualizada = citaRepository.save(cita);
        return mapToSalida(actualizada);
    }

    @Override
    @Transactional
    public CitaSalida cancelarCita(Integer id, String motivo, String userLogin, boolean isAdmin) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        if (!isAdmin) {
            boolean esDueno = cita.getCliente() != null
                    && cita.getCliente().getUsuario() != null
                    && userLogin != null
                    && userLogin.equalsIgnoreCase(cita.getCliente().getUsuario().getLogin());
            if (!esDueno) {
                throw new org.springframework.security.access.AccessDeniedException("No tiene permisos para cancelar una cita que no le pertenece.");
            }
        }

        String estadoActual = cita.getEstado();
        if ("Cancelled".equalsIgnoreCase(estadoActual)) {
            return mapToSalida(cita);
        }
        if ("Completed".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("No se puede cancelar una cita que ya fue finalizada.");
        }

        cita.setEstado("Cancelled");
        cita.setMotivoCancelacion(motivo != null && !motivo.isBlank() ? motivo : "Cancelada por el cliente desde la app móvil");

        Cita actualizada = citaRepository.save(cita);
        return mapToSalida(actualizada);
    }

    private String normalizarEstado(String input) {
        if (input == null) return "Confirmed";
        return switch (input.trim().toUpperCase()) {
            case "PENDIENTE", "PENDING" -> "Pending";
            case "CONFIRMADA", "CONFIRMED" -> "Confirmed";
            case "EN_PROCESO", "IN_PROGRESS", "INPROGRESS" -> "InProgress";
            case "COMPLETADA", "COMPLETED" -> "Completed";
            case "CANCELADA", "CANCELLED", "CANCELED" -> "Cancelled";
            default -> input;
        };
    }

    private LocalTime calcularHoraFin(LocalTime inicio, int duracion) {
        if (inicio == null || duracion <= 0 || duracion >= 24 * 60 || !inicio.plusMinutes(duracion).isAfter(inicio)) {
            throw new IllegalArgumentException("La cita debe tener una duración positiva y finalizar el mismo día.");
        }
        return inicio.plusMinutes(duracion);
    }

    private String generarCodigoCita(LocalDate fecha) {
        int anio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        long numero = citaRepository.siguienteNumeroCodigo();
        return String.format("SHU-%d-%04d", anio, numero);
    }

    private CitaSalida mapToSalida(Cita c) {
        String clienteNombre = c.getCliente() != null && c.getCliente().getUsuario() != null
                ? c.getCliente().getUsuario().getNombre() + " " + (c.getCliente().getUsuario().getApellido() != null ? c.getCliente().getUsuario().getApellido() : "")
                : (c.getCliente() != null ? c.getCliente().getNombreWalkin() : "Cliente General");

        String clienteTelefono = c.getCliente() != null && c.getCliente().getUsuario() != null
                ? c.getCliente().getUsuario().getTelefono()
                : (c.getCliente() != null ? c.getCliente().getTelefonoWalkin() : null);

        List<String> serviciosNombres = c.getServicios() != null
                ? c.getServicios().stream().map(cs -> cs.getServicio().getNombre()).collect(Collectors.toList())
                : List.of();

        return CitaSalida.builder()
                .id(c.getId())
                .codigoCita(c.getCodigoCita())
                .clienteId(c.getCliente() != null ? c.getCliente().getId() : null)
                .clienteNombre(clienteNombre)
                .clienteTelefono(clienteTelefono)
                .estilistaId(c.getEstilista() != null ? c.getEstilista().getId() : null)
                .estilistaNombre(c.getEstilista() != null ? c.getEstilista().getNombreCompleto() : null)
                .fechaCita(c.getFechaCita())
                .horaInicio(c.getHoraInicio())
                .horaFin(c.getHoraFin())
                .estado(c.getEstado())
                .subtotal(c.getSubtotal())
                .iva(c.getIva())
                .total(c.getTotal())
                .metodoPagoPreferente(c.getMetodoPagoPreferente())
                .estadoPago(c.getEstadoPago())
                .notasCliente(c.getNotasCliente())
                .serviciosNombres(serviciosNombres)
                .build();
    }
}
