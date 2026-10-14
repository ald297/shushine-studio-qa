package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IResenaRepository extends JpaRepository<Resena, Integer> {

    Optional<Resena> findByCitaId(Integer citaId);

    boolean existsByCitaId(Integer citaId);

    @Query("SELECT r FROM Resena r WHERE r.cliente.usuario.login = :login ORDER BY r.fechaEmision DESC")
    List<Resena> findByClienteLogin(@Param("login") String login);

    List<Resena> findByEstilistaIdOrderByFechaEmisionDesc(Integer estilistaId);

    List<Resena> findAllByOrderByFechaEmisionDesc();
}
