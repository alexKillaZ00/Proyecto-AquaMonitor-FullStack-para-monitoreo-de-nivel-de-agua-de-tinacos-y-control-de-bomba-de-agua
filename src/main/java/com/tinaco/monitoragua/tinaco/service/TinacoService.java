package com.tinaco.monitoragua.tinaco.service;

import com.tinaco.monitoragua.bomba.dto.BombaResponse;
import com.tinaco.monitoragua.bomba.entity.Bomba;
import com.tinaco.monitoragua.bomba.repository.BombaRepository;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.EstadoDispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;
import com.tinaco.monitoragua.dispositivo.repository.DispositivoRepository;
import com.tinaco.monitoragua.exception.BombaNoEncontradaException;
import com.tinaco.monitoragua.exception.BombaYaAsociadaATinacoException;
import com.tinaco.monitoragua.exception.DispositivoNoEncontradoException;
import com.tinaco.monitoragua.exception.DispositivoYaAsignadoException;
import com.tinaco.monitoragua.exception.RecursoNoPerteneceAlUsuarioException;
import com.tinaco.monitoragua.exception.TinacoNoEncontradoException;
import com.tinaco.monitoragua.exception.TinacoSinBombaAsociadaException;
import com.tinaco.monitoragua.exception.TinacoYaTieneBombaException;
import com.tinaco.monitoragua.tinaco.dto.ActualizarTinacoRequest;
import com.tinaco.monitoragua.tinaco.dto.TinacoConBombaResponse;
import com.tinaco.monitoragua.tinaco.dto.TinacoRequest;
import com.tinaco.monitoragua.tinaco.dto.TinacoResponse;
import com.tinaco.monitoragua.tinaco.dto.VincularBombaRequest;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import com.tinaco.monitoragua.tinaco.repository.TinacoRepository;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.utils.ValidationsService;

import org.springframework.stereotype.Service;
import com.tinaco.monitoragua.bomba.service.BombaService;

