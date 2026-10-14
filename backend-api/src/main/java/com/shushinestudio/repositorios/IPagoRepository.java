package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IPagoRepository extends JpaRepository<Pago, Integer> {
    List<Pago> findByCitaId(Integer citaId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = 'Aprobado' AND p.fechaPago >= :desde AND p.fechaPago <= :hasta")
    BigDecimal sumTotalRecaudadoEntre(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
