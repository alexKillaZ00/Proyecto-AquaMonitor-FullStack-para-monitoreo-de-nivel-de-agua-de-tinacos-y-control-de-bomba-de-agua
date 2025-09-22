package com.tinaco.monitoragua.tinaco.dto;

public class TinacoResponse {
    private Long id;
    private String nombre;
    private String codigoIdentificador;
    private Double alturaMaximaCm;
    private Integer capacidadLitros;
    private String ubicacion;
    private boolean tieneBomba;
    private String estado; // "ACTIVADO" o "DESACTIVADO"
    private String destinoAgua;

    // Getters y setters
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

    public Double getAlturaMaximaCm() {
        return alturaMaximaCm;
    }

    public void setAlturaMaximaCm(Double alturaMaximaCm) {
        this.alturaMaximaCm = alturaMaximaCm;
    }

    public Integer getCapacidadLitros() {
        return capacidadLitros;
    }

    public void setCapacidadLitros(Integer capacidadLitros) {
        this.capacidadLitros = capacidadLitros;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isTieneBomba() {
        return tieneBomba;
    }

    public void setTieneBomba(boolean tieneBomba) {
        this.tieneBomba = tieneBomba;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDestinoAgua() {
        return destinoAgua;
    }

    public void setDestinoAgua(String destinoAgua) {
        this.destinoAgua = destinoAgua;
    }
}
