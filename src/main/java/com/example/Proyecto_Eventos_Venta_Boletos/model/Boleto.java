package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoEstadoENUM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "boletos")
public class Boleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //LADO PROPIEDAD
    // Nullable: en venta general (sin mapa de asientos) un Boleto representa un TIPO/LOTE,
    // no una butaca concreta. El asiento se asigna solo cuando el evento usa mapa numerado.
    @OneToOne
    @JoinColumn(name = "asiento_id", nullable = true)
    private Asiento asiento;

    // Nombre comercial del tipo de boleto (Ej. "General", "VIP"). Nulo para boletos individuales con asiento.
    @Column(name = "tipoNombre", length = 60)
    private String tipoNombre;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "precio")
    private Double precio;

    @Enumerated(EnumType.STRING)
    private BoletoEstadoENUM estado;

    // Aforo total disponible para este tipo/lote de boleto
    @Column(name = "cantidad")
    private Integer cantidad;

    // Cuántos boletos de este tipo ya se han vendido (se actualiza con cada compra real)
    @Column(name = "cantidadVendida")
    private Integer cantidadVendida;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    // Solo en boletos vendidos: zona del mapa (ej. LATERAL_AZUL) y asiento (ej. D8; nulo en zonas de pie)
    @Column(name = "zonaCodigo", length = 30)
    private String zonaCodigo;

    @Column(name = "codigoAsiento", length = 10)
    private String codigoAsiento;

    // Un Boleto sin compra es un TIPO de boleto (el que configura el organizador);
    // con compra es un boleto vendido a un asistente.
    public boolean isTipo() {
        return compra == null;
    }

    public int getRestantes() {
        int total = cantidad != null ? cantidad : 0;
        int vendidos = cantidadVendida != null ? cantidadVendida : 0;
        return Math.max(total - vendidos, 0);
    }

    @Override
    public String toString() {
        // Sin evento ni compra completos para no entrar en un ciclo Boleto -> Compra -> Boleto
        return "Boleto{" +
                "id=" + id +
                ", tipoNombre='" + tipoNombre + '\'' +
                ", precio=" + precio +
                ", estado=" + estado +
                ", cantidad=" + cantidad +
                ", zonaCodigo='" + zonaCodigo + '\'' +
                ", codigoAsiento='" + codigoAsiento + '\'' +
                ", eventoId=" + (evento != null ? evento.getId() : null) +
                ", compraId=" + (compra != null ? compra.getId() : null) +
                '}';
    }

}




