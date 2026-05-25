package com.sps.purchase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendPaymentNotification(Long clienteId, Long compraId, BigDecimal total) {
        String email = "cliente_" + clienteId + "@sps-salud.com";
        String paymentUrl = "http://localhost:4200/saludpay?id=" + compraId;
        log.info("📧 EMAIL ENVIADO → Destinatario: {} | Asunto: Orden de Pago Pendiente | Valor a pagar: ${} | URL de Pago: {}", 
                email, total, paymentUrl);
    }

    public void sendConfirmationNotification(Long clienteId, Long compraId, BigDecimal total) {
        String email = "cliente_" + clienteId + "@sps-salud.com";
        log.info("📧 EMAIL ENVIADO → Destinatario: {} | Asunto: Confirmación de Compra Exitosa | Valor pagado: ${} | Compra ID: {}", 
                email, total, compraId);
    }
}
