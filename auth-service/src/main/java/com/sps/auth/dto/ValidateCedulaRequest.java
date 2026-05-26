package com.sps.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidateCedulaRequest {

    @NotBlank
    private String cedula;

    @NotBlank
    private String password;

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
