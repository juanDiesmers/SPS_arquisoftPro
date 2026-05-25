package com.sps.shc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shc_records")
public class ShcRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compra_id", nullable = false)
    private Long compraId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "plan_ids", columnDefinition = "TEXT")
    private String planIds;

    /** Nombres legibles de los planes adquiridos */
    @Column(name = "nombres_planes", columnDefinition = "TEXT")
    private String nombresPlanes;

    /** Desglose de servicios médicos incluidos en los planes */
    @Column(name = "servicios_medicos", columnDefinition = "TEXT")
    private String serviciosMedicos;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private String estado;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ShcRecord() {
    }

    public ShcRecord(Long compraId, Long clienteId, String planIds, String nombresPlanes,
                     String serviciosMedicos, BigDecimal total, String estado, Instant createdAt) {
        this.compraId = compraId;
        this.clienteId = clienteId;
        this.planIds = planIds;
        this.nombresPlanes = nombresPlanes;
        this.serviciosMedicos = serviciosMedicos;
        this.total = total;
        this.estado = estado;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getPlanIds() { return planIds; }
    public void setPlanIds(String planIds) { this.planIds = planIds; }
    public String getNombresPlanes() { return nombresPlanes; }
    public void setNombresPlanes(String nombresPlanes) { this.nombresPlanes = nombresPlanes; }
    public String getServiciosMedicos() { return serviciosMedicos; }
    public void setServiciosMedicos(String serviciosMedicos) { this.serviciosMedicos = serviciosMedicos; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
