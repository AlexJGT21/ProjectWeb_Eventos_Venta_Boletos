package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.dto.ResumenCompraDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.ZonaVentaDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.request.ProcesarPagoDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.response.CompraResponseDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.response.ConfirmacionResponseDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Compra;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class VentaApiController {

    private final CompraService compraService;
    private final EventoRepository eventoRepository;

    public VentaApiController(CompraService compraService, EventoRepository eventoRepository) {
        this.compraService = compraService;
        this.eventoRepository = eventoRepository;
    }

    // Opciones y disponibilidad de para la vista de venta
    @GetMapping("/eventos/{id}/opciones-venta")
    public ResponseEntity<List<ZonaVentaDTO>> obtenerOpcionesVenta(@PathVariable Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El evento solicitado no existe"));
        return ResponseEntity.ok(compraService.opcionesDeVenta(evento));
    }

    // Cotización de la compra en tiempo real
    @PostMapping("/compras/cotizar")
    public ResponseEntity<ResumenCompraDTO> cotizarCompra(
            @RequestParam Long eventoId,
            @RequestParam Long tipoId,
            @RequestParam(required = false) String asientos,
            @RequestParam(required = false, defaultValue = "1") Integer cantidad) {

        ResumenCompraDTO resumen = compraService.cotizar(eventoId, tipoId, asientos, cantidad);
        return ResponseEntity.ok(resumen);
    }

    // Registro de compra y cobro
    @PostMapping("/compras")
    public ResponseEntity<CompraResponseDTO> procesarPago(@Valid @RequestBody ProcesarPagoDTO dto) {
        String nombreCompleto = dto.getFullName().trim() + " " + dto.getFirstLastName().trim() +
                (dto.getSecondLastName() != null && !dto.getSecondLastName().isBlank() ? " " + dto.getSecondLastName().trim() : "");

        Compra compra = compraService.registrarCompra(
                dto.getEventoId(),
                dto.getTipoId(),
                dto.getAsientos(),
                dto.getCantidad(),
                nombreCompleto,
                dto.getEmail(),
                dto.getPhone(),
                dto.getPaymentMethod()
        );

        // Transformación de la entidad Compra al DTO de respuesta
        CompraResponseDTO responseDTO = CompraResponseDTO.builder()
                .id(compra.getId())
                .codigoPedido(compra.getCodigoPedido())
                .fechaHoraCompra(compra.getFechaHora())
                .metodoPago(compra.getMetodoPago())
                .montoTotal(compra.getMontoTotal())
                .totalVendidos(compra.getTotalVendidos())
                .asistenteId(compra.getAsistente() != null ? compra.getAsistente().getId() : null)
                .asistenteNombre(compra.getAsistente() != null ? compra.getAsistente().getNombre() : null)
                .asistenteCorreo(compra.getAsistente() != null ? compra.getAsistente().getCorreo() : null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/compras/{id}")
    public ResponseEntity<ConfirmacionResponseDTO> obtenerConfirmacion(@PathVariable Long id) {
        Map<String, Object> datos = compraService.datosConfirmacion(id);

        ConfirmacionResponseDTO dto = ConfirmacionResponseDTO.builder()
                .eventoId((Long) datos.get("eventoId"))
                .eventoNombre((String) datos.get("eventoNombre"))
                .eventoLugar((String) datos.get("eventoLugar"))
                .eventoFecha((String) datos.get("eventoFecha"))
                .titularNombre((String) datos.get("titularNombre"))
                .titularEmail((String) datos.get("titularEmail"))
                .titularTelefono((String) datos.get("titularTelefono"))
                .codigoPedido((String) datos.get("codigoPedido"))
                .codigoAcceso((String) datos.get("codigoAcceso"))
                .fechaCompra((String) datos.get("fechaCompra"))
                .metodoPago((String) datos.get("metodoPago"))
                .seccion((String) datos.get("seccion"))
                .fila((String) datos.get("fila"))
                .asientos((String) datos.get("asientos"))
                .cantidadBoletos((Integer) datos.get("cantidadBoletos"))
                .totalPagado((Double) datos.get("totalPagado"))
                .build();

        return ResponseEntity.ok(dto);
    }
}