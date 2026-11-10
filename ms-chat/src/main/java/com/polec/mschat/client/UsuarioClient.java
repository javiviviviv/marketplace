package com.polec.mschat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-usuario", url = "${ms.usuario.url}")
public interface UsuarioClient {

    @GetMapping("/usuarios/{id}")
    UsuarioDTO buscarPorId(@PathVariable("id") Long id);

    default boolean existe(Long id) {
        if (id == null) return false;
        try {
            return buscarPorId(id) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
