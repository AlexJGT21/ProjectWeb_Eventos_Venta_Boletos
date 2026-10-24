package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.ZonaENUM;

public class Zona {

    private String id;
    private Integer recintoId;
    private String nombreZona;
    private ZonaENUM tipo;

    public Zona() {
        //Constructor vacio
    }

    public Zona(String id, Integer recintoId, String nombreZona, ZonaENUM tipo) {
        this.id = id;
        this.recintoId = recintoId;
        this.nombreZona = nombreZona;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRecintoId() {
        return recintoId;
    }

    public void setRecintoId(Integer recintoId) {
        this.recintoId = recintoId;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public ZonaENUM getTipo() {
        return tipo;
    }

    public void setTipo(ZonaENUM tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Zona{" +
                "id='" + id + '\'' +
                ", recintoId=" + recintoId +
                ", nombreZona='" + nombreZona + '\'' +
                ", tipo=" + tipo +
                '}';
    }

}

