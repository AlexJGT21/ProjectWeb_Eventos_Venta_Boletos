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
    @OneToOne
    @JoinColumn(name = "asiento_id", nullable = false)
    private Asiento asiento;

    @Column(name = "precio")
    private Double precio;

    @Enumerated(EnumType.STRING)
    private BoletoEstadoENUM estado;

    @Column(name = "cantidad")
    private Integer cantidad;

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

