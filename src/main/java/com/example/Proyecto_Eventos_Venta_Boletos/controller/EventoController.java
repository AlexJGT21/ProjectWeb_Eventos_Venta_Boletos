package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.dto.ResumenCompraDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Compra;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.service.CompraService;
import com.example.Proyecto_Eventos_Venta_Boletos.service.EventoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
public class EventoController {

    // El controlador no accede a la base de datos: todo pasa por los servicios
    private final EventoService eventoService;
    private final CompraService compraService;

    public EventoController(EventoService eventoService, CompraService compraService) {
        this.eventoService = eventoService;
        this.compraService = compraService;
    }

    @GetMapping("/user-login")
    public String login(@RequestParam(name = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Correo o contraseña incorrectos.");
        }
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
            @RequestParam(name = "q", required = false) String q,
            Model model,
            HttpSession session) {

        // Eventos reales filtrados por categoría, precio, disponibilidad, tiempo y búsqueda (ver EventoService)
        List<Evento> eventosFiltrados = eventoService.buscarCatalogo(categoria, tiempo, precioMax, disponibilidad, q);
        boolean hayBusqueda = q != null && !q.isBlank();

        // Atributos pasados a la plantilla HTML
        model.addAttribute("tituloPagina", hayBusqueda
                ? "Resultados para \"" + q.trim() + "\""
                : "Catálogo de Eventos Locales");
        model.addAttribute("eventos", eventosFiltrados);
        // Carrusel: los 3 próximos eventos con boletos a la venta (o los 3 próximos si ninguno tiene)
        model.addAttribute("destacados", eventoService.obtenerDestacados());

        // Mantenimiento del estado de los filtros seleccionados en el HTML
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("tiempoSeleccionado", tiempo);
        model.addAttribute("precioMaxSeleccionado", precioMax);
        model.addAttribute("disponibilidadSeleccionada", disponibilidad);

        // Guardamos información opcional en la sesión
        session.setAttribute("moduloActual", "Venta de Boletos Locales");

        return "index";
    }

    // Las imágenes viven en Cloudinary; estas rutas se conservan para los enlaces que ya existían
    // y solo redirigen a la URL pública de la imagen.
    @GetMapping("/eventos/{id}/imagen")
    public ResponseEntity<Void> imagenEvento(@PathVariable Long id) {
        return redirigirA(eventoService.buscarPorId(id).map(Evento::getImagenUrl).orElse(null));
    }

    // Croquis del recinto subido por el organizador (opcional)
    @GetMapping("/eventos/{id}/croquis")
    public ResponseEntity<Void> croquisEvento(@PathVariable Long id) {
        return redirigirA(eventoService.buscarPorId(id).map(Evento::getCroquisUrl).orElse(null));
    }

    private static ResponseEntity<Void> redirigirA(String url) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @GetMapping("/venta-boletos")
    public String ventaBoletos(@RequestParam(name = "id", required = false) String eventoId,
                               @RequestParam(name = "zona", required = false) String zona,
                               @RequestParam(name = "asientos", required = false) String asientos,
                               Model model) {

        // Buscamos el evento seleccionado por su ID
        Evento eventoSeleccionado = eventoService.buscarPorId(eventoId).orElse(null);
        if (eventoSeleccionado == null) {
            return "redirect:/eventos";
        }

        model.addAttribute("evento", eventoSeleccionado);
        // Zonas del mapa (o tipos de boleto) con precio, disponibilidad y asientos ocupados reales
        model.addAttribute("opciones", compraService.opcionesDeVenta(eventoSeleccionado));
        model.addAttribute("usaMapa", compraService.usaMapa(eventoSeleccionado));
        model.addAttribute("maximoPorCompra", CompraService.MAXIMO_POR_COMPRA);
        // Permite recargar o compartir la URL con la zona y los asientos ya elegidos
        model.addAttribute("zonaInicial", zona != null ? zona.trim().toUpperCase(Locale.ROOT) : "");
        model.addAttribute("asientosIniciales", asientos != null ? asientos.trim() : "");

        return "ventaBoletos";
    }

