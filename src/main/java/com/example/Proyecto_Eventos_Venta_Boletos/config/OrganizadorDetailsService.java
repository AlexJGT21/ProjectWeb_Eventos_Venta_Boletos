package com.example.Proyecto_Eventos_Venta_Boletos.config;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Le dice a Spring Security cómo buscar a un organizador al iniciar sesión:
 * el correo es el usuario y la contraseña debe estar guardada con BCrypt.
 */
@Service
public class OrganizadorDetailsService implements UserDetailsService {

    private final OrganizadorRepository organizadorRepository;

    public OrganizadorDetailsService(OrganizadorRepository organizadorRepository) {
        this.organizadorRepository = organizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Organizador organizador = organizadorRepository.findByCorreo(correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("No existe un organizador con ese correo"));

        if (organizador.getContrasenia() == null || organizador.getContrasenia().isBlank()) {
            throw new UsernameNotFoundException("El organizador no tiene contraseña registrada");
        }

        return User.withUsername(organizador.getCorreo())
                .password(organizador.getContrasenia())
                .roles("ORGANIZADOR")
                .build();
    }
}
