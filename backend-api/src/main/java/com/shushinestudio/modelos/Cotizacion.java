package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cotizaciones")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private SolicitudDiseno solicitud;

    @Column(name = "precio_propuesto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPropuesto;

    @Column(name = "descripcion_trabajo", columnDefinition = "TEXT", nullable = false)
    private String descripcionTrabajo;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "Propuesta"; // Propuesta, Aceptada, Rechazada

    @Builder.Default
    @Column(name = "fecha_cotizacion", nullable = false)
    private LocalDateTime fechaCotizacion = LocalDateTime.now();

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}
