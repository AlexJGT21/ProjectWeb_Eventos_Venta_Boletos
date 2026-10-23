package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.util.Arrays;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstruct
@Table(name = "recintos")
public class Recinto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private byte[] croquis;

    @OneToMany(mappedBy = "recinto", cascade = CascadeType.ALL)
    private List<Asiento> asientos;
}
