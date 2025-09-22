package com.tinaco.monitoragua.auth.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;
    @ManyToOne(optional = false) // No puede ser null
    private com.tinaco.monitoragua.usuario.entity.Usuario usuario;
    @Column(nullable = false)
    private LocalDateTime expiracion;
    @Column(nullable = false)
    private Boolean usado = false;

    // getters/setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public com.tinaco.monitoragua.usuario.entity.Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(com.tinaco.monitoragua.usuario.entity.Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(LocalDateTime expiracion) {
        this.expiracion = expiracion;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }
}