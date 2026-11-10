package com.polec.mschat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnviarMensajeRequest(
        @NotNull Long emisorId,
        @NotBlank @Size(max = 2000) String contenido
) {
}
