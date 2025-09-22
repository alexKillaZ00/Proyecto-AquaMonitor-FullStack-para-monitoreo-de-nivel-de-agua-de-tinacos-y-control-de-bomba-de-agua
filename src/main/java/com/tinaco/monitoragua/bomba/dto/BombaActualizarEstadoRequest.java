package com.tinaco.monitoragua.bomba.dto;

import com.tinaco.monitoragua.bomba.entity.Bomba.ModoBomba;

public class BombaActualizarEstadoRequest {
    private ModoBomba modoBomba; // MANUAL o AUTOMATICO

    // Getters y Setters
    public ModoBomba getModoBomba() {
        return modoBomba;
    }

    public void setModoBomba(ModoBomba modoBomba) {
        this.modoBomba = modoBomba;
    }
}
