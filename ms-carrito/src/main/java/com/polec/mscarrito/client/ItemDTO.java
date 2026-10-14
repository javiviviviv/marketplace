package com.polec.mscarrito.client;

public record ItemDTO(
        Long id,
        String nombre,
        Long precio,
        Long vendedorId
) {
}