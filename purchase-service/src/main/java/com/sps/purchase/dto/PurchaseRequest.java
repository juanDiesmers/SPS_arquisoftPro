package com.sps.purchase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class PurchaseRequest {
    @NotNull(message = "clienteId is required")
    private Long clienteId;

    /** Cédula del cliente para identificarlo en SaludPay. */
    private String cedula;

    @NotEmpty(message = "planIds is required")
    private List<Long> planIds;

    @NotNull(message = "total is required")
    private BigDecimal total;

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

    public List<Long> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(List<Long> planIds) {
        this.planIds = planIds;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
