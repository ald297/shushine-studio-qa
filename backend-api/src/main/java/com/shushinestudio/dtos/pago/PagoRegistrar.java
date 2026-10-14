package com.shushinestudio.dtos.pago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoRegistrar {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Integer citaId;

    @NotNull(message = "El monto a pagar es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String referenciaPos;

    private String motivoAjustePrecio;
}
