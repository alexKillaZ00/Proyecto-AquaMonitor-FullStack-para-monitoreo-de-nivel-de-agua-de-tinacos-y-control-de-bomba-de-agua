package com.tinaco.monitoragua.tinaco.controller;

import com.tinaco.monitoragua.tinaco.dto.ActualizarTinacoRequest;
import com.tinaco.monitoragua.tinaco.dto.TinacoConBombaResponse;
import com.tinaco.monitoragua.tinaco.dto.TinacoRequest;
import com.tinaco.monitoragua.tinaco.dto.TinacoResponse;
import com.tinaco.monitoragua.tinaco.dto.VincularBombaRequest;
import com.tinaco.monitoragua.tinaco.service.TinacoService;
import com.tinaco.monitoragua.usuario.entity.Usuario;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tinacos")
public class TinacoController {

    private final TinacoService tinacoService;

    public TinacoController(TinacoService tinacoService) {
        this.tinacoService = tinacoService;
    }

    @PostMapping
    public TinacoResponse registrarTinaco(@RequestBody TinacoRequest request,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.registrarTinaco(request, usuarioAutenticado);
    }

    // Listar todos los tinacos del usuario autenticado
    @GetMapping
    public List<TinacoResponse> listarTodosLosTinacos(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.obtenerTodosLosTinacosDeUsuario(usuarioAutenticado);
    }

    // Listar tinacos con su bomba asociada
    @GetMapping("/con-bomba")
    public List<TinacoConBombaResponse> listarTinacosConBomba(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.obtenerTinacosConSuBombaDeUsuario(usuarioAutenticado);
    }

    // Listar tinacos sin bomba
    @GetMapping("/sin-bomba")
    public List<TinacoResponse> listarTinacosSinBomba(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.obtenerTinacosSinBomba(usuarioAutenticado);
    }

    //Listar tinacos desactivados
    @GetMapping("/desactivados")
    public List<TinacoResponse> listarTinacosDesactivados(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.obtenerTinacosDesactivados(usuarioAutenticado);
    }

    // Obtener un tinaco por ID
    @GetMapping("/{tinacoId}")
    public TinacoResponse obtenerTinacoPorId(@PathVariable Long tinacoId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.obtenerTinacoPorId(tinacoId, usuarioAutenticado);
    }

    //Editar tinaco
    @PutMapping("/editar/{tinacoId}")
    public TinacoResponse editarTinaco(@PathVariable Long tinacoId, @RequestBody ActualizarTinacoRequest request,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.editarTinaco(tinacoId, request, usuarioAutenticado);
    }

    //Desactivar tinaco
    @PostMapping("/desactivar/{tinacoId}")
    public TinacoResponse desactivarTinaco(@PathVariable Long tinacoId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.desactivarTinaco(tinacoId, usuarioAutenticado);
    }

    //Activar tinaco
    @PostMapping("/activar/{tinacoId}")
    public TinacoResponse activarTinaco(@PathVariable Long tinacoId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.activarTinaco(tinacoId, usuarioAutenticado);
    }

    // Vinvular bomba a tinaco
    @PostMapping("/vincular-bomba")
    public TinacoConBombaResponse vincularBombaATinaco(@RequestBody VincularBombaRequest request,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.vincularBombaATinaco(request, usuarioAutenticado);
    }

    // Desvincular bomba de tinaco
    @DeleteMapping("/desvincular-bomba/{tinacoId}")
    public TinacoResponse desvincularBomba(@PathVariable Long tinacoId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return tinacoService.desvincularBombaDeTinaco(tinacoId, usuarioAutenticado);
    }

}
