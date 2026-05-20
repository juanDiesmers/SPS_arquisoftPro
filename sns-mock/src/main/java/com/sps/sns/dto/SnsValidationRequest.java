package com.sps.sns.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SnsValidationRequest {

    @NotNull
    private Long compraId;
    @NotNull
    private Long clienteId;
    @NotNull
    private BigDecimal total;
    @NotNull
    private String payload;

    public SnsValidationRequest() {
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
}
