package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.service.OrganizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/organizador")
public class OrganizadorController {

    private final OrganizadorService organizadorService;
    // ID quemado temporalmente hasta que se configure la sesión con Spring Security
    private final Long ORGANIZADOR_ID = 1L;

    @Autowired
    public OrganizadorController(OrganizadorService organizadorService) {
        this.organizadorService = organizadorService;
    }

    // --- Panel Principal ---
    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("inventario", organizadorService.obtenerEventosPorOrganizador(ORGANIZADOR_ID));
        return "panelOrganizador";
    }

    // --- Gestión de Perfil ---
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        model.addAttribute("organizador", organizadorService.obtenerPorId(ORGANIZADOR_ID));
        return "perfilOrganizador"; // Necesitarás crear este HTML después
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute Organizador organizador) {
        organizadorService.actualizarPerfil(ORGANIZADOR_ID, organizador);
        return "redirect:/organizador/perfil?exito";
    }

    // --- Gestión de Eventos ---
    @GetMapping("/evento/nuevo")
    public String nuevoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        return "formEvento";
    }

    @PostMapping("/evento/guardar")
    public String guardarEvento(@ModelAttribute Evento evento,
                                @RequestParam("imagenFile") MultipartFile imagenFile) {
        organizadorService.procesarYGuardarEvento(evento, imagenFile, ORGANIZADOR_ID);
        return "redirect:/organizador/panel?eventoCreado";
    }

    @PostMapping("/evento/eliminar")
    public String eliminarEvento(@RequestParam("id") Long eventoId) {
        organizadorService.eliminarEvento(eventoId, ORGANIZADOR_ID);
        return "redirect:/organizador/panel?eventoEliminado";
    }
}