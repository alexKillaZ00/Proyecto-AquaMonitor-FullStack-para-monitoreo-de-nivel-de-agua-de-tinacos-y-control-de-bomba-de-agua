package com.tinaco.monitoragua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                //Rutas públicas
                                "/auth/login", "/auth/register", "/auth/password-reset/confirm",
                                 "/auth/password-reset/request", "/auth/verify-email",
                                //Archivos HTML accesibles sin autenticación
                                "/login.html", "/register.html", "/dashboard.html", "/change-name.html", 
                                "/change-password.html", "/tinacos-list.html", "/tinaco-edit.html", 
                                "/tinacos-deactivated-list.html", "/tinaco-add.html", "/bombas-list.html", 
                                "/bomba-edit.html", "/bombas-deactivated-list.html", "/bomba-add.html",
                                "/tinacos-bombas.html", "/tinacos-sin-bomba.html", "/bombas-sin-tinaco.html", "/reportes.html",
                                "/request-email.html", "/reset-password.html", "/verify-email.html",
                                 // Archivos estáticos (HTML, CSS, JS, imágenes, etc.)
                                "/css/**", "/js/**", "/assets/**")
                        .permitAll() // Acceso público, no requiere usuario autenticado
                        .anyRequest().authenticated() // Todo lo demás requiere autenticación
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                        response.getWriter().write("Unauthorized: Authentication required");
                })
        );
        return http.build();
    }

    /*@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir orígenes específicos (ajusta según tu frontend)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:8080", // Para desarrollo local
                "http://127.0.0.1:*", // Para desarrollo local
                "https://tu-dominio.com" // Para producción
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With"));

        // Permitir credenciales (cookies)
        configuration.setAllowCredentials(true);

        // Headers expuestos al frontend
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/
}