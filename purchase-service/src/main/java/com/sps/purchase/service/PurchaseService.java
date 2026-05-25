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
    private final String catalogServiceUrl;

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
        this.catalogServiceUrl = env.getProperty("catalog.url", "http://localhost:8082");
    }

    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        String cedula = request.getCedula() != null ? request.getCedula() : String.valueOf(request.getClienteId());
        // REGLA DE NEGOCIO: el total siempre se calcula en el backend consultando el catálogo.
        // No se acepta el total que viene del cliente para evitar manipulación de precios.
        BigDecimal totalCalculado = calculateTotalFromCatalog(request.getPlanIds());
        log.info("Total calculado en backend para planIds {}: ${}", request.getPlanIds(), totalCalculado);
        PurchaseEntity purchase = new PurchaseEntity(
                request.getClienteId(),
                cedula,
                PurchaseStatus.VALIDANDO_SNS,
                totalCalculado,
                serializePayload(request, totalCalculado)
        );
        purchase = purchaseRepository.save(purchase);
        invokeSnsValidationAsync(purchase);
        return toResponse(purchase);
    }

    /**
     * Consulta el catalog-service para obtener el precio real de cada plan
     * y retorna la suma total. Cumple la restricción: "lógica de negocio en el backend".
     */
    private BigDecimal calculateTotalFromCatalog(List<Long> planIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (Long planId : planIds) {
            try {
                Map<?, ?> plan = webClient.get()
                        .uri(catalogServiceUrl + "/planes/" + planId)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                if (plan != null && plan.get("precio") != null) {
                    total = total.add(new BigDecimal(plan.get("precio").toString()));
                }
            } catch (Exception e) {
                log.error("Error al consultar precio del plan {} en catalog-service: {}", planId, e.getMessage());
                throw new RuntimeException("No se pudo obtener el precio del plan " + planId + " desde el catálogo.");
            }
        }
        return total;
    }

    public PurchaseResponse getPurchase(Long id) {
        PurchaseEntity purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + id));
        return toResponse(purchase);
    }

    public List<PurchaseResponse> getPurchasesByCliente(Long clienteId) {
        return purchaseRepository.findByClienteIdOrderByCreatedAtDesc(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
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

    private String serializePayload(PurchaseRequest request, BigDecimal totalCalculado) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "clienteId", request.getClienteId(),
                    "planIds", request.getPlanIds(),
                    "total", totalCalculado
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
        List<Long> planIds = extractPlanIds(purchase.getPayload());
        List<String> nombresPlanes = new java.util.ArrayList<>();
        List<String> serviciosMedicos = new java.util.ArrayList<>();

        // Consultar el catálogo para obtener los nombres de planes y servicios médicos
        for (Long planId : planIds) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> plan = webClient.get()
                        .uri(catalogServiceUrl + "/planes/" + planId)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                if (plan != null) {
                    nombresPlanes.add((String) plan.getOrDefault("nombre", "Plan #" + planId));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> servicios = (List<Map<String, Object>>) plan.get("servicios");
                    if (servicios != null) {
                        servicios.forEach(s -> serviciosMedicos.add((String) s.getOrDefault("nombre", "Servicio")));
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo consultar el plan {} del catálogo para el evento: {}", planId, e.getMessage());
                nombresPlanes.add("Plan #" + planId);
            }
        }

        PurchaseCompletedEvent event = new PurchaseCompletedEvent(
                purchase.getId(),
                purchase.getClienteId(),
                planIds,
                nombresPlanes,
                serviciosMedicos,
                purchase.getTotal(),
                Instant.now(),
                purchase.getEstado().name()
        );
        log.info("Publicando PurchaseCompletedEvent: compraId={}, planes={}, servicios={}",
                purchase.getId(), nombresPlanes, serviciosMedicos);
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
