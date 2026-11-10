package com.polec.msvaloracion.client;

public record VentaDTO(
        Long id,
        Long itemId,
        Long vendedorId,
        Long compradorId,
        String estado
) {
}
