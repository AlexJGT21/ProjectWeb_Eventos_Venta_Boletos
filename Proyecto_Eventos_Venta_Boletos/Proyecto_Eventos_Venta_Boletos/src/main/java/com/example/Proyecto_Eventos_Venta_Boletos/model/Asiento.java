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
@Table(name = "asientos")
public class Asiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer numAsiento;
    private String tipoAsiento;
    @ManyToOne
    @JoinColumn(name = "recinto_id", nullable = false)
    private Recinto recinto;
=======
import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.AsientoENUM;

public class Asiento {

    private Integer id;
    private Integer zonaId;
    private Integer fila;
    private Integer numero;
    private AsientoENUM tipo;

    public Asiento() {
        //Constructor vacio
    }

    public Asiento(Integer id, Integer zonaId, Integer fila, Integer numero, AsientoENUM tipo) {
        this.id = id;
        this.zonaId = zonaId;
        this.fila = fila;
        this.numero = numero;
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getZonaId() {
        return zonaId;
    }

    public void setZonaId(Integer zonaId) {
        this.zonaId = zonaId;
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public AsientoENUM getTipo() {
        return tipo;
    }

    public void setTipo(AsientoENUM tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Asiento{" +
                "id=" + id +
                ", zonaId=" + zonaId +
                ", fila=" + fila +
                ", numero=" + numero +
                ", tipo=" + tipo +
                '}';
    }

>>>>>>> Stashed changes
}

