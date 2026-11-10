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
public class CompraResponseDTO {

    private Long id;
    private String codigoPedido;
    private LocalDateTime fechaHoraCompra;
    private String metodoPago;
    private Double montoTotal;
    private Integer totalVendidos;
    private Long asistenteId;
    private String asistenteNombre;
    private String asistenteCorreo;
}