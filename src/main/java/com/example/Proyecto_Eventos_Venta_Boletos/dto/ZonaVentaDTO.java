package com.example.Proyecto_Eventos_Venta_Boletos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una opción de compra en la pantalla de venta: una zona del mapa (con sus asientos)
 * o, en eventos de entrada general, un tipo de boleto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZonaVentaDTO {

    private String codigo;          // código de la zona (ej. LATERAL_AZUL) o "TIPO_<id>" en entrada general
    private String nombre;
    private String descripcion;
    private String color;
    private String iniciales;       // ej. "LA" para el distintivo de la zona seleccionada
    private Long tipoId;            // tipo de boleto con el que se vende (null = no está a la venta)
    private Double precio;
    private int disponibles;        // boletos que quedan en el tipo
    private boolean numerada;       // true = se eligen asientos
    private String filas;           // "A,B,C,..." (solo numeradas)
    private int asientosPorFila;
    private String pasillos;        // "4,8"
    private String ocupados;        // "A1,D8,..." asientos ya vendidos
    private int totalAsientos;

    public boolean isEnVenta() {
        return tipoId != null && disponibles > 0;
    }
}
