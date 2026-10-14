package com.shushinestudio.dtos.cita;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaCambiarEstado implements Serializable {
    private Integer id;
    private String nuevoEstado; // 'Confirmed', 'InProgress', 'Completed', 'Cancelled', 'NoShow'
    private String motivoCancelacion;
}
