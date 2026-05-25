package com.sps.catalog.dto;

import java.math.BigDecimal;

public class ServicioMedicoResponse {
    private Long id;
    private String nombre;
    private BigDecimal precio;

    public ServicioMedicoResponse(Long id, String nombre, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }
}
