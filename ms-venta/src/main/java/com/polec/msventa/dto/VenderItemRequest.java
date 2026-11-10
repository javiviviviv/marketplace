package com.polec.msventa.dto;

import jakarta.validation.constraints.NotNull;

public record VenderItemRequest(
        @NotNull Long compradorId
) {
}
