package com.example.Proyecto_Eventos_Venta_Boletos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProcesarPagoDTO {

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventoId;

    @NotNull(message = "El ID del tipo de boleto es obligatorio")
    private Long tipoId;

    private String asientos;

    private Integer cantidad;

    @NotBlank(message = "El nombre del titular es obligatorio")
    private String fullName;

    @NotBlank(message = "El primer apellido es obligatorio")
    private String firstLastName;

    private String secondLastName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe contener exactamente 10 dígitos")
    private String phone;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String email;

    @NotBlank(message = "El método de pago es obligatorio")
    private String paymentMethod;
}