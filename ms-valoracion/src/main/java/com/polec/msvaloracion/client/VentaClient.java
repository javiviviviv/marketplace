package com.polec.msvaloracion.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-venta", url = "${ms.venta.url}")
public interface VentaClient {

    @GetMapping("/ventas/item/{itemId}")
    VentaDTO buscarPorItem(@PathVariable("itemId") Long itemId);
}
