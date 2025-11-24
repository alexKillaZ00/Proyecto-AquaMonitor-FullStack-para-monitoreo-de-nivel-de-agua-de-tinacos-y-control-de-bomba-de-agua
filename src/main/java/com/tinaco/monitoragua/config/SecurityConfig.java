package com.tinaco.monitoragua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                // Rutas públicas
                                                                "/api/auth/login", "/api/auth/register",
                                                                "/api/auth/password-reset/confirm",
                                                                "/api/auth/password-reset/request",
                                                                "/api/auth/verify-email",
                                                                // Archivos HTML accesibles sin autenticación
                                                                "/login.html", "/register.html", "/dashboard.html",
                                                                "/change-name.html",
                                                                "/change-password.html", "/tinacos-list.html",
                                                                "/tinaco-edit.html",
                                                                "/tinacos-deactivated-list.html", "/tinaco-add.html",
                                                                "/bombas-list.html",
                                                                "/bomba-edit.html", "/bombas-deactivated-list.html",
                                                                "/bomba-add.html",
                                                                "/tinacos-bombas.html", "/tinacos-sin-bomba.html",
                                                                "/bombas-sin-tinaco.html", "/reportes.html",
                                                                "/request-email.html", "/reset-password.html",
                                                                "/verify-email.html",
                                                                // Páginas de error
                                                                "/404.html",
                                                                // Ruta raíz para redirección
                                                                "/",
                                                                // Archivos estáticos (HTML, CSS, JS, imágenes, etc.)
                                                                "/css/**", "/js/**", "/assets/**", "/favicon.ico")
                                                .permitAll() // Acceso público, no requiere usuario autenticado
                                                .anyRequest().authenticated() // Todo lo demás requiere autenticación
                                )
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(customAuthenticationEntryPoint())
                                                .accessDeniedHandler(customAccessDeniedHandler()))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public AuthenticationEntryPoint customAuthenticationEntryPoint() {
                return (request, response, authException) -> {
                        String requestUri = request.getRequestURI();

                        // Si es una petición a la API
                        if (requestUri.startsWith("/api/")) {
                                // Responder con response entity con 401
                                response.setContentType("application/json");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("{\"message\": \"Authentication required\"}");
                        } else {
                                // Para recursos estáticos y páginas, redirigir directamente a login
                                response.sendRedirect("/404.html");
                        }
                };
        }

        @Bean
        public AccessDeniedHandler customAccessDeniedHandler() {
                return (request, response, accessDeniedException) -> {
                        String requestUri = request.getRequestURI();

                        // Si es una petición a la API
                        if (requestUri.startsWith("/api/")) {
                                // Establecer código de estado 403 para que el CustomErrorController lo maneje
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        } else {
                                response.sendRedirect("/login.html");
                        }
                };
        }

}