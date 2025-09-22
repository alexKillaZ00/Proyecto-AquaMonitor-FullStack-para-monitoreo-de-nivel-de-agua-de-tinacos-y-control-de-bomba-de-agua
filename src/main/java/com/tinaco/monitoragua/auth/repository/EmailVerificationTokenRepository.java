package com.tinaco.monitoragua.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tinaco.monitoragua.auth.entity.EmailVerificationToken;
import com.tinaco.monitoragua.usuario.entity.Usuario;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUsuario(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
}
