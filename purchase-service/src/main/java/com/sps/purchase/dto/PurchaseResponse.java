package com.sps.purchase.dto;

import com.sps.purchase.entity.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PurchaseResponse {
    private Long id;
    private Long clienteId;
    private PurchaseStatus estado;
    private BigDecimal total;
    private String payload;
    private String snsResult;
    private Instant createdAt;
    private Instant updatedAt;

    public PurchaseResponse(Long id, Long clienteId, PurchaseStatus estado, BigDecimal total, String payload, String snsResult, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
        this.total = total;
        this.payload = payload;
        this.snsResult = snsResult;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public PurchaseStatus getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getPayload() {
        return payload;
    }

    public String getSnsResult() {
        return snsResult;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
