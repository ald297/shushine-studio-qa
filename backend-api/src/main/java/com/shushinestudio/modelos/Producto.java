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
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @Column(name = "codigo_producto", nullable = false, unique = true, length = 20)
    private String codigoProducto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
