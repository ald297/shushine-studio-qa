package com.shushinestudio.dtos.categoria;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaGuardar implements Serializable {
    private String nombre;
    private String descripcion;
    private String iconoUrl;
    private String tipo;

    public CategoriaGuardar(String nombre) {
        this.nombre = nombre;
    }
}
