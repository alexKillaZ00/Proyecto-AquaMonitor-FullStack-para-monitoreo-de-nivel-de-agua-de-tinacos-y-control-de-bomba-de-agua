package com.tinaco.monitoragua.bomba.controller;

import com.tinaco.monitoragua.bomba.dto.ActualizarBombaRequest;
import com.tinaco.monitoragua.bomba.dto.BombaActualizarEstadoRequest;
import com.tinaco.monitoragua.bomba.dto.BombaEstadoResponse;
import com.tinaco.monitoragua.bomba.dto.BombaRequest;
import com.tinaco.monitoragua.bomba.dto.BombaResponse;
import com.tinaco.monitoragua.bomba.service.BombaService;
import com.tinaco.monitoragua.usuario.entity.Usuario;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bombas")
public class BombaController {

    private final BombaService bombaService;

    public BombaController(BombaService bombaService) {
        this.bombaService = bombaService;
    }

    // @PostMapping("/registrar")
    @PostMapping
    public BombaResponse registrarBomba(@RequestBody BombaRequest request,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        return bombaService.registrarBomba(request, usuarioAutenticado);
    }

    // Listar todas las bombas tengan o no tengan tinaco asociado y que esten activas
    @GetMapping
    public List<BombaResponse> listarTodosLasBombas(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.obtenerTodosLasBombasDeUsuario(usuarioAutenticado);
    }

    // Listar bombas sin tinaco
    @GetMapping("/sin-tinaco")
    public List<BombaResponse> listarBombasSinTinaco(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.obtenerBombasSinTinaco(usuarioAutenticado);
    }

    // GET: Consultar estado actual de la bomba (para ESP32)
    @GetMapping("/estado/{codigoIdentificador}")
    public boolean isBombaEncendidaPorCodigo(@PathVariable String codigoIdentificador,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.isBombaEncendidaPorCodigo(codigoIdentificador, usuarioAutenticado);
    }

    // POST: Cambiar el modo de la bomba (para cliente/frontend)
    @PutMapping("/estado/{bombaId}")
    public BombaEstadoResponse actualizarModoBomba(@PathVariable Long bombaId,
            @RequestBody BombaActualizarEstadoRequest request, @AuthenticationPrincipal Usuario usuarioAutenticado) {

        return bombaService.actualizarModoBomba(bombaId, request, usuarioAutenticado);
    }

    // Encender bomba
    @PostMapping("/encender/{bombaId}")
    public BombaResponse encenderBombaManualmente(@PathVariable Long bombaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.encenderBombaManualmente(bombaId, usuarioAutenticado);
    }

    // Apagar bomba
    @PostMapping("/apagar/{bombaId}")
    public BombaResponse apagarBombaManualmente(@PathVariable Long bombaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.apagarBombaManualmente(bombaId, usuarioAutenticado);
    }

    //Listar bombas desactivadas
    @GetMapping("/desactivadas")
    public List<BombaResponse> listarBombasDesactivadas(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.obtenerBombasDesactivadas(usuarioAutenticado);
    }

    //Obtener bomba por id
    @GetMapping("/{bombaId}")
    public BombaResponse obtenerBombaPorId(@PathVariable Long bombaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.obtenerBombaPorId(bombaId, usuarioAutenticado);
    }

    //Desactivar bomba
    @PostMapping("/desactivar/{bombaId}")
    public BombaResponse desactivarBomba(@PathVariable Long bombaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.desactivarBomba(bombaId, usuarioAutenticado);
    }

    // Activar bomba
    @PostMapping("/activar/{bombaId}")
    public BombaResponse activarBomba(@PathVariable Long bombaId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.activarBomba(bombaId, usuarioAutenticado);
    }

    // Actualizar datos de la bomba
    @PutMapping("/editar/{bombaId}")
    public BombaResponse editarBomba(@PathVariable Long bombaId, @RequestBody ActualizarBombaRequest request,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return bombaService.editarBomba(bombaId, request, usuarioAutenticado);
    }

}
