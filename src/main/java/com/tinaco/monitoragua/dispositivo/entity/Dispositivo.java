package com.tinaco.monitoragua.dispositivo.entity;

import java.time.LocalDateTime;

import com.tinaco.monitoragua.usuario.entity.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_identificador", unique = true, nullable = false)
    private String codigoIdentificador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dispositivo", nullable = false)
    private TipoDispositivo tipoDispositivo;

    /*
     * @Enumerated(EnumType.STRING)
     * 
     * @Column(name = "estado", nullable = false)
     * private EstadoDispositivo estado = EstadoDispositivo.NO_ASIGNADO;
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, insertable = false, columnDefinition = "ENUM('NO_REGISTRADO', 'REGISTRADO') DEFAULT 'NO_REGISTRADO'")
    private EstadoDispositivo estado;

    @Column(name = "fecha_creacion", updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private LocalDateTime fechaCreacion;

    public enum TipoDispositivo {
        TINACO, BOMBA
    }

    public enum EstadoDispositivo {
        NO_REGISTRADO, REGISTRADO
    }

    public Dispositivo() {
    }

    public Dispositivo(Long id, String codigoIdentificador, TipoDispositivo tipoDispositivo, EstadoDispositivo estado,
            Usuario usuario) {
        this.id = id;
        this.codigoIdentificador = codigoIdentificador;
        this.tipoDispositivo = tipoDispositivo;
        this.estado = estado;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoIdentificador() {
        return codigoIdentificador;
    }

    public void setCodigoIdentificador(String codigoIdentificador) {
        this.codigoIdentificador = codigoIdentificador;
    }

    public TipoDispositivo getTipoDispositivo() {
        return tipoDispositivo;
    }

    public void setTipoDispositivo(TipoDispositivo tipoDispositivo) {
        this.tipoDispositivo = tipoDispositivo;
    }

    public EstadoDispositivo getEstado() {
        return estado;
    }

    public void setEstado(EstadoDispositivo estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
