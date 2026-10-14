package com.shushinestudio.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bloqueos_horarios")
public class BloqueoHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloqueo")
    private Integer id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estilista", nullable = false)
    private Estilista estilista;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate fecha;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "[HH:mm:ss][HH:mm]")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "[HH:mm:ss][HH:mm]")
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 200)
    private String motivo;
}
