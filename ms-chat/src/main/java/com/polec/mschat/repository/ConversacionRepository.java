package com.polec.mschat.repository;

import com.polec.mschat.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    @Query("SELECT c FROM Conversacion c WHERE " +
            "(c.usuario1Id = :usuario1Id AND c.usuario2Id = :usuario2Id) OR " +
            "(c.usuario1Id = :usuario2Id AND c.usuario2Id = :usuario1Id)")
    List<Conversacion> buscarEntreUsuarios(@Param("usuario1Id") Long usuario1Id,
                                           @Param("usuario2Id") Long usuario2Id);

    @Query("SELECT c FROM Conversacion c WHERE c.itemId = :itemId AND " +
            "((c.usuario1Id = :usuario1Id AND c.usuario2Id = :usuario2Id) OR " +
            "(c.usuario1Id = :usuario2Id AND c.usuario2Id = :usuario1Id))")
    List<Conversacion> buscarEntreUsuariosPorItem(@Param("usuario1Id") Long usuario1Id,
                                                  @Param("usuario2Id") Long usuario2Id,
                                                  @Param("itemId") Long itemId);

    @Query("SELECT c FROM Conversacion c WHERE c.usuario1Id = :usuarioId OR c.usuario2Id = :usuarioId " +
            "ORDER BY c.ultimaActividad DESC")
    List<Conversacion> listarPorUsuario(@Param("usuarioId") Long usuarioId);
}
