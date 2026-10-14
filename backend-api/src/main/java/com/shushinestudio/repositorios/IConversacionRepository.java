package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Conversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IConversacionRepository extends JpaRepository<Conversacion, Integer> {

    Optional<Conversacion> findByClienteId(Integer clienteId);

    Optional<Conversacion> findByClienteUsuarioId(UUID usuarioId);

    Optional<Conversacion> findByClienteUsuarioLogin(String login);

    Page<Conversacion> findAllByActivaTrueOrderByFechaUltimoMensajeDesc(Pageable pageable);

    List<Conversacion> findAllByActivaTrueOrderByFechaUltimoMensajeDesc();
}
