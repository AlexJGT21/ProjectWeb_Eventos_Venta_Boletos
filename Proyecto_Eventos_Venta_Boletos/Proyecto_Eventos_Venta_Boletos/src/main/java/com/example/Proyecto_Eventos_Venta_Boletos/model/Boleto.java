package com.example.Proyecto_Eventos_Venta_Boletos.model;

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
@Table(name = "boletos")
public class Boleto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "tipo_boleto_id", nullable = false)
    private TipoBoleto tipoBoleto;
    private Float precio;
    private String estado;
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;
    @OneToOne
    @JoinColumn(name = "asiento_id")
    private Asiento asiento;
}
