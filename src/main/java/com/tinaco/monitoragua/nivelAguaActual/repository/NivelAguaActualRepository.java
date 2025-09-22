package com.tinaco.monitoragua.nivelAguaActual.repository;

import com.tinaco.monitoragua.nivelAguaActual.entity.NivelAguaActual;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NivelAguaActualRepository extends JpaRepository<NivelAguaActual, Long> {
    Optional<NivelAguaActual> findByTinaco(Tinaco tinaco);
}
