package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.ResumenCompraDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Compra;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.service.CompraService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    // Estados que no se muestran en el catálogo público
    private static final List<EstadoEventoENUM> ESTADOS_OCULTOS =
            List.of(EstadoEventoENUM.CANCELADO, EstadoEventoENUM.PAUSADO);

    private final EventoRepository eventoRepository;
    private final CompraService compraService;

    public EventoController(EventoRepository eventoRepository, CompraService compraService) {
        this.eventoRepository = eventoRepository;
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

        // Eventos reales guardados por los organizadores (futuros y no cancelados/pausados)
        List<Evento> todosLosEventos = eventoRepository
                .findByFechaHoraEventoAfterAndEstadoEventoNotInOrderByFechaHoraEventoAsc(LocalDateTime.now(), ESTADOS_OCULTOS);
        String busqueda = normalizar(q);

        // Filtrado dinámico mediante Java Streams según los parámetros recibidos por GET
        List<Evento> eventosFiltrados = todosLosEventos.stream()
                // 1. Filtro por Categoría / Género
                .filter(e -> categoria.equalsIgnoreCase("Todos") || e.getCategoria().equalsIgnoreCase(categoria))
                // 2. Filtro por Precio Máximo (compara si el precio mínimo del evento está dentro del rango)
                // (el máximo del control deslizante, 1500, significa "sin límite")
                .filter(e -> precioMax >= 1500 || e.getPrecioMinimo() <= precioMax)
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
                // 5. Búsqueda del header (nombre, descripción, categoría o lugar)
                .filter(e -> busqueda.isEmpty()
                        || contiene(e.getNombre(), busqueda)
                        || contiene(e.getDescripcion(), busqueda)
                        || contiene(e.getCategoria(), busqueda)
                        || contiene(e.getLugar(), busqueda))
                .collect(Collectors.toList());

        // Atributos pasados a la plantilla HTML
        model.addAttribute("tituloPagina", busqueda.isEmpty()
                ? "Catálogo de Eventos Locales"
                : "Resultados para \"" + q.trim() + "\"");
        model.addAttribute("eventos", eventosFiltrados);
        // Carrusel: los 3 próximos eventos con boletos a la venta (o los 3 próximos si ninguno tiene)
        List<Evento> conBoletos = todosLosEventos.stream().filter(Evento::isDisponible).limit(3).toList();
        model.addAttribute("destacados", conBoletos.isEmpty()
                ? todosLosEventos.stream().limit(3).toList()
                : conBoletos);

        // Mantenimiento del estado de los filtros seleccionados en el HTML
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("tiempoSeleccionado", tiempo);
        model.addAttribute("precioMaxSeleccionado", precioMax);
        model.addAttribute("disponibilidadSeleccionada", disponibilidad);

        // Guardamos información opcional en la sesión
        session.setAttribute("moduloActual", "Venta de Boletos Locales");

        return "index";
    }

    // Minúsculas y sin acentos, para que "musica" encuentre "Música"
    private static String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    private static boolean contiene(String texto, String busqueda) {
        return texto != null && normalizar(texto).contains(busqueda);
    }

    // Busca un evento real por su id (llega como texto desde los formularios)
    private Evento buscarEvento(String eventoId) {
        try {
            return eventoId == null ? null : eventoRepository.findById(Long.parseLong(eventoId.trim())).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Banner del evento guardado en la base de datos
    @GetMapping("/eventos/{id}/imagen")
    public ResponseEntity<byte[]> imagenEvento(@PathVariable Long id) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento == null || evento.getImagenEvento() == null || evento.getImagenEvento().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return respuestaImagen(evento.getImagenEvento());
    }

    // Croquis del recinto subido por el organizador (opcional)
    @GetMapping("/eventos/{id}/croquis")
    public ResponseEntity<byte[]> croquisEvento(@PathVariable Long id) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento == null || evento.getCroquis() == null || evento.getCroquis().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return respuestaImagen(evento.getCroquis());
    }

    // Detecta el formato por los primeros bytes del archivo
    private static ResponseEntity<byte[]> respuestaImagen(byte[] imagen) {
        MediaType tipo = MediaType.IMAGE_JPEG;
        if (imagen.length > 3 && (imagen[0] & 0xFF) == 0x89 && imagen[1] == 'P') tipo = MediaType.IMAGE_PNG;
        else if (imagen.length > 3 && imagen[0] == 'G' && imagen[1] == 'I') tipo = MediaType.IMAGE_GIF;
        else if (imagen.length > 11 && imagen[0] == 'R' && imagen[8] == 'W') tipo = MediaType.parseMediaType("image/webp");
        return ResponseEntity.ok().contentType(tipo).body(imagen);
    }

    @GetMapping("/venta-boletos")
    public String ventaBoletos(@RequestParam(name = "id", required = false) String eventoId,
                               @RequestParam(name = "zona", required = false) String zona,
                               @RequestParam(name = "asientos", required = false) String asientos,
                               Model model) {

        // Buscamos el evento seleccionado por su ID en la base de datos
        Evento eventoSeleccionado = buscarEvento(eventoId);
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




