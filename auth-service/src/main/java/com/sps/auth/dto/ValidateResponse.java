package com.sps.auth.dto;

public class ValidateResponse {
    private boolean valid;
    private String username;
    // getters y setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}