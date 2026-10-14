package com.shushinestudio.dtos.solicitud;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSalidaDto {

    private Integer id;
    private Integer idCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String servicioDeseado;
    private String notasCliente;
    private String imagenesReferenciaUrls;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private CotizacionSalidaDto cotizacion;
}
