package com.shushinestudio.dtos.solicitud;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCrearDto {

    private String servicioDeseado;

    @NotBlank(message = "La descripción de lo que deseas realizarte es obligatoria")
    private String notasCliente;

    private String imagenesReferenciaUrls;
}
