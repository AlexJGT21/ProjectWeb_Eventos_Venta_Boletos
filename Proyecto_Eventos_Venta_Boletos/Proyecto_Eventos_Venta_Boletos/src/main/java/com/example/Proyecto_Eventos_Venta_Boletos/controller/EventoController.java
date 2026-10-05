package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EventoController {

    @GetMapping({"/", "/index"})
    public String catalogoEventos(Model model, HttpSession session) {
        List<Map<String, Object>> eventos = obtenerListaEventos();

        model.addAttribute("tituloPagina", "Catálogo de Eventos Locales");
        model.addAttribute("eventos", eventos);

        // Guardamos información opcional en la sesión
        session.setAttribute("moduloActual", "Venta de Boletos Locales");

        return "index";
    }

    @GetMapping("/venta-boletos")
    public String ventaBoletos(@RequestParam(name = "id", required = false, defaultValue = "1") int eventoId,
                               Model model) {

        List<Map<String, Object>> eventos = obtenerListaEventos();

        // Buscamos el evento seleccionado por su ID (si no existe, toma el primero por defecto)
        Map<String, Object> eventoSeleccionado = eventos.stream()
                .filter(e -> (int) e.get("id") == eventoId)
                .findFirst()
                .orElse(eventos.get(0));

        model.addAttribute("evento", eventoSeleccionado);
        model.addAttribute("mensaje", "¡Selecciona tus asientos y confirma tu compra!");

        // Retorna la vista: src/main/resources/templates/ventaBoletos.html
        return "ventaBoletos";
    }

    private List<Map<String, Object>> obtenerListaEventos() {
        List<Map<String, Object>> lista = new ArrayList<>();

        lista.add(crearEvento(1, "Concierto de Rock Local", "Teatro del Pueblo", "15 de Octubre - 20:00 hrs", 350.00, true));
        lista.add(crearEvento(2, "Feria Gastronómica y Cultural", "Plaza Principal", "20 de Octubre - 12:00 hrs", 50.00, true));
        lista.add(crearEvento(3, "Obra de Teatro: La Llorona", "Foro Cultural", "31 de Octubre - 19:00 hrs", 200.00, false));
        lista.add(crearEvento(4, "Torneo de Fútbol Local", "Estadio Municipal", "05 de Noviembre - 16:00 hrs", 100.00, true));

        return lista;
    }

    private Map<String, Object> crearEvento(int id, String nombre, String lugar, String fecha, double precio, boolean disponible) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("id", id);
        evento.put("nombre", nombre);
        evento.put("lugar", lugar);
        evento.put("fecha", fecha);
        evento.put("precio", precio);
        evento.put("disponible", disponible);
        return evento;
    }
}