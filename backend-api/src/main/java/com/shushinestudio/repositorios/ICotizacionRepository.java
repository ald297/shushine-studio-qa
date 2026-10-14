package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICotizacionRepository extends JpaRepository<Cotizacion, Integer> {

    Optional<Cotizacion> findBySolicitudId(Integer idSolicitud);
}
