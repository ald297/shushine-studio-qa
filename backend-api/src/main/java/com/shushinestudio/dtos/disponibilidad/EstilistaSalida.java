package com.shushinestudio.dtos.disponibilidad;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstilistaSalida implements Serializable {
    private Integer id;
    private String nombreCompleto;
    private String especialidadPrincipal;
    private String biografia;
    private String avatarUrl;
    private String colorAgenda;
    private Boolean activo;
}
