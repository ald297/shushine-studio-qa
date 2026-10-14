package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.SolicitudDiseno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudDisenoRepository extends JpaRepository<SolicitudDiseno, Integer> {

    @Query("SELECT s FROM SolicitudDiseno s WHERE s.cliente.usuario.login = :login ORDER BY s.fechaSolicitud DESC")
    List<SolicitudDiseno> findByClienteLogin(@Param("login") String login);

    List<SolicitudDiseno> findAllByOrderByFechaSolicitudDesc();

    List<SolicitudDiseno> findByEstadoOrderByFechaSolicitudDesc(String estado);
}
