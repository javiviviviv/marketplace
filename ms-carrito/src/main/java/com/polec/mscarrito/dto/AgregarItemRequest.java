package com.polec.mscarrito.dto;

import jakarta.validation.constraints.NotNull;

public record AgregarItemRequest(
        @NotNull Long usuarioId,
        @NotNull Long itemId
) {
}
