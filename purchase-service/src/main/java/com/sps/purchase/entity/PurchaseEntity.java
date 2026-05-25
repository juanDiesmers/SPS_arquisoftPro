package com.sps.purchase.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "compras")
public class PurchaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "cedula", length = 50)
    private String cedula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus estado;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(columnDefinition = "json")
    private String payload;

    @Column(name = "sns_result")
    private String snsResult;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PurchaseEntity() {
    }

    public PurchaseEntity(Long clienteId, PurchaseStatus estado, BigDecimal total, String payload) {
        this.clienteId = clienteId;
        this.estado = estado;
        this.total = total;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public PurchaseEntity(Long clienteId, String cedula, PurchaseStatus estado, BigDecimal total, String payload) {
        this(clienteId, estado, total, payload);
        this.cedula = cedula;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public PurchaseStatus getEstado() {
        return estado;
    }

    public void setEstado(PurchaseStatus estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSnsResult() {
        return snsResult;
    }

    public void setSnsResult(String snsResult) {
        this.snsResult = snsResult;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
