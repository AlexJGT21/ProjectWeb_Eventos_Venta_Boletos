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
=======
import java.util.Arrays;

public class Artista {
>>>>>>> Stashed changes

@Entity
@Data
@NoArgsConstructor
@AllArgsConstruct
@Table(name = "artista")
public class Artista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer edad;
<<<<<<< Updated upstream
=======
    private String telefono;
    private byte[] imagen;

    public Artista() {
        //Constructor vacio
    }

    public Artista(Integer id, String nombre, Integer edad, String telefono, byte[] imagen) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
        this.telefono = telefono;
        this.imagen = imagen;
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

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return "Artista{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", edad=" + edad +
                ", telefono='" + telefono + '\'' +
                ", imagen=" + Arrays.toString(imagen) +
                '}';
    }

>>>>>>> Stashed changes
}

