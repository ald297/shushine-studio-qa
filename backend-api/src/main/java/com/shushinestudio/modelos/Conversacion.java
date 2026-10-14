package com.shushinestudio.modelos;

import com.shushinestudio.seguridad.modelos.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversaciones")
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false, unique = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin")
    private Usuario admin;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    @Column(name = "fecha_ultimo_mensaje", nullable = false)
    private LocalDateTime fechaUltimoMensaje = LocalDateTime.now();

    @Builder.Default
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @Builder.Default
    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaEnvio ASC")
    private List<Mensaje> mensajes = new ArrayList<>();
}
