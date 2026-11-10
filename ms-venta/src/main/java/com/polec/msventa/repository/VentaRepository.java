package com.polec.msventa.repository;

import com.polec.msventa.entity.EstadoVenta;
import com.polec.msventa.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByItemId(Long itemId);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByCompradorId(Long compradorId);

    List<Venta> findByVendedorId(Long vendedorId);
}
