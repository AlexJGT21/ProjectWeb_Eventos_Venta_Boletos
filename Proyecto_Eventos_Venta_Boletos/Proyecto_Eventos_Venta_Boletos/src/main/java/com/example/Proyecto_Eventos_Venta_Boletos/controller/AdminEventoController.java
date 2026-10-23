import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/eventos")
public class AdminEventoController {

    private final AdminService adminService;

    @Autowired
    public AdminEventoController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Muestra la lista de todos los eventos globales
    @GetMapping
    public String listarEventos(Model model) {
        // La obtención de datos se delega al servicio
        model.addAttribute("eventos", adminService.obtenerTodosLosEventos());
        return "admin/eventos";
    }

    // Procesa la cancelación de un evento por incumplimiento
    @PostMapping("/cancelar")
    public String cancelarEvento(@RequestParam("id") Long id) {
        adminService.cancelarEventoPorIncumplimiento(id);
        // REDIRECT: Redirección a la ruta GET para cumplir con el patrón PRG
        return "redirect:/admin/eventos?cancelado";
    }
}