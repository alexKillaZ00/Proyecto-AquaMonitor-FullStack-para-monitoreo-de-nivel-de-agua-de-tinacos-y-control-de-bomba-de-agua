package com.tinaco.monitoragua.usuario.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tinaco.monitoragua.usuario.dto.ActualizarPasswordRequest;
import com.tinaco.monitoragua.usuario.dto.UsuarioDataResponse;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.usuario.service.UsuarioService;

@RestController
@RequestMapping("/usuario/me")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public UsuarioDataResponse obtenerDatosDeUsuarioAutenticado(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return usuarioService.obtenerDatosDeUsuario(usuarioAutenticado);
    }

    @PutMapping("/actualizar-nombre")
    public UsuarioDataResponse actualizarNombre(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody String nuevoNombre) {
        return usuarioService.actualizarNombre(usuarioAutenticado, nuevoNombre);
    }

    @PutMapping("/actualizar-password")
    public ResponseEntity<String> actualizarPassword(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody ActualizarPasswordRequest request,
            @CookieValue(value = "refresh", required = false) String refreshToken) {
        usuarioService.actualizarPassword(usuarioAutenticado, request.getPasswordActual(), request.getNuevoPassword(), refreshToken);
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }
}
