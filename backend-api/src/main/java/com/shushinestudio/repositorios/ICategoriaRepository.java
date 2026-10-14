package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findByActivoTrue();
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
