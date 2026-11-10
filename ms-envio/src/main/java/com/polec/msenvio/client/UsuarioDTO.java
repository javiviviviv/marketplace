package com.polec.msenvio.client;

public record UsuarioDTO(
        Long id,
        String nombre,
        String correo,
        Long rolId,
        String rol
) {
}
