package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes_diseno")
public class SolicitudDiseno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "servicio_deseado", length = 150)
    private String servicioDeseado;

    @Column(name = "notas_cliente", columnDefinition = "TEXT", nullable = false)
    private String notasCliente;

    @Column(name = "imagenes_referencia_urls", columnDefinition = "TEXT")
    private String imagenesReferenciaUrls;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "Pendiente"; // Pendiente, Cotizada, Aceptada, Rechazada

    @Builder.Default
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @OneToOne(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Cotizacion cotizacion;
}
