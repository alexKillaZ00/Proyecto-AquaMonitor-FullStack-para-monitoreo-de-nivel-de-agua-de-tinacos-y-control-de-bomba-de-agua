package com.tinaco.monitoragua.dispositivo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tinaco.monitoragua.auth.annotation.RequireRole;
import com.tinaco.monitoragua.dispositivo.dto.DispositivoRequest;
import com.tinaco.monitoragua.dispositivo.dto.DispositivoResponse;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.EstadoDispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;
import com.tinaco.monitoragua.dispositivo.service.DispositivoService;
import com.tinaco.monitoragua.usuario.entity.Usuario.Role;

@RestController
@RequestMapping("/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;

    public DispositivoController(DispositivoService dispositivoService) {
        this.dispositivoService = dispositivoService;
    }

    @PostMapping
    @RequireRole(Role.FABRICANTE)
    public ResponseEntity<DispositivoResponse> registrarDispositivo(@RequestBody DispositivoRequest request) {
        DispositivoResponse nuevoDispositivo = dispositivoService.registrarDispositivo(request);
        return ResponseEntity.ok(nuevoDispositivo);
    }

    @GetMapping
    @RequireRole(Role.FABRICANTE)
    public List<DispositivoResponse> obtenerTodosLosDispositivos() {
        List<DispositivoResponse> dispositivos = dispositivoService.obtenerTodosLosDispositivos();
        return dispositivos;
    }

    @GetMapping("/no-registrados")
    @RequireRole(Role.FABRICANTE)
    public List<DispositivoResponse> obtenerDispositivosNoRegistrados() {
        List<DispositivoResponse> dispositivosNoRegistrados = dispositivoService
                .obtenerDispostivosPorEstado(EstadoDispositivo.NO_REGISTRADO);
        return dispositivosNoRegistrados;
    }

    @GetMapping("/no-registrados/tinacos")
    @RequireRole(Role.FABRICANTE)
    public List<DispositivoResponse> obtenerDispositivosNoRegistradosTipoTinaco() {
        List<DispositivoResponse> dispositivosNoRegistrados = dispositivoService
                .obtenerDispostivosPorEstadoYTipo(EstadoDispositivo.NO_REGISTRADO, TipoDispositivo.TINACO);
        return dispositivosNoRegistrados;
    }

     @GetMapping("/no-registrados/bombas")
     @RequireRole(Role.FABRICANTE)
    public List<DispositivoResponse> obtenerDispositivosNoRegistradosTipoBomba() {
        List<DispositivoResponse> dispositivosNoRegistrados = dispositivoService
                .obtenerDispostivosPorEstadoYTipo(EstadoDispositivo.NO_REGISTRADO, TipoDispositivo.BOMBA);
        return dispositivosNoRegistrados;
    }

}
