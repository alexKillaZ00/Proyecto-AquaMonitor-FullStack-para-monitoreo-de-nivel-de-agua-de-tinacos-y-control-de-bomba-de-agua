package com.tinaco.monitoragua.nivelAguaHistorial.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tinaco.monitoragua.nivelAguaHistorial.entity.NivelAguaHistorial;

public interface NivelAguaHistorialRepository extends JpaRepository<NivelAguaHistorial, Long> {
    Optional<NivelAguaHistorial> findByTinacoId(Long tinacoId);

    List<NivelAguaHistorial> findByTinacoIdAndFechaRegistroBetweenOrderByFechaRegistroAsc(Long tinacoId, LocalDateTime inicio, LocalDateTime fin);
}
