package com.example.Proyecto_Eventos_Venta_Boletos.model;

public class Asiento {

    private Integer id;
    private Integer numAsiento;
    private String tipoAsiento;

    public Asiento() {
        //Constructor vacio
    }

    public Asiento(Integer id, Integer numAsiento, String tipoAsiento) {
        this.id = id;
        this.numAsiento = numAsiento;
        this.tipoAsiento = tipoAsiento;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumAsiento() {
        return numAsiento;
    }

    public void setNumAsiento(Integer numAsiento) {
        this.numAsiento = numAsiento;
    }

    public String getTipoAsiento() {
        return tipoAsiento;
    }

    public void setTipoAsiento(String tipoAsiento) {
        this.tipoAsiento = tipoAsiento;
    }
}
