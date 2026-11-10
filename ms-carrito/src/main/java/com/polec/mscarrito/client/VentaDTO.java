package com.polec.mscarrito.client;

public record VentaDTO(
        Long id,
        Long itemId,
        Long vendedorId,
        Long compradorId,
        String estado
) {
}
