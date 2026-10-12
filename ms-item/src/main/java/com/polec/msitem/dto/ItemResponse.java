package com.polec.msitem.dto;

import com.polec.msitem.client.CategoriaDTO;
import com.polec.msitem.entity.Item;

public record ItemResponse(
        Long id,
        String nombre,
        Long anio,     //no menor a 1800
        Long precio,     //no menor a 0
        Long vendedorId,
        Long categoriaId
) {
    public static ItemResponse de(Item i, CategoriaDTO categoria){
        return new ItemResponse(
                i.getId(),
                i.getNombre(),
                i.getAnio(),
                i.getPrecio(),
                i.getVendedorId(),
                i.getCategoriaId()
        );
    }
}
