package com.shushinestudio.dtos.disponibilidad;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstilistaGuardarDto implements Serializable {
    private String nombreCompleto;
    private String especialidadPrincipal;
    private String biografia;
    private String avatarUrl;
    private String colorAgenda;
    private Boolean activo;
}
