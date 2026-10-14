package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ICitaRepository extends JpaRepository<Cita, Integer> {
    Optional<Cita> findByCodigoCita(String codigoCita);

    List<Cita> findByEstilistaIdAndFechaCitaAndEstadoNot(Integer estilistaId, LocalDate fechaCita, String estado);

    List<Cita> findByFechaCitaOrderByHoraInicioAsc(LocalDate fechaCita);

    List<Cita> findByFechaCitaAndEstilistaIdOrderByHoraInicioAsc(LocalDate fechaCita, Integer estilistaId);

    List<Cita> findByClienteIdOrderByFechaCitaDescHoraInicioDesc(Integer clienteId);

    long countByFechaCita(LocalDate fechaCita);

    long countByFechaCitaBetween(LocalDate desde, LocalDate hasta);

    long countByEstado(String estado);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.estilista.id = :estilistaId AND c.fechaCita = :fecha " +
           "AND c.estado != 'Cancelled' " +
           "AND (:horaInicio < c.horaFin AND :horaFin > c.horaInicio)")
    boolean existeSolapamiento(
            @Param("estilistaId") Integer estilistaId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );
}
