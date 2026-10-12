package com.polec.msitem.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CategoriaClient {
    private final RestClient rest;

    public CategoriaClient(@Value("http://localhost:8083") String urlMsCategoria){
        this.rest = RestClient.create(urlMsCategoria);
    }

    public CategoriaDTO buscarPorId(Long id){
        if(id == null){
            return null;
        }
        try{
            return rest.get()
                    .uri("/categorias/{id}", id)
                    .retrieve()
                    .body(CategoriaDTO.class);
        } catch (Exception e) {
            System.out.println("No se pudo obtener la categoria " + id + " : " + e.getMessage());
            return null;
        }
    }

    public boolean existe(Long id){
        return buscarPorId(id) != null;
    }
}
