package com.polec.msvaloracion.dto;

public record PromedioResponse(
        Long itemId,
        Double promedio,
        long total
) {
}
