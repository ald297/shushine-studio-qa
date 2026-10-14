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
public class ActualizarPerfilDto {
    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String apellido;

    private String telefono;

    private String correo;
}
