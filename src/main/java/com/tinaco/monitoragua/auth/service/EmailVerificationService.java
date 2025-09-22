package com.tinaco.monitoragua.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinaco.monitoragua.auth.entity.EmailVerificationToken;
import com.tinaco.monitoragua.auth.repository.EmailVerificationTokenRepository;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.usuario.repository.UsuarioRepository;

@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 48; // 64 chars aprox Base64 URL
    private static final int EXP_MINUTES = 15; // Duración del token en minutos

    private final UsuarioRepository usuarioRepo;
    private final EmailVerificationTokenRepository emailVerificationTokenRepo;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(UsuarioRepository usuarioRepo,
            EmailVerificationTokenRepository emailVerificationTokenRepo,
            EmailService emailService) {
        this.usuarioRepo = usuarioRepo;
        this.emailVerificationTokenRepo = emailVerificationTokenRepo;
        this.emailService = emailService;
    }

    /**
     * Genera o reutiliza un token vigente para el usuario.
     * 
     * @param usuario Usuario destino.
     * @return true si se generó y envió un NUEVO token, false si ya existía uno
     *         vigente y no se reenviò.
     */
    @Transactional
    public boolean createOrReuseToken(Usuario usuario) {
        var existingOpt = emailVerificationTokenRepo.findByUsuario(usuario);
        if (existingOpt.isPresent()) {
            EmailVerificationToken existing = existingOpt.get();
            if (existing.getExpiracion().isAfter(LocalDateTime.now())) {
                // Token vigente: no reenviar
                return false;
            }
            // Expirado: eliminar para reemplazar
            emailVerificationTokenRepo.delete(existing);
        }

        // Generar nuevo token
        String token = generateSecureToken();
        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setUsuario(usuario);
        evt.setToken(token);
        evt.setExpiracion(LocalDateTime.now().plusMinutes(EXP_MINUTES));
        emailVerificationTokenRepo.save(evt);

        // Enviar email
        emailService.sendEmailVerification(usuario.getEmail(), token);
        return true;
    }

    public void sendVerificationEmail(String email) {
        usuarioRepo.findByEmail(email).ifPresent(this::createOrReuseToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token de verificación no proporcionado.");
        }

        EmailVerificationToken evt = emailVerificationTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de verificación inválido o expirado."));

        if (evt.getExpiracion().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token de verificación inválido o expirado.");
        }

        Usuario usuario = evt.getUsuario();
        if (usuario.isEmailVerified()) {
            // Ya verificado: eliminar token redundante
            emailVerificationTokenRepo.delete(evt);
            return;
        }

        usuario.setEmailVerified(true);
        usuarioRepo.save(usuario);

        // Consumido: eliminar token
        emailVerificationTokenRepo.delete(evt);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
