package com.polec.msenvio.dto;

import java.util.List;

public record RepartidorResumenResponse(
        Long repartidorId,
        String repartidorNombre,
        long totalPedidos,
        long totalEntregados,
        long totalPendientes,
        EnvioResponse siguiente,
        List<EnvioResponse> pedidos,
        List<EnvioResponse> pendientes,
        List<EnvioResponse> entregados
) {
}
