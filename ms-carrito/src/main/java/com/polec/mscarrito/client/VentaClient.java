package com.polec.mscarrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-venta", url = "${ms.venta.url}")
public interface VentaClient {

    @PostMapping("/ventas")
    VentaDTO registrar(@RequestBody RegistrarVentaRequest request);

    @GetMapping("/ventas/item/{itemId}")
    VentaDTO buscarPorItem(@PathVariable("itemId") Long itemId);
}
