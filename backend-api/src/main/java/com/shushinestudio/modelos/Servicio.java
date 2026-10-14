package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer id;

    @Column(name = "codigo_servicio", nullable = false, unique = true, length = 20)
    private String codigoServicio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Builder.Default
    @Column(name = "es_precio_variable", nullable = false)
    private Boolean esPrecioVariable = false;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Builder.Default
    @Column(name = "intervalo_seguimiento_dias", nullable = false)
    private Integer intervaloSeguimientoDias = 21;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Builder.Default
    @Column(name = "costo_insumos", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoInsumos = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
