package com.sps.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitGatewayFilter extends AbstractGatewayFilterFactory<RateLimitGatewayFilter.Config> {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private static final int REQUESTS_PER_MINUTE = 100;

    public RateLimitGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String remoteAddr = getRemoteAddress(exchange);
            TokenBucket bucket = buckets.computeIfAbsent(remoteAddr, key -> new TokenBucket(REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
            
            if (!bucket.tryConsume()) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }
            
            return chain.filter(exchange);
        };
    }

    private String getRemoteAddress(ServerWebExchange exchange) {
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : "unknown";
    }

    public static class Config {
    }

    private static class TokenBucket {
        private final int capacity;
        private final Duration refillDuration;
        private int tokens;
        private Instant lastRefill;

        public TokenBucket(int capacity, Duration refillDuration) {
            this.capacity = capacity;
            this.refillDuration = refillDuration;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long refillCount = Duration.between(lastRefill, now).dividedBy(refillDuration);
            if (refillCount > 0) {
                tokens = capacity;
                lastRefill = now;
            }
        }
    }
}
