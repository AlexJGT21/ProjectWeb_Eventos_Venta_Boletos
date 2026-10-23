package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadoENUM.EstadoEvento;

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