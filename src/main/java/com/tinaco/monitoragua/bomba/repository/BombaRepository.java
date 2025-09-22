package com.tinaco.monitoragua.bomba.repository;

import com.tinaco.monitoragua.bomba.entity.Bomba;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BombaRepository extends JpaRepository<Bomba, Long> {
    boolean existsByDispositivoId(Long dispositivoId);

    List<Bomba> findByUsuarioId(Long usuarioId);

    Optional<Bomba> findByDispositivo(Dispositivo dispositivo);

    Optional<Bomba> findByDispositivoCodigoIdentificador(String codigoIdentificador);
}
