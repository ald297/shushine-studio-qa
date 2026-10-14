package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.timeline.TimelineItemSalida;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.servicios.interfaces.ITimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineService implements ITimelineService {

    @Autowired
    private ICitaRepository citaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TimelineItemSalida> obtenerTimeline(LocalDate fecha, Integer estilistaId) {
        LocalDate fechaFiltro = fecha != null ? fecha : LocalDate.now();

        List<Cita> citas = (estilistaId != null)
                ? citaRepository.findByFechaCitaAndEstilistaIdOrderByHoraInicioAsc(fechaFiltro, estilistaId)
                : citaRepository.findByFechaCitaOrderByHoraInicioAsc(fechaFiltro);

        return citas.stream().map(c -> {
            String clienteNombre = c.getCliente() != null && c.getCliente().getUsuario() != null
                    ? c.getCliente().getUsuario().getNombre() + " " + (c.getCliente().getUsuario().getApellido() != null ? c.getCliente().getUsuario().getApellido() : "")
                    : (c.getCliente() != null ? c.getCliente().getNombreWalkin() : "Cliente General");

            String clienteTelefono = c.getCliente() != null && c.getCliente().getUsuario() != null
                    ? c.getCliente().getUsuario().getTelefono()
                    : (c.getCliente() != null ? c.getCliente().getTelefonoWalkin() : null);

            List<String> servicios = c.getServicios() != null
                    ? c.getServicios().stream().map(cs -> cs.getServicio().getNombre()).collect(Collectors.toList())
                    : List.of();

            return TimelineItemSalida.builder()
                    .citaId(c.getId())
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
                    .total(c.getTotal())
                    .esWalkin(c.getEsWalkin())
                    .notas(c.getNotasCliente())
                    .servicios(servicios)
                    .build();
        }).collect(Collectors.toList());
    }
}
