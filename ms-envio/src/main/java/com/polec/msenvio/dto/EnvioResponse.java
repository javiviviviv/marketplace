package com.polec.msenvio.dto;

import com.polec.msenvio.entity.Envio;
import com.polec.msenvio.entity.EstadoEnvio;

import java.time.LocalDateTime;

public record EnvioResponse(
        Long id,
        String numeroPedido,
        Long ventaId,
        Long compradorId,
        Long repartidorId,
        String direccion,
        EstadoEnvio estado,
        Integer posicion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaEntrega
) {
    public static EnvioResponse de(Envio envio) {
        return new EnvioResponse(
                envio.getId(),
                envio.getNumeroPedido(),
                envio.getVentaId(),
                envio.getCompradorId(),
                envio.getRepartidorId(),
                envio.getDireccion(),
                envio.getEstado(),
                envio.getPosicion(),
                envio.getFechaCreacion(),
                envio.getFechaActualizacion(),
                envio.getFechaEntrega()
        );
    }
}
