package com.tinaco.monitoragua.auth.repository;

import com.tinaco.monitoragua.auth.entity.RefreshToken;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);

    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.usuario WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUsuario(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.fechaExpiracion < :fechaLimite")
    int deleteByFechaExpiracionBefore(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.usuario = :usuario AND rt.token <> :token")
    void deleteByUsuarioAndTokenNot(@Param("usuario") Usuario usuario, @Param("token") String token);
}