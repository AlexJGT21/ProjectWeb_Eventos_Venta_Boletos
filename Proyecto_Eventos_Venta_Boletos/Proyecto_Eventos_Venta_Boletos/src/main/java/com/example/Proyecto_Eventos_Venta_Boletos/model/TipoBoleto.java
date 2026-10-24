package com.example.Proyecto_Eventos_Venta_Boletos.model;

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
@Table(name = "tipoBoleto")
public class TipoBoleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoBoletoENU2M tipoBoleto;

    @Column(name = "cantidadDisponible", nullable = false)
    private Integer cantidadDisponible;

    @Column(name = "caracteristicas", nullable = false, length = 100)
    private String caracteristicas;

    @Override
    public String toString() {
        return "TipoBoleto{" +
                "id=" + id +
                ", tipoBoleto=" + tipoBoleto +
                ", cantidadDisponible=" + cantidadDisponible +
                ", caracteristicas='" + caracteristicas + '\'' +
                '}';
    }

}


