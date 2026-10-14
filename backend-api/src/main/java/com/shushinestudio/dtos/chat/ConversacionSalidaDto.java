package com.shushinestudio.dtos.chat;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversacionSalidaDto {

    private Integer id;
    private Integer idCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private long mensajesNoLeidos;
    private Boolean activa;
}
