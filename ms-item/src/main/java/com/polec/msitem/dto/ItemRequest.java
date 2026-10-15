package com.polec.msitem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotBlank String nombre,
        @Min(1800) Long anio,
        @Min(0) Long precio,
        @NotNull Long categoriaId
) {}