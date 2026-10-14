package com.shushinestudio.repositorios;

import com.shushinestudio.modelos.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    Page<Producto> findByActivoTrue(Pageable pageable);

    Optional<Producto> findByCodigoProducto(String codigoProducto);

    boolean existsByCodigoProducto(String codigoProducto);

    List<Producto> findByCategoriaIdAndActivoTrue(Integer idCategoria);
}
