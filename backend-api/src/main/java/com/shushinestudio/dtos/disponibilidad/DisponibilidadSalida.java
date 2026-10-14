package com.shushinestudio.dtos.disponibilidad;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadSalida implements Serializable {

    private Integer estilistaId;
    private String estilistaNombre;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private List<FranjaHorariaDto> franjas;
}
