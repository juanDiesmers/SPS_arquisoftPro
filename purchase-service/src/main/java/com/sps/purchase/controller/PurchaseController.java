package com.sps.purchase.controller;

import com.sps.purchase.dto.PurchaseRequest;
import com.sps.purchase.dto.PurchaseResponse;
import com.sps.purchase.dto.WebhookPagoRequest;
import com.sps.purchase.dto.WebhookSnsRequest;
import com.sps.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compras")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchase(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchase(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<java.util.List<PurchaseResponse>> getPurchasesByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(purchaseService.getPurchasesByCliente(clienteId));
    }

    @PostMapping("/webhook-sns")
    public ResponseEntity<PurchaseResponse> handleSnsWebhook(@Valid @RequestBody WebhookSnsRequest request) {
        return ResponseEntity.ok(purchaseService.handleWebhookSns(request));
    }

    @PostMapping("/webhook-pago")
    public ResponseEntity<PurchaseResponse> handlePagoWebhook(@Valid @RequestBody WebhookPagoRequest request) {
        return ResponseEntity.ok(purchaseService.handleWebhookPago(request));
    }
}
