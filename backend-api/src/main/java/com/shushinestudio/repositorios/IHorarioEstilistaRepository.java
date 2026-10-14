package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.HorarioEstilista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IHorarioEstilistaRepository extends JpaRepository<HorarioEstilista, Integer> {
    List<HorarioEstilista> findByEstilistaId(Integer estilistaId);
    List<HorarioEstilista> findByEstilistaIdAndActivoTrue(Integer estilistaId);
    Optional<HorarioEstilista> findByEstilistaIdAndDiaSemanaAndActivoTrue(Integer estilistaId, Integer diaSemana);
    Optional<HorarioEstilista> findByEstilistaIdAndDiaSemana(Integer estilistaId, Integer diaSemana);
}
