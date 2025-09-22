package com.tinaco.monitoragua.dispositivo.dto;

public class DispositivoResponse {
    private Long id;
    private String tipoDispositivo;
    private String codigoIdentificador;
    private String estado;

    public DispositivoResponse() {
    }

    public DispositivoResponse(Long id, String tipoDispositivo, String codigoIdentificador, String estado) {
        this.id = id;
        this.tipoDispositivo = tipoDispositivo;
        this.codigoIdentificador = codigoIdentificador;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getTipoDispositivo() {
        return tipoDispositivo;
    }

    public String getCodigoIdentificador() {
        return codigoIdentificador;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTipoDispositivo(String tipoDispositivo) {
        this.tipoDispositivo = tipoDispositivo;
    }

    public void setCodigoIdentificador(String codigoIdentificador) {
        this.codigoIdentificador = codigoIdentificador;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
