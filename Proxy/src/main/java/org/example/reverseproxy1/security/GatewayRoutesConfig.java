package org.example.reverseproxy1.security;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("node_services", r -> r
                        .path("/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("lb://nodea"))
                .build();
    }
}
