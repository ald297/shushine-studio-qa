package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @Column(name = "monto", precision = 10, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(name = "metodo_pago", length = 50, nullable = false)
    private String metodoPago;

    @Column(name = "estado", length = 30, nullable = false)
    @Builder.Default
    private String estado = "Aprobado";

    @Column(name = "referencia_pos", length = 100)
    private String referenciaPos;

    @Column(name = "motivo_ajuste_precio", length = 300)
    private String motivoAjustePrecio;

    @CreationTimestamp
    @Column(name = "fecha_pago", updatable = false)
    private LocalDateTime fechaPago;
}