//import java.security.SecureRandom;
//import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TinacoService {

    private final TinacoRepository tinacoRepo;
    private final DispositivoRepository dispositivoRepo;
    private final BombaRepository bombaRepo;
    private final ValidationsService validationsService;

    public TinacoService(TinacoRepository tinacoRepo, DispositivoRepository dispositivoRepo,
            BombaRepository bombaRepo, ValidationsService validationsService) {
        this.tinacoRepo = tinacoRepo;
        this.dispositivoRepo = dispositivoRepo;
        this.bombaRepo = bombaRepo;
        this.validationsService = validationsService;
    }

    public TinacoResponse registrarTinaco(TinacoRequest request, Usuario usuarioAutenticado) {
        validationsService.validarRegistroTinaco(request.getNombre(), request.getUbicacion(),
                request.getCapacidadLitros(), request.getDestinoAgua(), request.getAlturaMaximaCm(),
                request.getCodigoIdentificador());

        Dispositivo dispositivo = dispositivoRepo.findByCodigoIdentificadorAndTipoDispositivo(
                request.getCodigoIdentificador(), TipoDispositivo.TINACO)
                .orElseThrow(() -> new DispositivoNoEncontradoException("Dispositivo no encontrado o no es un tinaco"));

        if (dispositivo.getEstado() != Dispositivo.EstadoDispositivo.NO_REGISTRADO) {
            throw new DispositivoYaAsignadoException("El dispositivo ya está asignado a otro tinaco");
        }

        if (tinacoRepo.existsByDispositivoId(dispositivo.getId())) {
            throw new DispositivoYaAsignadoException("El dispositivo ya está asociado a un tinaco");
        }

        Tinaco tinaco = new Tinaco();
        tinaco.setNombre(request.getNombre());
        tinaco.setCapacidadLitros(request.getCapacidadLitros());
        tinaco.setAlturaMaximaCm(request.getAlturaMaximaCm());
        tinaco.setDispositivo(dispositivo);
        tinaco.setUsuario(usuarioAutenticado);
        tinaco.setEstado(Tinaco.EstadoTinaco.ACTIVADO); // Por defecto, el tinaco se activa al registrarse

        // Registrar los campos si vienen en la request
        if (request.getUbicacion() != null && !request.getUbicacion().trim().isEmpty()) {
            tinaco.setUbicacion(request.getUbicacion().trim());
        } else {
            tinaco.setUbicacion(null); // Si no viene, se pone como null
        }
        if (request.getDestinoAgua() != null && !request.getDestinoAgua().trim().isEmpty()) {
            tinaco.setDestinoAgua(request.getDestinoAgua().trim());
        } else {
            tinaco.setDestinoAgua(null); // Si no viene, se pone como null
        }

        Tinaco guardado = tinacoRepo.save(tinaco);

        // Actualizar el Dispositivo para que se marque como REGISTRADO EN LA BD
        dispositivo.setEstado(EstadoDispositivo.REGISTRADO);
        dispositivoRepo.save(dispositivo);

        return mapToResponse(guardado);
    }

    public TinacoResponse obtenerTinacoPorId(Long tinacoId, Usuario usuarioAutenticado) {
        Tinaco tinaco = tinacoRepo.findById(tinacoId)
                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

        // Verificar que el tinaco pertenezca al usuario
        if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("El tinaco no pertenece al usuario autenticado");
        }

        return mapToResponse(tinaco);
    }

    public TinacoResponse editarTinaco(Long tinacoId, ActualizarTinacoRequest request, Usuario usuarioAutenticado) {
        // Validar los campos que vienen en la request
        validationsService.validarActualizarTinaco(request.getNombre(), request.getUbicacion(),
                request.getCapacidadLitros(), request.getDestinoAgua(), request.getAlturaMaximaCm());

        Tinaco tinaco = tinacoRepo.findById(tinacoId)
                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

        // Verificar que el tinaco pertenezca al usuario
        if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("El tinaco no pertenece al usuario autenticado");
        }

        tinaco.setNombre(request.getNombre().trim());
        tinaco.setCapacidadLitros(request.getCapacidadLitros());
        tinaco.setAlturaMaximaCm(request.getAlturaMaximaCm());

        // Actualizar los campos si vienen en la request
        if (request.getUbicacion() != null && !request.getUbicacion().trim().isEmpty()) {
            tinaco.setUbicacion(request.getUbicacion().trim());
        } else {
            tinaco.setUbicacion(null); // Si no viene, se pone como null
        }
        if (request.getDestinoAgua() != null && !request.getDestinoAgua().trim().isEmpty()) {
            tinaco.setDestinoAgua(request.getDestinoAgua().trim());
        } else {
            tinaco.setDestinoAgua(null); // Si no viene, se pone como null
        }

        Tinaco actualizado = tinacoRepo.save(tinaco);
        return mapToResponse(actualizado);
    }

    // Para el dashboard, obtener tinacos con su bomba
    public List<TinacoConBombaResponse> obtenerTinacosConSuBombaDeUsuario(Usuario usuarioAutenticado) {
        return tinacoRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(t -> t.getBomba() != null && t.getEstado() != Tinaco.EstadoTinaco.DESACTIVADO &&
                        t.getBomba().getEstado() != Bomba.EstadoBomba.DESACTIVADA)
                .map(t -> mapToTinacoConBombaResponse(t))
                .collect(Collectors.toList());
    }

    // Para listar unicamente tinacos que no tienen bomba
    public List<TinacoResponse> obtenerTinacosSinBomba(Usuario usuarioAutenticado) {
        return tinacoRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(t -> t.getBomba() == null && t.getEstado() != Tinaco.EstadoTinaco.DESACTIVADO)
                .map(t -> mapToResponse(t))
                .collect(Collectors.toList());
    }

    // Para listar unicamente tinacos incluyendo los que no tienen bomba
    public List<TinacoResponse> obtenerTodosLosTinacosDeUsuario(Usuario usuarioAutenticado) {
        return tinacoRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(t -> t.getEstado() != Tinaco.EstadoTinaco.DESACTIVADO)
                .map((t) -> mapToResponse(t))
                .collect(Collectors.toList());
    }

    // Para listar tinacos desactivados
    public List<TinacoResponse> obtenerTinacosDesactivados(Usuario usuarioAutenticado) {
        return tinacoRepo.findByUsuarioId(usuarioAutenticado.getId()).stream()
                .filter(t -> t.getEstado() == Tinaco.EstadoTinaco.DESACTIVADO)
                .map(t -> mapToResponse(t))
                .collect(Collectors.toList());
    }

    public TinacoResponse desactivarTinaco(Long tinacoId, Usuario usuarioAutenticado) {
        Tinaco tinaco = tinacoRepo.findById(tinacoId)
                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

        // Verificar que el tinaco pertenezca al usuario
        if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("El tinaco no pertenece al usuario autenticado");
        }

        // Si el tinaco tiene bomba asociada, lanzar excepción
        if (tinaco.getBomba() != null) {
            throw new IllegalArgumentException("El tinaco tiene una bomba asociada, desvincula la bomba primero");
        }

        // Desactivar el tinaco
        tinaco.setEstado(Tinaco.EstadoTinaco.DESACTIVADO);
        Tinaco desactivado = tinacoRepo.save(tinaco);

        return mapToResponse(desactivado);
    }

    public TinacoResponse activarTinaco(Long tinacoId, Usuario usuarioAutenticado) {
        Tinaco tinaco = tinacoRepo.findById(tinacoId)
                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

        // Verificar que el tinaco pertenezca al usuario
        if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("El tinaco no pertenece al usuario autenticado");
        }

        // Activar el tinaco
        tinaco.setEstado(Tinaco.EstadoTinaco.ACTIVADO);
        Tinaco activado = tinacoRepo.save(tinaco);

        return mapToResponse(activado);
    }

    public TinacoConBombaResponse vincularBombaATinaco(VincularBombaRequest request, Usuario usuarioAutenticado) {
        Tinaco tinaco = tinacoRepo.findById(request.getTinacoId())
                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

        Bomba bomba = bombaRepo.findById(request.getBombaId())
                .orElseThrow(() -> new BombaNoEncontradaException("Bomba no encontrada"));

        // Validar que el tinaco y la bomba pertenezcan al mismo usuario
        if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId()) ||
                !bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("El tinaco o la bomba no pertenecen al usuario autenticado");
        }

        // Validar que el tinaco no esté ya asociado a otra una bomba
        if (tinaco.getBomba() != null) {
            throw new TinacoYaTieneBombaException("El tinaco ya está asociada a una bomba");
        }

        // Validar que la bomba no esté ya asociada a otro tinaco
        if (bomba.getTinaco() != null) {
            throw new BombaYaAsociadaATinacoException("La bomba ya está asociada a un tinaco");
        }

        // Validar que el tinaco esté activo
        if (tinaco.getEstado() == Tinaco.EstadoTinaco.DESACTIVADO) {
            throw new IllegalArgumentException("El tinaco está desactivado y no puede asociarse a una bomba");
        }

        // Validar que la bomba esté activa
        if (bomba.getEstado() == Bomba.EstadoBomba.DESACTIVADA) {
            throw new IllegalArgumentException("La bomba está desactivada y no puede asociarse a un tinaco");
        }

        // Vincular bomba al tinaco
        tinaco.setBomba(bomba);
        bomba.setTinaco(tinaco);

        // Marcar la bomba como que tiene tinaco
        bomba.setTieneTinaco(Bomba.AsociacionTinaco.SI);

        // Guardar los cambios
        tinacoRepo.save(tinaco);
        bombaRepo.save(bomba);

        return mapToTinacoConBombaResponse(tinaco);
    }

    public TinacoResponse desvincularBombaDeTinaco(Long tinacoId, Usuario usuarioAutenticado) {
        Tinaco tinaco = tinacoRepo.findById(tinacoId)
                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

        // Verificar que el tinaco pertenezca al usuario
        if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("El tinaco no pertenece al usuario autenticado");
        }

        // Si no hay bomba asociada, no hay nada que hacer
        if (tinaco.getBomba() == null) {
            throw new TinacoSinBombaAsociadaException("El tinaco no tiene una bomba asociada");
        }

        // Validar que el tinaco esté activo
        if (tinaco.getEstado() == Tinaco.EstadoTinaco.DESACTIVADO) {
            throw new IllegalArgumentException("El tinaco está desactivado y no puede desvincularse de una bomba");
        }

        Bomba bomba = tinaco.getBomba();

        // Validar que la bomba pertenezca al usuario
        if (!bomba.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new RecursoNoPerteneceAlUsuarioException("La bomba no pertenece al usuario autenticado");
        }

        // Validar que la bomba esté activa
        if (bomba.getEstado() == Bomba.EstadoBomba.DESACTIVADA) {
            throw new IllegalArgumentException("La bomba está desactivada y no puede desvincularse de un tinaco");
        }

        // Validar que la bomba esté en modo MANUAL y apagada para poder desvincularla
        if (bomba.getModoBomba() != Bomba.ModoBomba.MANUAL || bomba.getEncendida() != Bomba.Encendida.FALSE) {
            throw new IllegalArgumentException("La bomba debe estar en modo MANUAL y apagada para poder desvincularla");
        }

        // Desvincular
        tinaco.setBomba(null);
        bomba.setTinaco(null);

        // Marcar la bomba como que NO tiene tinaco
        bomba.setTieneTinaco(Bomba.AsociacionTinaco.NO);

        // Guardar cambios
        tinacoRepo.save(tinaco);
        bombaRepo.save(bomba);

        return mapToResponse(tinaco); // traerá bomba null
    }

    private TinacoResponse mapToResponse(Tinaco tinaco) {
        TinacoResponse res = new TinacoResponse();
        res.setId(tinaco.getId());
        res.setNombre(tinaco.getNombre());
        res.setCodigoIdentificador(tinaco.getDispositivo().getCodigoIdentificador());
        res.setAlturaMaximaCm(tinaco.getAlturaMaximaCm());
        res.setCapacidadLitros(tinaco.getCapacidadLitros());
        res.setUbicacion(tinaco.getUbicacion());
        res.setTieneBomba(tinaco.getBomba() != null);
        res.setEstado(tinaco.getEstado() == Tinaco.EstadoTinaco.ACTIVADO ? "ACTIVADO" : "DESACTIVADO");
        res.setDestinoAgua(tinaco.getDestinoAgua());
        return res;
    }

    private TinacoConBombaResponse mapToTinacoConBombaResponse(Tinaco tinaco) {
        TinacoConBombaResponse res = new TinacoConBombaResponse();

        TinacoResponse tinacoResponse = this.mapToResponse(tinaco);
        res.setTinacoResponse(tinacoResponse);

        BombaResponse bombaResponse = BombaService.mapToResponse(tinaco.getBomba());
        res.setBombaResponse(bombaResponse);

        /*
         * res.setBombaId(tinaco.getBomba().getId());
         * res.setNombreBomba(tinaco.getBomba().getNombre());
         * res.setCodigoIdentificadorBomba(tinaco.getBomba().getDispositivo().
         * getCodigoIdentificador());
         * res.setUbicacionBomba(tinaco.getBomba().getUbicacion());
         * res.setEncendidaBomba(tinaco.getBomba().getEncendida() == Encendida.TRUE);
         * res.setModoBomba(tinaco.getBomba().getModoBomba() == ModoBomba.AUTOMATICO ?
         * "AUTOMATICO" : "MANUAL");
         */

        return res;
    }
}