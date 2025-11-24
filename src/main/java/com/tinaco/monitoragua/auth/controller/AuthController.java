package com.tinaco.monitoragua.auth.controller;

import com.tinaco.monitoragua.auth.dto.*;
import com.tinaco.monitoragua.auth.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tinaco.monitoragua.auth.annotation.RequireRole;
import com.tinaco.monitoragua.usuario.entity.Usuario.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setAuthCookies(response, authResponse);
        return ResponseEntity.ok(new AuthResponse("Login exitoso"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(new AuthResponse("No hay refresh token"));
        }
        //try { //Se puede quitar el try-catch para que el GlobalExceptionHandler maneje las excepciones
            AuthResponse authResponse = authService.refresh(refreshToken);
            setAuthCookies(response, authResponse);
            return ResponseEntity.ok(new AuthResponse("Token refrescado"));
       //  } catch (Exception e) {
       //     return ResponseEntity.status(401).body(new AuthResponse(e.getMessage()));
       // }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        // Elimina ambas cookies del cliente
        deleteCookie(response, "jwt");
        String refreshToken = getCookieValue(request, "refresh");
        deleteCookie(response, "refresh");
        if (refreshToken != null) {
            authService.logout(refreshToken); // Elimina de BD
        }
        return ResponseEntity.ok(new AuthResponse("Logout exitoso"));
    }

    // Endpoint para crear usuarios FABRICANTE con contraseña hasheada
    // Protegido: requiere que el usuario autenticado tenga rol FABRICANTE.
    // Nota: El primer usuario FABRICANTE debe crearse manualmente en la BD.
    @PostMapping("/admin/create-fabricante")
    @RequireRole(Role.FABRICANTE)
    public ResponseEntity<AuthResponse> createFabricante(@RequestBody RegisterRequest request) {
        authService.createFabricante(request);
        return ResponseEntity.ok(new AuthResponse("Usuario FABRICANTE creado exitosamente"));
    }

    private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        // Access token 15 minutos
        Cookie jwtCookie = new Cookie("jwt", authResponse.getAccessToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // false para desarrollo (HTTP), true en producción con HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(900); // 15 minutos, cambiar a 10 minutos en desarrollo
        jwtCookie.setAttribute("SameSite", "Lax"); // Lax para permitir cookies en red local
        response.addCookie(jwtCookie);

        // Refresh token 10 días
        Cookie refreshCookie = new Cookie("refresh", authResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // false para desarrollo (HTTP), true en producción con HTTPS
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(864000); // 10 días, cambiar a 1 dia en desarrollo
        refreshCookie.setAttribute("SameSite", "Lax"); // Lax para permitir cookies en red local
        response.addCookie(refreshCookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // false para desarrollo (HTTP), true en producción con HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax"); // Lax para permitir cookies en red local
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}