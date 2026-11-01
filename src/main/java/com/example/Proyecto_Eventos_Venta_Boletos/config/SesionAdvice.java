package com.example.Proyecto_Eventos_Venta_Boletos.config;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Datos de la sesión disponibles en todas las vistas, para que el header y el menú
 * lateral (fragmentos/header.html) sepan si hay alguien con sesión y si es organizador.
 */
@ControllerAdvice
public class SesionAdvice {

    private final OrganizadorRepository organizadorRepository;

    public SesionAdvice(OrganizadorRepository organizadorRepository) {
        this.organizadorRepository = organizadorRepository;
    }

    private Authentication autenticacion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    @ModelAttribute("sesionActiva")
    public boolean sesionActiva() {
        return autenticacion() != null;
    }

    @ModelAttribute("esOrganizador")
    public boolean esOrganizador() {
        Authentication auth = autenticacion();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ORGANIZADOR".equals(a.getAuthority()));
    }

    @ModelAttribute("usuarioCorreo")
    public String usuarioCorreo() {
        Authentication auth = autenticacion();
        return auth != null ? auth.getName() : null;
    }

    @ModelAttribute("usuarioNombre")
    public String usuarioNombre() {
        Authentication auth = autenticacion();
        if (auth == null) {
            return null;
        }
        return organizadorRepository.findByCorreo(auth.getName())
                .map(Organizador::getNombre)
                .orElse(auth.getName());
    }
}
