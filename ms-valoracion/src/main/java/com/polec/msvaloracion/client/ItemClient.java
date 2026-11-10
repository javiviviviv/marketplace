package com.polec.msvaloracion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-item", url = "${ms.item.url}")
public interface ItemClient {

    @GetMapping("/items/{id}")
    ItemDTO buscarPorId(@PathVariable("id") Long id);
}
