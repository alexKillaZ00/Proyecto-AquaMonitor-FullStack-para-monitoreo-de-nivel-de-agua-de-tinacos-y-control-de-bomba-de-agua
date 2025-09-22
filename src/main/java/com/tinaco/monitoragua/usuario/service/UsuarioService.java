package com.tinaco.monitoragua.usuario.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinaco.monitoragua.auth.repository.RefreshTokenRepository;
import com.tinaco.monitoragua.exception.CredencialesInvalidasException;
import com.tinaco.monitoragua.usuario.dto.UsuarioDataResponse;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.usuario.repository.UsuarioRepository;
import com.tinaco.monitoragua.utils.ValidationsService;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ValidationsService validationsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ValidationsService validationsService,
                          RefreshTokenRepository refreshTokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.validationsService = validationsService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UsuarioDataResponse obtenerDatosDeUsuario(Usuario usuarioAutenticado) {
        UsuarioDataResponse usuarioDataResponse = new UsuarioDataResponse();
        usuarioDataResponse.setEmail(usuarioAutenticado.getEmail());
        usuarioDataResponse.setNombre(usuarioAutenticado.getNombre());
        return usuarioDataResponse;
    }

    public UsuarioDataResponse actualizarNombre(Usuario usuarioAutenticado, String nuevoNombre) {
        validationsService.validarNombre(nuevoNombre);
        
        usuarioAutenticado.setNombre(nuevoNombre.trim());
        usuarioRepository.updateNombre(usuarioAutenticado.getId(), nuevoNombre.trim());
        
        return obtenerDatosDeUsuario(usuarioAutenticado);
    }

    @Transactional
    public void actualizarPassword(Usuario usuarioAutenticado, String passwordActual, String nuevoPassword,
                                   String currentRefreshToken) {
        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, usuarioAutenticado.getPasswordHash())) {
            throw new CredencialesInvalidasException("La contraseña actual es incorrecta");
        }

        // Validar el nuevo password
        validationsService.validarPassword(nuevoPassword);

        // Actualizar la contraseña
        String nuevoPasswordHash = passwordEncoder.encode(nuevoPassword);
        usuarioAutenticado.setPasswordHash(nuevoPasswordHash);
        usuarioRepository.updatePasswordHash(usuarioAutenticado.getId(), nuevoPasswordHash);

        // Cerrar todas las demás sesiones excepto la actual (identificada por su refresh token)
        if (currentRefreshToken != null && !currentRefreshToken.isBlank()) {
            refreshTokenRepository.deleteByUsuarioAndTokenNot(usuarioAutenticado, currentRefreshToken);
        }
    }
}
