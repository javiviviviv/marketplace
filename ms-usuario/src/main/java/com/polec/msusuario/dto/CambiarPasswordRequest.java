package com.polec.msusuario.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarPasswordRequest(@NotBlank String password) {}