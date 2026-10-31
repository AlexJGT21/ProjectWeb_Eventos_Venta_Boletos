package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoEstadoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.InventarioBoletoDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.MetricasOrganizadorDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizadorService {

    private final OrganizadorRepository organizadorRepository;
    private final EventoRepository eventoRepository;

    @Autowired
    public OrganizadorService(OrganizadorRepository organizadorRepository, EventoRepository eventoRepository) {
        this.organizadorRepository = organizadorRepository;
        this.eventoRepository = eventoRepository;
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

    public Organizador actualizarPerfil(Long id, Organizador datosActualizados) {
        Organizador organizador = obtenerPorId(id);
        organizador.setNombre(datosActualizados.getNombre());
        organizador.setCorreo(datosActualizados.getCorreo());
        organizador.setTelefono(datosActualizados.getTelefono());

        if (datosActualizados.getContrasenia() != null && !datosActualizados.getContrasenia().isEmpty()) {
            organizador.setContrasenia(datosActualizados.getContrasenia());
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

    /**
     * Guarda un evento nuevo (o actualiza uno existente) junto con los tipos de boleto
     * configurados en el Paso 3 del formulario. Cada tipo de boleto se persiste como un
     * Boleto "lote" (tipoNombre + precio + cantidad/aforo), listo para que, más adelante,
     * el flujo de compra vaya incrementando cantidadVendida con ventas reales.
     */
    public Evento procesarYGuardarEvento(Evento evento,
                                          MultipartFile imagenFile,
                                          Long organizadorId,
                                          List<String> tipoNombre,
                                          List<Double> tipoPrecio,
                                          List<Integer> tipoAforo,
                                          List<String> tipoDescripcion) {
        try {
            Organizador organizador = obtenerPorId(organizadorId);
            evento.setOrganizador(organizador);

            if (evento.getEstadoEvento() == null) {
                evento.setEstadoEvento(EstadoEventoENUM.PROGRAMADO);
            }

            if (imagenFile != null && !imagenFile.isEmpty()) {
                evento.setImagenEvento(imagenFile.getBytes());
            }

            List<Boleto> tiposDeBoleto = construirTiposDeBoleto(evento, tipoNombre, tipoPrecio, tipoAforo, tipoDescripcion);
            if (!tiposDeBoleto.isEmpty()) {
                evento.setBoletos(tiposDeBoleto);
            }

            return eventoRepository.save(evento);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen del evento", e);
        }
    }

    private List<Boleto> construirTiposDeBoleto(Evento evento,
                                                  List<String> tipoNombre,
                                                  List<Double> tipoPrecio,
                                                  List<Integer> tipoAforo,
                                                  List<String> tipoDescripcion) {
        List<Boleto> resultado = new ArrayList<>();
        if (tipoNombre == null) {
            return resultado;
        }

        for (int i = 0; i < tipoNombre.size(); i++) {
            String nombre = tipoNombre.get(i);
            if (nombre == null || nombre.isBlank()) {
                continue; // fila vacía, se ignora
            }

            Double precio = (tipoPrecio != null && i < tipoPrecio.size()) ? tipoPrecio.get(i) : 0.0;
            Integer aforo = (tipoAforo != null && i < tipoAforo.size()) ? tipoAforo.get(i) : 0;
            String descripcion = (tipoDescripcion != null && i < tipoDescripcion.size()) ? tipoDescripcion.get(i) : null;

            Boleto boleto = Boleto.builder()
                    .tipoNombre(nombre)
                    .descripcion(descripcion)
                    .precio(precio)
                    .cantidad(aforo)
                    .cantidadVendida(0)
                    .estado(BoletoEstadoENUM.DISPONIBLE)
                    .evento(evento)
                    .build();
            resultado.add(boleto);
        }
        return resultado;
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

    /**
     * Construye la tabla de "Gestión de Boletos por Tipo y Aforo" a partir de los
     * Boleto reales de todos los eventos del organizador. Si todavía no hay ventas,
     * vendidos/ingresos aparecerán en 0 (dato real, no inventado).
     */
    @Transactional(readOnly = true)
    public List<InventarioBoletoDTO> obtenerInventario(Long organizadorId) {
        List<Evento> eventos = eventoRepository.findByOrganizadorId(organizadorId);
        List<InventarioBoletoDTO> inventario = new ArrayList<>();

        for (Evento evento : eventos) {
            if (evento.getBoletos() == null) continue;

            for (Boleto boleto : evento.getBoletos()) {
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
                        .tipoBoleto(evento.getNombre() + " • " + nombreTipo)
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
    public MetricasOrganizadorDTO calcularMetricas(Long organizadorId) {
        List<Evento> eventos = eventoRepository.findByOrganizadorId(organizadorId);

        int capacidadTotal = 0;
        int boletosVendidos = 0;
        double ingresosTotales = 0.0;

        for (Evento evento : eventos) {
            capacidadTotal += evento.getCapacidad() != null ? evento.getCapacidad() : 0;

            if (evento.getBoletos() == null) continue;
            for (Boleto boleto : evento.getBoletos()) {
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
}
