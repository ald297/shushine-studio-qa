package com.shushinestudio.modelos;

import com.shushinestudio.seguridad.modelos.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", nullable = false)
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_remitente", nullable = false)
    private Usuario remitente;

    @Column(name = "contenido", nullable = false, length = 1000)
    private String contenido;

    @Column(name = "imagen_url", length = 1000)
    private String imagenUrl;

    @Builder.Default
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Builder.Default
    @Column(name = "leido", nullable = false)
    private Boolean leido = false;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
