package com.sps.sns.service;

import com.sps.sns.dto.SnsValidationRequest;
import com.sps.sns.entity.SnsValidationLog;
import com.sps.sns.repository.SnsValidationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;

@Service
public class SnsMockService {

    private static final Logger log = LoggerFactory.getLogger(SnsMockService.class);
    private static final Random RANDOM = new Random();

    private final SnsValidationLogRepository repository;
    private final String responseMode;

    public SnsMockService(SnsValidationLogRepository repository,
                          @Value("${sns.response-mode:APROBADO}") String responseMode) {
        this.repository = repository;
        this.responseMode = responseMode;
    }

    @Transactional
    public Map<String, Object> validate(SnsValidationRequest request) {
        String resultado = determineResultado();
        log.info("[SNS] Procesando validacion reactiva para compraId={}, resultado={}", request.getCompraId(), resultado);
        SnsValidationLog logEntry = new SnsValidationLog(
                request.getCompraId(),
                request.getClienteId(),
                request.getTotal(),
                request.getPayload(),
                resultado
        );
        repository.save(logEntry);
        // El resultado se devuelve directamente en la respuesta HTTP.
        // El purchase-service lo procesa en su cadena WebFlux reactiva (sin callback).
        return Map.of(
                "status", "VALIDADO",
                "compraId", request.getCompraId(),
                "resultado", resultado
        );
    }

    private String determineResultado() {
        String mode = responseMode == null ? "APROBADO" : responseMode.trim().toUpperCase();
        return switch (mode) {
            case "RECHAZADO" -> "RECHAZADO";
            case "ENPROCESO" -> "ENPROCESO";
            case "ALEATORIO" -> RANDOM.nextBoolean() ? "APROBADO" : "RECHAZADO";
            default -> "APROBADO";
        };
    }
}
