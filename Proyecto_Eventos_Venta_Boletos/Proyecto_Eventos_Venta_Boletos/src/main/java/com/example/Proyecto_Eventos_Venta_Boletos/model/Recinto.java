package com.example.Proyecto_Eventos_Venta_Boletos.model;

import java.util.Arrays;

public class Recinto {

    private Integer id;
    private String nombre;
    private String direccion;
    private byte[] croquis;

    public Recinto() {
        //Constructor vacio
    }

    public Recinto(Integer id, String nombre, String direccion, byte[] croquis) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.croquis = croquis;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public byte[] getCroquis() {
        return croquis;
    }

    public void setCroquis(byte[] croquis) {
        this.croquis = croquis;
    }

    @Override
    public String toString() {
        return "Recinto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", croquis=" + Arrays.toString(croquis) +
                '}';
    }
}
