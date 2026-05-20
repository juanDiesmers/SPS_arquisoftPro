package com.sps.catalog.dto;

import java.math.BigDecimal;

public class PlanResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String convenio;
    private boolean activo;

    public PlanResponse(Long id, String nombre, String descripcion, BigDecimal precio, String convenio, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.convenio = convenio;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getConvenio() {
        return convenio;
    }

    public boolean isActivo() {
        return activo;
    }
}
