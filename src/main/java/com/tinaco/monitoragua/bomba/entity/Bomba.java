package com.tinaco.monitoragua.bomba.entity;

import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bombas")
public class Bomba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String ubicacion;

    /*
     * @Column(name = "fecha_registro")
     * private LocalDateTime fechaRegistro = LocalDateTime.now();
     */
    @Column(name = "fecha_registro", updatable = false, insertable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_bomba", nullable = false, insertable = false, columnDefinition = "ENUM('MANUAL', 'AUTOMATICO') DEFAULT 'MANUAL'")
    private ModoBomba modoBomba;

    @Enumerated(EnumType.STRING)
    @Column(name = "encendida", nullable = false, insertable = false, columnDefinition = "ENUM('TRUE', 'FALSE') DEFAULT 'FALSE'")
    private Encendida encendida;

    @Column(name = "porcentaje_encender", nullable = false, columnDefinition = "TINYINT DEFAULT 20")
    private Integer porcentajeEncender = 20;

    @Column(name = "porcentaje_apagar", nullable = false, columnDefinition = "TINYINT DEFAULT 85")
    private Integer porcentajeApagar = 85;

    @OneToOne
    @JoinColumn(name = "dispositivo_id", nullable = false, unique = true)
    private Dispositivo dispositivo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToOne(mappedBy = "bomba")
    private Tinaco tinaco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tiene_tinaco", nullable = false)
    private AsociacionTinaco tieneTinaco = AsociacionTinaco.NO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, insertable = false, columnDefinition = "ENUM('ACTIVADA', 'DESACTIVADA') DEFAULT 'ACTIVADA'")
    private EstadoBomba estado;

    public enum EstadoBomba {
        ACTIVADA, DESACTIVADA
    }

    public enum AsociacionTinaco {
        SI, NO
    }

    public enum Encendida {
        TRUE, FALSE
    }

    public enum ModoBomba {
        MANUAL, AUTOMATICO
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ModoBomba getModoBomba() {
        return modoBomba;
    }

    public void setModoBomba(ModoBomba modoBomba) {
        this.modoBomba = modoBomba;
    }

    public Encendida getEncendida() {
        return encendida;
    }

    public void setEncendida(Encendida encendida) {
        this.encendida = encendida;
    }

    public Integer getPorcentajeEncender() {
        return porcentajeEncender;
    }

    public void setPorcentajeEncender(Integer porcentajeEncender) {
        this.porcentajeEncender = porcentajeEncender;
    }

    public Integer getPorcentajeApagar() {
        return porcentajeApagar;
    }

    public void setPorcentajeApagar(Integer porcentajeApagar) {
        this.porcentajeApagar = porcentajeApagar;
    }

    public Tinaco getTinaco() {
        return tinaco;
    }

    public void setTinaco(Tinaco tinaco) {
        this.tinaco = tinaco;
    }

    public AsociacionTinaco getTieneTinaco() {
        return tieneTinaco;
    }

    public void setTieneTinaco(AsociacionTinaco tieneTinaco) {
        this.tieneTinaco = tieneTinaco;
    }

    public EstadoBomba getEstado() {
        return estado;
    }

    public void setEstado(EstadoBomba estado) {
        this.estado = estado;
    }
}
