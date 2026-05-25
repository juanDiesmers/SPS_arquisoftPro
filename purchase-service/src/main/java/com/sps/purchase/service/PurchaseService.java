package com.sps.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sps.purchase.config.RabbitMqConfig;
import com.sps.purchase.dto.PurchaseCompletedEvent;
import com.sps.purchase.dto.PurchaseRequest;
import com.sps.purchase.dto.PurchaseResponse;
import com.sps.purchase.dto.WebhookPagoRequest;
import com.sps.purchase.dto.WebhookSnsRequest;
import com.sps.purchase.entity.PurchaseEntity;
import com.sps.purchase.entity.PurchaseStatus;
import com.sps.purchase.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseRepository purchaseRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final String snsServiceUrl;
    private final String saludPayUrl;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           RabbitTemplate rabbitTemplate,
                           WebClient webClient,
                           ObjectMapper objectMapper,
                           NotificationService notificationService,
                           org.springframework.core.env.Environment env) {
        this.purchaseRepository = purchaseRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.snsServiceUrl = env.getProperty("sns.url", "http://localhost:8085");
        this.saludPayUrl = env.getProperty("saludpay.url", "http://localhost:8086");
    }

    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        String cedula = request.getCedula() != null ? request.getCedula() : String.valueOf(request.getClienteId());
        PurchaseEntity purchase = new PurchaseEntity(
                request.getClienteId(),
                cedula,
                PurchaseStatus.VALIDANDO_SNS,
                request.getTotal(),
                serializePayload(request)
        );
        purchase = purchaseRepository.save(purchase);
        invokeSnsValidationAsync(purchase);
        return toResponse(purchase);
    }

    public PurchaseResponse getPurchase(Long id) {
        PurchaseEntity purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + id));
        return toResponse(purchase);
    }

    @Transactional
    public PurchaseResponse handleWebhookSns(WebhookSnsRequest request) {
        PurchaseEntity purchase = purchaseRepository.findById(request.getCompraId())
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + request.getCompraId()));

        String resultado = request.getResultado();
        purchase.setSnsResult(resultado);
        if ("APROBADO".equalsIgnoreCase(resultado)) {
            purchase.setEstado(PurchaseStatus.PENDIENTE_PAGO);
            notifySaludPay(purchase);
            notificationService.sendPaymentNotification(purchase.getClienteId(), purchase.getId(), purchase.getTotal());
        } else if ("RECHAZADO".equalsIgnoreCase(resultado)) {
            purchase.setEstado(PurchaseStatus.RECHAZADO);
        } else if ("ENPROCESO".equalsIgnoreCase(resultado)) {
            purchase.setEstado(PurchaseStatus.VALIDANDO_SNS);
        } else {
            purchase.setEstado(PurchaseStatus.ERROR_SNS);
        }
        purchase = purchaseRepository.save(purchase);
        return toResponse(purchase);
    }

    @Transactional
    public PurchaseResponse handleWebhookPago(WebhookPagoRequest request) {
        PurchaseEntity purchase = purchaseRepository.findById(request.getCompraId())
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + request.getCompraId()));

        if (!"PAGADO".equalsIgnoreCase(request.getEstado())) {
            purchase.setEstado(PurchaseStatus.RECHAZADO);
        } else {
            purchase.setEstado(PurchaseStatus.PAGADO);
            purchase = purchaseRepository.save(purchase);
            publishPurchaseCompleted(purchase);
            notificationService.sendConfirmationNotification(purchase.getClienteId(), purchase.getId(), purchase.getTotal());
            return toResponse(purchase);
        }
        purchase = purchaseRepository.save(purchase);
        return toResponse(purchase);
    }

    private String serializePayload(PurchaseRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "clienteId", request.getClienteId(),
                    "planIds", request.getPlanIds(),
                    "total", request.getTotal()
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error serializando payload", e);
        }
    }

    private PurchaseResponse toResponse(PurchaseEntity purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getClienteId(),
                purchase.getEstado(),
                purchase.getTotal(),
                purchase.getPayload(),
                purchase.getSnsResult(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    @Retryable(value = Exception.class, maxAttempts = 3)
    public void notifySaludPay(PurchaseEntity purchase) {
        log.info("Notificando Salud Pay para compra {}", purchase.getId());
        String cedula = purchase.getCedula() != null ? purchase.getCedula() : String.valueOf(purchase.getClienteId());
        webClient.post()
                .uri(saludPayUrl + "/api/pagos/pendientes")
                .bodyValue(Map.of(
                        "compraId", purchase.getId(),
                        "clienteId", purchase.getClienteId(),
                        "total", purchase.getTotal(),
                        "estado", purchase.getEstado().name(),
                        "cedula", cedula
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Recover
    public void recoverNotifySaludPay(Exception ex, PurchaseEntity purchase) {
        log.error("No fue posible notificar Salud Pay para compra {}: {}", purchase.getId(), ex.getMessage());
        purchase.setEstado(PurchaseStatus.ERROR_SNS);
        purchaseRepository.save(purchase);
    }

    @CircuitBreaker(name = "purchaseCircuitBreaker", fallbackMethod = "fallbackSnsValidation")
    public Mono<Void> invokeSnsValidation(PurchaseEntity purchase) {
        log.info("Invocando SNS para compra {}", purchase.getId());
        return webClient.post()
                .uri(snsServiceUrl + "/sns/validar")
                .bodyValue(Map.of(
                        "compraId", purchase.getId(),
                        "clienteId", purchase.getClienteId(),
                        "total", purchase.getTotal(),
                        "payload", purchase.getPayload()
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> log.info("Respuesta SNS recibida: {}", response))
                .then();
    }

    public Mono<Void> fallbackSnsValidation(Throwable ex, PurchaseEntity purchase) {
        log.error("SNS no disponible para compra {}: {}", purchase.getId(), ex.getMessage());
        purchase.setEstado(PurchaseStatus.ERROR_SNS);
        purchaseRepository.save(purchase);
        return Mono.empty();
    }

    private void invokeSnsValidationAsync(PurchaseEntity purchase) {
        invokeSnsValidation(purchase).subscribe(
                ignored -> log.info("SNS validation request sent for purchase {}", purchase.getId()),
                throwable -> log.error("SNS async error for purchase {}: {}", purchase.getId(), throwable.getMessage())
        );
    }

    public void publishPurchaseCompleted(PurchaseEntity purchase) {
        PurchaseCompletedEvent event = new PurchaseCompletedEvent(
                purchase.getId(),
                purchase.getClienteId(),
                extractPlanIds(purchase.getPayload()),
                purchase.getTotal(),
                Instant.now(),
                purchase.getEstado().name()
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.SHC_ROUTING_KEY, event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.SAM_ROUTING_KEY, event);
    }

    private List<Long> extractPlanIds(String payload) {
        try {
            Map<?, ?> json = objectMapper.readValue(payload, Map.class);
            Object planIds = json.get("planIds");
            return objectMapper.convertValue(planIds, objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            log.warn("No se pudieron extraer planIds del payload", e);
            return List.of();
        }
    }

    @Scheduled(fixedDelay = 20000)
    public void retrySnsValidation() {
        log.info("Buscando compras en estado VALIDANDO_SNS para reintentar validacion...");
        List<PurchaseEntity> pendingPurchases = purchaseRepository.findByEstado(PurchaseStatus.VALIDANDO_SNS);
        for (PurchaseEntity purchase : pendingPurchases) {
            log.info("Reintentando validacion SNS para compra {}", purchase.getId());
            invokeSnsValidationAsync(purchase);
        }
    }
}
