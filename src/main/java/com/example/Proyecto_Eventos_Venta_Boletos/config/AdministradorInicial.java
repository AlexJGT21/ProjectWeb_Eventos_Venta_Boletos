package com.example.Proyecto_Eventos_Venta_Boletos.config;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Administrador;
import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Asegura que exista un administrador para poder entrar al panel de administración:
 *   correo: admin@konticket.mx   contraseña: Admin12345
 * Si ya está registrado no se modifica.
 */
@Component
@Order(2)
public class AdministradorInicial implements CommandLineRunner {

    private static final String CORREO_INICIAL = "admin@konticket.mx";
    private static final String CONTRASENIA_INICIAL = "Admin12345";

    private final AdminService adminService;

    public AdministradorInicial(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) {
        if (adminService.existeAdministrador(CORREO_INICIAL)) {
            return;
        }
        adminService.registrarAdministrador(Administrador.builder()
                .nombre("Admin Konticket")
                .edad(30)
                .correo(CORREO_INICIAL)
                .contrasenia(CONTRASENIA_INICIAL) // el servicio la guarda cifrada con BCrypt
                .telefono("0000000000")
                .build());
    }
}
