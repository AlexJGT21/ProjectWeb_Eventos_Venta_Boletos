import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Se delega la obtención de métricas al servicio
        long totalOrganizadores = adminService.obtenerTotalOrganizadores();
        long totalEventos = adminService.obtenerTotalEventos();

        model.addAttribute("titulo", "Panel de Control Administrativo");
        model.addAttribute("totalOrganizadores", totalOrganizadores);
        model.addAttribute("totalEventos", totalEventos);

        return "admin/dashboard";
    }

    // Ejemplo de formulario aplicando estrictamente el patrón POST/Redirect/GET
    @PostMapping("/actualizar-metricas")
    public String actualizarMetricasSistema() {
        // Aquí iría el llamado al servicio para procesar alguna acción (POST)
        // adminService.ejecutarProceso(...);

        // Redirección inmediata a una ruta GET para evitar reenvío de formularios
        return "redirect:/admin/dashboard?actualizado=true";
    }
}