package com.polec.msrol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OperationCustomizer;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marketplaceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("MS Rol")
                .description("Gestion de roles del marketplace")
                .version("1.0.0"))
                .servers(List.of(new Server().url("/")));
    }

    @Bean
    public OperationCustomizer respuestasComunes() {
        return (operation, handlerMethod) -> {
            if (operation.getResponses() == null) {
                operation.setResponses(new io.swagger.v3.oas.models.responses.ApiResponses());
            }
            operation.getResponses().putIfAbsent("400", new io.swagger.v3.oas.models.responses.ApiResponse()
                    .description("Solicitud invalida: faltan campos obligatorios, formato incorrecto, valores fuera de rango, estado no valido o se envio un id autogenerado."));
            operation.getResponses().putIfAbsent("500", new io.swagger.v3.oas.models.responses.ApiResponse()
                    .description("Error interno del servidor o violacion de una restriccion de la base de datos, por ejemplo un valor unico duplicado."));
            return operation;
        };
    }
}