package com.tinaco.monitoragua.dispositivo.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tinaco.monitoragua.dispositivo.dto.DispositivoRequest;
import com.tinaco.monitoragua.dispositivo.dto.DispositivoResponse;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.EstadoDispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;
import com.tinaco.monitoragua.dispositivo.repository.DispositivoRepository;

@Service
public class DispositivoService {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    public DispositivoResponse registrarDispositivo(DispositivoRequest request) {
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setTipoDispositivo(request.getTipoDispositivo());
        dispositivo.setCodigoIdentificador(generarCodigoIdentificadorUnico());
        dispositivo.setEstado(EstadoDispositivo.NO_REGISTRADO);

        Dispositivo dispositivoGuardado = dispositivoRepository.save(dispositivo);
        return mapToResponse(dispositivoGuardado);
    }

    private String generarCodigoIdentificador() {
        byte[] randomBytes = new byte[8];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generarCodigoIdentificadorUnico() {
        String codigo;
        do {
            codigo = generarCodigoIdentificador().toUpperCase();
        } while (dispositivoRepository.existsByCodigoIdentificador(codigo)); // si esxiste, generará un nuevo código
        return codigo;
    }

    private DispositivoResponse mapToResponse(Dispositivo dispositivo) {
        return new DispositivoResponse(dispositivo.getId(), dispositivo.getTipoDispositivo().name(),
                dispositivo.getCodigoIdentificador(), dispositivo.getEstado().name());
    }

    public List<DispositivoResponse> obtenerTodosLosDispositivos() {
        return dispositivoRepository.findAll().stream()
        .map((d) -> mapToResponse(d))
        .collect(Collectors.toList());
    }

    public List<DispositivoResponse> obtenerDispostivosPorEstado(EstadoDispositivo estadoDispositivo) {
        return dispositivoRepository.findByEstado(estadoDispositivo).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    }

    public List<DispositivoResponse> obtenerDispostivosPorEstadoYTipo(EstadoDispositivo estadoDispositivo,
            TipoDispositivo tipo) {
        return dispositivoRepository.findByEstadoAndTipoDispositivo(estadoDispositivo, tipo).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    }
    
    /* 
    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LONGITUD_CODIGO = 12;

    private String generarCodigoIdentificador() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }
    
    */
}
