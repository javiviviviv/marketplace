package com.polec.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    private static final String DOCS = "/v3/api-docs/";

    @Bean
    public RouteLocator marketplaceRoutes(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        routes.route("ms-usuario", r -> r.path("/usuarios/**")
                .uri("http://localhost:8081"));
        routes.route("ms-rol", r -> r.path("/roles/**")
                .uri("http://localhost:8082"));
        routes.route("ms-item", r -> r.path("/items/**")
                .uri("http://localhost:8083"));
        routes.route("ms-carrito", r -> r.path("/carritos/**")
                .uri("http://localhost:8084"));
        routes.route("ms-categoria", r -> r.path("/categorias/**")
                .uri("http://localhost:8085"));
        routes.route("ms-chat", r -> r.path("/chats/**")
                .uri("http://localhost:8086"));
        routes.route("ms-venta", r -> r.path("/ventas/**")
                .uri("http://localhost:8087"));
        routes.route("ms-valoracion", r -> r.path("/valoraciones/**")
                .uri("http://localhost:8088"));
        routes.route("ms-pago", r -> r.path("/pagos/**")
                .uri("http://localhost:8089"));
        routes.route("ms-envio", r -> r.path("/envios/**")
                .uri("http://localhost:8090"));

        // se reescribe la ruta para que cada ms entregue su propio openapi
        routes.route("docs-ms-usuario", r -> r.path(DOCS + "ms-usuario")
                .filters(f -> f.rewritePath(DOCS + "ms-usuario", "/v3/api-docs"))
                .uri("http://localhost:8081"));
        routes.route("docs-ms-rol", r -> r.path(DOCS + "ms-rol")
                .filters(f -> f.rewritePath(DOCS + "ms-rol", "/v3/api-docs"))
                .uri("http://localhost:8082"));
        routes.route("docs-ms-item", r -> r.path(DOCS + "ms-item")
                .filters(f -> f.rewritePath(DOCS + "ms-item", "/v3/api-docs"))
                .uri("http://localhost:8083"));
        routes.route("docs-ms-carrito", r -> r.path(DOCS + "ms-carrito")
                .filters(f -> f.rewritePath(DOCS + "ms-carrito", "/v3/api-docs"))
                .uri("http://localhost:8084"));
        routes.route("docs-ms-categoria", r -> r.path(DOCS + "ms-categoria")
                .filters(f -> f.rewritePath(DOCS + "ms-categoria", "/v3/api-docs"))
                .uri("http://localhost:8085"));
        routes.route("docs-ms-chat", r -> r.path(DOCS + "ms-chat")
                .filters(f -> f.rewritePath(DOCS + "ms-chat", "/v3/api-docs"))
                .uri("http://localhost:8086"));
        routes.route("docs-ms-venta", r -> r.path(DOCS + "ms-venta")
                .filters(f -> f.rewritePath(DOCS + "ms-venta", "/v3/api-docs"))
                .uri("http://localhost:8087"));
        routes.route("docs-ms-valoracion", r -> r.path(DOCS + "ms-valoracion")
                .filters(f -> f.rewritePath(DOCS + "ms-valoracion", "/v3/api-docs"))
                .uri("http://localhost:8088"));
        routes.route("docs-ms-pago", r -> r.path(DOCS + "ms-pago")
                .filters(f -> f.rewritePath(DOCS + "ms-pago", "/v3/api-docs"))
                .uri("http://localhost:8089"));
        routes.route("docs-ms-envio", r -> r.path(DOCS + "ms-envio")
                .filters(f -> f.rewritePath(DOCS + "ms-envio", "/v3/api-docs"))
                .uri("http://localhost:8090"));

        return routes.build();
    }
}
