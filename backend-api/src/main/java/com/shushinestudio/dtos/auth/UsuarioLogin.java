package com.shushinestudio.dtos.auth;

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
public class UsuarioLogin {
    private String login;

    @com.fasterxml.jackson.annotation.JsonProperty("clave")
    @com.fasterxml.jackson.annotation.JsonAlias({"password", "contrasena"})
    private String clave;
}
