package com.example.Proyecto_Eventos_Venta_Boletos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila de la tabla "Gestión de Boletos por Tipo y Aforo" del panelOrganizador.
 * Se construye a partir de los Boleto (tipos/lotes) reales guardados en BD para
 * los eventos del organizador, nunca con datos inventados.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventarioBoletoDTO {

    private String tipoBoleto;
    private String descripcion;
    private String iconoCss;
    private Double precio;
    private Integer vendidos;
    private Integer capacidad;
    private Double porcentaje;
    private Double ingresoGenerado;
    private String estado;
}
