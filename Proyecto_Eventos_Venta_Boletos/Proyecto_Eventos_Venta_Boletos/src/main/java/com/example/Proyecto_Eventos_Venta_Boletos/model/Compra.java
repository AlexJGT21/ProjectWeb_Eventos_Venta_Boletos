package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.time.LocalDateTime;
<<<<<<< Updated upstream
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
=======
import java.util.Arrays;
>>>>>>> Stashed changes

@Entity
@Data
@NoArgsConstructor
@AllArgsConstruct
@Table(name = "compra")
public class Compra {
<<<<<<< Updated upstream
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
=======

    private Integer id;
    private Integer asistenteId;
    private Integer boletoId;
    private LocalDateTime fechaHora;
    private Integer cantidadVendido;
    private Double total;
    private byte[] codigoQR;

    public Compra() {
        //Constructor vacio
    }

    public Compra(Integer id, Integer asistenteId, Integer boletoId, LocalDateTime fechaHora, Integer cantidadVendido, Double total, byte[] codigoQR) {
        this.id = id;
        this.asistenteId = asistenteId;
        this.boletoId = boletoId;
        this.fechaHora = fechaHora;
        this.cantidadVendido = cantidadVendido;
        this.total = total;
        this.codigoQR = codigoQR;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAsistenteId() {
        return asistenteId;
    }

    public void setAsistenteId(Integer asistenteId) {
        this.asistenteId = asistenteId;
    }

    public Integer getBoletoId() {
        return boletoId;
    }

    public void setBoletoId(Integer boletoId) {
        this.boletoId = boletoId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getCantidadVendido() {
        return cantidadVendido;
    }

    public void setCantidadVendido(Integer cantidadVendido) {
        this.cantidadVendido = cantidadVendido;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public byte[] getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(byte[] codigoQR) {
        this.codigoQR = codigoQR;
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", asistenteId=" + asistenteId +
                ", boletoId=" + boletoId +
                ", fechaHora=" + fechaHora +
                ", cantidadVendido=" + cantidadVendido +
                ", total=" + total +
                ", codigoQR=" + Arrays.toString(codigoQR) +
                '}';
    }

>>>>>>> Stashed changes
}

