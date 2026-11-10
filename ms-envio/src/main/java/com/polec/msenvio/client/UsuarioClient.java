package com.polec.msenvio.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-usuario", url = "${ms.usuario.url}")
public interface UsuarioClient {

    @GetMapping("/usuarios/{id}")
    UsuarioDTO buscarPorId(@PathVariable("id") Long id);

    default UsuarioDTO buscarSinLanzar(Long id) {
        if (id == null) return null;
        try {
            return buscarPorId(id);
        } catch (Exception e) {
            return null;
        }
    }
}
