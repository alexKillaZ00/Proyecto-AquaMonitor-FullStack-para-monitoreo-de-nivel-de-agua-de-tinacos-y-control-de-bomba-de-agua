package com.tinaco.monitoragua.dispositivo.dto;

import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;

public class DispositivoRequest {
    
    private TipoDispositivo tipoDispositivo;

    public TipoDispositivo getTipoDispositivo() {
        return tipoDispositivo;
    }

    public void setTipoDispositivo(TipoDispositivo tipoDispositivo) {
        this.tipoDispositivo = tipoDispositivo;
    }
}
