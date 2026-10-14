package com.shushinestudio.dtos.servicio;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioSalida implements Serializable {
    private Integer id;
    private String codigoServicio;
    private Integer categoriaId;
    private String categoriaNombre;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Boolean esPrecioVariable;
    private Integer duracionMinutos;
    private Integer intervaloSeguimientoDias;
    private String imagenUrl;
    private BigDecimal costoInsumos;
    private Boolean activo;
}
