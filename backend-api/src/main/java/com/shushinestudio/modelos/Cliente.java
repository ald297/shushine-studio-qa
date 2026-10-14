package com.shushinestudio.modelos;

import com.shushinestudio.seguridad.modelos.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre_walkin", length = 150)
    private String nombreWalkin;

    @Column(name = "telefono_walkin", length = 20)
    private String telefonoWalkin;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Builder.Default
    @Column(name = "nivel_fidelidad", nullable = false, length = 50)
    private String nivelFidelidad = "Bronce";

    @Builder.Default
    @Column(name = "puntos_acumulados", nullable = false)
    private Integer puntosAcumulados = 0;

    @Column(name = "tipo_cabello", length = 100)
    private String tipoCabello;

    @Column(name = "notas_preferencias", length = 500)
    private String notasPreferencias;

    @Builder.Default
    @Column(name = "es_walkin", nullable = false)
    private Boolean esWalkin = false;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
