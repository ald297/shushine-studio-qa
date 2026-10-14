package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.BloqueoHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IBloqueoHorarioRepository extends JpaRepository<BloqueoHorario, Integer> {
    List<BloqueoHorario> findByEstilistaId(Integer estilistaId);
    List<BloqueoHorario> findByEstilistaIdAndFecha(Integer estilistaId, LocalDate fecha);
}
