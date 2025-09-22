package com.tinaco.monitoragua.auth.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinaco.monitoragua.auth.repository.PasswordResetTokenRepository;
import com.tinaco.monitoragua.auth.repository.RefreshTokenRepository;
import com.tinaco.monitoragua.usuario.repository.UsuarioRepository;
import com.tinaco.monitoragua.utils.ValidationsService;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepo;
    private final UsuarioRepository usuarioRepo;
    private final EmailService emailService; // Servicio para enviar correos
    private final ValidationsService validationsService;
    private final RefreshTokenRepository refreshTokenRepo; // Repositorio para manejar los refresh tokens y cerrar sesión en todas las sesiones activas
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokenRepo, UsuarioRepository usuarioRepo,
            EmailService emailService, RefreshTokenRepository refreshTokenRepo, ValidationsService vaService) {
        this.tokenRepo = tokenRepo;
        this.usuarioRepo = usuarioRepo;
        this.emailService = emailService;
        this.refreshTokenRepo = refreshTokenRepo;
        this.validationsService = vaService;
    }

    // Métodos para manejar la lógica de restablecimiento de contraseña
    @Transactional
    public void requestPasswordReset(String email) {
        // Lógica para generar y enviar el token de restablecimiento
        var usuarioOpt = usuarioRepo.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            var usuario = usuarioOpt.get();
            var token = generateToken();

            var resetToken = new com.tinaco.monitoragua.auth.entity.PasswordResetToken();
            resetToken.setUsuario(usuario);
            resetToken.setToken(token);
            resetToken.setExpiracion(java.time.LocalDateTime.now().plusMinutes(15)); // Expira en 15 minutos
            resetToken.setUsado(false);

            tokenRepo.save(resetToken);

            // Enviar el correo con el token
            emailService.sendPasswordResetEmail(usuario.getEmail(), token);
        }
    }

    @Transactional
    public void confirmPasswordReset(String tokenHash, String newPassword) {
        // Lógica para validar el token y actualizar la contraseña
        validationsService.validarPassword(newPassword);
        var tokenOpt = tokenRepo.findByToken(tokenHash);
        if (tokenOpt.isPresent()) {
            var token = tokenOpt.get();
            if (token.getExpiracion().isAfter(java.time.LocalDateTime.now()) && !token.getUsado()) {
                var usuario = token.getUsuario();
                usuario.setPasswordHash(encoder.encode(newPassword));
                usuarioRepo.save(usuario);

                token.setUsado(true);
                tokenRepo.delete(token); // Eliminar el token después de usarlo
                //tokenRepo.save(token);

                //Cerrar sesión de todas las sesiones activas del usuario borrando sus refresh tokens
                refreshTokenRepo.deleteByUsuario(usuario);
            } else {
                throw new IllegalArgumentException("Token expirado o ya usado.");
            }
        } else {
            throw new IllegalArgumentException("Token inválido.");
        }
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
