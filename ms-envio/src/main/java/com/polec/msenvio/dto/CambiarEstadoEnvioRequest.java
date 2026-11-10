package com.polec.msenvio.dto;

import com.polec.msenvio.entity.EstadoEnvio;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoEnvioRequest(
        @NotNull EstadoEnvio estado
) {
}
