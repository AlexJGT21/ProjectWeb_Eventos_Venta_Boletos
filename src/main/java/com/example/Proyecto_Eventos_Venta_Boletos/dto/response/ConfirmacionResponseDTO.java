package com.example.Proyecto_Eventos_Venta_Boletos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmacionResponseDTO {

    private Long eventoId;
    private String eventoNombre;
    private String eventoLugar;
    private String eventoFecha;
    private String titularNombre;
    private String titularEmail;
    private String titularTelefono;
    private String codigoPedido;
    private String codigoAcceso;
    private String fechaCompra;
    private String metodoPago;
    private String seccion;
    private String fila;
    private String asientos;
    private Integer cantidadBoletos;
    private Double totalPagado;
}