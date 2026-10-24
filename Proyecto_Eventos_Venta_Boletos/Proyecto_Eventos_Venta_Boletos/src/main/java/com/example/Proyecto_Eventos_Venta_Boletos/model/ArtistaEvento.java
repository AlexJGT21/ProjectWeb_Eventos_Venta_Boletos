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
@Table(name = "ArtistaEvento")
public class ArtistaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "artista_id", nullable = false)
    private Artista artista;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Override
    public String toString() {
        return "ArtistaEvento{" +
                "id=" + id +
                ", artista=" + artista +
                ", evento=" + evento +
                '}';
    }
}

