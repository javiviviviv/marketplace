package com.polec.msusuario.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-rol", url = "http://localhost:8082")
public interface RolClient {

    @GetMapping("/roles/{id}")
    RolDTO buscarPorId(@PathVariable("id") Long id);

    default boolean existe(Long id) {
        if (id == null) return false;
        try {
            return buscarPorId(id) != null;
        } catch (Exception e) {
            return false;
        }
    }
}