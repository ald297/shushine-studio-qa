package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IFacturaRepository extends JpaRepository<Factura, Integer> {
    Optional<Factura> findByCitaId(Integer citaId);
    Optional<Factura> findByNumeroFactura(String numeroFactura);
}
