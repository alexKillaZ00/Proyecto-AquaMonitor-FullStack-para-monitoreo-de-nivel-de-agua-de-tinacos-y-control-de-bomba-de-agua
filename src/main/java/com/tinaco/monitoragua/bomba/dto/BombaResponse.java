package com.tinaco.monitoragua.bomba.dto;

public class BombaResponse {
    private Long id;
    private String nombre;
    private String ubicacion;
    private String codigoIdentificador;
    private boolean encendida;
    private String modoBomba;
    private Integer porcentajeEncender;
    private Integer porcentajeApagar;
    private boolean tieneTinaco;
    private String estado; // "ACTIVADA" o "DESACTIVADA"

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

    public String getCodigoIdentificador() {
        return codigoIdentificador;
    }

    public void setCodigoIdentificador(String codigoIdentificador) {
        this.codigoIdentificador = codigoIdentificador;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isEncendida() {
        return encendida;
    }

    public void setEncendida(boolean encendida) {
        this.encendida = encendida;
    }

    public String getModoBomba() {
        return modoBomba;
    }

    public void setModoBomba(String modoBomba) {
        this.modoBomba = modoBomba;
    }

    public Integer getPorcentajeEncender() {
        return porcentajeEncender;
    }

    public void setPorcentajeEncender(Integer porcentajeEncender) {
        this.porcentajeEncender = porcentajeEncender;
    }

    public Integer getPorcentajeApagar() {
        return porcentajeApagar;
    }

    public void setPorcentajeApagar(Integer porcentajeApagar) {
        this.porcentajeApagar = porcentajeApagar;
    }

    public boolean isTieneTinaco() {
        return tieneTinaco;
    }

    public void setTieneTinaco(boolean tieneTinaco) {
        this.tieneTinaco = tieneTinaco;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
