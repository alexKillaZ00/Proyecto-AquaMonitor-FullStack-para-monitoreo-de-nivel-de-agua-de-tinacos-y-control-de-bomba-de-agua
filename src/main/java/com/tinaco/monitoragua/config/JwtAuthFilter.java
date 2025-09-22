package com.tinaco.monitoragua.config;

import com.tinaco.monitoragua.auth.jwt.JwtService;
import com.tinaco.monitoragua.auth.repository.RefreshTokenRepository;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.usuario.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String jwt = null;
        String userEmail = null;

        // Si es /auth/refresh, validar JWT y continuar
        if ("/auth/refresh".equals(path)) {

            // IMPORTANTE: Solo extraer de la cookie "refresh" (refresh token)
            jwt = extractRefreshTokenFromCookies(request);

            if (jwt == null) {
                // Si no hay token en la cookie, continuar sin autenticación
                filterChain.doFilter(request, response);
                return;
            }
            if (!isRefreshToken(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Solo se permiten refresh tokens para /auth/refresh");
                return;
            }
        } else {
            // Para requests que NO sean /auth/refresh, aplicar validación de refresh token primero
            // FLUJO DE SEGURIDAD: Verificar que el refresh token siga existiendo en BD
            String refreshToken = extractRefreshTokenFromCookies(request);
            
            // Si existe refresh token, verificar que existe en BD
            if (refreshToken != null) {
                boolean refreshTokenExistsInDB = refreshTokenRepository.findByToken(refreshToken).isPresent(); //Genera dos consultas a BD, se puede optimizar si es necesario
                if (!refreshTokenExistsInDB) {
                    // Si el refresh token no existe en BD (fue invalidado/expirado), 
                    // continuar sin autenticación para que Spring Security maneje la autorización
                    deleteCookie(response, "refresh"); // Eliminar cookie de refresh token
                    filterChain.doFilter(request, response);
                    return;
                }
                // Si el refresh token existe en BD, continúa con validación del access token
            } else {
                // Si no hay refresh token, continuar sin autenticación
                filterChain.doFilter(request, response);
                return;
            }
            
            // Proceder con la extracción del access token
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            } else {
                // IMPORTANTE: Solo extraer de la cookie "jwt" (access token)
                jwt = extractAccessTokenFromCookies(request);

                if (jwt == null) {
                    // Si no hay token en la cabecera ni en las cookies, continuar sin autenticación
                    filterChain.doFilter(request, response);
                    return;
                }
                // Si hay token en las cookies, pero es un refresh token, no permitir acceso a recursos protegidos
                if (isRefreshToken(jwt)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("No puedes usar refresh token para acceder a recursos protegidos");
                    return;
                }
            }
        }

        // Si no hay token en ningún lugar, continuar sin autenticación
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Token inválido o expirado
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("Invalid token: " + e.getMessage());
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Usuario usuario = usuarioRepository.findByEmail(userEmail).orElse(null);

            if (usuario != null) {
                boolean isTokenValid = false;

                // Para /auth/refresh, validar de forma especial
                if ("/auth/refresh".equals(path)) {
                    // Para refresh, solo verificar que es un refresh token válido estructuralmente
                    // No validar expiración aquí, eso se hará en el servicio AuthService, en el método refresh()
                    isTokenValid = isRefreshToken(jwt);
                } else {
                    // Para otras rutas, validación completa
                    isTokenValid = jwtService.isTokenValid(jwt, usuario);
                }

                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            usuario, null, null);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    // Método para extraer SOLO el access token
    private String extractAccessTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) { // Solo la cookie del access token
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Método para extraer SOLO el refresh token
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh".equals(cookie.getName())) { // Solo la cookie del access token
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Método para detectar si un token es refresh token
    private boolean isRefreshToken(String token) {
        try {
            String tokenType = jwtService.extractTokenType(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    // Método para excluir rutas del filtro
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Ajusta los prefijos a tus rutas
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/assets/")
                || path.startsWith("/login.html")
                || path.startsWith("/register.html")
                || path.startsWith("/dashboard.html")
                || path.startsWith("/change-name.html")
                || path.startsWith("/change-password.html")
                || path.startsWith("/tinacos-list.html")
                || path.startsWith("/tinaco-edit.html")
                || path.startsWith("/tinacos-deactivated-list.html")
                || path.startsWith("/tinaco-add.html")
                || path.startsWith("/bombas-list.html")
                || path.startsWith("/bomba-edit.html")
                || path.startsWith("/bombas-deactivated-list.html")
                || path.startsWith("/bomba-add.html")
                || path.startsWith("/tinacos-bombas.html")
                || path.startsWith("/tinacos-sin-bomba.html")
                || path.startsWith("/bombas-sin-tinaco.html")
                || path.startsWith("/reportes.html")
                || path.startsWith("/reset-password.html")
                || path.startsWith("/request-email.html")
                || path.startsWith("/verify-email.html")
                || path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.startsWith("/auth/password-reset/request")
                || path.equals("/auth/password-reset/confirm")
                || path.equals("/auth/verify-email") // <-- nuevo endpoint para verificar email
                || path.equals("/favicon.ico") // <-- excluimos el favicon
                || path.startsWith("/.well-known/"); // Excluir rutas de Chrome DevTools y similares
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
}