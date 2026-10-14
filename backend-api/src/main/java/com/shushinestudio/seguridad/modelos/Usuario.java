package com.shushinestudio.seguridad.modelos;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario")
    private UUID id;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column(name = "correo")
    private String correo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, unique = true, length = 100)
    private String login;

    @Column(nullable = false, length = 255)
    private String clave;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (rol == null || rol.getNombre() == null) {
            return List.of();
        }
        String nombreRol = rol.getNombre().trim().toUpperCase();
        String roleWithPrefix = nombreRol.startsWith("ROLE_") ? nombreRol : "ROLE_" + nombreRol;
        String roleWithoutPrefix = nombreRol.startsWith("ROLE_") ? nombreRol.substring(5) : nombreRol;

        return List.of(
                new SimpleGrantedAuthority(roleWithPrefix),
                new SimpleGrantedAuthority(roleWithoutPrefix)
        );
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @PrePersist
    public void prePersist() {
        if (this.nombreCompleto == null) {
            this.nombreCompleto = (this.nombre != null ? this.nombre : "") + (this.apellido != null ? " " + this.apellido : "");
        }
        if (this.correo == null) {
            this.correo = (this.login != null ? this.login : "user") + "@shushinestudio.com";
        }
    }

    @Override
    public boolean isEnabled() {
        return activo != null && activo;
    }
}
