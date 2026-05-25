package com.sps.shc.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseCompletedEvent {

    private Long compraId;
    private Long clienteId;
    private List<Long> planIds;
    /** Nombres de los planes adquiridos */
    private List<String> nombresPlanes;
    /** Servicios médicos incluidos (para agenda de doctores) */
    private List<String> serviciosMedicos;
    @JsonProperty("valorPagado")
    private BigDecimal total;
    @JsonProperty("fecha")
    private Instant completedAt;
    private String estado;

    public PurchaseCompletedEvent() {
    }

    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public List<Long> getPlanIds() { return planIds; }
    public void setPlanIds(List<Long> planIds) { this.planIds = planIds; }

    public List<String> getNombresPlanes() { return nombresPlanes; }
    public void setNombresPlanes(List<String> nombresPlanes) { this.nombresPlanes = nombresPlanes; }

    public List<String> getServiciosMedicos() { return serviciosMedicos; }
    public void setServiciosMedicos(List<String> serviciosMedicos) { this.serviciosMedicos = serviciosMedicos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
