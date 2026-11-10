package com.polec.mspago.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CrearPagoRequest(
        @NotNull Long ventaId,
        @NotNull @PositiveOrZero Long monto
) {
}
