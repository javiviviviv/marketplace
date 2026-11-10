package com.polec.mspago.repository;

import com.polec.mspago.entity.EstadoPago;
import com.polec.mspago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByVentaId(Long ventaId);

    List<Pago> findByEstado(EstadoPago estado);
}
