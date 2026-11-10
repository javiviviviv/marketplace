package com.polec.msvaloracion.dto;

import com.polec.msvaloracion.entity.Valoracion;

import java.time.LocalDateTime;

public record ValoracionResponse(
        Long id,
        Long itemId,
        String itemNombre,
        Long usuarioId,
        Long vendedorId,
        Integer puntuacion,
        String comentario,
        LocalDateTime fechaCreacion
) {
    public static ValoracionResponse de(Valoracion valoracion, String itemNombre) {
        return new ValoracionResponse(
                valoracion.getId(),
                valoracion.getItemId(),
                itemNombre,
                valoracion.getUsuarioId(),
                valoracion.getVendedorId(),
                valoracion.getPuntuacion(),
                valoracion.getComentario(),
                valoracion.getFechaCreacion()
        );
    }
}
