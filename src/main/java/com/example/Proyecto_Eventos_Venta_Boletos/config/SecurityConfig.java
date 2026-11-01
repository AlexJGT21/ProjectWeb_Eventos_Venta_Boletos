package com.example.Proyecto_Eventos_Venta_Boletos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // REQUISITO: Las contraseñas deben almacenarse cifradas con BCrypt, nunca en texto plano
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // REQUISITO: Configuración mediante SecurityFilterChain
    // El inicio de sesión se valida contra la tabla organizadores (ver OrganizadorDetailsService)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/organizador/**").hasRole("ORGANIZADOR") // Panel y eventos: solo organizadores
                        .requestMatchers("/admin/**").authenticated() // Protegiendo /admin/** para que requiera sesión
                        .anyRequest().permitAll() // Permite las rutas públicas (catálogo, venta, login, registro)
                )
                .formLogin(form -> form
                        .loginPage("/user-login") // Vista de login (login-user.html)
                        .loginProcessingUrl("/login") // La ruta a la que el formulario hace POST
                        .usernameParameter("email") // Nombres de los campos del formulario
                        .passwordParameter("password")
                        .defaultSuccessUrl("/organizador/panel") // Si venía de una página protegida, regresa a ella
                        .failureUrl("/user-login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index") // Redirección al catálogo tras salir
                        .invalidateHttpSession(true) // Cierre de sesión con invalidación de la sesión HTTP
                        .permitAll()
                );

        // NOTA: CSRF está habilitado por defecto en Spring Security en todas sus configuraciones base.
        // Al no agregar `.csrf(csrf -> csrf.disable())`, se cumple el requisito automáticamente.
        return http.build();
    }
}
