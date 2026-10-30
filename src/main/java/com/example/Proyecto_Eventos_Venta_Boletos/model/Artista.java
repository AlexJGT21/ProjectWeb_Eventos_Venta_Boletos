package com.example.Proyecto_Eventos_Venta_Boletos.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Artista")
public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 20)
    private String nombre;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "telefono",  nullable = false)
    private String telefono;

    @Lob
    @Column(name = "imagen", nullable = false, columnDefinition = "BLOB")
    private byte[] imagen;

    //LADO INVERSO
    @OneToMany(mappedBy = "artista", cascade = CascadeType.ALL)
    private List<ArtistaEvento> artistaEventos;

    @Override
    public String toString() {
        return "Artista{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", edad=" + edad +
                ", telefono='" + telefono + '\'' +
                ", imagen=" + Arrays.toString(imagen) +
                '}';
    }

}


