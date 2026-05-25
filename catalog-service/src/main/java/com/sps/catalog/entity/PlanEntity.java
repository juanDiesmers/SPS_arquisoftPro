package com.sps.catalog.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planes")
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio = BigDecimal.ZERO;

    @Column
    private String convenio;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "plan_servicios",
        joinColumns = @JoinColumn(name = "plan_id"),
        inverseJoinColumns = @JoinColumn(name = "servicio_id")
    )
    private List<ServicioMedicoEntity> servicios = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public PlanEntity() {
    }

    public PlanEntity(String nombre, String descripcion, BigDecimal precio, String convenio, boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.convenio = convenio;
        this.activo = activo;
        this.createdAt = Instant.now();
    }

    @PrePersist
    @PreUpdate
    public void calculatePrice() {
        if (servicios != null && !servicios.isEmpty()) {
            this.precio = servicios.stream()
                    .map(ServicioMedicoEntity::getPrecio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getConvenio() {
        return convenio;
    }

    public void setConvenio(String convenio) {
        this.convenio = convenio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ServicioMedicoEntity> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioMedicoEntity> servicios) {
        this.servicios = servicios;
        calculatePrice();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
