package com.polec.msenvio.repository;

import com.polec.msenvio.entity.Envio;
import com.polec.msenvio.entity.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    Optional<Envio> findByVentaId(Long ventaId);

    Optional<Envio> findByNumeroPedido(String numeroPedido);

    List<Envio> findByRepartidorIdOrderByPosicionAsc(Long repartidorId);

    List<Envio> findByRepartidorIdAndEstadoOrderByPosicionAsc(Long repartidorId, EstadoEnvio estado);

    List<Envio> findByRepartidorIdAndEstadoNotOrderByPosicionAsc(Long repartidorId, EstadoEnvio estado);

    Optional<Envio> findFirstByRepartidorIdAndEstadoNotOrderByPosicionAsc(Long repartidorId, EstadoEnvio estado);

    long countByRepartidorId(Long repartidorId);

    long countByRepartidorIdAndEstado(Long repartidorId, EstadoEnvio estado);

    long countByRepartidorIdAndEstadoNot(Long repartidorId, EstadoEnvio estado);

    @Query("SELECT COALESCE(MAX(e.posicion), 0) FROM Envio e WHERE e.repartidorId = :repartidorId")
    Integer ultimaPosicion(@Param("repartidorId") Long repartidorId);

    boolean existsByVentaId(Long ventaId);
}
