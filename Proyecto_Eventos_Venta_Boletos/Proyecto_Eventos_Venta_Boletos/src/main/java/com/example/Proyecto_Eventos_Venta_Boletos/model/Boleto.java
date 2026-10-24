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
@Table(name = "boletos")
public class Boleto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "tipo_boleto_id", nullable = false)
    private TipoBoleto tipoBoleto;
    private Float precio;
    private String estado;
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;
    @OneToOne
    @JoinColumn(name = "asiento_id")
    private Asiento asiento;
=======
import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoENUM;

public class Boleto {

    private Integer id;
    private Integer asientoId;
    private Integer tipoBoletoId;
    private Double precio;
    private Integer cantidad;
    private BoletoENUM estado;

    public Boleto() {
        //Constructor vacio
    }

    public Boleto(Integer id, Integer asientoId, Integer tipoBoletoId, Double precio, Integer cantidad, BoletoENUM estado) {
        this.id = id;
        this.asientoId = asientoId;
        this.tipoBoletoId = tipoBoletoId;
        this.precio = precio;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Integer asientoId) {
        this.asientoId = asientoId;
    }

    public Integer getTipoBoletoId() {
        return tipoBoletoId;
    }

    public void setTipoBoletoId(Integer tipoBoletoId) {
        this.tipoBoletoId = tipoBoletoId;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BoletoENUM getEstado() {
        return estado;
    }

    public void setEstado(BoletoENUM estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Boleto{" +
                "id=" + id +
                ", asientoId=" + asientoId +
                ", tipoBoletoId=" + tipoBoletoId +
                ", precio=" + precio +
                ", cantidad=" + cantidad +
                ", estado=" + estado +
                '}';
    }

>>>>>>> Stashed changes
}

