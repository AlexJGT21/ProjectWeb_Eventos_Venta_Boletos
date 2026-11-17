package com.example.Proyecto_Eventos_Venta_Boletos.config;

import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import com.example.Proyecto_Eventos_Venta_Boletos.service.OrganizadorService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Datos de la sesión disponibles en todas las vistas, para que el header y el menú
 * lateral (fragmentos/header.html) sepan si hay alguien con sesión y qué rol tiene.
 */
@ControllerAdvice
public class SesionAdvice {

    private final OrganizadorService organizadorService;
    private final AdminService adminService;

    public SesionAdvice(OrganizadorService organizadorService, AdminService adminService) {
        this.organizadorService = organizadorService;
        this.adminService = adminService;
    }

    private Authentication autenticacion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    private boolean tieneRol(String rol) {
        Authentication auth = autenticacion();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + rol).equals(a.getAuthority()));
    }

    @ModelAttribute("sesionActiva")
    public boolean sesionActiva() {
        return autenticacion() != null;
    }

    @ModelAttribute("esOrganizador")
    public boolean esOrganizador() {
        return tieneRol("ORGANIZADOR");
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        return tieneRol("ADMIN");
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
        String correo = auth.getName();
        if (esAdmin()) {
            return adminService.buscarNombrePorCorreo(correo).orElse(correo);
        }
        return organizadorService.buscarNombrePorCorreo(correo).orElse(correo);
    }
}
