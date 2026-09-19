package com.example.Proyecto_Eventos_Venta_Boletos.model;

public class ArtistaEvento {

    private Integer id;
    private Artista artistaID;
    private Evento eventoID;

    public ArtistaEvento() {
        //Constructor vacio
    }

    public ArtistaEvento(Integer id, Artista artistaID, Evento eventoID) {
        this.id = id;
        this.artistaID = artistaID;
        this.eventoID = eventoID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Artista getArtistaID() {
        return artistaID;
    }

    public void setArtistaID(Artista artistaID) {
        this.artistaID = artistaID;
    }

    public Evento getEventoID() {
        return eventoID;
    }

    public void setEventoID(Evento eventoID) {
        this.eventoID = eventoID;
    }

    @Override
    public String toString() {
        return "ArtistaEvento{" +
                "id=" + id +
                ", artistaID=" + artistaID +
                ", eventoID=" + eventoID +
                '}';
    }
}
