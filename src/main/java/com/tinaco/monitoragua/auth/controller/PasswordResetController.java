package com.tinaco.monitoragua.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tinaco.monitoragua.auth.service.PasswordResetService;

@RestController
@RequestMapping("/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        // Lógica para solicitar el restablecimiento de la contraseña
        try {
            passwordResetService.requestPasswordReset(email);
            return ResponseEntity.ok("Si el correo existe, se ha enviado un enlace de restablecimiento.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("Error al procesar la solicitud." + e.getMessage());
        }

    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestParam String token, @RequestParam String newPassword) {
        // Lógica para confirmar el restablecimiento de la contraseña
        passwordResetService.confirmPasswordReset(token, newPassword);
        return ResponseEntity.ok("Contraseña restablecida con éxito.");
    }

}
