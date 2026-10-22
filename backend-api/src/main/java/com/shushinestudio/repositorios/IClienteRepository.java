package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuarioId(UUID usuarioId);
    @EntityGraph(attributePaths = {"usuario", "usuario.rol"})
    Optional<Cliente> findByUsuarioLogin(String login);
}
