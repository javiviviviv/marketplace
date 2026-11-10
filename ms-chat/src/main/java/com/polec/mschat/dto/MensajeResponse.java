package com.polec.mschat.dto;

import com.polec.mschat.entity.Mensaje;

import java.time.LocalDateTime;

public record MensajeResponse(
        Long id,
        Long conversacionId,
        Long emisorId,
        String contenido,
        LocalDateTime fechaEnvio,
        boolean leido
) {
    public static MensajeResponse de(Mensaje m) {
        return new MensajeResponse(
                m.getId(),
                m.getConversacion().getId(),
                m.getEmisorId(),
                m.getContenido(),
                m.getFechaEnvio(),
                m.isLeido()
        );
    }
}
