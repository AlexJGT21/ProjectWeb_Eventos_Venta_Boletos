package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoEstadoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.CurvaVentasDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.InventarioBoletoDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.MetricasOrganizadorDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Compra;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.BoletoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.CompraRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.util.MapaRecinto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class OrganizadorService {

    private final OrganizadorRepository organizadorRepository;
    private final EventoRepository eventoRepository;
    private final CompraRepository compraRepository;
    private final BoletoRepository boletoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OrganizadorService(OrganizadorRepository organizadorRepository, EventoRepository eventoRepository,
                              CompraRepository compraRepository, BoletoRepository boletoRepository,
                              PasswordEncoder passwordEncoder) {
        this.organizadorRepository = organizadorRepository;
        this.eventoRepository = eventoRepository;
        this.compraRepository = compraRepository;
        this.boletoRepository = boletoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- CRUD del Organizador ---
    public Organizador guardarOrganizador(Organizador organizador) {
        return organizadorRepository.save(organizador);
    }

    @Transactional(readOnly = true)
    public Organizador obtenerPorId(Long id) {
        return organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));
    }

    // Organizador que inició sesión (el correo es el usuario de Spring Security)
    @Transactional(readOnly = true)
    public Organizador obtenerPorCorreo(String correo) {
        return organizadorRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));
    }

    public Organizador actualizarPerfil(Long id, Organizador datosActualizados) {
        Organizador organizador = obtenerPorId(id);
        organizador.setNombre(datosActualizados.getNombre());
        organizador.setCorreo(datosActualizados.getCorreo());
        organizador.setTelefono(datosActualizados.getTelefono());

        if (datosActualizados.getContrasenia() != null && !datosActualizados.getContrasenia().isEmpty()) {
            organizador.setContrasenia(passwordEncoder.encode(datosActualizados.getContrasenia()));
        }
        return organizadorRepository.save(organizador);
    }

    public void eliminarOrganizador(Long id) {
        organizadorRepository.deleteById(id);
    }

    // --- Lógica de Eventos ---
    @Transactional(readOnly = true)
    public List<Evento> obtenerEventosPorOrganizador(Long organizadorId) {
        return eventoRepository.findByOrganizadorId(organizadorId);
    }

    /** Evento del organizador para editarlo (si no es suyo, no se puede abrir). */
    @Transactional(readOnly = true)
    public Evento obtenerEventoParaEditar(Long eventoId, Long organizadorId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (!evento.getOrganizador().getId().equals(organizadorId)) {
            throw new SecurityException("No tienes permiso para editar este evento");
        }
        evento.getTiposBoleto().size(); // carga los tipos de boleto para la vista
        return evento;
    }

    /** Fila de tipo de boleto tal como llega del Paso 3 del formulario. */
    private record FilaTipo(Long id, String nombre, double precio, int aforo, String descripcion) {}

    /**
     * Guarda un evento nuevo o, si trae id, actualiza uno existente del organizador junto con
     * sus tipos de boleto (Paso 3). Las reglas que protegen las ventas ya hechas lanzan
     * IllegalStateException con un mensaje para mostrar en el formulario.
     */
    @Transactional
    public Evento procesarYGuardarEvento(Evento datos,
                                          MultipartFile imagenFile,
                                          MultipartFile croquisFile,
                                          Long organizadorId,
                                          List<Long> tipoId,
                                          List<String> tipoNombre,
                                          List<Double> tipoPrecio,
                                          List<Integer> tipoAforo,
                                          List<String> tipoDescripcion) {
        boolean edicion = datos.getId() != null;
        Evento evento;
        if (edicion) {
            evento = obtenerEventoParaEditar(datos.getId(), organizadorId);
            evento.setNombre(datos.getNombre());
            evento.setDescripcion(datos.getDescripcion());
            evento.setCategoria(datos.getCategoria());
            evento.setLugar(datos.getLugar());
            evento.setFechaHoraEvento(datos.getFechaHoraEvento());
            evento.setCapacidad(datos.getCapacidad());
            if (datos.getEstadoEvento() != null) {
                evento.setEstadoEvento(datos.getEstadoEvento());
            }
        } else {
            evento = datos;
            evento.setOrganizador(obtenerPorId(organizadorId));
            if (evento.getEstadoEvento() == null) {
                evento.setEstadoEvento(EstadoEventoENUM.PROGRAMADO);
            }
        }
        if (evento.getDescripcion() == null) {
            evento.setDescripcion("");
        }

        // Con boletos vendidos no se cambia la modalidad (los asientos vendidos dependen de ella)
        String modalidad = "NUMERADO".equals(datos.getModalidadAsientos()) ? "NUMERADO" : "GENERAL";
        if (edicion && evento.isConVentas() && evento.getModalidadAsientos() != null
                && !modalidad.equals(evento.getModalidadAsientos())) {
            throw new IllegalStateException("No puedes cambiar la modalidad de asientos porque el evento ya tiene boletos vendidos.");
        }
        evento.setModalidadAsientos(modalidad);

        try {
            if (imagenFile != null && !imagenFile.isEmpty()) {
                evento.setImagenEvento(imagenFile.getBytes());
            }
            if (croquisFile != null && !croquisFile.isEmpty()) {
                evento.setCroquis(croquisFile.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar las imágenes del evento", e);
        }

        List<FilaTipo> filas = leerFilas(tipoId, tipoNombre, tipoPrecio, tipoAforo, tipoDescripcion);
        if ("NUMERADO".equals(modalidad)) {
            filas = ajustarAlMapa(filas);
        }
        sincronizarTipos(evento, filas);

        // Si no se capturó la capacidad, se usa el aforo de los boletos
        if (evento.getCapacidad() == null || evento.getCapacidad() <= 0) {
            evento.setCapacidad(filas.stream().mapToInt(FilaTipo::aforo).sum());
        }
        return eventoRepository.save(evento);
    }

    private List<FilaTipo> leerFilas(List<Long> tipoId, List<String> tipoNombre, List<Double> tipoPrecio,
                                     List<Integer> tipoAforo, List<String> tipoDescripcion) {
        List<FilaTipo> filas = new ArrayList<>();
        if (tipoNombre == null) {
            return filas;
        }
        for (int i = 0; i < tipoNombre.size(); i++) {
            String nombre = tipoNombre.get(i);
            if (nombre == null || nombre.isBlank()) {
                continue; // fila vacía, se ignora
            }
            Long id = (tipoId != null && i < tipoId.size()) ? tipoId.get(i) : null;
            Double precio = (tipoPrecio != null && i < tipoPrecio.size()) ? tipoPrecio.get(i) : null;
            Integer aforo = (tipoAforo != null && i < tipoAforo.size()) ? tipoAforo.get(i) : null;
            String descripcion = (tipoDescripcion != null && i < tipoDescripcion.size()) ? tipoDescripcion.get(i) : null;
            filas.add(new FilaTipo(id, nombre.trim(), precio != null ? Math.max(precio, 0) : 0.0,
                    aforo != null ? Math.max(aforo, 0) : 0, descripcion));
        }
        return filas;
    }

    /**
     * Mapa numerado: cada tipo de boleto es una zona de la plantilla del recinto. Las zonas con
     * asientos tienen el aforo fijo del mapa (no se puede vender más asientos de los que existen).
     */
    private List<FilaTipo> ajustarAlMapa(List<FilaTipo> filas) {
        List<FilaTipo> ajustadas = new ArrayList<>();
        java.util.Set<String> zonasUsadas = new java.util.HashSet<>();
        for (FilaTipo fila : filas) {
            MapaRecinto.Zona zona = MapaRecinto.porNombreDeBoleto(fila.nombre())
                    .orElseThrow(() -> new IllegalStateException("«" + fila.nombre() + "» no es una zona del mapa del recinto."));
            if (!zonasUsadas.add(zona.codigo())) {
                throw new IllegalStateException("La zona «" + zona.nombre() + "» está repetida.");
            }
            int aforo = zona.isNumerada() ? zona.getTotalAsientos() : fila.aforo();
            ajustadas.add(new FilaTipo(fila.id(), zona.nombre(), fila.precio(), aforo, fila.descripcion()));
        }
        return ajustadas;
    }

    /** Crea, actualiza o elimina los tipos de boleto del evento según lo que llegó del formulario. */
    private void sincronizarTipos(Evento evento, List<FilaTipo> filas) {
        if (evento.getBoletos() == null) {
            evento.setBoletos(new ArrayList<>());
        }
        java.util.Map<Long, Boleto> existentes = new java.util.HashMap<>();
        for (Boleto tipo : evento.getTiposBoleto()) {
            existentes.put(tipo.getId(), tipo);
        }

        java.util.Set<Long> conservados = new java.util.HashSet<>();
        for (FilaTipo fila : filas) {
            Boleto tipo = fila.id() != null ? existentes.get(fila.id()) : null;
            if (tipo == null) {
                evento.getBoletos().add(Boleto.builder()
                        .tipoNombre(fila.nombre())
                        .descripcion(fila.descripcion())
                        .precio(fila.precio())
                        .cantidad(fila.aforo())
                        .cantidadVendida(0)
                        .estado(fila.aforo() > 0 ? BoletoEstadoENUM.DISPONIBLE : BoletoEstadoENUM.AGOTADO)
                        .evento(evento)
                        .build());
                continue;
            }

            conservados.add(tipo.getId());
            int vendidos = tipo.getCantidadVendida() != null ? tipo.getCantidadVendida() : 0;
            if (vendidos > 0 && !MapaRecinto.normalizar(tipo.getTipoNombre()).equals(MapaRecinto.normalizar(fila.nombre()))) {
                throw new IllegalStateException("No puedes cambiar el nombre de «" + tipo.getTipoNombre()
                        + "» porque ya tiene " + vendidos + " boletos vendidos.");
            }
            if (fila.aforo() < vendidos) {
                throw new IllegalStateException("El aforo de «" + tipo.getTipoNombre() + "» no puede ser menor a los "
                        + vendidos + " boletos que ya se vendieron.");
            }
            tipo.setTipoNombre(fila.nombre());
            tipo.setDescripcion(fila.descripcion());
            tipo.setPrecio(fila.precio()); // el precio nuevo aplica a las ventas siguientes
            tipo.setCantidad(fila.aforo());
            if (tipo.getRestantes() == 0) {
                tipo.setEstado(BoletoEstadoENUM.AGOTADO);
            } else if (tipo.getEstado() == BoletoEstadoENUM.AGOTADO || tipo.getEstado() == null) {
                tipo.setEstado(BoletoEstadoENUM.DISPONIBLE);
            }
        }

        // Tipos que se quitaron en el formulario: solo se borran si no tienen ventas
        for (Boleto tipo : existentes.values()) {
            if (conservados.contains(tipo.getId())) continue;
            int vendidos = tipo.getCantidadVendida() != null ? tipo.getCantidadVendida() : 0;
            if (vendidos > 0) {
                throw new IllegalStateException("No puedes eliminar «" + tipo.getTipoNombre()
                        + "» porque ya tiene " + vendidos + " boletos vendidos.");
            }
            evento.getBoletos().remove(tipo);
            boletoRepository.delete(tipo);
        }
    }

    public void eliminarEvento(Long eventoId, Long organizadorId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // Regla de negocio: Validar que el evento pertenezca a este organizador
        if (evento.getOrganizador().getId().equals(organizadorId)) {
            eventoRepository.delete(evento);
        } else {
            throw new SecurityException("No tienes permiso para eliminar este evento");
        }
    }

    // --- Métricas e Inventario (Panel del Organizador) ---

    /** Eventos del organizador; si se indica eventoId, solo ese evento (filtro "Evento Activo" del panel). */
    private List<Evento> eventosFiltrados(Long organizadorId, Long eventoId) {
        List<Evento> eventos = eventoRepository.findByOrganizadorId(organizadorId);
        if (eventoId == null) {
            return eventos;
        }
        return eventos.stream().filter(e -> e.getId().equals(eventoId)).toList();
    }

    /**
     * Construye la tabla de "Gestión de Boletos por Tipo y Aforo" a partir de los
     * Boleto reales de todos los eventos del organizador. Si todavía no hay ventas,
     * vendidos/ingresos aparecerán en 0 (dato real, no inventado).
     */
    @Transactional(readOnly = true)
    public List<InventarioBoletoDTO> obtenerInventario(Long organizadorId, Long eventoId) {
        List<Evento> eventos = eventosFiltrados(organizadorId, eventoId);
        List<InventarioBoletoDTO> inventario = new ArrayList<>();

        for (Evento evento : eventos) {
            // Solo los tipos de boleto; los boletos vendidos ya están contados en cantidadVendida
            for (Boleto boleto : evento.getTiposBoleto()) {
                int capacidad = boleto.getCantidad() != null ? boleto.getCantidad() : 0;
                int vendidos = boleto.getCantidadVendida() != null ? boleto.getCantidadVendida() : 0;
                double precio = boleto.getPrecio() != null ? boleto.getPrecio() : 0.0;

                double porcentaje = capacidad > 0 ? (vendidos * 100.0 / capacidad) : 0.0;
                double ingreso = vendidos * precio;

                String estado;
                if (capacidad > 0 && vendidos >= capacidad) {
                    estado = "AGOTADO";
                } else if (porcentaje >= 80.0) {
                    estado = "POCOS BOLETOS";
                } else {
                    estado = "DISPONIBLE";
                }

                String nombreTipo = boleto.getTipoNombre() != null ? boleto.getTipoNombre() : "General";
                String icono = nombreTipo.toUpperCase().contains("VIP") ? "fas fa-star" : "fas fa-ticket-alt";

                inventario.add(InventarioBoletoDTO.builder()
                        .eventoId(evento.getId())
                        // Con todos los eventos se antepone el nombre del evento para distinguirlos
                        .tipoBoleto(eventoId == null ? evento.getNombre() + " • " + nombreTipo : nombreTipo)
                        .descripcion(boleto.getDescripcion() != null ? boleto.getDescripcion() : "")
                        .iconoCss(icono)
                        .precio(precio)
                        .vendidos(vendidos)
                        .capacidad(capacidad)
                        .porcentaje(porcentaje)
                        .ingresoGenerado(ingreso)
                        .estado(estado)
                        .build());
            }
        }
        return inventario;
    }

    /**
     * Calcula las métricas/estimaciones del panel (ingresos, ocupación, ticket promedio, etc.)
     * en tiempo real a partir de los eventos y boletos reales del organizador.
     */
    @Transactional(readOnly = true)
    public MetricasOrganizadorDTO calcularMetricas(Long organizadorId, Long eventoId) {
        List<Evento> eventos = eventosFiltrados(organizadorId, eventoId);

        int capacidadTotal = 0;
        int boletosVendidos = 0;
        double ingresosTotales = 0.0;

        for (Evento evento : eventos) {
            capacidadTotal += evento.getCapacidad() != null ? evento.getCapacidad() : 0;

            for (Boleto boleto : evento.getTiposBoleto()) {
                int vendidos = boleto.getCantidadVendida() != null ? boleto.getCantidadVendida() : 0;
                double precio = boleto.getPrecio() != null ? boleto.getPrecio() : 0.0;
                boletosVendidos += vendidos;
                ingresosTotales += vendidos * precio;
            }
        }

        int boletosRestantes = Math.max(capacidadTotal - boletosVendidos, 0);
        double porcentajeOcupacion = capacidadTotal > 0 ? (boletosVendidos * 100.0 / capacidadTotal) : 0.0;
        double ticketPromedio = boletosVendidos > 0 ? (ingresosTotales / boletosVendidos) : 0.0;

        return MetricasOrganizadorDTO.builder()
                .ingresosTotales(ingresosTotales)
                .boletosVendidos(boletosVendidos)
                .capacidadTotal(capacidadTotal)
                .boletosRestantes(boletosRestantes)
                .porcentajeOcupacion(porcentajeOcupacion)
                .ticketPromedio(ticketPromedio)
                .build();
    }

    /** Días que faltan para el próximo evento (o null si no hay eventos futuros). */
    @Transactional(readOnly = true)
    public Long diasParaProximoEvento(Long organizadorId, Long eventoId) {
        LocalDateTime ahora = LocalDateTime.now();
        return eventosFiltrados(organizadorId, eventoId).stream()
                .map(Evento::getFechaHoraEvento)
                .filter(fecha -> fecha != null && fecha.isAfter(ahora))
                .min(Comparator.naturalOrder())
                .map(fecha -> ChronoUnit.DAYS.between(LocalDate.now(), fecha.toLocalDate()))
                .orElse(null);
    }

    /** Tipo de boleto que más ingresos ha generado (vacío si todavía no hay ventas). */
    @Transactional(readOnly = true)
    public Optional<InventarioBoletoDTO> tipoConMasIngresos(Long organizadorId, Long eventoId) {
        return obtenerInventario(organizadorId, eventoId).stream()
                .filter(fila -> fila.getIngresoGenerado() > 0)
                .max(Comparator.comparingDouble(InventarioBoletoDTO::getIngresoGenerado));
    }

    /**
     * Boletos vendidos por día en los últimos {@code dias} días, a partir de las compras guardadas,
     * convertidos a trazos SVG para la gráfica "Ritmo de Venta de Boletos por Día".
     */
    @Transactional(readOnly = true)
    public CurvaVentasDTO curvaVentas(Long organizadorId, Long eventoId, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(dias - 1L);
        int[] ventasPorDia = new int[dias];

        for (Compra compra : compraRepository.buscarVentasDesde(organizadorId, eventoId, inicio.atStartOfDay())) {
            if (compra.getFechaHora() == null) continue;
            int indice = (int) ChronoUnit.DAYS.between(inicio, compra.getFechaHora().toLocalDate());
            if (indice < 0 || indice >= dias) continue;
            int boletos = compra.getTotalVendidos() != null ? compra.getTotalVendidos()
                    : (compra.getBoletos() != null ? compra.getBoletos().size() : 0);
            ventasPorDia[indice] += boletos;
        }

        int maximo = 0;
        int total = 0;
        for (int ventas : ventasPorDia) {
            maximo = Math.max(maximo, ventas);
            total += ventas;
        }

        // viewBox 800 x 250, con 20px de margen arriba y abajo
        StringBuilder linea = new StringBuilder();
        double paso = dias > 1 ? 800.0 / (dias - 1) : 800.0;
        for (int i = 0; i < dias; i++) {
            double x = i * paso;
            double y = 230 - (maximo > 0 ? ventasPorDia[i] * 210.0 / maximo : 0);
            linea.append(i == 0 ? "M" : " L").append(String.format(Locale.ROOT, "%.1f,%.1f", x, y));
        }
        String area = linea + " L800,250 L0,250 Z";

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("es-MX"));
        List<String> etiquetas = List.of(inicio.format(formato), inicio.plusDays(dias / 2).format(formato), hoy.format(formato));

        return new CurvaVentasDTO(linea.toString(), area, total, maximo, dias, etiquetas);
    }

    /** Reporte CSV del inventario y las métricas (abre directo en Excel; incluye BOM para los acentos). */
    @Transactional(readOnly = true)
    public String generarReporteCsv(Long organizadorId, Long eventoId) {
        MetricasOrganizadorDTO metricas = calcularMetricas(organizadorId, eventoId);
        StringBuilder csv = new StringBuilder("﻿");
        csv.append("Reporte de ventas Konticket,").append(LocalDate.now()).append('\n');
        csv.append("Ingresos totales (MXN),").append(String.format(Locale.ROOT, "%.2f", metricas.getIngresosTotales())).append('\n');
        csv.append("Boletos vendidos,").append(metricas.getBoletosVendidos()).append('\n');
        csv.append("Capacidad total,").append(metricas.getCapacidadTotal()).append('\n');
        csv.append("Ocupación (%),").append(String.format(Locale.ROOT, "%.1f", metricas.getPorcentajeOcupacion())).append('\n');
        csv.append("Ticket promedio (MXN),").append(String.format(Locale.ROOT, "%.2f", metricas.getTicketPromedio())).append("\n\n");

        csv.append("Tipo de boleto,Descripción,Precio unitario (MXN),Vendidos,Capacidad,Ocupación (%),Ingreso generado (MXN),Estado\n");
        for (InventarioBoletoDTO fila : obtenerInventario(organizadorId, eventoId)) {
            csv.append(celdaCsv(fila.getTipoBoleto())).append(',')
               .append(celdaCsv(fila.getDescripcion())).append(',')
               .append(String.format(Locale.ROOT, "%.2f", fila.getPrecio())).append(',')
               .append(fila.getVendidos()).append(',')
               .append(fila.getCapacidad()).append(',')
               .append(String.format(Locale.ROOT, "%.1f", fila.getPorcentaje())).append(',')
               .append(String.format(Locale.ROOT, "%.2f", fila.getIngresoGenerado())).append(',')
               .append(celdaCsv(fila.getEstado())).append('\n');
        }
        return csv.toString();
    }

    private static String celdaCsv(String valor) {
        if (valor == null) return "";
        String texto = valor.replace("\"", "\"\"");
        // Evita que Excel interprete el texto como fórmula
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) texto = "'" + texto;
        return "\"" + texto + "\"";
    }
}
