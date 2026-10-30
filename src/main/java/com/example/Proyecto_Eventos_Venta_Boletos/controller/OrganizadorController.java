package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.dto.MetricasOrganizadorDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.service.OrganizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        Organizador organizador = organizadorService.obtenerPorId(ORGANIZADOR_ID);
        List<Evento> eventos = organizadorService.obtenerEventosPorOrganizador(ORGANIZADOR_ID);

        model.addAttribute("organizador", organizador);
        model.addAttribute("eventos", eventos);
        model.addAttribute("inventario", organizadorService.obtenerInventario(ORGANIZADOR_ID));

        // Métricas/estimaciones calculadas en tiempo real a partir de los datos reales del organizador
        MetricasOrganizadorDTO metricas = organizadorService.calcularMetricas(ORGANIZADOR_ID);
        model.addAttribute("ingresosTotales", metricas.getIngresosTotales());
        model.addAttribute("boletosVendidos", metricas.getBoletosVendidos());
        model.addAttribute("capacidadTotal", metricas.getCapacidadTotal());
        model.addAttribute("boletosRestantes", metricas.getBoletosRestantes());
        model.addAttribute("porcentajeOcupacion", metricas.getPorcentajeOcupacion());
        model.addAttribute("ticketPromedio", metricas.getTicketPromedio());

        return "panelOrganizador";
    }

    // --- Gestión de Perfil ---
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        model.addAttribute("organizador", organizadorService.obtenerPorId(ORGANIZADOR_ID));
        return "perfilOrganizador";
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
        model.addAttribute("organizadorActual", organizadorService.obtenerPorId(ORGANIZADOR_ID));
        return "formEvento";
    }

    @PostMapping("/evento/guardar")
    public String guardarEvento(@ModelAttribute Evento evento,
                                 @RequestParam("imagenFile") MultipartFile imagenFile,
                                 @RequestParam(name = "tipoNombre", required = false) List<String> tipoNombre,
                                 @RequestParam(name = "tipoPrecio", required = false) List<Double> tipoPrecio,
                                 @RequestParam(name = "tipoAforo", required = false) List<Integer> tipoAforo,
                                 @RequestParam(name = "tipoDescripcion", required = false) List<String> tipoDescripcion) {
        organizadorService.procesarYGuardarEvento(evento, imagenFile, ORGANIZADOR_ID,
                tipoNombre, tipoPrecio, tipoAforo, tipoDescripcion);
        return "redirect:/organizador/panel?eventoCreado";
    }

    @PostMapping("/evento/eliminar")
    public String eliminarEvento(@RequestParam("id") Long eventoId) {
        organizadorService.eliminarEvento(eventoId, ORGANIZADOR_ID);
        return "redirect:/organizador/panel?eventoEliminado";
    }
}
