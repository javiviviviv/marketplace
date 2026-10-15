package com.polec.msusuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.antlr.v4.runtime.misc.NotNull;

public record UsuarioRequest(
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotNull Long rolId
) {}

