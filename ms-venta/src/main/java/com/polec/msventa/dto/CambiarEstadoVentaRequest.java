package com.polec.msventa.dto;

import com.polec.msventa.entity.EstadoVenta;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoVentaRequest(
        @NotNull EstadoVenta estado,
        Long compradorId
) {
}
