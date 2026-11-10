package com.polec.mschat.dto;

import jakarta.validation.constraints.NotNull;

public record CrearConversacionRequest(
        @NotNull Long usuario1Id,
        @NotNull Long usuario2Id,
        Long itemId
) {
}
