package com.shushinestudio.dtos.cita;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaWalkinGuardar {

    @NotBlank(message = "El nombre del cliente walk-in es obligatorio")
    private String nombreCliente;

    private String telefonoCliente;

    @NotNull(message = "El ID del estilista es obligatorio")
    private Integer estilistaId;

    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDate fechaCita;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotEmpty(message = "Debe seleccionar al menos un servicio")
    private List<Integer> servicioIds;

    private String metodoPagoPreferente;

    private String notas;
}
