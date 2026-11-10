package com.example.Proyecto_Eventos_Venta_Boletos.dto;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cotización de una compra calculada en el servidor (nunca se confía en los montos del navegador).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResumenCompraDTO {

    private Evento evento;
    private Boleto tipo;
    private String zonaCodigo;      // null si el tipo no corresponde a una zona del mapa
    private String zonaNombre;
    private List<String> asientos;  // vacía en zonas de pie / entrada general
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal cargoServicio;
    private BigDecimal iva;
    private BigDecimal total;

    public boolean isConAsientos() {
        return asientos != null && !asientos.isEmpty();
    }

    // "D, E"
    public String getFilasTexto() {
        if (!isConAsientos()) return "—";
        return asientos.stream().map(a -> a.substring(0, 1)).distinct().sorted().collect(Collectors.joining(", "));
    }

    // "D8, E8"
    public String getAsientosTexto() {
        if (!isConAsientos()) return "Admisión general (sin asiento asignado)";
        return String.join(", ", asientos);
    }
}
