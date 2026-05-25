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

        // Nombres legibles de los planes (ej: "Plan Básico, Plan Avanzado")
        String nombresPlanes = event.getNombresPlanes() != null
                ? String.join(", ", event.getNombresPlanes())
                : planIds;

        // Servicios médicos detallados (ej: "Consulta General, Laboratorio, Hospitalización")
        String serviciosMedicos = event.getServiciosMedicos() != null
                ? String.join(", ", event.getServiciosMedicos())
                : "";

        ShcRecord record = new ShcRecord(
                event.getCompraId(),
                event.getClienteId(),
                planIds,
                nombresPlanes,
                serviciosMedicos,
                event.getTotal(),
                event.getEstado(),
                Instant.now()
        );
        repository.save(record);
        log.info("SHC registro historia clinica para compra {} | Planes: {} | Servicios: {}",
                event.getCompraId(), nombresPlanes, serviciosMedicos);
    }
}
