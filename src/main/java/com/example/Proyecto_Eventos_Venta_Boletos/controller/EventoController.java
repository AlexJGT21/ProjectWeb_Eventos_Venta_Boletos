package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoEstadoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    // NOTA: El panel del organizador (con métricas y datos reales) y el formulario de
    // creación de eventos ahora viven exclusivamente en OrganizadorController, bajo
    // /organizador/panel y /organizador/evento/nuevo, respaldados por OrganizadorService
    // y la base de datos. Las rutas duplicadas que existían aquí (/panel-organizador,
    // /formEvento, /guardar-evento) se eliminaron porque mostraban siempre datos vacíos
    // o ficticios y chocaban con las rutas reales del organizador.

    // Las rutas anteriores redirigen a las nuevas para no romper enlaces ni marcadores
    @GetMapping("/panel-organizador")
    public String redirigirPanelOrganizador() {
        return "redirect:/organizador/panel";
    }

    @GetMapping("/formEvento")
    public String redirigirFormEvento() {
        return "redirect:/organizador/evento/nuevo";
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
                        return e.getFechaHoraEvento().toLocalDate().isEqual(LocalDateTime.now().toLocalDate());
                    } else if ("ProximoMes".equalsIgnoreCase(tiempo)) {
                        return e.getFechaHoraEvento().isBefore(LocalDateTime.now().plusMonths(1));
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
                .filter(e -> String.valueOf(e.getId()).equals(eventoId))
                .findFirst()
                .orElse(eventos.get(0));

        model.addAttribute("evento", eventoSeleccionado);
        model.addAttribute("mensaje", "¡Selecciona tus asientos y confirma tu compra!");

        return "ventaBoletos";
    }

    private List<Evento> obtenerListaEventos() {
        List<Evento> lista = new ArrayList<>();

        // NOTA: Catálogo público de demostración, todavía no migrado a EventoRepository.
        // Los eventos reales creados por organizadores desde /organizador/evento/nuevo
        // sí se guardan en la base de datos (ver OrganizadorController/OrganizadorService).

        // Evento 1
        List<Boleto> boletos1 = List.of(
                Boleto.builder().id(1L).tipoNombre("General").precio(150.0).estado(BoletoEstadoENUM.DISPONIBLE).cantidad(400).cantidadVendida(120).build(),
                Boleto.builder().id(2L).tipoNombre("VIP").precio(300.0).estado(BoletoEstadoENUM.DISPONIBLE).cantidad(100).cantidadVendida(30).build()
        );
        lista.add(Evento.builder().id(1L).nombre("Noche Indie: Neon Waves Fest")
                .descripcion("La cumbre del synthwave y dream-pop latinoamericano con 6 bandas en vivo.")
                .categoria("Indie").fechaHoraEvento(LocalDateTime.now().plusDays(5))
                .lugar("Desierto Bar").capacidad(500)
                .estadoEvento(EstadoEventoENUM.ACTIVO).boletos(boletos1).build());

        // Evento 2
        List<Boleto> boletos2 = List.of(
                Boleto.builder().id(3L).tipoNombre("General").precio(300.0).estado(BoletoEstadoENUM.DISPONIBLE).cantidad(250).cantidadVendida(80).build(),
                Boleto.builder().id(4L).tipoNombre("VIP").precio(600.0).estado(BoletoEstadoENUM.DISPONIBLE).cantidad(50).cantidadVendida(10).build()
        );
        lista.add(Evento.builder().id(2L).nombre("Techno Underground: Dark Beats")
                .descripcion("Una noche inmersiva con DJs internacionales y un sistema de audio envolvente de 360°.")
                .categoria("Electrónica").fechaHoraEvento(LocalDateTime.now().plusDays(12))
                .lugar("Arena Metropolitana").capacidad(300)
                .estadoEvento(EstadoEventoENUM.ACTIVO).boletos(boletos2).build());

        // Evento 3
        List<Boleto> boletos3 = List.of(
                Boleto.builder().id(5L).tipoNombre("General").precio(220.0).estado(BoletoEstadoENUM.AGOTADO).cantidad(150).cantidadVendida(150).build(),
                Boleto.builder().id(6L).tipoNombre("VIP").precio(450.0).estado(BoletoEstadoENUM.AGOTADO).cantidad(50).cantidadVendida(50).build()
        );
        lista.add(Evento.builder().id(3L).nombre("Noche Acústica & Jazz Latino")
                .descripcion("Un recorrido íntimo por los clásicos del bolero y jazz moderno al aire libre.")
                .categoria("Rock").fechaHoraEvento(LocalDateTime.now().plusDays(25))
                .lugar("Teatro de la Ciudad").capacidad(200)
                .estadoEvento(EstadoEventoENUM.AGOTADO).boletos(boletos3).build());

        // Evento 4
        List<Boleto> boletos4 = List.of(
                Boleto.builder().id(7L).tipoNombre("General").precio(100.0).estado(BoletoEstadoENUM.DISPONIBLE).cantidad(1000).cantidadVendida(300).build()
        );
        lista.add(Evento.builder().id(4L).nombre("Torneo de Fútbol Local")
                .descripcion("Gran final del torneo municipal de verano.")
                .categoria("Urbano").fechaHoraEvento(LocalDateTime.now().plusDays(30))
                .lugar("Estadio Municipal").capacidad(1000)
                .estadoEvento(EstadoEventoENUM.ACTIVO).boletos(boletos4).build());

        return lista;
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
                .filter(e -> String.valueOf(e.getId()).equals(eventoId))
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
                .filter(e -> String.valueOf(e.getId()).equals(eventoId != null ? eventoId : "1"))
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

}




