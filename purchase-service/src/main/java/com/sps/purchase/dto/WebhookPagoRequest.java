package com.sps.purchase.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class WebhookPagoRequest {
    @NotNull(message = "compraId is required")
    private Long compraId;

    @NotNull(message = "monto is required")
    private BigDecimal monto;

    @NotNull(message = "estado is required")
    private String estado;

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
