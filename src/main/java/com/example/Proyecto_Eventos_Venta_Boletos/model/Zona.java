package com.example.Proyecto_Eventos_Venta_Boletos.model;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.TipoZonaENUM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "zonas")
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //LADO PROPIEDAD
    @ManyToOne
    @JoinColumn(name = "recinto_id",  nullable = false) //COMO SE LLAMARA EN LA BD
    private Recinto recinto; //MISMO NOMBRE QUE LADO INVERSO DEL "mappedBy"

    @Column(name = "nombreZona", nullable = false)
    private String nombreZona;

    @Enumerated(EnumType.STRING)
    private TipoZonaENUM tipo;

    //LADO INVERSO
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL)
    private List<Asiento> asientos;

    @Override
    public String toString() {
        return "Zona{" +
                "id='" + id + '\'' +
                ", recinto=" + recinto +
                ", nombreZona='" + nombreZona + '\'' +
                ", tipo=" + tipo +
                ", asiento=" + asientos +
                '}';
    }

}