    // Paso 1: Recibir selección de mapa de asientos -> Ir al Formulario de Pago
    // Solo llega QUÉ se quiere comprar; el precio y el total los calcula CompraService.
    @PostMapping("/form-compra")
    public String procesarSeleccionAsientos(
            @RequestParam("eventoId") Long eventoId,
            @RequestParam(value = "tipoId", required = false) Long tipoId,
            @RequestParam(value = "asientos", required = false) String asientos,
            @RequestParam(value = "cantidad", required = false) Integer cantidad,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (tipoId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selecciona una zona o tipo de boleto.");
            return "redirect:/venta-boletos?id=" + eventoId;
        }

        ResumenCompraDTO resumen;
        try {
            resumen = compraService.cotizar(eventoId, tipoId, asientos, cantidad);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/venta-boletos?id=" + eventoId;
        }

        // Guardar la selección en la sesión HTTP (solo ids y asientos, no entidades)
        session.setAttribute("eventoId", eventoId);
        session.setAttribute("tipoId", tipoId);
        session.setAttribute("asientos", String.join(",", resumen.getAsientos()));
        session.setAttribute("cantidad", resumen.getCantidad());

        model.addAttribute("resumen", resumen);
        model.addAttribute("evento", resumen.getEvento());
        return "formCompraBoletos";
    }

    // Paso 2: Recibir los datos del cliente -> registrar la compra en la base de datos
    @PostMapping("/procesar-pago")
    public String procesarPago(
            @RequestParam("fullName") String fullName,
            @RequestParam("firstLastName") String firstLastName,
            @RequestParam(value = "secondLastName", required = false) String secondLastName,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email,
            @RequestParam(value = "paymentMethod", defaultValue = "Tarjeta de Crédito / Débito") String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Recuperar la selección guardada en el paso anterior
        Long eventoId = (Long) session.getAttribute("eventoId");
        Long tipoId = (Long) session.getAttribute("tipoId");
        String asientos = (String) session.getAttribute("asientos");
        Integer cantidad = (Integer) session.getAttribute("cantidad");

        if (eventoId == null || tipoId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu selección expiró, elige tus boletos de nuevo.");
            return "redirect:/eventos";
        }

        String nombreCompleto = fullName.trim() + " " + firstLastName.trim() +
                (secondLastName != null && !secondLastName.isBlank() ? " " + secondLastName.trim() : "");

        Compra compra;
        try {
            compra = compraService.registrarCompra(eventoId, tipoId, asientos, cantidad,
                    nombreCompleto, email, phone, paymentMethod);
        } catch (IllegalStateException e) {
            // Por ejemplo: alguien más compró el asiento mientras se llenaba el formulario
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/venta-boletos?id=" + eventoId;
        }

        // La selección ya se usó; se recuerdan las compras de esta sesión para poder ver su confirmación
        session.removeAttribute("tipoId");
        session.removeAttribute("asientos");
        session.removeAttribute("cantidad");
        @SuppressWarnings("unchecked")
        Set<Long> compras = (Set<Long>) session.getAttribute("comprasRealizadas");
        if (compras == null) {
            compras = new HashSet<>();
        }
        compras.add(compra.getId());
        session.setAttribute("comprasRealizadas", compras);

        // Redirección para que recargar la página no repita el cobro
        return "redirect:/confirmacion-compra?id=" + compra.getId();
    }

    // Paso 3: Confirmación con los datos guardados de la compra
    @GetMapping("/confirmacion-compra")
    public String confirmacionCompra(@RequestParam("id") Long compraId, Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<Long> compras = (Set<Long>) session.getAttribute("comprasRealizadas");
        // Solo quien hizo la compra (en esta sesión) puede ver sus datos
        if (compras == null || !compras.contains(compraId)) {
            return "redirect:/eventos";
        }
        model.addAllAttributes(compraService.datosConfirmacion(compraId));
        return "confirmacionPago";
    }

}
