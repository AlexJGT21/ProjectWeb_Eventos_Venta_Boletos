package com.example.Proyecto_Eventos_Venta_Boletos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // REQUISITO: Las contraseñas deben almacenarse cifradas con BCrypt, nunca en texto plano
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // REQUISITO: Configuración mediante SecurityFilterChain
    // El inicio de sesión se valida contra administradores y organizadores (ver UsuarioDetailsService)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Administración: solo administradores
                        .requestMatchers("/organizador/**").hasRole("ORGANIZADOR") // Panel y eventos: solo organizadores
                        .anyRequest().permitAll() // Permite las rutas públicas (catálogo, venta, login, registro)
                )
                .formLogin(form -> form
                        .loginPage("/user-login") // Vista de login (login-user.html), la misma para todos los roles
                        .loginProcessingUrl("/login") // La ruta a la que el formulario hace POST
                        .usernameParameter("email") // Nombres de los campos del formulario
                        .passwordParameter("password")
                        .successHandler(redireccionPorRol())
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

    // Si venía de una página protegida, regresa a ella; si no, va al panel que le toca según su rol
    private AuthenticationSuccessHandler redireccionPorRol() {
        return (request, response, authentication) -> {
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            SavedRequestAwareAuthenticationSuccessHandler destino = new SavedRequestAwareAuthenticationSuccessHandler();
            destino.setDefaultTargetUrl(esAdmin ? "/admin/dashboard" : "/organizador/panel");
            destino.onAuthenticationSuccess(request, response, authentication);
        };
    }
}
