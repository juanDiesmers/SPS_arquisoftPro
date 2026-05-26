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
                                // Auth service
                                .route("auth-service", r -> r
                                                .path("/auth/**")
                                                .uri("http://auth-service:8081"))

                                // Catalog service
                                .route("catalog-service", r -> r
                                                .path("/catalog/**")
                                                .filters(f -> f.rewritePath("/catalog/(?<segment>.*)", "/${segment}"))
                                                .uri("http://catalog-service:8082"))

                                // Purchase service
                                .route("purchase-service", r -> r
                                                .path("/purchase/**")
                                                .filters(f -> f.rewritePath("/purchase/(?<segment>.*)", "/${segment}"))
                                                .uri("http://purchase-service:8083"))

                                // SHC service — historias clinicas
                                .route("shc-service", r -> r
                                                .path("/shc/**")
                                                .uri("http://shc-service:8087"))

                                // SAM service — agendas medicas
                                .route("sam-service", r -> r
                                                .path("/sam/**")
                                                .uri("http://sam-service:8088"))

                                // SaludPay — ruta principal
                                .route("saludpay-service", r -> r
                                                .path("/payments/**")
                                                .filters(f -> f.rewritePath("/payments/(?<segment>.*)",
                                                                "/api/${segment}"))
                                                .uri("http://saludpay:8086"))

                                // SaludPay — ruta de compatibilidad
                                .route("saludpay-service-route", r -> r
                                                .path("/saludpay/**")
                                                .filters(f -> f.rewritePath("/saludpay/(?<segment>.*)",
                                                                "/api/${segment}"))
                                                .uri("http://saludpay:8086"))

                                // SNS mock
                                .route("sns-mock-service", r -> r
                                                .path("/sns/**")
                                                .uri("http://sns-mock:8085"))

                                .build();
        }
}