package com.shushinestudio.dtos.chat;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversacionDetalleDto {

    private Integer id;
    private Integer idCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private long mensajesNoLeidos;
    private Boolean activa;
    private List<MensajeSalidaDto> mensajes;
}
