package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.disponibilidad.DisponibilidadSalida;
import com.shushinestudio.dtos.disponibilidad.EstilistaSalida;
import com.shushinestudio.dtos.disponibilidad.FranjaHorariaDto;
import com.shushinestudio.modelos.BloqueoHorario;
import com.shushinestudio.repositorios.DisponibilidadConsultaRepository;
import com.shushinestudio.modelos.Estilista;
import com.shushinestudio.modelos.HorarioEstilista;
import com.shushinestudio.repositorios.IBloqueoHorarioRepository;
import com.shushinestudio.repositorios.IEstilistaRepository;
import com.shushinestudio.repositorios.IHorarioEstilistaRepository;
import com.shushinestudio.servicios.interfaces.IDisponibilidadService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisponibilidadService implements IDisponibilidadService {

    @Autowired
    private IEstilistaRepository estilistaRepository;

    @Autowired
    private IHorarioEstilistaRepository horarioEstilistaRepository;

    @Autowired
    private IBloqueoHorarioRepository bloqueoHorarioRepository;

    @Autowired
    private DisponibilidadConsultaRepository disponibilidadConsultaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EstilistaSalida> obtenerEstilistasActivos() {
        return estilistaRepository.findByActivoTrue().stream()
                .map(e -> modelMapper.map(e, EstilistaSalida.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstilistaSalida obtenerEstilistaPorId(Integer id) {
        return estilistaRepository.findById(id)
                .map(e -> modelMapper.map(e, EstilistaSalida.class))
                .orElse(null);
    }

    @Override
    public DisponibilidadSalida calcularDisponibilidad(Integer estilistaId, LocalDate fecha, Integer duracionMinutos) {
        return calcularDisponibilidad(estilistaId, fecha, duracionMinutos, null);
    }

    @Override
    public DisponibilidadSalida calcularDisponibilidad(Integer estilistaId, LocalDate fecha, Integer duracionMinutos, Integer servicioId) {
        // JdbcTemplate uses one autocommit SELECT: a consistent statement snapshot without
        // extra JPA transaction/commit round trips or lazy entity loads.
        var contexto = disponibilidadConsultaRepository.consultar(estilistaId, fecha, servicioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el estilista con ID: " + estilistaId));
        if (contexto.inicio() == null) {
            return DisponibilidadSalida.builder().estilistaId(contexto.estilistaId())
                    .estilistaNombre(contexto.nombre()).fecha(fecha).franjas(List.of()).build();
        }
        int duracion = duracionMinutos != null ? duracionMinutos
                : contexto.duracionServicio() != null ? contexto.duracionServicio() : 30;
        int pasoMinutos = duracion > 0 ? duracion : 30;
        if (pasoMinutos >= 24 * 60) throw new IllegalArgumentException("La duración debe ser menor de 24 horas");
        var bloqueos = contexto.bloqueos();
        var citas = contexto.citas();
        List<FranjaHorariaDto> franjas = new ArrayList<>();

        long finNanos = contexto.fin().toNanoOfDay();
        long duracionNanos = java.time.Duration.ofMinutes(pasoMinutos).toNanos();
        long pasoNanos = java.time.Duration.ofMinutes(30).toNanos();
        // Linear time arithmetic preserves seconds without wrapping to midnight.
        for (long nanos = contexto.inicio().toNanoOfDay(); nanos + duracionNanos <= finNanos; nanos += pasoNanos) {
            LocalTime slotInicio = LocalTime.ofNanoOfDay(nanos);
            LocalTime slotFin = LocalTime.ofNanoOfDay(nanos + duracionNanos);

            boolean disponible = true;
            String motivo = null;

            // 1. Validar horario de almuerzo
            if (contexto.almuerzoInicio() != null && contexto.almuerzoFin() != null) {
                if (seSolapan(slotInicio, slotFin, contexto.almuerzoInicio(), contexto.almuerzoFin())) {
                    disponible = false;
                    motivo = "Horario de almuerzo";
                }
            }

            // 2. Validar bloqueos horarios (vacaciones, capacitaciones, permisos)
            if (disponible) {
                for (var b : bloqueos) {
                    if (seSolapan(slotInicio, slotFin, b.inicio(), b.fin())) {
                        disponible = false;
                        motivo = b.motivo();
                        break;
                    }
                }
            }

            // 3. Validar citas agendadas
            if (disponible) {
                for (var c : citas) {
                    if (seSolapan(slotInicio, slotFin, c.inicio(), c.fin())) {
                        disponible = false;
                        motivo = "Horario reservado";
                        break;
                    }
                }
            }

            // 4. Validar horas pasadas si la consulta es hoy
            if (disponible && fecha.isEqual(LocalDate.now()) && slotInicio.isBefore(LocalTime.now())) {
                disponible = false;
                motivo = "Horario no disponible";
            }

            franjas.add(FranjaHorariaDto.builder()
                    .horaInicio(slotInicio)
                    .horaFin(slotFin)
                    .disponible(disponible)
                    .motivoNoDisponible(motivo)
                    .build());

        }

        return DisponibilidadSalida.builder()
                .estilistaId(contexto.estilistaId())
                .estilistaNombre(contexto.nombre())
                .fecha(fecha)
                .franjas(franjas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstilistaSalida> obtenerTodosEstilistas() {
        return estilistaRepository.findAll().stream()
                .map(e -> modelMapper.map(e, EstilistaSalida.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EstilistaSalida crearEstilista(com.shushinestudio.dtos.disponibilidad.EstilistaGuardarDto dto) {
        Estilista estilista = Estilista.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .especialidadPrincipal(dto.getEspecialidadPrincipal())
                .biografia(dto.getBiografia() != null ? dto.getBiografia() : "Estilista profesional en Shushine Studio")
                .avatarUrl(dto.getAvatarUrl())
                .colorAgenda(dto.getColorAgenda() != null ? dto.getColorAgenda() : "#D48B96")
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
        Estilista guardado = estilistaRepository.save(estilista);
        return modelMapper.map(guardado, EstilistaSalida.class);
    }

    @Override
    @Transactional
    public void actualizarEstadoEstilista(Integer id, Boolean activo) {
        estilistaRepository.findById(id).ifPresent(e -> {
            e.setActivo(activo);
            estilistaRepository.save(e);
        });
    }

    @Override
    @Transactional
    public void eliminarEstilista(Integer id) {
        estilistaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioEstilista> obtenerHorariosEstilista(Integer estilistaId) {
        return horarioEstilistaRepository.findByEstilistaId(estilistaId);
    }

    @Override
    @Transactional
    public HorarioEstilista guardarHorarioEstilista(Integer estilistaId, HorarioEstilista horario) {
        Estilista estilista = estilistaRepository.findById(estilistaId)
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado con ID: " + estilistaId));

        var existenteOpt = horarioEstilistaRepository.findByEstilistaIdAndDiaSemana(estilistaId, horario.getDiaSemana());
        HorarioEstilista target = existenteOpt.orElseGet(() -> HorarioEstilista.builder()
                .estilista(estilista)
                .diaSemana(horario.getDiaSemana())
                .build());

        target.setHoraInicio(horario.getHoraInicio());
        target.setHoraFin(horario.getHoraFin());
        target.setHoraInicioAlmuerzo(horario.getHoraInicioAlmuerzo());
        target.setHoraFinAlmuerzo(horario.getHoraFinAlmuerzo());
        target.setActivo(horario.getActivo() != null ? horario.getActivo() : true);

        return horarioEstilistaRepository.save(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloqueoHorario> obtenerBloqueosEstilista(Integer estilistaId) {
        return bloqueoHorarioRepository.findByEstilistaId(estilistaId);
    }

    @Override
    @Transactional
    public BloqueoHorario crearBloqueo(Integer estilistaId, BloqueoHorario bloqueo) {
        Estilista estilista = estilistaRepository.findById(estilistaId)
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado con ID: " + estilistaId));

        bloqueo.setEstilista(estilista);
        if (bloqueo.getMotivo() == null || bloqueo.getMotivo().isBlank()) {
            bloqueo.setMotivo("Bloqueo administrativo");
        }
        return bloqueoHorarioRepository.save(bloqueo);
    }

    @Override
    @Transactional
    public void eliminarBloqueo(Integer bloqueoId) {
        bloqueoHorarioRepository.deleteById(bloqueoId);
    }

    private boolean seSolapan(LocalTime inicioA, LocalTime finA, LocalTime inicioB, LocalTime finB) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }
}
