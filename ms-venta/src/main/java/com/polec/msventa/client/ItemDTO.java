package com.polec.msventa.client;

public record ItemDTO(
        Long id,
        String nombre,
        Long precio,
        Long vendedorId
) {
}
