package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer id;

    @Column(name = "codigo_cita", nullable = false, unique = true, length = 20)
    private String codigoCita;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estilista", nullable = false)
    private Estilista estilista;

    @Column(name = "fecha_cita", nullable = false)
    private LocalDate fechaCita;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String estado = "Confirmed";

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "descuento_puntos", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoPuntos = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal iva = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "metodo_pago_preferente", nullable = false, length = 50)
    private String metodoPagoPreferente = "Efectivo";

    @Builder.Default
    @Column(name = "estado_pago", nullable = false, length = 30)
    private String estadoPago = "Pending";

    @Builder.Default
    @Column(name = "es_walkin", nullable = false)
    private Boolean esWalkin = false;

    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String origen = "App";

    @Column(name = "notas_cliente", length = 500)
    private String notasCliente;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CitaServicio> servicios = new ArrayList<>();
}
