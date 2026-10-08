package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadoENUM.EstadoEvento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    @GetMapping("/user-login")
    public String login() {
        return "login-user";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @GetMapping("/mis-boletos")
    public String misBoletos() {
        return "misBoletos";
    }

    @GetMapping("/formEvento")
    public String mostrarFormularioEvento(Model model) {
        model.addAttribute("evento", new Evento());
        return "formEvento";
    }

    @GetMapping({"/", "/index", "/eventos"})
    public String catalogoEventos(
            @RequestParam(name = "categoria", required = false, defaultValue = "Todos") String categoria,
            @RequestParam(name = "tiempo", required = false, defaultValue = "Todos") String tiempo,
            @RequestParam(name = "precioMax", required = false, defaultValue = "1500") Double precioMax,
            @RequestParam(name = "disponibilidad", required = false, defaultValue = "Todos") String disponibilidad,
            Model model,
            HttpSession session) {

        List<Evento> todosLosEventos = obtenerListaEventos();

        // Filtrado dinámico mediante Java Streams según los parámetros recibidos por GET
        List<Evento> eventosFiltrados = todosLosEventos.stream()
                // 1. Filtro por Categoría / Género
                .filter(e -> categoria.equalsIgnoreCase("Todos") || e.getCategoria().equalsIgnoreCase(categoria))
                // 2. Filtro por Precio Máximo (compara si el precio mínimo del evento está dentro del rango)
                .filter(e -> e.getPrecioMinimo() <= precioMax)
                // 3. Filtro por Disponibilidad
                .filter(e -> {
                    if ("disponibles".equalsIgnoreCase(disponibilidad)) return e.isDisponible();
                    if ("agotados".equalsIgnoreCase(disponibilidad)) return !e.isDisponible();
                    return true; // "Todos"
                })
                // 4. Filtro por Tiempo
                .filter(e -> {
                    if ("Hoy".equalsIgnoreCase(tiempo)) {
                        return e.getFechaHora().toLocalDate().isEqual(LocalDateTime.now().toLocalDate());
                    } else if ("ProximoMes".equalsIgnoreCase(tiempo)) {
                        return e.getFechaHora().isBefore(LocalDateTime.now().plusMonths(1));
                    }
                    return true; // "Todos" o "FinDeSemana"
                })
                .collect(Collectors.toList());

        // Atributos pasados a la plantilla HTML
        model.addAttribute("tituloPagina", "Catálogo de Eventos Locales");
        model.addAttribute("eventos", eventosFiltrados);

        // Mantenimiento del estado de los filtros seleccionados en el HTML
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("tiempoSeleccionado", tiempo);
        model.addAttribute("precioMaxSeleccionado", precioMax);
        model.addAttribute("disponibilidadSeleccionada", disponibilidad);

        // Guardamos información opcional en la sesión
        session.setAttribute("moduloActual", "Venta de Boletos Locales");

        return "index";
    }

    @GetMapping("/venta-boletos")
    public String ventaBoletos(@RequestParam(name = "id", required = false, defaultValue = "1") String eventoId,
                               Model model) {

        List<Evento> eventos = obtenerListaEventos();

        // Buscamos el evento seleccionado por su ID
        Evento eventoSeleccionado = eventos.stream()
                .filter(e -> e.getId().equals(eventoId))
                .findFirst()
                .orElse(eventos.get(0));

        model.addAttribute("evento", eventoSeleccionado);
        model.addAttribute("mensaje", "¡Selecciona tus asientos y confirma tu compra!");

        return "ventaBoletos";
    }

    private List<Evento> obtenerListaEventos() {
        List<Evento> lista = new ArrayList<>();

        // Evento 1
        List<Boleto> boletos1 = List.of(
                new Boleto(1, "General", 150.0f, "DISPONIBLE"),
                new Boleto(2, "VIP", 300.0f, "DISPONIBLE")
        );
        lista.add(new Evento("1", "Noche Indie: Neon Waves Fest",
                "La cumbre del synthwave y dream-pop latinoamericano con 6 bandas en vivo.",
                "Indie", LocalDateTime.now().plusDays(5), null, 500,
                "Luces & Sonido SONORA", EstadoEvento.ACTIVO, "Desierto Bar", boletos1));

        // Evento 2
        List<Boleto> boletos2 = List.of(
                new Boleto(3, "General", 300.0f, "DISPONIBLE"),
                new Boleto(4, "VIP", 600.0f, "DISPONIBLE")
        );
        lista.add(new Evento("2", "Techno Underground: Dark Beats",
                "Una noche inmersiva con DJs internacionales y un sistema de audio envolvente de 360°.",
                "Electrónica", LocalDateTime.now().plusDays(12), null, 300,
                "Sound System MX", EstadoEvento.ACTIVO, "Arena Metropolitana", boletos2));

        // Evento 3
        List<Boleto> boletos3 = List.of(
                new Boleto(5, "General", 220.0f, "AGOTADO"),
                new Boleto(6, "VIP", 450.0f, "AGOTADO")
        );
        lista.add(new Evento("3", "Noche Acústica & Jazz Latino",
                "Un recorrido íntimo por los clásicos del bolero y jazz moderno al aire libre.",
                "Rock", LocalDateTime.now().plusDays(25), null, 200,
                "Culturas del Norte", EstadoEvento.AGOTADO, "Teatro de la Ciudad", boletos3));

        // Evento 4
        List<Boleto> boletos4 = List.of(
                new Boleto(7, "General", 100.0f, "DISPONIBLE")
        );
        lista.add(new Evento("4", "Torneo de Fútbol Local",
                "Gran final del torneo municipal de verano.",
                "Urbano", LocalDateTime.now().plusDays(30), null, 1000,
                "Deportes Sonora", EstadoEvento.ACTIVO, "Estadio Municipal", boletos4));

        return lista;
    }

    @PostMapping("/guardar-evento")
    public String guardarEvento(Evento evento, 
                                @RequestParam("imagenFile") MultipartFile imagenFile,
                                @RequestParam(value = "croquisFile", required = false) MultipartFile croquisFile) {
        try {
            // Convertir la imagen principal a byte[] para la entidad Evento
            if (!imagenFile.isEmpty()) {
                evento.setImagen(imagenFile.getBytes());
            }

            // Aquí se puede procesar el croquis si se asigna a un objeto Recinto
            if (croquisFile != null && !croquisFile.isEmpty()) {
                byte[] croquisBytes = croquisFile.getBytes();
                // recinto.setCroquis(croquisBytes);
            }

            // Como aún no hay base de datos esto es para checar si funciona
            System.out.println("¡Evento recibido exitosamente!");
            System.out.println("Nombre: " + evento.getNombre());
            System.out.println("Categoría: " + evento.getCategoria());
            System.out.println("Fecha y Hora: " + evento.getFechaHora());
            System.out.println("Capacidad: " + evento.getCapacidad());
            System.out.println("Estado: " + evento.getEstadoEvento());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Redirige a la misma página
        return "redirect:/formEvento"; 
    }

    // Paso 1: Recibir selección de mapa de asientos -> Ir al Formulario de Pago
    @PostMapping("/form-compra")
    public String procesarSeleccionAsientos(
            @RequestParam("eventoId") String eventoId,
            @RequestParam("seccion") String seccion,
            @RequestParam("fila") String fila,
            @RequestParam("asientos") String asientos,
            @RequestParam("cantidad") Integer cantidad,
            @RequestParam("precioBase") Double precioBase,
            @RequestParam("totalFinal") Double totalFinal,
            Model model,
            HttpSession session) {

        Evento evento = obtenerListaEventos().stream()
                .filter(e -> e.getId().equals(eventoId))
                .findFirst()
                .orElse(null);

        // Guardar la selección en la sesión HTTP
        session.setAttribute("eventoId", eventoId);
        session.setAttribute("seccion", seccion);
        session.setAttribute("fila", fila);
        session.setAttribute("asientos", asientos);
        session.setAttribute("cantidad", cantidad);
        session.setAttribute("precioBase", precioBase);
        session.setAttribute("totalFinal", totalFinal);

        model.addAttribute("evento", evento);
        model.addAttribute("seccion", seccion);
        model.addAttribute("fila", fila);
        model.addAttribute("asientos", asientos);
        model.addAttribute("cantidad", cantidad);
        model.addAttribute("precioBase", precioBase);
        model.addAttribute("totalFinal", totalFinal);

        return "formCompraBoletos";
    }

    // Paso 2: Recibir los datos de la tarjeta y cliente -> Ir a Confirmación
    @PostMapping("/procesar-pago")
    public String procesarPago(
            @RequestParam("fullName") String fullName,
            @RequestParam("firstLastName") String firstLastName,
            @RequestParam(value = "secondLastName", required = false) String secondLastName,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email,
            @RequestParam(value = "eventoId", required = false) String eventoIdParam,
            @RequestParam(value = "paymentMethod", defaultValue = "Tarjeta de Crédito / Débito") String paymentMethod,
            Model model,
            HttpSession session) {

        // Recuperar información del boleto desde la sesión
        String eventoId = eventoIdParam != null ? eventoIdParam : (String) session.getAttribute("eventoId");
        String seccion = (String) session.getAttribute("seccion");
        String fila = (String) session.getAttribute("fila");
        String asientos = (String) session.getAttribute("asientos");
        Integer cantidad = (Integer) session.getAttribute("cantidad");
        Double totalFinal = (Double) session.getAttribute("totalFinal");

        Evento evento = obtenerListaEventos().stream()
                .filter(e -> e.getId().equals(eventoId != null ? eventoId : "1"))
                .findFirst()
                .orElse(obtenerListaEventos().get(0));

        String nombreCompleto = fullName + " " + firstLastName +
                (secondLastName != null && !secondLastName.isBlank() ? " " + secondLastName : "");

        Random random = new Random();
        String codigoPedido = "#KT-2026-" + (10000 + random.nextInt(90000));
        String codigoAcceso = "KT-VIP-" + (1000 + random.nextInt(9000)) + "-Q4";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm 'hrs'", Locale.forLanguageTag("es-ES"));
        String fechaHoraRegistro = LocalDateTime.now().format(formatter);

        // Atributos para la vista confirmacion-pago.html
        model.addAttribute("evento", evento);
        model.addAttribute("titularNombre", nombreCompleto);
        model.addAttribute("titularEmail", email);
        model.addAttribute("titularTelefono", phone);
        model.addAttribute("codigoPedido", codigoPedido);
        model.addAttribute("codigoAcceso", codigoAcceso);
        model.addAttribute("fechaCompra", fechaHoraRegistro);
        model.addAttribute("metodoPago", paymentMethod);

        model.addAttribute("seccion", seccion != null ? seccion : "Sección A • VIP Platino");
        model.addAttribute("fila", fila != null ? fila : "B");
        model.addAttribute("asientos", asientos != null ? asientos : "07 y 08");
        model.addAttribute("cantidadBoletos", cantidad != null ? cantidad : 2);
        model.addAttribute("totalPagado", totalFinal != null ? totalFinal : 3767.68);

        return "confirmacionPago";
    }

    private List<Evento> obtenerListaEvento() {
        List<Evento> lista = new ArrayList<>();

        List<Boleto> boletos1 = List.of(
                new Boleto(1, "General", 150.0f, "DISPONIBLE"),
                new Boleto(2, "VIP", 300.0f, "DISPONIBLE")
        );
        lista.add(new Evento("1", "INDIE / POST-PUNK: Midnight Echoes Tour",
                "La cumbre del synthwave y dream-pop latinoamericano con 6 bandas en vivo.",
                "Indie", LocalDateTime.now().plusDays(5), null, 500,
                "Luces & Sonido SONORA", EstadoEvento.ACTIVO, "Estadio GNP / Foro Sol", boletos1));

        List<Boleto> boletos2 = List.of(
                new Boleto(3, "General", 300.0f, "DISPONIBLE"),
                new Boleto(4, "VIP", 600.0f, "DISPONIBLE")
        );
        lista.add(new Evento("2", "Techno Underground: Dark Beats",
                "Una noche inmersiva con DJs internacionales y un sistema de audio envolvente de 360°.",
                "Electrónica", LocalDateTime.now().plusDays(12), null, 300,
                "Sound System MX", EstadoEvento.ACTIVO, "Arena Metropolitana", boletos2));

        return lista;
    }



}

