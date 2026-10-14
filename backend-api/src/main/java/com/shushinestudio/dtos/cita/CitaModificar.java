package com.shushinestudio.dtos.cita;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaModificar implements Serializable {

    private Integer id;
    private Integer estilistaId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCita;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    private String notasCliente;
}
