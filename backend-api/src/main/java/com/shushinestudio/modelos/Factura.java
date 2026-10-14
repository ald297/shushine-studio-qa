package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false, unique = true)
    private Cita cita;

    @Column(name = "numero_factura", length = 50, nullable = false, unique = true)
    private String numeroFactura;

    @CreationTimestamp
    @Column(name = "fecha_emision", updatable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "iva", precision = 10, scale = 2, nullable = false)
    private BigDecimal iva;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "datos_emisor_receptor", columnDefinition = "text")
    private String datosEmisorReceptor;
}
