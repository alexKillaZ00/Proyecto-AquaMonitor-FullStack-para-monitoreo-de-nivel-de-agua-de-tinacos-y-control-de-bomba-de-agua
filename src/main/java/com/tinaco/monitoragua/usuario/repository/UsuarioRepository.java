package com.tinaco.monitoragua.usuario.repository;

import com.tinaco.monitoragua.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.nombre = :nombre WHERE u.id = :id")
    void updateNombre(Long id, String nombre);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.passwordHash = :passwordHash WHERE u.id = :id")
    void updatePasswordHash(Long id, String passwordHash);
}