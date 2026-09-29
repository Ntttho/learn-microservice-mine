package com.nttho.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    // tạo bean quản lý truc tiếp thông qua route
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder
    ){
        return routeLocatorBuilder
                .routes()
                .route(
                        "product-route", r -> r
                                .path("/api/products","/api/products/**")
                                .uri("lb://product-service")
                )
                .route(
                        "order-route",r -> r
                                .path("/api/orders","/api/orders/**")
                                .uri("lb://order-service")
                )
                .route("notification-route", r -> r
                        .path("/api/notification/**")
                        .uri("lb://notification-service")
                )
                .route("orchestrator-route", r -> r
                        .path("/api/orchestrator/**")
                        .uri("lb://orchestrator-service")
                )
                // ... cấu hinh thêm (loadbalancer đã cấu hình cho port instance trên eureka server ồi không cần localhost:port)
                .build();
    }
}
