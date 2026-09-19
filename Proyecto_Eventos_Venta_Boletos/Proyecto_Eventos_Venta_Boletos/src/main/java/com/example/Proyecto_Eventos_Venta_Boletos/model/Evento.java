package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadoENUM.EstadoEvento;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Evento {

    private String id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private LocalDateTime fechaHora;
    private byte[] imagen;
    private Integer capacidad;
    private String organizador;
    private EstadoEvento estadoEvento;

    public Evento() {
        //Constructor vacio
    }

    public Evento(String id, String nombre, String descripcion, String categoria, LocalDateTime fechaHora, byte[] imagen, Integer capacidad, String organizador, EstadoEvento estadoEvento) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaHora = fechaHora;
        this.imagen = imagen;
        this.capacidad = capacidad;
        this.organizador = organizador;
        this.estadoEvento = estadoEvento;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    public EstadoEvento getEstadoEvento() {
        return estadoEvento;
    }

    public void setEstadoEvento(EstadoEvento estadoEvento) {
        this.estadoEvento = estadoEvento;
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", categoria='" + categoria + '\'' +
                ", fechaHora=" + fechaHora +
                ", imagen=" + Arrays.toString(imagen) +
                ", capacidad=" + capacidad +
                ", organizador='" + organizador + '\'' +
                ", estadoEvento=" + estadoEvento +
                '}';
    }
}
