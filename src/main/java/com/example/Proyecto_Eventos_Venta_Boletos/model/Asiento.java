package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.TipoAsientoENUM;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Asiento")
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(name = "fila", nullable = false)
    private Integer fila;

    @Column(name = "numero",  nullable = false)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    private TipoAsientoENUM tipo;

    //LADO INVERSO
    @OneToOne(mappedBy = "asiento", cascade = CascadeType.ALL)
    private Boleto boleto;

    @Override
    public String toString() {
        return "Asiento{" +
                "id=" + id +
                ", zona=" + zona +
                ", fila=" + fila +
                ", numero=" + numero +
                ", tipo=" + tipo +
                '}';
    }

}

