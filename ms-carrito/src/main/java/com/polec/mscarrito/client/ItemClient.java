package com.polec.mscarrito.client;

import com.polec.mscarrito.client.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-item", url = "http://localhost:8083")
public interface ItemClient {

    @GetMapping("/items/{id}")
    ItemDTO obtenerItemPorId(@PathVariable("id") Long id);
}