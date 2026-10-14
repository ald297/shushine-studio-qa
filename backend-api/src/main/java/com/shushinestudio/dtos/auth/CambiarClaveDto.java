package com.shushinestudio.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarClaveDto {
    @NotBlank(message = "La clave actual es requerida")
    private String claveActual;

    @NotBlank(message = "La nueva clave es requerida")
    private String nuevaClave;

    @NotBlank(message = "La confirmación de la nueva clave es requerida")
    private String confirmarClave;
}
