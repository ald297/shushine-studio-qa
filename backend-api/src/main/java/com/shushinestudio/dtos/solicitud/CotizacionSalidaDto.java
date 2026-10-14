package com.shushinestudio.dtos.solicitud;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionSalidaDto {

    private Integer id;
    private Integer idSolicitud;
    private BigDecimal precioPropuesto;
    private String descripcionTrabajo;
    private String estado;
    private LocalDateTime fechaCotizacion;
    private LocalDateTime fechaRespuesta;
}
