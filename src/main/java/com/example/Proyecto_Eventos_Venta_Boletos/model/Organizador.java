package com.example.Proyecto_Eventos_Venta_Boletos.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "organizadores")
public class Organizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "correo", nullable = false)
    private String correo;

    @Column(name = "contrasenia")
    private String contrasenia;

    @Column(name = "fechaNacimiento")
    private LocalDate fechaNacimiento;

    //LADO INVERSO
    @OneToMany(mappedBy = "organizador", cascade = CascadeType.ALL)
    private List<Evento> eventos;

    @Override
    public String toString() {
        return "Organizador{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", correo='" + correo + '\'' +
                ", contrasenia='" + contrasenia + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                '}';
    }

}




