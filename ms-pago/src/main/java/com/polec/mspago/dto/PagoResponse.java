package com.polec.mspago.dto;

import com.polec.mspago.entity.EstadoPago;
import com.polec.mspago.entity.Pago;

import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        Long ventaId,
        Long monto,
        EstadoPago estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static PagoResponse de(Pago pago) {
        return new PagoResponse(
                pago.getId(),
                pago.getVentaId(),
                pago.getMonto(),
                pago.getEstado(),
                pago.getFechaCreacion(),
                pago.getFechaActualizacion()
        );
    }
}
