package com.example.Proyecto_Eventos_Venta_Boletos.model;

public class TipoBoleto {

    private Integer id;
    private String tipo;
    private Integer cantidadDisponible;
    private String categorias;

    public TipoBoleto() {
        //Constructor vacio
    }

    public TipoBoleto(Integer id, String tipo, Integer cantidadDisponible, String categorias) {
        this.id = id;
        this.tipo = tipo;
        this.cantidadDisponible = cantidadDisponible;
        this.categorias = categorias;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public String getCategorias() {
        return categorias;
    }

    public void setCategorias(String categorias) {
        this.categorias = categorias;
    }

    @Override
    public String toString() {
        return "TipoBoleto{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", cantidadDisponible=" + cantidadDisponible +
                ", categorias='" + categorias + '\'' +
                '}';
    }
}
