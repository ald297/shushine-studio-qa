package com.shushinestudio.dtos.categoria;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaSalida implements Serializable {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String iconoUrl;
    private String tipo;
    private Boolean activo;
}
