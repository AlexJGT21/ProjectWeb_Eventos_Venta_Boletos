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
import org.springframework.format.annotation.DateTimeFormat;

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

    // Nombre libre del recinto/lugar del evento (versión simple mientras no se integra Recinto/Zona)
    @Column(name = "lugar", length = 150)
    private String lugar;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "fechaHoraEvento", nullable = false)
    private LocalDateTime fechaHoraEvento;

    @Lob
    @Column(name = "imagenEvento", columnDefinition = "LONGBLOB")
    private byte[] imagenEvento;

    // Croquis opcional subido en el Paso 2 (imagen de referencia para eventos de entrada general)
    @Lob
    @Column(name = "croquis", columnDefinition = "LONGBLOB")
    private byte[] croquis;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    private EstadoEventoENUM estadoEvento;

    // Modalidad de asignación de espacios elegida en el Paso 3: GENERAL o NUMERADO
    @Column(name = "modalidadAsientos", length = 20)
    private String modalidadAsientos;

    //LADO INVERSO
    @OneToMany(mappedBy = "evento",  cascade = CascadeType.ALL)
    private List<Boleto> boletos;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    //@ManyToOne
    //@JoinColumn(name = "recinto_id", nullable = false)
    //private Recinto recinto;

    // Tipos de boleto configurados por el organizador (sin los boletos ya vendidos)
    public List<Boleto> getTiposBoleto() {
        if (boletos == null) return List.of();
        return boletos.stream().filter(Boleto::isTipo).toList();
    }

    // Retorna el precio mínimo ("Desde $X MXN")
    public Double getPrecioMinimo() {
        return getTiposBoleto().stream()
                .filter(b -> b.getPrecio() != null)
                .mapToDouble(b -> b.getPrecio().doubleValue())
                .min()
                .orElse(0.0);
    }

    // Retorna el precio máximo
    public Double getPrecioMaximo() {
        return getTiposBoleto().stream()
                .filter(b -> b.getPrecio() != null)
                .mapToDouble(b -> b.getPrecio().doubleValue())
                .max()
                .orElse(0.0);
    }

    // Boletos que todavía se pueden vender (lo que queda en cada tipo disponible)
    public Integer getBoletosDisponibles() {
        return getTiposBoleto().stream()
                .filter(b -> b.getEstado() == BoletoEstadoENUM.DISPONIBLE)
                .mapToInt(Boleto::getRestantes)
                .sum();
    }

    // Total de boletos que el organizador puso a la venta (sumando todos los tipos)
    public Integer getAforoTipos() {
        return getTiposBoleto().stream()
                .mapToInt(b -> b.getCantidad() != null ? b.getCantidad() : 0)
                .sum();
    }

    public boolean isDisponible() {
        return getBoletosDisponibles() > 0
                && estadoEvento != EstadoEventoENUM.AGOTADO
                && estadoEvento != EstadoEventoENUM.CANCELADO
                && estadoEvento != EstadoEventoENUM.PAUSADO
                && (fechaHoraEvento == null || fechaHoraEvento.isAfter(LocalDateTime.now()));
    }

    // "SÁBADO 26 DE SEPTIEMBRE, 2026" (carrusel del catálogo)
    public String getFechaCompleta() {
        if (fechaHoraEvento == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX"));
        return fechaHoraEvento.format(formatter).toUpperCase(Locale.forLanguageTag("es-MX"));
    }

    // "20:30"
    public String getHora() {
        if (fechaHoraEvento == null) return "";
        return fechaHoraEvento.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // "26 SEP 2026"
    public String getFechaCorta() {
        if (fechaHoraEvento == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-MX"));
        return fechaHoraEvento.format(formatter).replace(".", "").toUpperCase(Locale.forLanguageTag("es-MX"));
    }

    // true si algún tipo de boleto ya tiene ventas (limita lo que se puede modificar del evento)
    public boolean isConVentas() {
        return getTiposBoleto().stream().anyMatch(b -> b.getCantidadVendida() != null && b.getCantidadVendida() > 0);
    }

    public String getFecha() {
        if (fechaHoraEvento == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM", new Locale("es", "ES"));
        return fechaHoraEvento.format(formatter).toUpperCase();
    }
}


