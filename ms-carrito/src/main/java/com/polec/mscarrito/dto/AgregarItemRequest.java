package com.polec.mscarrito.dto;

import org.antlr.v4.runtime.misc.NotNull;

public record AgregarItemRequest(
        @NotNull Long usuarioId,
        @NotNull Long itemId
) {
}
