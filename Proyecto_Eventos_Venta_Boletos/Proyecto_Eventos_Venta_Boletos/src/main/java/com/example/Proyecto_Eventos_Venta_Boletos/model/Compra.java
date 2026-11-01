package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.time.LocalDateTime;

public class Compra {

    private Integer id;
    private Integer asistenteID;
    private Integer boletoID;
    private LocalDateTime fechaHora;
    private Integer cantidadVendido;
    private Float total;

    public Compra() {
        //Constructor vacio
    }
    public Compra(Integer id, Integer asistenteID, Integer boletoID, LocalDateTime fechaHora, Integer cantidadVendido, Float total) {
        this.id = id;
        this.asistenteID = asistenteID;
        this.boletoID = boletoID;
        this.fechaHora = fechaHora;
        this.cantidadVendido = cantidadVendido;
        this.total = total;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAsistenteID() {
        return asistenteID;
    }

    public void setAsistenteID(Integer asistenteID) {
        this.asistenteID = asistenteID;
    }

    public Integer getBoletoID() {
        return boletoID;
    }

    public void setBoletoID(Integer boletoID) {
        this.boletoID = boletoID;
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

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", asistenteID=" + asistenteID +
                ", boletoID=" + boletoID +
                ", fechaHora=" + fechaHora +
                ", cantidadVendido=" + cantidadVendido +
                ", total=" + total +
                '}';
    }
}
