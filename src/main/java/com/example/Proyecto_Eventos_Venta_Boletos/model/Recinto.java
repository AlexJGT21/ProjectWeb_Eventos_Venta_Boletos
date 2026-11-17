package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.util.List;

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
@Table(name = "Recinto")
public class Recinto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 70)
    private String nombre;

    @Column(name = "direccion", nullable = false, length = 30)
    private String direccion;

    // Croquis del recinto en Cloudinary (solo se guarda la URL, no el archivo)
    @Column(name = "croquisUrl", length = 500)
    private String croquisUrl;

    //LADO INVERSO
    @OneToMany(mappedBy = "recinto", cascade = CascadeType.ALL)
    private List<Zona> zonas;

    //@OneToMany(mappedBy = "recinto",  cascade = CascadeType.ALL)
    //private List<Evento> eventos;

    @Override
    public String toString() {
        return "Recinto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", croquisUrl='" + croquisUrl + '\'' +
                '}';
    }

}
