package com.shushinestudio.dtos.pago;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoSalida {
    private Integer id;
    private Integer citaId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String referenciaPos;
    private LocalDateTime fechaPago;
}
