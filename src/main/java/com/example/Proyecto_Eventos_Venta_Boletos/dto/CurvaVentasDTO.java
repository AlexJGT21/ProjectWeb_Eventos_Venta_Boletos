package com.example.Proyecto_Eventos_Venta_Boletos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de la gráfica "Ritmo de Venta de Boletos por Día" del panel, calculados con las
 * compras reales y ya convertidos a trazos SVG (viewBox 0 0 800 250).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurvaVentasDTO {

    private String trazoLinea;
    private String trazoArea;
    private int totalPeriodo;
    private int maximoDiario;
    private int dias;
    private List<String> etiquetas;
}
