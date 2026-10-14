package com.shushinestudio.dtos.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaCrearDto {

    @NotNull(message = "El identificador de la cita es obligatorio")
    private Integer idCita;

    @NotNull(message = "La calificación de estrellas es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    private Integer estrellas;

    private Integer estrellasCalidad;
    private Integer estrellasAtencion;
    private Integer estrellasAmbiente;

    private String comentario;

    @Builder.Default
    private Boolean visiblePublica = true;
}
