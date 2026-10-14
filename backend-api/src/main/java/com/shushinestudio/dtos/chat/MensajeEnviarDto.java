package com.shushinestudio.dtos.chat;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class MensajeEnviarDto {

    @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres.")
    private String contenido;

    private String imagenUrl;
}
