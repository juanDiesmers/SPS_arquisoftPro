package com.sps.sns.service;

import com.sps.sns.dto.PurchaseWebhookRequest;
import com.sps.sns.dto.SnsValidationRequest;
import com.sps.sns.entity.SnsValidationLog;
import com.sps.sns.repository.SnsValidationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class SnsMockService {

    private static final Logger log = LoggerFactory.getLogger(SnsMockService.class);
    private static final Random RANDOM = new Random();

    private final SnsValidationLogRepository repository;
    private final RestTemplate restTemplate;
    private final String purchaseServiceUrl;
    private final String responseMode;

    public SnsMockService(SnsValidationLogRepository repository,
                          RestTemplate restTemplate,
                          @Value("${purchase.service.url:http://purchase-service:8083}") String purchaseServiceUrl,
                          @Value("${sns.response-mode:APROBADO}") String responseMode) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.purchaseServiceUrl = purchaseServiceUrl;
        this.responseMode = responseMode;
    }

    @Transactional
    public Map<String, Object> validate(SnsValidationRequest request) {
        String resultado = determineResultado();
        SnsValidationLog logEntry = new SnsValidationLog(
                request.getCompraId(),
                request.getClienteId(),
                request.getTotal(),
                request.getPayload(),
                resultado
        );
        repository.save(logEntry);
        sendCompraWebhookAsync(request.getCompraId(), resultado);

        return Map.of(
                "status", "RECIBIDO",
                "compraId", request.getCompraId(),
                "resultado", resultado
        );
    }

    private String determineResultado() {
        String mode = responseMode == null ? "APROBADO" : responseMode.trim().toUpperCase();
        return switch (mode) {
            case "RECHAZADO" -> "RECHAZADO";
            case "ALEATORIO" -> RANDOM.nextBoolean() ? "APROBADO" : "RECHAZADO";
            default -> "APROBADO";
        };
    }

    private void sendCompraWebhookAsync(Long compraId, String resultado) {
        CompletableFuture.runAsync(() -> {
            try {
                String webhookUrl = purchaseServiceUrl + "/compras/webhook-sns";
                PurchaseWebhookRequest webhookRequest = new PurchaseWebhookRequest(compraId, resultado);
                ResponseEntity<Void> response = restTemplate.postForEntity(webhookUrl, webhookRequest, Void.class);
                log.info("SNS mock callback enviado a {}, compraId={}, resultado={}, status={}", webhookUrl, compraId, resultado, response.getStatusCode());
            } catch (Exception ex) {
                log.error("Error al notificar compra {} a Purchase Service: {}", compraId, ex.getMessage(), ex);
            }
        });
    }
}
