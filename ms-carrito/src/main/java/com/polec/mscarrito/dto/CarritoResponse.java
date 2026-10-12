package com.polec.mscarrito.dto;

import com.polec.mscarrito.client.RolDTO;
import com.polec.mscarrito.entity.carrito;

public record CarritoResponse(
        Long id,
        String nombre,
        String correo,
        Long rolId,
        String rol
) {
    public static CarritoResponse de(Carrito c, carrito u){
        return new CarritoResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getRolId(),
                rol != null? rol.nombre() : "SIN ROL"
        );
    }
}
