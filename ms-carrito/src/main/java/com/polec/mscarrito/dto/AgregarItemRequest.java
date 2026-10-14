package com.polec.mscarrito.dto;

public record AgregarItemRequest(
        Long usuarioId,
        Long itemId,
        Long itemPrecio
) {
}
