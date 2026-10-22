package com.shushinestudio.seguridad.repositorios;

import com.shushinestudio.seguridad.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByLogin(String login);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.login = :login")
    Optional<Usuario> findByLoginForUpdate(@Param("login") String login);

    boolean existsByLogin(String login);
}
