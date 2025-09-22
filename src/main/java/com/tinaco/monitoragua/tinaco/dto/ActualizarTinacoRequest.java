package com.tinaco.monitoragua.tinaco.dto;

public class ActualizarTinacoRequest {
    private String nombre;
    private String ubicacion;
    private Integer capacidadLitros;
    private String destinoAgua;
    private Double alturaMaximaCm;

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

    public Integer getCapacidadLitros() {
        return capacidadLitros;
    }

    public void setCapacidadLitros(Integer capacidadLitros) {
        this.capacidadLitros = capacidadLitros;
    }

    public String getDestinoAgua() {
        return destinoAgua;
    }

    public void setDestinoAgua(String destinoAgua) {
        this.destinoAgua = destinoAgua;
    }

    public Double getAlturaMaximaCm() {
        return alturaMaximaCm;
    }
}
