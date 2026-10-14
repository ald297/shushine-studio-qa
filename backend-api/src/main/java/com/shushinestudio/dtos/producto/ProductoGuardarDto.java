package com.shushinestudio.dtos.producto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoGuardarDto {

    private String codigoProducto;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @Builder.Default
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual = 0;

    @Builder.Default
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;

    private String imagenUrl;

    @Builder.Default
    private Boolean activo = true;
}
