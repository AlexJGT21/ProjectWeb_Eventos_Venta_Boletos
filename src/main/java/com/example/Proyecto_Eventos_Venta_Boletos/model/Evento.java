package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoEstadoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //LADO INVERSO
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private List<ArtistaEvento> artistaEventos;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 300)
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "fechaHoraEvento", nullable = false)
    private LocalDateTime fechaHoraEvento;

    @Lob
    @Column(name = "imagenEvento", columnDefinition = "BLOB")
    private byte[] imagenEvento;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    private EstadoEventoENUM estadoEvento;

    //LADO INVERSO
    @OneToMany(mappedBy = "evento",  cascade = CascadeType.ALL)
    private List<Boleto> boletos;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

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
                .filter(b -> b.getEstado() == BoletoEstadoENUM.DISPONIBLE)
                .count();
    }

    public boolean isDisponible() {
        return getBoletosDisponibles() > 0 && estadoEvento != EstadoEventoENUM.AGOTADO;
    }

    public String getFecha() {
        if (fechaHoraEvento == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM", new Locale("es", "ES"));
        return fechaHoraEvento.format(formatter).toUpperCase();
    }
}