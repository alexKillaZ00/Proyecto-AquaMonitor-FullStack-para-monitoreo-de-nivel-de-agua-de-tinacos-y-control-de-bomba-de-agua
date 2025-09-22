package com.tinaco.monitoragua.tinaco.entity;

import com.tinaco.monitoragua.bomba.entity.Bomba;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.usuario.entity.Usuario;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tinacos")
public class Tinaco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_tinaco", nullable = false)
    private String nombre;
    private String ubicacion;
    @Column(name = "capacidad_litros", nullable = false)
    private Integer capacidadLitros;
    private String destinoAgua;

    @Column(name = "altura_maxima_cm", nullable = false)
    private Double alturaMaximaCm;

    @Column(name = "fecha_registro", updatable = false, insertable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaRegistro;

    @OneToOne
    @JoinColumn(name = "dispositivo_id", nullable = false, unique = true)
    private Dispositivo dispositivo;

    @OneToOne
    @JoinColumn(name = "bomba_id", unique = true)
    private Bomba bomba;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, insertable = false, columnDefinition = "ENUM('ACTIVADO', 'DESACTIVADO') DEFAULT 'ACTIVADO'")
    private EstadoTinaco estado;

    public enum EstadoTinaco {
        ACTIVADO, DESACTIVADO
    }

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

    public void setAlturaMaximaCm(Double alturaMaximaCm) {
        this.alturaMaximaCm = alturaMaximaCm;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Dispositivo getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(Dispositivo dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Bomba getBomba() {
        return bomba;
    }

    public void setBomba(Bomba bomba) {
        this.bomba = bomba;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EstadoTinaco getEstado() {
        return estado;
    }

    public void setEstado(EstadoTinaco estado) {
        this.estado = estado;
    }
}
