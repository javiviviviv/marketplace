package com.polec.msusuario.dto;

import com.polec.msusuario.client.RolDTO;
import com.polec.msusuario.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        Long rolId,
        String rol
) {
    public static UsuarioResponse de(Usuario u, RolDTO rol){
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getRolId(),
                rol != null? rol.nombre() : "SIN ROL"
        );
    }
}
