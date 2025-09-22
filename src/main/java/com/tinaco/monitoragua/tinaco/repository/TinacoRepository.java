package com.tinaco.monitoragua.tinaco.repository;

import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TinacoRepository extends JpaRepository<Tinaco, Long> {

    List<Tinaco> findByUsuarioId(Long usuarioId);

    boolean existsByDispositivoId(Long id);

    Optional<Tinaco> findByDispositivo(Dispositivo dispositivo);
}