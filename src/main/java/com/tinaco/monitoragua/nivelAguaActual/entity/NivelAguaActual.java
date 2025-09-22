package com.tinaco.monitoragua.nivelAguaActual.entity;

import java.time.LocalDateTime;

import com.tinaco.monitoragua.tinaco.entity.Tinaco;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "niveles_agua_actual")
public class NivelAguaActual {

    @Id
    private Long id; // Misma que el id del tinaco

    @OneToOne
    @MapsId
    @JoinColumn(name = "tinaco_id", unique = true, nullable = false)
    private Tinaco tinaco;

    private double alturaCm;
    private double porcentajeLlenado;

    @Enumerated(EnumType.STRING)
    @Column(name = "desbordado", nullable = false, columnDefinition = "ENUM('TRUE', 'FALSE') DEFAULT 'FALSE'")
    private Desbordado desbordado;

    public enum Desbordado {
        TRUE, FALSE
    }

    @Column(name = "fecha_ultima_actualizacion", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fechaActualizacion;

    // Getters, setters, constructores

    public NivelAguaActual() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tinaco getTinaco() {
        return tinaco;
    }

    public void setTinaco(Tinaco tinaco) {
        this.tinaco = tinaco;
    }

    public double getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(double alturaCm) {
        this.alturaCm = alturaCm;
    }

    public double getPorcentajeLlenado() {
        return porcentajeLlenado;
    }

    public void setPorcentajeLlenado(double porcentajeLlenado) {
        this.porcentajeLlenado = porcentajeLlenado;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Desbordado getDesbordado() {
        return desbordado;
    }

    public void setDesbordado(Desbordado desbordado) {
        this.desbordado = desbordado;
    }
}
