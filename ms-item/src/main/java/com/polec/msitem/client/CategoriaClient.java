package com.polec.msitem.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-categoria", url = "http://localhost:8085")
public interface CategoriaClient {

    @GetMapping("/categorias/{id}")
    CategoriaDTO buscarPorId(@PathVariable("id") Long id);


    default boolean existe(Long id){
        return buscarPorId(id) != null;
}
}
