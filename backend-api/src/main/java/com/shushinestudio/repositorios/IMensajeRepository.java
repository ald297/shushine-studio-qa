package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMensajeRepository extends JpaRepository<Mensaje, Integer> {

    Page<Mensaje> findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(Integer conversacionId, Pageable pageable);

    List<Mensaje> findByConversacionIdAndActivoTrueOrderByFechaEnvioAsc(Integer conversacionId);

    long countByConversacionIdAndLeidoFalseAndRemitenteIdNot(Integer conversacionId, UUID remitenteId);

    Optional<Mensaje> findTopByConversacionIdAndActivoTrueOrderByFechaEnvioDesc(Integer conversacionId);

    @Modifying
    @Query("UPDATE Mensaje m SET m.leido = true WHERE m.conversacion.id = :conversacionId AND m.remitente.id <> :usuarioId AND m.leido = false")
    int marcarComoLeidos(@Param("conversacionId") Integer conversacionId, @Param("usuarioId") UUID usuarioId);
}
