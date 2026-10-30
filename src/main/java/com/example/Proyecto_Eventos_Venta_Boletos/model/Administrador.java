package com.example.Proyecto_Eventos_Venta_Boletos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Administrador")
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 20)
    private String nombre;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name ="contrasenia", nullable = false)
    private String contrasenia;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Override
    public String toString() {
        return "Administrador{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", edad=" + edad +
                ", correo='" + correo + '\'' +
                ", contrasenia='" + contrasenia + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }

}




