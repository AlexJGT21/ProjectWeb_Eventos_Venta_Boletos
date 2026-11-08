package com.example.Proyecto_Eventos_Venta_Boletos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String lugar;
    private LocalDateTime fechaHoraEvento;
    private String fechaCompleta;
    private String fechaCorta;
    private String hora;
    private Double precioMinimo;
    private Double precioMaximo;
    private boolean disponible;
    private String estadoEvento;
}