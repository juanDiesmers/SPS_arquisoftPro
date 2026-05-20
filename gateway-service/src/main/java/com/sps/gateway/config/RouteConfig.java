package com.sps.gateway.config;

import com.sps.gateway.filters.JwtAuthenticationFilter;
import com.sps.gateway.filters.RateLimitGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            JwtAuthenticationFilter jwtFilter,
            RateLimitGatewayFilter rateLimitFilter) {

        return builder.routes()

                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("http://auth-service:8081"))

                .route("catalog-service", r -> r
                        .path("/catalog/**")
                        .filters(f -> f
                                .rewritePath("/catalog/(?<segment>.*)", "/planes/${segment}")
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitGatewayFilter.Config())))
                        .uri("http://catalog-service:8082"))

                .route("purchase-service", r -> r
                        .path("/purchase/**")
                        .filters(f -> f
                                .rewritePath("/purchase/(?<segment>.*)", "/compras/${segment}")
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitGatewayFilter.Config())))
                        .uri("http://purchase-service:8083"))

                .route("saludpay-service", r -> r
                        .path("/payments/**")
                        .filters(f -> f
                                .rewritePath("/payments/(?<segment>.*)", "/api/${segment}")
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitGatewayFilter.Config())))
                        .uri("http://saludpay:8086"))

                .route("sns-mock-service", r -> r
                        .path("/sns/**")
                        .uri("http://sns-mock:8085"))

                .build();
    }
}