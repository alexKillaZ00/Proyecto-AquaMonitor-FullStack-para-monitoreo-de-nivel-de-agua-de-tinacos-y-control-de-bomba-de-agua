package com.tinaco.monitoragua.nivelAguaActual.dto;

public class NivelAguaActualResponse {
    private double alturaCm;
    private double porcentajeLlenado;
    private boolean desbordado;

    public double getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(double alturaCm) {
        this.alturaCm = alturaCm;
    }

    public double getPorcentajeLlenado() {
        return porcentajeLlenado;
    }

    public void setPorcentajeLlenado(double porcentajeLlenado) {
        this.porcentajeLlenado = porcentajeLlenado;
    }

    public boolean isDesbordado() {
        return desbordado;
    }

    public void setDesbordado(boolean desbordado) {
        this.desbordado = desbordado;
    }
}
