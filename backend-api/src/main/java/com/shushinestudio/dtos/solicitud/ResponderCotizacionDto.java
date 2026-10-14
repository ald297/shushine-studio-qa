package com.shushinestudio.dtos.solicitud;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponderCotizacionDto {

    @NotNull(message = "Debe indicar si acepta o rechaza la cotización")
    private Boolean aceptar;
}
