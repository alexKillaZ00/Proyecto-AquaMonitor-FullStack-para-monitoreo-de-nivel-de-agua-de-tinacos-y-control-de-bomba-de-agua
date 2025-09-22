package com.tinaco.monitoragua.bomba.dto;

public class ActualizarBombaRequest {

    private String nombre;
    private String ubicacion;
    private Integer porcentajeEncender;
    private Integer porcentajeApagar;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
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
}
