package com.tinaco.monitoragua.usuario.dto;

public class ActualizarPasswordRequest {
    private String passwordActual;
    private String nuevoPassword;

    // Getters y setters
    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getNuevoPassword() {
        return nuevoPassword;
    }

    public void setNuevoPassword(String nuevoPassword) {
        this.nuevoPassword = nuevoPassword;
    }
}
