package com.shushinestudio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPerfil {
    private UUID id;
    private String login;
    private String correo;
    private String nombre;
    private String apellido;
    private String telefono;
    private String rol;
    private Boolean activo;
    private Integer puntosAcumulados;
    private String nivelFidelidad;
    private String tipoCabello;
    private String notasPreferencias;
}
