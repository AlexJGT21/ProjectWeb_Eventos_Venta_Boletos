package com.example.Proyecto_Eventos_Venta_Boletos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResumenCompraDTO {

    private Long eventoId;
    private String eventoNombre;
    private Long tipoBoletoId;
    private String tipoBoletoNombre;
    private String zonaCodigo;
    private String zonaNombre;
    private List<String> asientos;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal cargoServicio;
    private BigDecimal iva;
    private BigDecimal total;

    public boolean isConAsientos() {
        return asientos != null && !asientos.isEmpty();
    }

    public String getFilasTexto() {
        if (!isConAsientos()) return "—";
        return asientos.stream().map(a -> a.substring(0, 1)).distinct().sorted().collect(Collectors.joining(", "));
    }

    public String getAsientosTexto() {
        if (!isConAsientos()) return "Admisión general (sin asiento asignado)";
        return String.join(", ", asientos);
    }
}