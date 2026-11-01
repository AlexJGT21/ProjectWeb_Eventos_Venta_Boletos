package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadoENUM.EstadoEvento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Evento {

    private String id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private LocalDateTime fechaHora;
    private byte[] imagen;
    private Integer capacidad;
    private String organizador;
    private EstadoEvento estadoEvento;
    private String lugar;

    // Relación 1..N con Boletos según el diagrama de clases
    private List<Boleto> boletos = new ArrayList<>();

    public Evento() {
    }

    public Evento(String id, String nombre, String descripcion, String categoria,
                  LocalDateTime fechaHora, byte[] imagen, Integer capacidad,
                  String organizador, EstadoEvento estadoEvento, String lugar,
                  List<Boleto> boletos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaHora = fechaHora;
        this.imagen = imagen;
        this.capacidad = capacidad;
        this.organizador = organizador;
        this.estadoEvento = estadoEvento;
        this.lugar = lugar;
        this.boletos = boletos != null ? boletos : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public String getOrganizador() { return organizador; }
    public void setOrganizador(String organizador) { this.organizador = organizador; }

    public EstadoEvento getEstadoEvento() { return estadoEvento; }
    public void setEstadoEvento(EstadoEvento estadoEvento) { this.estadoEvento = estadoEvento; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public List<Boleto> getBoletos() { return boletos; }
    public void setBoletos(List<Boleto> boletos) { this.boletos = boletos; }

    // Retorna el precio mínimo ("Desde $X MXN")
    public Double getPrecioMinimo() {
        if (boletos == null || boletos.isEmpty()) return 0.0;
        return boletos.stream()
                .filter(b -> b.getPrecio() != null)
                .mapToDouble(b -> b.getPrecio().doubleValue())
                .min()
                .orElse(0.0);
    }

    // Retorna el precio máximo
    public Double getPrecioMaximo() {
        if (boletos == null || boletos.isEmpty()) return 0.0;
        return boletos.stream()
                .filter(b -> b.getPrecio() != null)
                .mapToDouble(b -> b.getPrecio().doubleValue())
                .max()
                .orElse(0.0);
    }

    // Cuenta cuántos boletos activos/disponibles quedan
    public Integer getBoletosDisponibles() {
        if (boletos == null) return 0;
        return (int) boletos.stream()
                .filter(b -> "DISPONIBLE".equalsIgnoreCase(b.getEstado()))
                .count();
    }

    public boolean isDisponible() {
        return getBoletosDisponibles() > 0 && estadoEvento != EstadoEvento.AGOTADO;
    }

    public String getFecha() {
        if (fechaHora == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM", new Locale("es", "ES"));
        return fechaHora.format(formatter).toUpperCase();
    }
}