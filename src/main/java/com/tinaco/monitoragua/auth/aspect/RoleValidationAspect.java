package com.tinaco.monitoragua.auth.aspect;

import com.tinaco.monitoragua.auth.annotation.RequireRole;
import com.tinaco.monitoragua.auth.jwt.JwtService;
import com.tinaco.monitoragua.exception.AccesoDenegadoPorRoleException;
import com.tinaco.monitoragua.exception.AccessTokenNoEncontradoException;
import com.tinaco.monitoragua.usuario.entity.Usuario.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RoleValidationAspect {

    private final JwtService jwtService;

    public RoleValidationAspect(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Before("@annotation(requireRole)")
    public void validateRole(RequireRole requireRole) {
        HttpServletRequest request = getCurrentRequest();
        String jwt = extractAccessToken(request);

        if (jwt == null) {
            throw new AccessTokenNoEncontradoException("No se encontró token de acceso válido");
        }

        String userRole = jwtService.extractRole(jwt);
        Role requiredRole = requireRole.value();

        if (userRole == null || !userRole.equals(requiredRole.name())) {
            throw new AccesoDenegadoPorRoleException(
                    "Acceso denegado. Se requiere role: " + requiredRole.name());
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AccessTokenNoEncontradoException("No se pudo obtener el contexto de la petición");
        }
        return attributes.getRequest();
    }

    private String extractAccessToken(HttpServletRequest request) {
        // Primero intentar desde Authorization header
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Si no está en el header, intentar desde cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
