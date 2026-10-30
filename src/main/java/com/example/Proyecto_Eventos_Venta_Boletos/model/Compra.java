package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.time.LocalDateTime;

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

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", asistente=" + asistente +
                ", boletos=" + boletos +
                ", fechaHora=" + fechaHora +
                ", totalVendidos=" + totalVendidos +
                ", montoTotal=" + montoTotal +
                ", codigoQR=" + Arrays.toString(codigoQR) +
                '}';
    }
}

