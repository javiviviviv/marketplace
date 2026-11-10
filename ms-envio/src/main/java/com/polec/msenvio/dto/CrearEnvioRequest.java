package com.polec.msenvio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearEnvioRequest(
        @NotNull Long ventaId,
        @NotNull Long compradorId,
        @NotNull Long repartidorId,
        @NotBlank @Size(max = 250) String direccion
) {
}
