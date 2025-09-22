package com.tinaco.monitoragua.dispositivo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tinaco.monitoragua.dispositivo.entity.Dispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.EstadoDispositivo;
import com.tinaco.monitoragua.dispositivo.entity.Dispositivo.TipoDispositivo;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    Optional<Dispositivo> findByCodigoIdentificadorAndTipoDispositivo(String codigoIdentificador,
            TipoDispositivo tinaco);

    boolean existsByCodigoIdentificador(String codigoIdentificador);

    List<Dispositivo> findByEstado(EstadoDispositivo estadoDispositivo);

    List<Dispositivo> findByEstadoAndTipoDispositivo(EstadoDispositivo estado, TipoDispositivo tipoDispositivo);

}
