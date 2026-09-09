package com.session05.gateway_service.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    @Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("category-route", r -> r.path("/api/greeting/**", "/api/greeting")
						.uri("lb://category-service"))
				.build();
	}
}