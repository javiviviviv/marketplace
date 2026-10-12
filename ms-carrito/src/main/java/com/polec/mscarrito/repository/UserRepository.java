package com.polec.mscarrito.repository;

import com.polec.mscarrito.entity.carrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface carritoRepository extends JpaRepository<carrito, Long> {
}
