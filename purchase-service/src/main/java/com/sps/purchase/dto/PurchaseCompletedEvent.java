package com.sps.purchase.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PurchaseCompletedEvent {
    private Long compraId;
    private Long clienteId;
    private List<Long> planIds;
    private BigDecimal valorPagado;
    private Instant fecha;
    private String estado;

    public PurchaseCompletedEvent() {
    }

    public PurchaseCompletedEvent(Long compraId, Long clienteId, List<Long> planIds, BigDecimal valorPagado, Instant fecha, String estado) {
        this.compraId = compraId;
        this.clienteId = clienteId;
        this.planIds = planIds;
        this.valorPagado = valorPagado;
        this.fecha = fecha;
        this.estado = estado;
    }

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<Long> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(List<Long> planIds) {
        this.planIds = planIds;
    }

    public BigDecimal getValorPagado() {
        return valorPagado;
    }

    public void setValorPagado(BigDecimal valorPagado) {
        this.valorPagado = valorPagado;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
