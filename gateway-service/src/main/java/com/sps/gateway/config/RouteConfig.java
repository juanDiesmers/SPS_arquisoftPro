package com.sps.gateway.config;

import com.sps.gateway.filters.JwtAuthenticationFilter;
import com.sps.gateway.filters.RateLimitGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter, RateLimitGatewayFilter rateLimitFilter) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://auth-service"))
                .route("catalog-service", r -> r.path("/planes/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())).filter(rateLimitFilter.apply(new RateLimitGatewayFilter.Config())))
                        .uri("lb://catalog-service"))
                .route("purchase-service", r -> r.path("/compras/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())).filter(rateLimitFilter.apply(new RateLimitGatewayFilter.Config())))
                        .uri("lb://purchase-service"))
                .route("saludpay-service", r -> r.path("/api/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())).filter(rateLimitFilter.apply(new RateLimitGatewayFilter.Config())))
                        .uri("lb://saludpay"))
                .build();
    }
}
