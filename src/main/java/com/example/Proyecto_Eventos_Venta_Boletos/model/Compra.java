package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.time.LocalDateTime;

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
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "asistente_id", nullable = false)
    private Asistente asistente;

    //LADO INVERSO
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<Boleto> boletos;

    @Column(name = "fechaHoraCompra")
    private LocalDateTime fechaHora;

    @Column(name = "totalVendidos")
    private Integer totalVendidos;

    @Column(name = "montoTotal")
    private Double montoTotal;

    @Lob
    @Column(name = "codigoQR", columnDefinition = "BLOB")
    private byte[] codigoQR;

    @Column(name = "codigoPedido", length = 30, unique = true)
    private String codigoPedido;

    @Column(name = "metodoPago", length = 60)
    private String metodoPago;

    @Override
    public String toString() {
        // Sin la lista de boletos ni el QR para no entrar en un ciclo Compra -> Boleto -> Compra
        return "Compra{" +
                "id=" + id +
                ", codigoPedido='" + codigoPedido + '\'' +
                ", asistenteId=" + (asistente != null ? asistente.getId() : null) +
                ", fechaHora=" + fechaHora +
                ", totalVendidos=" + totalVendidos +
                ", montoTotal=" + montoTotal +
                ", metodoPago='" + metodoPago + '\'' +
                '}';
    }
}




