package com.example.Proyecto_Eventos_Venta_Boletos.model;

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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstruct
@Table(name = "artistaEvento")
public class ArtistaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "artista_id", nullable = false)
    private Artista artista;
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
=======
import java.util.List;

public class ArtistaEvento {

    private Integer id;
    private List<Artista> artistaId;
    private List<Evento> eventoId;

    public ArtistaEvento() {
        //Constructor vacio
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Artista> getArtistaId() {
        return artistaId;
    }

    public void setArtistaId(List<Artista> artistaId) {
        this.artistaId = artistaId;
    }

    public List<Evento> getEventoId() {
        return eventoId;
    }

    public void setEventoId(List<Evento> eventoId) {
        this.eventoId = eventoId;
    }

    @Override
    public String toString() {
        return "ArtistaEvento{" +
                "id=" + id +
                ", artistaId=" + artistaId +
                ", eventoId=" + eventoId +
                '}';
    }

>>>>>>> Stashed changes
}

