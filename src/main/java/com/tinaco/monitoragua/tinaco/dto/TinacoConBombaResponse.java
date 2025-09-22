package com.tinaco.monitoragua.tinaco.dto;

import com.tinaco.monitoragua.bomba.dto.BombaResponse;

public class TinacoConBombaResponse {

    private TinacoResponse tinacoResponse;
    private BombaResponse bombaResponse;

    public TinacoResponse getTinacoResponse() {
        return tinacoResponse;
    }

    public void setTinacoResponse(TinacoResponse tinacoResponse) {
        this.tinacoResponse = tinacoResponse;
    }

    public BombaResponse getBombaResponse() {
        return bombaResponse;
    }

    public void setBombaResponse(BombaResponse bombaResponse) {
        this.bombaResponse = bombaResponse;
    }
}
