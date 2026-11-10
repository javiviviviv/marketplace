package com.polec.msenvio.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarRepartidorRequest(
        @NotNull Long repartidorId
) {
}
