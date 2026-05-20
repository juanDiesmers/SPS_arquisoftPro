package com.sps.shc.listener;

import com.sps.shc.config.RabbitMqConfig;
import com.sps.shc.dto.PurchaseCompletedEvent;
import com.sps.shc.entity.ShcRecord;
import com.sps.shc.repository.ShcRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class PurchaseCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(PurchaseCompletedListener.class);

    private final ShcRecordRepository repository;

    public PurchaseCompletedListener(ShcRecordRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMqConfig.SHC_QUEUE)
    public void handlePurchaseCompleted(PurchaseCompletedEvent event) {
        String planIds = event.getPlanIds() == null ? "[]" : event.getPlanIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        ShcRecord record = new ShcRecord(
                event.getCompraId(),
                event.getClienteId(),
                planIds,
                event.getTotal(),
                event.getEstado(),
                Instant.now()
        );
        repository.save(record);
        log.info("SHC procesó compra {} con estado {}", event.getCompraId(), event.getEstado());
    }
}
