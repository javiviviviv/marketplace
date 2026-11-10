package com.polec.mspago.dto;

import com.polec.mspago.entity.EstadoPago;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPagoRequest(
        @NotNull EstadoPago estado
) {
}
