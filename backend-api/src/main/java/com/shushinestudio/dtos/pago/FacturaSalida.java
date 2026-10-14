package com.shushinestudio.dtos.pago;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaSalida {
    private Integer id;
    private Integer citaId;
    private String codigoCita;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private String clienteNombre;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private String metodoPago;
}
