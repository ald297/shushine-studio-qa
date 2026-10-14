package com.shushinestudio.dtos.categoria;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaModificar implements Serializable {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String iconoUrl;
    private String tipo;
    private Boolean activo;

    public CategoriaModificar(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
