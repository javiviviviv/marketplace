package com.polec.msvaloracion.repository;

import com.polec.msvaloracion.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    boolean existsByItemIdAndUsuarioId(Long itemId, Long usuarioId);

    List<Valoracion> findByItemIdOrderByFechaCreacionDesc(Long itemId);

    List<Valoracion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Valoracion> findByVendedorIdOrderByFechaCreacionDesc(Long vendedorId);

    long countByItemId(Long itemId);

    @Query("SELECT AVG(v.puntuacion) FROM Valoracion v WHERE v.itemId = :itemId")
    Double promedioPorItem(@Param("itemId") Long itemId);
}
