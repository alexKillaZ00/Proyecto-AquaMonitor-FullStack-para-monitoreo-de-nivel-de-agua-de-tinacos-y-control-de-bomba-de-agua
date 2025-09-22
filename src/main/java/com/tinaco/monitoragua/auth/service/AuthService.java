package com.tinaco.monitoragua.auth.service;

import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.usuario.entity.Usuario.Role;
import com.tinaco.monitoragua.usuario.repository.UsuarioRepository;
import com.tinaco.monitoragua.auth.entity.RefreshToken;
import com.tinaco.monitoragua.auth.repository.RefreshTokenRepository;
import com.tinaco.monitoragua.utils.ValidationsService;
import com.tinaco.monitoragua.auth.dto.AuthResponse;
import com.tinaco.monitoragua.auth.dto.LoginRequest;
import com.tinaco.monitoragua.auth.dto.RegisterRequest;
import com.tinaco.monitoragua.auth.jwt.JwtService;
import com.tinaco.monitoragua.exception.CredencialesInvalidasException;
import com.tinaco.monitoragua.exception.EmailYaRegistradoException;
import com.tinaco.monitoragua.exception.RefreshTokenNoEncontradoException;
import com.tinaco.monitoragua.exception.RefreshTokenExpiradoException;
import com.tinaco.monitoragua.exception.RefreshTokenInvalidoException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationsService validationsService;
    private final EmailVerificationService emailVerificationService; // Servicio para enviar correos de verificación

    public AuthService(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            ValidationsService vaService,
            EmailVerificationService emailVerificationService) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.validationsService = vaService;
        this.emailVerificationService = emailVerificationService;
    }

    public AuthResponse register(RegisterRequest request) {
        validationsService.validarRegistroUsuario(request.getNombre(), request.getEmail(), request.getPassword());

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail().trim());
        if (usuarioOpt.isPresent()) {
            throw new EmailYaRegistradoException("Error al registrar el email.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setEmailVerified(false); // Por defecto, no verificado
        usuario.setRole(Role.USER); // Por defecto, role USER
        usuarioRepository.save(usuario);

        // Enviar (o reutilizar) token de verificación
        emailVerificationService.createOrReuseToken(usuario);

        return new AuthResponse("Usuario registrado exitosamente");
    }

    public AuthResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());
        if (usuarioOpt.isEmpty()) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        if (!usuario.isEmailVerified()) {
            boolean nuevo = emailVerificationService.createOrReuseToken(usuario);
            if (nuevo) {
                throw new CredencialesInvalidasException(
                        "Correo no verificado. Se envió un nuevo email de verificación.");
            } else {
                throw new CredencialesInvalidasException(
                        "Correo no verificado. Ya existe un email de verificación vigente.");
            }
        }

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshTokenStr = jwtService.generateRefreshToken(usuario);

        // Guardar refresh token en BD
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUsuario(usuario);
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenStr);
    }

    public AuthResponse refresh(String refreshTokenStr) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenWithUsuario(refreshTokenStr);
        if (refreshTokenOpt.isEmpty()) {
            throw new RefreshTokenNoEncontradoException("Refresh token no encontrado");
        }
        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiradoException("Refresh token expirado");
        }

        Usuario usuario = refreshToken.getUsuario();
        // También valida el JWT internamente (firma/expiración)
        if (!jwtService.isTokenValid(refreshTokenStr, usuario)) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenInvalidoException("Refresh token inválido o manipulado");
        }

        // Genera y almacena nuevo access token
        String newAccessToken = jwtService.generateAccessToken(usuario);

        return new AuthResponse(newAccessToken, refreshTokenStr);
    }

    public void logout(String refreshTokenStr) {
        // Elimina solo el refresh token usado para esta sesión
        refreshTokenRepository.findByToken(refreshTokenStr) // genera dos consultas
                .ifPresent(refreshTokenRepository::delete);
    }

    // Método temporal para crear usuarios FABRICANTE con contraseña hasheada
    public AuthResponse createFabricante(RegisterRequest request) {
        validationsService.validarRegistroUsuario(request.getNombre(), request.getEmail(), request.getPassword());

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail().trim());
        if (usuarioOpt.isPresent()) {
            throw new EmailYaRegistradoException("El email ya fue registrado por otro usuario");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(Role.FABRICANTE); // Role FABRICANTE
        usuarioRepository.save(usuario);

        return new AuthResponse("Usuario FABRICANTE creado exitosamente");
    }
}