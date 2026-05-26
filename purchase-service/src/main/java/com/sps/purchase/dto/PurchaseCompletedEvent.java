package com.sps.purchase.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PurchaseCompletedEvent {
    private Long compraId;
    private Long clienteId;
    private List<Long> planIds;
    /** Nombres de los planes adquiridos (para SHC y SAM) */
    private List<String> nombresPlanes;
    /** Desglose de servicios medicos incluidos en todos los planes (para SAM) */
    private List<String> serviciosMedicos;
    private BigDecimal valorPagado;
    private Instant fecha;
    private String estado;

    public PurchaseCompletedEvent() {
    }

    public PurchaseCompletedEvent(Long compraId, Long clienteId, List<Long> planIds,
            List<String> nombresPlanes, List<String> serviciosMedicos,
            BigDecimal valorPagado, Instant fecha, String estado) {
        this.compraId = compraId;
        this.clienteId = clienteId;
        this.planIds = planIds;
        this.nombresPlanes = nombresPlanes;
        this.serviciosMedicos = serviciosMedicos;
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

    public List<String> getNombresPlanes() {
        return nombresPlanes;
    }

    public void setNombresPlanes(List<String> nombresPlanes) {
        this.nombresPlanes = nombresPlanes;
    }

    public List<String> getServiciosMedicos() {
        return serviciosMedicos;
    }

    public void setServiciosMedicos(List<String> serviciosMedicos) {
        this.serviciosMedicos = serviciosMedicos;
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
