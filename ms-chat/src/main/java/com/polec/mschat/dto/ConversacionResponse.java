package com.polec.mschat.dto;

import java.time.LocalDateTime;

public record ConversacionResponse(
        Long id,
        Long itemId,
        ParticipanteResponse usuario1,
        ParticipanteResponse usuario2,
        LocalDateTime fechaCreacion,
        LocalDateTime ultimaActividad
) {
}
