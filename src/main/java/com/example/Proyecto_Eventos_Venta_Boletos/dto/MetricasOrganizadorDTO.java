package com.example.Proyecto_Eventos_Venta_Boletos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Métricas/estimaciones agregadas del organizador, calculadas en tiempo real
 * a partir de los eventos y boletos que tiene guardados en la base de datos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetricasOrganizadorDTO {

    private Double ingresosTotales;
    private Integer boletosVendidos;
    private Integer capacidadTotal;
    private Integer boletosRestantes;
    private Double porcentajeOcupacion;
    private Double ticketPromedio;
}
