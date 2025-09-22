package com.tinaco.monitoragua.reportes.dto;

import java.time.YearMonth;

public class ReporteTinacoViewModel {
    // Usuario
    public String usuarioNombre;
    public String usuarioEmail;
    public String fechaGeneracion; // texto formateado
    public String mesTexto; // Ej: "Septiembre 2025"

    // Tinaco
    public Long tinacoId;
    public String tinacoNombre;
    public String tinacoUbicacion;
    public Integer capacidadLitros;
    public Double alturaMaximaCm;
    public String destinoAgua;

    // Nivel actual
    public Double alturaActualCm;
    public Double porcentajeActual;

    // Bomba
    public String bombaNombre = null;
    public String bombaCodigo = null;
    public String bombaModo; // MANUAL / AUTOMATICO
    public String bombaEstado; // ENCENDIDA / APAGADA / N/A

    // Mes
    public YearMonth yearMonth;

    // Métricas
    public double consumoTotalLitros;
    public double consumoPromedioDiarioLitros;
    public double subidaTotalLitros;
    public double subidaPromedioDiariaLitros;
}
