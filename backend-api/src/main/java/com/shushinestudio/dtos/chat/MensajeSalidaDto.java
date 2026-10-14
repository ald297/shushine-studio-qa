package com.shushinestudio.dtos.chat;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeSalidaDto {

    private Integer id;
    private Integer conversacionId;
    private String contenido;
    private String imagenUrl;
    private UUID idRemitente;
    private String nombreRemitente;
    private String rolRemitente;
    private Boolean esMio;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
}
