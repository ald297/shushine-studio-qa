package com.shushinestudio.dtos.cita;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaSalida implements Serializable {

    private Integer id;
    private String codigoCita;
    private Integer clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private Integer estilistaId;
    private String estilistaNombre;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCita;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private String estado;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private String metodoPagoPreferente;
    private String estadoPago;
    private String notasCliente;
    private List<String> serviciosNombres;
}
