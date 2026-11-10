package com.polec.msventa.dto;

import com.polec.msventa.entity.EstadoVenta;
import com.polec.msventa.entity.Venta;

import java.time.LocalDateTime;

public record VentaResponse(
        Long id,
        Long itemId,
        Long vendedorId,
        Long compradorId,
        EstadoVenta estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static VentaResponse de(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getItemId(),
                venta.getVendedorId(),
                venta.getCompradorId(),
                venta.getEstado(),
                venta.getFechaCreacion(),
                venta.getFechaActualizacion()
        );
    }
}
