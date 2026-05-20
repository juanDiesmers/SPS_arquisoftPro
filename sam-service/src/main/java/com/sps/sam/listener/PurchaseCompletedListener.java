package com.sps.sam.listener;

import com.sps.sam.config.RabbitMqConfig;
import com.sps.sam.dto.PurchaseCompletedEvent;
import com.sps.sam.entity.SamRecord;
import com.sps.sam.repository.SamRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class PurchaseCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(PurchaseCompletedListener.class);

    private final SamRecordRepository repository;

    public PurchaseCompletedListener(SamRecordRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMqConfig.SAM_QUEUE)
    public void handlePurchaseCompleted(PurchaseCompletedEvent event) {
        String planIds = event.getPlanIds() == null ? "[]" : event.getPlanIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        SamRecord record = new SamRecord(
                event.getCompraId(),
                event.getClienteId(),
                planIds,
                event.getTotal(),
                event.getEstado(),
                Instant.now()
        );
        repository.save(record);
        log.info("SAM procesó compra {} con estado {}", event.getCompraId(), event.getEstado());
    }
}
