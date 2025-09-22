package com.tinaco.monitoragua.nivelAguaActual.dto;

public class NivelAguaActualRequest {
    private String codigoIdentificador;
    private double alturaCm;

    public String getCodigoIdentificador() {
        return codigoIdentificador;
    }

    public void setCodigoIdentificador(String codigoIdentificador) {
        this.codigoIdentificador = codigoIdentificador;
    }

    public double getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(double alturaCm) {
        this.alturaCm = alturaCm;
    }
}
