package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Consultas del catálogo público de eventos. El controlador (EventoController) ya no usa el
 * repositorio directamente: todo acceso a la base de datos pasa por aquí.
 */
@Service
public class EventoService {

    // Estados que no se muestran en el catálogo público
    private static final List<EstadoEventoENUM> ESTADOS_OCULTOS =
            List.of(EstadoEventoENUM.CANCELADO, EstadoEventoENUM.PAUSADO);

    // El máximo del control deslizante de precio significa "sin límite"
    public static final double PRECIO_SIN_LIMITE = 1500;

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    /** Eventos reales guardados por los organizadores (futuros y no cancelados/pausados), ordenados por fecha. */
    @Transactional(readOnly = true)
    public List<Evento> obtenerEventosPublicos() {
        return eventoRepository
                .findByFechaHoraEventoAfterAndEstadoEventoNotInOrderByFechaHoraEventoAsc(LocalDateTime.now(), ESTADOS_OCULTOS);
    }

    /** Filtrado dinámico del catálogo con Java Streams según los parámetros recibidos por GET. */
    @Transactional(readOnly = true)
    public List<Evento> buscarCatalogo(String categoria, String tiempo, Double precioMax, String disponibilidad, String q) {
        String busqueda = normalizar(q);
        double precioLimite = precioMax != null ? precioMax : PRECIO_SIN_LIMITE;

        return obtenerEventosPublicos().stream()
                // 1. Filtro por Categoría / Género
                .filter(e -> categoria == null || categoria.equalsIgnoreCase("Todos") || e.getCategoria().equalsIgnoreCase(categoria))
                // 2. Filtro por Precio Máximo (compara si el precio mínimo del evento está dentro del rango)
                .filter(e -> precioLimite >= PRECIO_SIN_LIMITE || e.getPrecioMinimo() <= precioLimite)
                // 3. Filtro por Disponibilidad
                .filter(e -> {
                    if ("disponibles".equalsIgnoreCase(disponibilidad)) return e.isDisponible();
                    if ("agotados".equalsIgnoreCase(disponibilidad)) return !e.isDisponible();
                    return true; // "Todos"
                })
                // 4. Filtro por Tiempo
                .filter(e -> cumpleFiltroTiempo(e.getFechaHoraEvento(), tiempo))
                // 5. Búsqueda del header (nombre, descripción, categoría o lugar)
                .filter(e -> busqueda.isEmpty()
                        || contiene(e.getNombre(), busqueda)
                        || contiene(e.getDescripcion(), busqueda)
                        || contiene(e.getCategoria(), busqueda)
                        || contiene(e.getLugar(), busqueda))
                .toList();
    }

    /** Carrusel: los 3 próximos eventos con boletos a la venta (o los 3 próximos si ninguno tiene). */
    @Transactional(readOnly = true)
    public List<Evento> obtenerDestacados() {
        List<Evento> eventos = obtenerEventosPublicos();
        List<Evento> conBoletos = eventos.stream().filter(Evento::isDisponible).limit(3).toList();
        return conBoletos.isEmpty() ? eventos.stream().limit(3).toList() : conBoletos;
    }

    @Transactional(readOnly = true)
    public Optional<Evento> buscarPorId(Long id) {
        return id == null ? Optional.empty() : eventoRepository.findById(id);
    }

    /** Busca un evento por su id cuando llega como texto desde los formularios o la URL. */
    @Transactional(readOnly = true)
    public Optional<Evento> buscarPorId(String id) {
        try {
            return id == null ? Optional.empty() : buscarPorId(Long.parseLong(id.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public boolean hayEventos() {
        return eventoRepository.count() > 0;
    }

    // "Hoy", "FinDeSemana" (de hoy al domingo, solo viernes a domingo), "ProximoMes" o "Todos"
    private static boolean cumpleFiltroTiempo(LocalDateTime fecha, String tiempo) {
        if (fecha == null) return true;
        LocalDate hoy = LocalDate.now();
        LocalDate dia = fecha.toLocalDate();
        if ("Hoy".equalsIgnoreCase(tiempo)) {
            return dia.isEqual(hoy);
        }
        if ("FinDeSemana".equalsIgnoreCase(tiempo)) {
            LocalDate domingo = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            DayOfWeek diaSemana = dia.getDayOfWeek();
            boolean esFinDeSemana = diaSemana == DayOfWeek.FRIDAY || diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY;
            return esFinDeSemana && !dia.isAfter(domingo);
        }
        if ("ProximoMes".equalsIgnoreCase(tiempo)) {
            return fecha.isBefore(LocalDateTime.now().plusMonths(1));
        }
        return true; // "Todos"
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
}
