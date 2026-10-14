package com.shushinestudio.dtos.producto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoSalidaDto {

    private Integer id;
    private String codigoProducto;
    private Integer idCategoria;
    private String nombreCategoria;
    private String nombre;
    private String marca;
    private String descripcion;
    private BigDecimal precio;
    private Integer stockActual;
    private Integer stockMinimo;
    private String imagenUrl;
    private Boolean activo;
    private Boolean stockBajo;
}
