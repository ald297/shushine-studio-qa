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
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cita", nullable = false, unique = true)
    private Cita cita;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estilista", nullable = false)
    private Estilista estilista;

    @Column(name = "estrellas_general", nullable = false)
    private Integer estrellasGeneral;

    @Builder.Default
    @Column(name = "estrellas_calidad")
    private Integer estrellasCalidad = 5;

    @Builder.Default
    @Column(name = "estrellas_atencion")
    private Integer estrellasAtencion = 5;

    @Builder.Default
    @Column(name = "estrellas_ambiente")
    private Integer estrellasAmbiente = 5;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Builder.Default
    @Column(name = "visible_publica", nullable = false)
    private Boolean visiblePublica = true;

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();
}
