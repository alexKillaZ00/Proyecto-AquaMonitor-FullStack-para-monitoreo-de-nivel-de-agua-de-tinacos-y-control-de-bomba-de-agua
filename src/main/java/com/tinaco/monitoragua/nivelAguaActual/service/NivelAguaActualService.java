package com.tinaco.monitoragua.nivelAguaActual.service;

import com.tinaco.monitoragua.bomba.entity.Bomba;
import com.tinaco.monitoragua.bomba.repository.BombaRepository;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;
import com.tinaco.monitoragua.dispositivo.repository.DispositivoRepository;
import com.tinaco.monitoragua.exception.DispositivoNoEncontradoException;
import com.tinaco.monitoragua.exception.RecursoNoPerteneceAlUsuarioException;
import com.tinaco.monitoragua.exception.TinacoNoEncontradoException;
import com.tinaco.monitoragua.nivelAguaActual.dto.NivelAguaActualRequest;
import com.tinaco.monitoragua.nivelAguaActual.dto.NivelAguaActualResponse;
import com.tinaco.monitoragua.nivelAguaActual.entity.NivelAguaActual;
import com.tinaco.monitoragua.nivelAguaActual.entity.NivelAguaActual.Desbordado;
import com.tinaco.monitoragua.nivelAguaActual.repository.NivelAguaActualRepository;
import com.tinaco.monitoragua.nivelAguaHistorial.entity.NivelAguaHistorial;
import com.tinaco.monitoragua.nivelAguaHistorial.repository.NivelAguaHistorialRepository;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import com.tinaco.monitoragua.tinaco.repository.TinacoRepository;
import com.tinaco.monitoragua.usuario.entity.Usuario;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class NivelAguaActualService {

        private final NivelAguaHistorialRepository nivelAguaHistorialRepository;
        private final TinacoRepository tinacoRepository;
        private final NivelAguaActualRepository nivelAguaActualRepository;
        private final DispositivoRepository dispositivoRepo;
        private final BombaRepository bombaRepo;

        public NivelAguaActualService(NivelAguaHistorialRepository nivelAguaHistorialRepository,
                        TinacoRepository tinacoRepository,
                        NivelAguaActualRepository nivelAguaActualRepository,
                        DispositivoRepository dispositivoRepository,
                        BombaRepository bombaRepository) {
                this.nivelAguaHistorialRepository = nivelAguaHistorialRepository;
                this.tinacoRepository = tinacoRepository;
                this.nivelAguaActualRepository = nivelAguaActualRepository;
                this.dispositivoRepo = dispositivoRepository;
                this.bombaRepo = bombaRepository;
        }

        public NivelAguaActualResponse registrarNivel(NivelAguaActualRequest request, Usuario usuarioAutenticado) {
                Dispositivo dispositivo = dispositivoRepo
                                .findByCodigoIdentificadorAndTipoDispositivo(request.getCodigoIdentificador(),
                                                TipoDispositivo.TINACO)
                                .orElseThrow(() -> new DispositivoNoEncontradoException(
                                                "Dispositivo no encontrado o no es un tinaco"));

                Tinaco tinaco = tinacoRepository.findByDispositivo(dispositivo)
                                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

                // Verificamos que el usuario autenticado tenga asociado el dispositivo
                if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                        throw new RecursoNoPerteneceAlUsuarioException("El tinaco no pertenece al usuario autenticado");
                }

                // Validar que el tinaco esta activo
                if (tinaco.getEstado() != Tinaco.EstadoTinaco.ACTIVADO) {
                        throw new IllegalArgumentException("El tinaco no está activado");
                }

                double porcentaje;
                if (request.getAlturaCm() <= 0) {
                        porcentaje = 0.0;
                } else if (request.getAlturaCm() >= tinaco.getAlturaMaximaCm()) {
                        porcentaje = 100.0;
                } else {
                        porcentaje = (request.getAlturaCm() / tinaco.getAlturaMaximaCm()) * 100.0;
                }
                boolean desbordado = request.getAlturaCm() > tinaco.getAlturaMaximaCm();

                NivelAguaActual nivelActual = nivelAguaActualRepository.findByTinaco(tinaco)
                                .orElse(new NivelAguaActual());

                nivelActual.setAlturaCm(request.getAlturaCm());
                nivelActual.setPorcentajeLlenado(Math.min(porcentaje, 100.0));
                nivelActual.setDesbordado(desbordado ? Desbordado.TRUE : Desbordado.FALSE);
                nivelActual.setTinaco(tinaco);
                nivelActual.setFechaActualizacion(LocalDateTime.now().withNano(0));

                // Guardar/actualizar estado actual
                NivelAguaActual nivelAguaActual = nivelAguaActualRepository.save(nivelActual);

                NivelAguaHistorial nivelHistorial = new NivelAguaHistorial();
                nivelHistorial.setAlturaCm(nivelActual.getAlturaCm());
                nivelHistorial.setPorcentajeLlenado(nivelActual.getPorcentajeLlenado());
                nivelHistorial.setDesbordado(nivelActual.getDesbordado());
                nivelHistorial.setTinaco(nivelActual.getTinaco());

                // Guardar en historial
                nivelAguaHistorialRepository.save(nivelHistorial);

                Bomba bomba = tinaco.getBomba();
                if (bomba != null && bomba.getEstado() == Bomba.EstadoBomba.ACTIVADA
                                && bomba.getModoBomba() == Bomba.ModoBomba.AUTOMATICO) {
                        // Lógica para encender o apagar la bomba según el nivel y los porcentajes
                        if (nivelActual.getPorcentajeLlenado() <= bomba.getPorcentajeEncender()) {
                                bomba.setEncendida(Bomba.Encendida.TRUE); // Encender bomba
                        } else if (nivelActual.getPorcentajeLlenado() >= bomba.getPorcentajeApagar()) {
                                bomba.setEncendida(Bomba.Encendida.FALSE); // Apagar bomba
                        }
                        // Si el nivel está entre los dos porcentajes, no cambiar el estado de la bomba
                        // Guardar el estado actualizado de la bomba
                        // Esto ya se hace en el método isBombaEncendidaPorCodigo, que es llamado por el
                        // ESP32 periódicamente

                        bombaRepo.save(bomba);
                }

                return mapToNivelAguaActualResponse(nivelAguaActual);
        }

        public NivelAguaActualResponse obtenerUltimoNivel(Long tinacoId, Usuario usuarioAutenticado) {
                // Verificar que el tinaco exista mediante el tinacoId
                Tinaco tinaco = tinacoRepository.findById(tinacoId)
                                .orElseThrow(() -> new TinacoNoEncontradoException("Tinaco no encontrado"));

                // Verificar que el tinaco pertenezca al usuario
                if (!tinaco.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                        throw new RecursoNoPerteneceAlUsuarioException(
                                        "El dispositivo no pertenece al usuario autenticado");
                }

                // Validar que el tinaco esta activo
                if (tinaco.getEstado() != Tinaco.EstadoTinaco.ACTIVADO) {
                        throw new IllegalArgumentException("El tinaco no está activado");
                }

                // NivelAguaActual nivelAguaActual =
                // nivelAguaActualRepository.findByTinaco(tinaco)
                // .orElseThrow(() -> new NivelActualNoEncontradoException("Nivel actual no
                // encontrado"));

                NivelAguaActual nivelAguaActual = nivelAguaActualRepository.findByTinaco(tinaco)
                                .orElseGet(() -> {
                                        NivelAguaActual nivel = new NivelAguaActual();
                                        nivel.setAlturaCm(0.0);
                                        nivel.setPorcentajeLlenado(0.0);
                                        nivel.setDesbordado(Desbordado.FALSE);
                                        return nivel;
                                });

                return mapToNivelAguaActualResponse(nivelAguaActual);
        }

        private NivelAguaActualResponse mapToNivelAguaActualResponse(NivelAguaActual nivelAguaActual) {

                NivelAguaActualResponse nivelAguaActualResponse = new NivelAguaActualResponse();
                nivelAguaActualResponse.setAlturaCm(nivelAguaActual.getAlturaCm());
                nivelAguaActualResponse.setPorcentajeLlenado(nivelAguaActual.getPorcentajeLlenado());
                nivelAguaActualResponse.setDesbordado(nivelAguaActual.getDesbordado() == Desbordado.TRUE);

                return nivelAguaActualResponse;
        }

}
