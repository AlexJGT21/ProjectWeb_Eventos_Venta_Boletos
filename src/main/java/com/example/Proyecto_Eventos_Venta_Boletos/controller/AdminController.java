package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Panel de control del administrador. Todas las rutas /admin/** exigen el rol ADMIN (ver SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping({"", "/"})
    public String inicio() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Se delega la obtención de métricas al servicio
        model.addAttribute("titulo", "Panel de Control Administrativo");
        model.addAttribute("totalOrganizadores", adminService.obtenerTotalOrganizadores());
        model.addAttribute("totalEventos", adminService.obtenerTotalEventos());
        model.addAttribute("eventosCancelados", adminService.obtenerEventosCancelados());
        model.addAttribute("totalCompras", adminService.obtenerTotalCompras());
        model.addAttribute("boletosVendidos", adminService.obtenerBoletosVendidos());
        model.addAttribute("ingresosTotales", adminService.obtenerIngresosTotales());
        model.addAttribute("proximosEventos", adminService.obtenerProximosEventos(5));

        return "admin/dashboard";
    }
}
