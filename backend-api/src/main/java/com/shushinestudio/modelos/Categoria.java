package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "icono_url", length = 500)
    private String iconoUrl;

    @Builder.Default
    @Column(length = 30, nullable = false)
    private String tipo = "Servicio";

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servicio> servicios;
}
