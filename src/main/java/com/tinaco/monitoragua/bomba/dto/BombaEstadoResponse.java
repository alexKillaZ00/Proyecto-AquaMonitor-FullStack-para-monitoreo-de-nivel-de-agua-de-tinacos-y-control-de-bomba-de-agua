package com.tinaco.monitoragua.bomba.dto;

import com.tinaco.monitoragua.bomba.entity.Bomba.ModoBomba;

public class BombaEstadoResponse {
    private ModoBomba modoBomba;

    public ModoBomba getModoBomba() {
        return modoBomba;
    }

    public void setModoBomba(ModoBomba modoBomba) {
        this.modoBomba = modoBomba;
    }
}
