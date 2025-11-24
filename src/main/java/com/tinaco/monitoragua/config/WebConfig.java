package com.tinaco.monitoragua.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                    "http://localhost:*", 
                    "http://127.0.0.1:*", 
                    "http://192.168.1.*:*", // Permitir toda la red local 192.168.1.x
                    "http://192.168.*.*:*"   // Permitir cualquier red 192.168.x.x
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // IMPORTANTE: permitir cookies
                .maxAge(3600);
    }
}
