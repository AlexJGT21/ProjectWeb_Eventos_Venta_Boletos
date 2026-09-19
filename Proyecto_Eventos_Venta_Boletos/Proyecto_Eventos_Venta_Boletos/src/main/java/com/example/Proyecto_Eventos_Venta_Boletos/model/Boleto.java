package com.example.Proyecto_Eventos_Venta_Boletos.model;

public class Boleto {

    private Integer id;
    private String tipoBoleto;
    private Float precio;
    private String estado;

    public Boleto() {
        //Constructor vacio
    }

    public Boleto(Integer id, String tipoBoleto, Float precio, String estado) {
        this.id = id;
        this.tipoBoleto = tipoBoleto;
        this.precio = precio;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoBoleto() {
        return tipoBoleto;
    }

    public void setTipoBoleto(String tipoBoleto) {
        this.tipoBoleto = tipoBoleto;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Boleto{" +
                "id=" + id +
                ", tipoBoleto='" + tipoBoleto + '\'' +
                ", precio=" + precio +
                ", estado='" + estado + '\'' +
                '}';
    }
}
