package com.polec.msusuario.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RolClient {
    private final RestClient rest;

    public RolClient(@Value("http://localhost:8082") String urlMsRol){
        this.rest = RestClient.create(urlMsRol);
    }

    public RolDTO buscarPorId(Long id){
        if(id == null){
            return null;
        }
        try{
            return rest.get()
                    .uri("/roles/{id}", id)
                    .retrieve()
                    .body(RolDTO.class);
        } catch (Exception e) {
            System.out.println("No se pudo obtener el rol " + id + " : " + e.getMessage());
            return null;
        }
    }

    public boolean existe(Long id){
        return buscarPorId(id) != null;
    }
}
