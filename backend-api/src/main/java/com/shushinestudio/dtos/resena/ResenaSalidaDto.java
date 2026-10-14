package com.shushinestudio.dtos.resena;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaSalidaDto {

    private Integer id;
    private Integer idCita;
    private String codigoCita;
    private Integer idCliente;
    private String nombreCliente;
    private Integer idEstilista;
    private String nombreEstilista;
    private String servicioNombre;
    private Integer estrellas;
    private Integer estrellasCalidad;
    private Integer estrellasAtencion;
    private Integer estrellasAmbiente;
    private String comentario;
    private Boolean visiblePublica;
    private LocalDateTime fechaEmision;
}
