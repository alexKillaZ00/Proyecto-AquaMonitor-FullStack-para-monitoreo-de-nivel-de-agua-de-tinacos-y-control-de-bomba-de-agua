package com.tinaco.monitoragua.nivelAguaActual.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tinaco.monitoragua.nivelAguaActual.dto.NivelAguaActualRequest;
import com.tinaco.monitoragua.nivelAguaActual.dto.NivelAguaActualResponse;
import com.tinaco.monitoragua.nivelAguaActual.service.NivelAguaActualService;

import com.tinaco.monitoragua.usuario.entity.Usuario;

@RestController
@RequestMapping("/api/nivel")
public class NivelAguaActualController {

    private final NivelAguaActualService nivelAguaActualService;

    public NivelAguaActualController(NivelAguaActualService nivelAguaActualService) {
        this.nivelAguaActualService = nivelAguaActualService;
    }

    // Endpoint para que el ESP32 reporte el nivel
    @PostMapping
    public NivelAguaActualResponse registrarNivel(@RequestBody NivelAguaActualRequest request, @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return nivelAguaActualService.registrarNivel(request, usuarioAutenticado);
    }

    // Endpoint para obtener el último nivel registrado de un tinaco dado
    @GetMapping("/actual/{tinacoId}")
    public NivelAguaActualResponse obtenerNivelActual(@PathVariable Long tinacoId, @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return nivelAguaActualService.obtenerUltimoNivel(tinacoId, usuarioAutenticado);
    }   
}
