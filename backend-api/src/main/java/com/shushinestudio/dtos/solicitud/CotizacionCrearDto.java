package com.shushinestudio.dtos.solicitud;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionCrearDto {

    @NotNull(message = "El precio propuesto es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio propuesto debe ser mayor a 0")
    private BigDecimal precioPropuesto;

    @NotBlank(message = "La descripción del trabajo a realizar es obligatoria")
    private String descripcionTrabajo;
}
