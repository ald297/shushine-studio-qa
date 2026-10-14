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
@Table(name = "estilistas")
public class Estilista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estilista")
    private Integer id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "especialidad_principal", nullable = false, length = 100)
    private String especialidadPrincipal;

    @Column(columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(name = "color_agenda", nullable = false, length = 20)
    private String colorAgenda = "#C5A059";

    @Builder.Default
    @Column(name = "porcentaje_comision", precision = 5, scale = 2)
    private BigDecimal porcentajeComision = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
