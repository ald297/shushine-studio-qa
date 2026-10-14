package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.resena.ResenaCrearDto;
import com.shushinestudio.dtos.resena.ResenaSalidaDto;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.modelos.Resena;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IResenaRepository;
import com.shushinestudio.servicios.interfaces.IResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResenaService implements IResenaService {

    @Autowired
    private IResenaRepository resenaRepository;

    @Autowired
    private ICitaRepository citaRepository;

    @Override
    @Transactional
    public ResenaSalidaDto crearResena(String loginCliente, ResenaCrearDto dto) {
        Cita cita = citaRepository.findById(dto.getIdCita())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cita con ID " + dto.getIdCita()));

        if (cita.getCliente() == null || cita.getCliente().getUsuario() == null ||
                !cita.getCliente().getUsuario().getLogin().equalsIgnoreCase(loginCliente)) {
            throw new SecurityException("No tiene autorización para calificar una cita que no le pertenece.");
        }

        String estado = cita.getEstado() != null ? cita.getEstado().trim().toUpperCase() : "";
        if (!estado.equals("COMPLETED") && !estado.equals("COMPLETADA")) {
            throw new IllegalStateException("Solo se pueden calificar citas completadas. Estado actual: " + cita.getEstado());
        }

        if (resenaRepository.existsByCitaId(cita.getId())) {
            throw new IllegalStateException("Esta cita ya cuenta con una calificación registrada.");
        }

        Resena resena = Resena.builder()
                .cita(cita)
                .cliente(cita.getCliente())
                .estilista(cita.getEstilista())
                .estrellasGeneral(dto.getEstrellas())
                .estrellasCalidad(dto.getEstrellasCalidad() != null ? dto.getEstrellasCalidad() : dto.getEstrellas())
                .estrellasAtencion(dto.getEstrellasAtencion() != null ? dto.getEstrellasAtencion() : dto.getEstrellas())
                .estrellasAmbiente(dto.getEstrellasAmbiente() != null ? dto.getEstrellasAmbiente() : dto.getEstrellas())
                .comentario(dto.getComentario())
                .visiblePublica(dto.getVisiblePublica() != null ? dto.getVisiblePublica() : true)
                .fechaEmision(LocalDateTime.now())
                .build();

        Resena guardada = resenaRepository.save(resena);
        return mapearADto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaSalidaDto> obtenerMisResenas(String loginCliente) {
        return resenaRepository.findByClienteLogin(loginCliente).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaSalidaDto> obtenerTodasResenasAdmin() {
        return resenaRepository.findAllByOrderByFechaEmisionDesc().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaSalidaDto obtenerPorId(Integer id) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada con ID " + id));
        return mapearADto(resena);
    }

    private ResenaSalidaDto mapearADto(Resena resena) {
        String servicioNombre = "Servicio del Salón";
        if (resena.getCita() != null && resena.getCita().getServicios() != null && !resena.getCita().getServicios().isEmpty()) {
            servicioNombre = resena.getCita().getServicios().get(0).getServicio() != null ?
                    resena.getCita().getServicios().get(0).getServicio().getNombre() : "Servicio";
        }

        String clienteNombre = "Cliente";
        if (resena.getCliente() != null && resena.getCliente().getUsuario() != null) {
            clienteNombre = (resena.getCliente().getUsuario().getNombre() != null ? resena.getCliente().getUsuario().getNombre() : "") +
                    (resena.getCliente().getUsuario().getApellido() != null ? " " + resena.getCliente().getUsuario().getApellido() : "");
        }

        String estilistaNombre = "Estilista";
        if (resena.getEstilista() != null) {
            estilistaNombre = resena.getEstilista().getNombreCompleto();
        }

        return ResenaSalidaDto.builder()
                .id(resena.getId())
                .idCita(resena.getCita() != null ? resena.getCita().getId() : null)
                .codigoCita(resena.getCita() != null ? resena.getCita().getCodigoCita() : null)
                .idCliente(resena.getCliente() != null ? resena.getCliente().getId() : null)
                .nombreCliente(clienteNombre.trim())
                .idEstilista(resena.getEstilista() != null ? resena.getEstilista().getId() : null)
                .nombreEstilista(estilistaNombre)
                .servicioNombre(servicioNombre)
                .estrellas(resena.getEstrellasGeneral())
                .estrellasCalidad(resena.getEstrellasCalidad())
                .estrellasAtencion(resena.getEstrellasAtencion())
                .estrellasAmbiente(resena.getEstrellasAmbiente())
                .comentario(resena.getComentario())
                .visiblePublica(resena.getVisiblePublica())
                .fechaEmision(resena.getFechaEmision())
                .build();
    }
}
