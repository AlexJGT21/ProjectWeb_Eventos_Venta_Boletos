package com.example.Proyecto_Eventos_Venta_Boletos.config;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Administrador;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.AdministradorRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Le dice a Spring Security cómo buscar a un usuario al iniciar sesión: el correo es el usuario
 * y la contraseña debe estar guardada con BCrypt. Primero se busca en los administradores
 * (rol ADMIN) y después en los organizadores (rol ORGANIZADOR).
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final AdministradorRepository administradorRepository;
    private final OrganizadorRepository organizadorRepository;

    public UsuarioDetailsService(AdministradorRepository administradorRepository,
                                 OrganizadorRepository organizadorRepository) {
        this.administradorRepository = administradorRepository;
        this.organizadorRepository = organizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        String correoBuscado = correo == null ? "" : correo.trim();

        Optional<Administrador> administrador = administradorRepository.findByCorreo(correoBuscado);
        if (administrador.isPresent()) {
            return construirUsuario(administrador.get().getCorreo(), administrador.get().getContrasenia(), "ADMIN");
        }

        Organizador organizador = organizadorRepository.findByCorreo(correoBuscado)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese correo"));
        return construirUsuario(organizador.getCorreo(), organizador.getContrasenia(), "ORGANIZADOR");
    }

    private UserDetails construirUsuario(String correo, String contrasenia, String rol) {
        if (contrasenia == null || contrasenia.isBlank()) {
            throw new UsernameNotFoundException("El usuario no tiene contraseña registrada");
        }
        return User.withUsername(correo)
                .password(contrasenia)
                .roles(rol)
                .build();
    }
}
