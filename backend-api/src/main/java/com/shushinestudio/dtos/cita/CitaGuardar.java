package com.shushinestudio.dtos.cita;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaGuardar implements Serializable {

    private Integer estilistaId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCita;

    @JsonFormat(pattern = "[HH:mm:ss][HH:mm]")
    private LocalTime horaInicio;

    private List<Integer> servicioIds;

    private String notasCliente;

    @Builder.Default
    private String metodoPagoPreferente = "Efectivo";
}
