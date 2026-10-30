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

    @Override
    public String toString() {
        return "Boleto{" +
                "id=" + id +
                ", asiento=" + asiento +
                ", precio=" + precio +
                ", estado=" + estado +
                ", cantidad=" + cantidad +
                ", evento=" + evento +
                ", compra=" + compra +
                '}';
    }

}




