package com.tinaco.monitoragua.bomba.service;

import com.tinaco.monitoragua.bomba.dto.ActualizarBombaRequest;
import com.tinaco.monitoragua.bomba.dto.BombaActualizarEstadoRequest;
import com.tinaco.monitoragua.bomba.dto.BombaEstadoResponse;
import com.tinaco.monitoragua.bomba.dto.BombaRequest;
import com.tinaco.monitoragua.bomba.dto.BombaResponse;
import com.tinaco.monitoragua.bomba.entity.Bomba;
import com.tinaco.monitoragua.bomba.entity.Bomba.AsociacionTinaco;
import com.tinaco.monitoragua.bomba.entity.Bomba.Encendida;
import com.tinaco.monitoragua.bomba.entity.Bomba.ModoBomba;
import com.tinaco.monitoragua.bomba.repository.BombaRepository;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.EstadoDispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;
import com.tinaco.monitoragua.dispositivo.repository.DispositivoRepository;
import com.tinaco.monitoragua.exception.BombaNoEncontradaException;
import com.tinaco.monitoragua.exception.DispositivoYaAsignadoException;
import com.tinaco.monitoragua.exception.RecursoNoPerteneceAlUsuarioException;
//import com.tinaco.monitoragua.nivelAguaActual.entity.NivelAguaActual;
//import com.tinaco.monitoragua.nivelAguaActual.repository.NivelAguaActualRepository;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.utils.ValidationsService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class BombaService {

    private final BombaRepository bombaRepo;
    private final DispositivoRepository dispositivoRepo;
    //private final NivelAguaActualRepository nivelRepo;
    private final ValidationsService validationsService;

    public BombaService(BombaRepository bombaRepo, DispositivoRepository dispositivoRepo,
            /*NivelAguaActualRepository nivelRepo,*/ ValidationsService validationsService) {
        this.bombaRepo = bombaRepo;
        this.dispositivoRepo = dispositivoRepo;
        //this.nivelRepo = nivelRepo;
        this.validationsService = validationsService;
    }

    public BombaResponse registrarBomba(BombaRequest request, Usuario usuarioAutenticado) {
        validationsService.validarRegistroBomba(request.getNombre(), request.getUbicacion(),
                request.getCodigoIdentificador());

        Dispositivo dispositivo = dispositivoRepo.findByCodigoIdentificadorAndTipoDispositivo(
                request.getCodigoIdentificador(), TipoDispositivo.BOMBA)
                .orElseThrow(() -> new BombaNoEncontradaException("Dispositivo no encontrado o no es una bomba"));

        if (dispositivo.getEstado() != Dispositivo.EstadoDispositivo.NO_REGISTRADO) {
            throw new DispositivoYaAsignadoException("El dispositivo ya está asignado a otra bomba");
        }

        if (bombaRepo.existsByDispositivoId(dispositivo.getId())) {
            throw new DispositivoYaAsignadoException("El dispositivo ya está asociado a una bomba.");
        }

        Bomba bomba = new Bomba();
        bomba.setNombre(request.getNombre());
        if (request.getUbicacion() != null && !request.getUbicacion().trim().isEmpty()) {
            bomba.setUbicacion(request.getUbicacion().trim());
        } else {
            bomba.setUbicacion(null); // Si no viene, se pone como null
        }
        bomba.setUsuario(usuarioAutenticado);
        bomba.setDispositivo(dispositivo);
        bomba.setModoBomba(ModoBomba.MANUAL); // por defecto
        bomba.setEncendida(Encendida.FALSE); // por defecto
        bomba.setEstado(Bomba.EstadoBomba.ACTIVADA);

        Bomba guardada = bombaRepo.save(bomba);

        // Actualizar el Dispositivo para que se marque como ASIGNADO EN LA BD
        dispositivo.setEstado(EstadoDispositivo.REGISTRADO);
        dispositivoRepo.save(dispositivo);

        return mapToResponse(guardada);
    }

    // Para listar todas las bombas incluyendo las que no tienen tinaco
    public List<BombaResponse> obtenerTodosLasBombasDeUsuario(Usuario usuarioAutenticado) {
        return bombaRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(b -> b.getEstado() == Bomba.EstadoBomba.ACTIVADA)
                .map((b) -> mapToResponse(b))
                .collect(Collectors.toList());
    }

    // Para listar unicamente bombas que no tienen tinaco
    public List<BombaResponse> obtenerBombasSinTinaco(Usuario usuarioAutenticado) {
        return bombaRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(b -> b.getTinaco() == null && b.getEstado() == Bomba.EstadoBomba.ACTIVADA)
                .map(b -> mapToResponse(b))
                .collect(Collectors.toList());
    }

    public boolean isBombaEncendidaPorCodigo(String codigoIdentificador, Usuario usuarioAutenticado) {
        Bomba bomba = bombaRepo.findByDispositivoCodigoIdentificador(codigoIdentificador)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        /*if (bomba.getModoBomba() == ModoBomba.AUTOMATICO) {
            NivelAguaActual nivel = nivelRepo.findByTinaco(bomba.getTinaco()).orElse(null);
            if (nivel != null) {
                double porcentaje = nivel.getPorcentajeLlenado();
                if (porcentaje <= bomba.getPorcentajeEncender()) {
                    bomba.setEncendida(Encendida.TRUE);
                } else if (porcentaje >= bomba.getPorcentajeApagar()) {
                    bomba.setEncendida(Encendida.FALSE);
                }
            } else {
                bomba.setEncendida(Encendida.FALSE); // Si no hay nivel, se apaga la bomba
            }

            bombaRepo.save(bomba); // Guardar el estado actualizado de la bomba
        }*/
       
        // Retornar el estado de la bomba
        return bomba.getEncendida() == Encendida.TRUE ? true : false;
    }

    public BombaEstadoResponse actualizarModoBomba(Long bombaId, BombaActualizarEstadoRequest request,
            Usuario usuarioAutenticado) {

        // Bomba bomba =
        // bombaRepo.findByDispositivoCodigoIdentificador(codigoIdentificador)
        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        if (bomba.getEstado() != Bomba.EstadoBomba.ACTIVADA) {
            throw new IllegalArgumentException("La bomba no está activada");
        }

        // Verificar que la bomba tenga tinaco asociado si el modo es AUTOMATICO
        if (request.getModoBomba() == ModoBomba.AUTOMATICO && bomba.getTinaco() == null) {
            // bomba.setModoBomba(ModoBomba.MANUAL); // Cambiar a MANUAL si no tiene tinaco
            throw new IllegalArgumentException("La bomba debe tener un tinaco asociado para cambiar a modo AUTOMATICO");
        } else {
            bomba.setModoBomba(request.getModoBomba());
        }

        // Verificar que la bomba no esté encendida al cambiar de modo (OPCIONAL)

        return mapToModoBombaResponse(bombaRepo.save(bomba));
    }

    public List<BombaResponse> obtenerBombasDesactivadas(Usuario usuarioAutenticado) {
        return bombaRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(b -> b.getEstado() == Bomba.EstadoBomba.DESACTIVADA)
                .map((b) -> mapToResponse(b))
                .collect(Collectors.toList());
    }

    public BombaResponse obtenerBombaPorId(Long bombaId, Usuario usuarioAutenticado) {
        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        return mapToResponse(bomba);
    }

    public BombaResponse desactivarBomba(Long bombaId, Usuario usuarioAutenticado) {
        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        // Verificar que la bomba no esté encendida
        if (bomba.getEncendida() == Encendida.TRUE) {
            throw new IllegalArgumentException("No se puede desactivar la bomba mientras está encendida");
        }

        // Si la bomba tiene tinaco asociado, lanzar excepción
        if (bomba.getTinaco() != null) {
            throw new IllegalArgumentException("La bomba tiene un tinaco asociado, desvincula el tinaco primero");
        }

        // Verificar que la bomba no esté en modo AUTOMATICO, si lo está, cambiar a MANUAL
        if (bomba.getModoBomba() == ModoBomba.AUTOMATICO) {
            bomba.setModoBomba(ModoBomba.MANUAL); // Cambiar a MANUAL si está en AUTOMATICO
        }

        bomba.setEstado(Bomba.EstadoBomba.DESACTIVADA);
        return mapToResponse(bombaRepo.save(bomba));
    }

    public BombaResponse activarBomba(Long bombaId, Usuario usuarioAutenticado) {
        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        bomba.setEstado(Bomba.EstadoBomba.ACTIVADA);
        return mapToResponse(bombaRepo.save(bomba));
    }

    public BombaResponse encenderBombaManualmente(Long bombaId, Usuario usuarioAutenticado) {
        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        if (bomba.getEstado() != Bomba.EstadoBomba.ACTIVADA) {
            throw new IllegalArgumentException("La bomba no está activada");
        }

        if (bomba.getModoBomba() != ModoBomba.MANUAL) {
            throw new IllegalArgumentException("La bomba no está en modo MANUAL");
        }

        bomba.setEncendida(Encendida.TRUE);
        return mapToResponse(bombaRepo.save(bomba));
    }

    public BombaResponse apagarBombaManualmente(Long bombaId, Usuario usuarioAutenticado) {
        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        if (bomba.getEstado() != Bomba.EstadoBomba.ACTIVADA) {
            throw new IllegalArgumentException("La bomba no está activada");
        }

        if (bomba.getModoBomba() != ModoBomba.MANUAL) {
            throw new IllegalArgumentException("La bomba no está en modo MANUAL");
        }

        bomba.setEncendida(Encendida.FALSE);
        return mapToResponse(bombaRepo.save(bomba));
    }

    public BombaResponse editarBomba(Long bombaId, ActualizarBombaRequest request, Usuario usuarioAutenticado) {
        validationsService.validarActualizarBomba(request.getNombre(), request.getUbicacion(),
                request.getPorcentajeEncender(), request.getPorcentajeApagar());

        Bomba bomba = bombaRepo.findById(bombaId)
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        // Actualizar los campos de la bomba
        bomba.setNombre(request.getNombre());

        // Validar que la ubicación no sea nula o vacía
        if (request.getUbicacion() != null && !request.getUbicacion().trim().isEmpty()) {
            bomba.setUbicacion(request.getUbicacion().trim());
        } else {
            bomba.setUbicacion(null); // Si no viene, se pone como null
        }

        bomba.setPorcentajeEncender(request.getPorcentajeEncender());
        bomba.setPorcentajeApagar(request.getPorcentajeApagar());

        // Guardar la bomba actualizada
        return mapToResponse(bombaRepo.save(bomba));
    }

    public static BombaResponse mapToResponse(Bomba guardada) {
        BombaResponse response = new BombaResponse();
        response.setId(guardada.getId());
        response.setNombre(guardada.getNombre());
        response.setUbicacion(guardada.getUbicacion());
        response.setCodigoIdentificador(guardada.getDispositivo().getCodigoIdentificador());
        response.setEncendida(guardada.getEncendida() == Encendida.TRUE);
        response.setModoBomba(guardada.getModoBomba().name());
        response.setPorcentajeEncender(guardada.getPorcentajeEncender());
        response.setPorcentajeApagar(guardada.getPorcentajeApagar());
        response.setTieneTinaco(guardada.getTieneTinaco() == AsociacionTinaco.SI);
        response.setEstado(guardada.getEstado().name());

        return response;
    }

    private BombaEstadoResponse mapToModoBombaResponse(Bomba bomba) {
        BombaEstadoResponse response = new BombaEstadoResponse();

        response.setModoBomba(bomba.getModoBomba());
        return response;
    }
}
