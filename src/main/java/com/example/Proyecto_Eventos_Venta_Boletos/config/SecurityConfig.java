package com.example.Proyecto_Eventos_Venta_Boletos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivar CSRF: Requerimiento fundamental en APIs REST que consumen JSON y JWT
                .csrf(csrf -> csrf.disable())

                // Política de sesiones sin estado (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Desactivar mecanismos de login por formulario HTML o login básico HTTP
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Reglas de autorización para la API REST
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos del catálogo, cotizaciones y compras
                        .requestMatchers("/api/v1/eventos/**").permitAll()
                        .requestMatchers("/api/v1/compras/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Endpoints protegidos por roles o autenticación JWT
                        .requestMatchers("/api/v1/organizador/**").hasRole("ORGANIZADOR")
                        .requestMatchers("/api/v1/admin/**").authenticated()

                        // Cualquier otra ruta requerirá autenticación
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}