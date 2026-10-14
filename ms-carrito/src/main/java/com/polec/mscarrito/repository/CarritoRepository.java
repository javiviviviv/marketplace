package com.polec.mscarrito.repository;

import com.polec.mscarrito.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuarioId(Long UsuarioId);
    Optional<Carrito> deleteByUsuarioId(Long UsuarioId);
    Boolean existsByUsuarioId(Long UsuarioId);

}
