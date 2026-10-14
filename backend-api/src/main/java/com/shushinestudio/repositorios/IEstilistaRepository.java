package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Estilista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEstilistaRepository extends JpaRepository<Estilista, Integer> {
    List<Estilista> findByActivoTrue();
}
