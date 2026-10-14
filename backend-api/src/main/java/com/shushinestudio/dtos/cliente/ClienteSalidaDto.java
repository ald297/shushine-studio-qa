package com.shushinestudio.dtos.cliente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteSalidaDto {
    private Integer id;
    private String nombreCompleto;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String login;
    private String nivelFidelidad;
    private Integer puntosAcumulados;
    private String tipoCabello;
    private String notasPreferencias;
    private Boolean esWalkin;
    private Integer totalCitas;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
