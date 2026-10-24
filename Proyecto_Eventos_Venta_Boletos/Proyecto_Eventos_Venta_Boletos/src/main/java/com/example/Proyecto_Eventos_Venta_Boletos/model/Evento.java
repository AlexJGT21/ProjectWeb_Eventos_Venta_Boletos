package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstruct
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private LocalDateTime fechaHora;
    private byte[] imagen;
<<<<<<< Updated upstream
    private Integer capacidad; // no recuerdo si esto seria la relacion con los boletos
    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;
    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private List<Boleto> boletos = new ArrayList<>();

=======
    private Integer capacidad;
    private String organizador;
    private EstadoEventoENUM estadoEvento;
    private String lugar;

    // Relación 1..N con Boletos según el diagrama de clases
    private List<Boleto> boletos = new ArrayList<>();

    public Evento() {
        //Constructor vacio
    }

    public Evento(String id, String nombre, String descripcion, String categoria,
                  LocalDateTime fechaHora, byte[] imagen, Integer capacidad,
                  String organizador, EstadoEventoENUM estadoEvento, String lugar,
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public byte[] getImagen() {
        return imagen;
    }
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    public EstadoEventoENUM getEstadoEvento() {
        return estadoEvento;
    }

    public void setEstadoEvento(EstadoEventoENUM estadoEvento) {
        this.estadoEvento = estadoEvento;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public List<Boleto> getBoletos() {
        return boletos;
    }

    public void setBoletos(List<Boleto> boletos) {
        this.boletos = boletos;
    }

>>>>>>> Stashed changes
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
        return getBoletosDisponibles() > 0 && estadoEvento != EstadoEventoENUM.AGOTADO;
    }

    public String getFecha() {
        if (fechaHora == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM", new Locale("es", "ES"));
        return fechaHora.format(formatter).toUpperCase();
    }
}