package com.shushinestudio.dtos.timeline;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineItemSalida {
    private Integer citaId;
    private String codigoCita;
    private Integer clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private Integer estilistaId;
    private String estilistaNombre;
    private LocalDate fechaCita;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private BigDecimal total;
    private Boolean esWalkin;
    private String notas;
    private List<String> servicios;
}
