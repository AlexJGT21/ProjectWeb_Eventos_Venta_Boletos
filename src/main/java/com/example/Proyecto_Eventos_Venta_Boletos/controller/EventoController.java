package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.response.EventoResponseDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    private static final List<EstadoEventoENUM> ESTADOS_OCULTOS =
            List.of(EstadoEventoENUM.CANCELADO, EstadoEventoENUM.PAUSADO);

    private final EventoRepository eventoRepository;

    public EventoController(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    // Catálogo público de eventos filtrado (devuelve JSON de DTOs)
    @GetMapping
    public ResponseEntity<List<EventoResponseDTO>> listarEventos(
            @RequestParam(name = "categoria", required = false, defaultValue = "Todos") String categoria,
            @RequestParam(name = "tiempo", required = false, defaultValue = "Todos") String tiempo,
            @RequestParam(name = "precioMax", required = false, defaultValue = "1500") Double precioMax,
            @RequestParam(name = "disponibilidad", required = false, defaultValue = "Todos") String disponibilidad,
            @RequestParam(name = "q", required = false) String q) {

        List<Evento> todosLosEventos = eventoRepository
                .findByFechaHoraEventoAfterAndEstadoEventoNotInOrderByFechaHoraEventoAsc(LocalDateTime.now(), ESTADOS_OCULTOS);
        String busqueda = normalizar(q);

        List<EventoResponseDTO> eventosFiltrados = todosLosEventos.stream()
                .filter(e -> categoria.equalsIgnoreCase("Todos") || e.getCategoria().equalsIgnoreCase(categoria))
                .filter(e -> precioMax >= 1500 || e.getPrecioMinimo() <= precioMax)
                .filter(e -> {
                    if ("disponibles".equalsIgnoreCase(disponibilidad)) return e.isDisponible();
                    if ("agotados".equalsIgnoreCase(disponibilidad)) return !e.isDisponible();
                    return true;
                })
                .filter(e -> {
                    if ("Hoy".equalsIgnoreCase(tiempo)) {
                        return e.getFechaHoraEvento().toLocalDate().isEqual(LocalDateTime.now().toLocalDate());
                    } else if ("ProximoMes".equalsIgnoreCase(tiempo)) {
                        return e.getFechaHoraEvento().isBefore(LocalDateTime.now().plusMonths(1));
                    }
                    return true;
                })
                .filter(e -> busqueda.isEmpty()
                        || contiene(e.getNombre(), busqueda)
                        || contiene(e.getDescripcion(), busqueda)
                        || contiene(e.getCategoria(), busqueda)
                        || contiene(e.getLugar(), busqueda))
                .map(this::aEventoResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventosFiltrados);
    }

    // Detalle individual de un evento por ID
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> obtenerEventoPorId(@PathVariable Long id) {
        return eventoRepository.findById(id)
                .map(this::aEventoResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Banner del evento
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> imagenEvento(@PathVariable Long id) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento == null || evento.getImagenEvento() == null || evento.getImagenEvento().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return respuestaImagen(evento.getImagenEvento());
    }

    // Croquis del recinto
    @GetMapping("/{id}/croquis")
    public ResponseEntity<byte[]> croquisEvento(@PathVariable Long id) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        if (evento == null || evento.getCroquis() == null || evento.getCroquis().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return respuestaImagen(evento.getCroquis());
    }

    private EventoResponseDTO aEventoResponseDTO(Evento evento) {
        return EventoResponseDTO.builder()
                .id(evento.getId())
                .nombre(evento.getNombre())
                .descripcion(evento.getDescripcion())
                .categoria(evento.getCategoria())
                .lugar(evento.getLugar())
                .fechaHoraEvento(evento.getFechaHoraEvento())
                .fechaCompleta(evento.getFechaCompleta())
                .fechaCorta(evento.getFechaCorta())
                .hora(evento.getHora())
                .precioMinimo(evento.getPrecioMinimo())
                .precioMaximo(evento.getPrecioMaximo())
                .disponible(evento.isDisponible())
                .estadoEvento(evento.getEstadoEvento() != null ? evento.getEstadoEvento().name() : null)
                .build();
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    private static boolean contiene(String texto, String busqueda) {
        return texto != null && normalizar(texto).contains(busqueda);
    }

    private static ResponseEntity<byte[]> respuestaImagen(byte[] imagen) {
        MediaType tipo = MediaType.IMAGE_JPEG;
        if (imagen.length > 3 && (imagen[0] & 0xFF) == 0x89 && imagen[1] == 'P') tipo = MediaType.IMAGE_PNG;
        else if (imagen.length > 3 && imagen[0] == 'G' && imagen[1] == 'I') tipo = MediaType.IMAGE_GIF;
        else if (imagen.length > 11 && imagen[0] == 'R' && imagen[8] == 'W') tipo = MediaType.parseMediaType("image/webp");
        return ResponseEntity.ok().contentType(tipo).body(imagen);
    }
}