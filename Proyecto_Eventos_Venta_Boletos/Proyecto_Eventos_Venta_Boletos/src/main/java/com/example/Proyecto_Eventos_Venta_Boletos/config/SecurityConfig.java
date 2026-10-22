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
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll() // Exponiendo /admin/login como ruta pública
                        .requestMatchers("/admin/**").authenticated() // Protegiendo /admin/** para que requiera sesión
                        .anyRequest().permitAll() // Permite las rutas públicas fuera del panel de administración
                )
                .formLogin(form -> form
                        .loginPage("/admin/login") // Le indica a Spring Security cuál es la vista de login
                        .loginProcessingUrl("/admin/login") // La ruta a la que el formulario hará POST
                        .defaultSuccessUrl("/admin", true) // A dónde redirigir tras un login exitoso
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout") // Redirección al login tras salir
                        .invalidateHttpSession(true) // Cierre de sesión con invalidación de la sesión HTTP
                        .permitAll()
                );

        // NOTA: CSRF está habilitado por defecto en Spring Security en todas sus configuraciones base.
        // Al no agregar `.csrf(csrf -> csrf.disable())`, se cumple el requisito automáticamente.
        return http.build();
    }
}