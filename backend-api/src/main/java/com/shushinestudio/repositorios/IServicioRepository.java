package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IServicioRepository extends JpaRepository<Servicio, Integer> {
    @EntityGraph(attributePaths = "categoria")
    List<Servicio> findByActivoTrue();
    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Servicio> findAllById(Iterable<Integer> ids);

    List<Servicio> findByCategoriaIdAndActivoTrue(Integer idCategoria);
    Page<Servicio> findByCategoriaIdAndActivoTrue(Integer idCategoria, Pageable pageable);
    List<Servicio> findByNombreContainingIgnoreCaseAndActivoTrue(String query);
    Optional<Servicio> findByCodigoServicio(String codigoServicio);
}
