package com.neobank.portfolio.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para la aplicación.
 * 
 * 🎓 PROPÓSITO:
 * Esta clase configura Spring Security para permitir acceso libre a todos
 * los endpoints. Es una configuración PERMISIVA para entornos de desarrollo
 * y demostración.
 * 
 * ⚠️ ADVERTENCIA:
 * Esta configuración NO es apta para producción. En un entorno real
 * se debe implementar autenticación (JWT, OAuth2, etc.) y autorización.
 * 
 * 🔑 ANOTACIONES:
 * - @Configuration: Indica que esta clase define Beans de Spring
 * - @EnableWebSecurity: Activa la configuración personalizada de Spring Security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * 🎓 ¿QUÉ ES UN SecurityFilterChain?
     * Es una cadena de filtros que intercepta TODAS las peticiones HTTP
     * antes de que lleguen a los controllers. Decide qué se permite y qué no.
     * 
     * 💡 ANALOGÍA:
     * Es como el guardia de seguridad en la entrada de un edificio.
     * Esta configuración le dice: "Deja pasar a todos sin preguntar".
     * 
     * @param http Constructor para configurar la seguridad HTTP
     * @return La cadena de filtros configurada
     * @throws Exception Si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ─────────────────────────────────────────────────────────────
            // CSRF (Cross-Site Request Forgery) - DESHABILITADO
            // ─────────────────────────────────────────────────────────────
            // 
            // 🎓 ¿QUÉ ES CSRF?
            // Es un ataque donde un sitio malicioso hace que tu navegador
            // envíe peticiones a otro sitio donde estás autenticado.
            // 
            // 💡 ¿POR QUÉ LO DESHABILITAMOS?
            // - GraphQL usa POST para todas las operaciones
            // - Las APIs stateless (sin sesión) no necesitan CSRF
            // - Simplifica el desarrollo y las pruebas
            // 
            // ⚠️ EN PRODUCCIÓN: Evaluar si se necesita según el caso de uso
            // ─────────────────────────────────────────────────────────────
            .csrf(csrf -> csrf.disable())
            
            // ─────────────────────────────────────────────────────────────
            // AUTORIZACIÓN DE PETICIONES - TODO PERMITIDO
            // ─────────────────────────────────────────────────────────────
            // 
            // 🎓 ¿QUÉ HACE ESTO?
            // .anyRequest() → Aplica a TODAS las URLs
            // .permitAll()  → Permite acceso sin autenticación
            // 
            // 💡 EQUIVALE A DECIR:
            // "Cualquier persona puede acceder a cualquier endpoint"
            // 
            // 🔒 EN PRODUCCIÓN SE VERÍA ASÍ:
            // .authorizeHttpRequests(auth -> auth
            //     .requestMatchers("/graphql").authenticated()
            //     .requestMatchers("/graphiql").hasRole("DEVELOPER")
            //     .anyRequest().denyAll()
            // )
            // ─────────────────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        // Construye y retorna la cadena de filtros configurada
        return http.build();
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO
 * =============================================================================
 * 
 * 📊 ESTA CONFIGURACIÓN:
 * ┌─────────────────────────────────────┐
 * │  CSRF Protection    │  ❌ Disabled  │
 * │  Authentication     │  ❌ None      │
 * │  Authorization      │  ✅ All Open  │
 * │  Production Ready   │  ❌ No        │
 * └─────────────────────────────────────┘
 * 
 * 🎯 IDEAL PARA:
 * - Desarrollo local
 * - Demos y presentaciones
 * - Pruebas de concepto
 * - Cursos y capacitación (como este!)
 * 
 * 🔒 PARA PRODUCCIÓN NECESITARÍAS:
 * - JWT Authentication
 * - Role-based Authorization
 * - CORS configurado
 * - Rate Limiting
 * - HTTPS obligatorio
 * 
 * =============================================================================
 */