package com.tinaco.monitoragua.nivelAguaHistorial.entity;

import com.tinaco.monitoragua.nivelAguaActual.entity.NivelAguaActual.Desbordado;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "niveles_agua_historial")
public class NivelAguaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double alturaCm;

    private double porcentajeLlenado;

    @Enumerated(EnumType.STRING)
    @Column(name = "desbordado", nullable = false, columnDefinition = "ENUM('TRUE', 'FALSE') DEFAULT 'FALSE'")
    private Desbordado desbordado;

    @Column(name = "fecha_registro", updatable = false, insertable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tinaco_id", nullable = false)
    private Tinaco tinaco;

    // Constructores
    public NivelAguaHistorial() {
    }

    public NivelAguaHistorial(double alturaCm, double porcentajeLlenado, Desbordado desbordado, Tinaco tinaco) {
        this.alturaCm = alturaCm;
        this.porcentajeLlenado = porcentajeLlenado;
        this.tinaco = tinaco;
        this.desbordado = desbordado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Tinaco getTinaco() {
        return tinaco;
    }

    public void setTinaco(Tinaco tinaco) {
        this.tinaco = tinaco;
    }

    public Desbordado getDesbordado() {
        return desbordado;
    }

    public void setDesbordado(Desbordado desbordado) {
        this.desbordado = desbordado;
    }
}
