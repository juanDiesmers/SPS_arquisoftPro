package com.sps.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service - simple pass-through
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("http://auth-service:8081"))

                // Catalog service - simple pass-through with rewrite
                .route("catalog-service", r -> r
                        .path("/catalog/**")
                        .filters(f -> f.rewritePath("/catalog/(?<segment>.*)", "/${segment}"))
                        .uri("http://catalog-service:8082"))

                // Purchase service - simple pass-through with rewrite
                .route("purchase-service", r -> r
                        .path("/purchase/**")
                        .filters(f -> f.rewritePath("/purchase/(?<segment>.*)", "/${segment}"))
                        .uri("http://purchase-service:8083"))

                // Payments service - simple pass-through with rewrite
                .route("saludpay-service", r -> r
                        .path("/payments/**")
                        .filters(f -> f.rewritePath("/payments/(?<segment>.*)", "/api/${segment}"))
                        .uri("http://saludpay:8086"))

                // SaludPay service - compatibility route
                .route("saludpay-service-route", r -> r
                        .path("/saludpay/**")
                        .filters(f -> f.rewritePath("/saludpay/(?<segment>.*)", "/api/${segment}"))
                        .uri("http://saludpay:8086"))

                // SNS mock - simple pass-through
                .route("sns-mock-service", r -> r
                        .path("/sns/**")
                        .uri("http://sns-mock:8085"))

                .build();
    }
}