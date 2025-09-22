package com.tinaco.monitoragua.tinaco.dto;

public class VincularBombaRequest {
    private Long tinacoId;
    private Long bombaId;

    // Getters y Setters
    public Long getTinacoId() {
        return tinacoId;
    }

    public void setTinacoId(Long tinacoId) {
        this.tinacoId = tinacoId;
    }

    public Long getBombaId() {
        return bombaId;
    }

    public void setBombaId(Long bombaId) {
        this.bombaId = bombaId;
    }
}
