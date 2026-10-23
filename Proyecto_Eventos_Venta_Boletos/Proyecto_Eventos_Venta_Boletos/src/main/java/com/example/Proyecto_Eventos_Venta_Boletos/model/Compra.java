package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.time.LocalDateTime;
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
@Table(name = "compra")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "asistente_id", nullable = false)
    private Asistente asistente;
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<Boleto> boletos;
    private LocalDateTime fechaHora;
    private Integer cantidadVendido;
    private Float total;
}
