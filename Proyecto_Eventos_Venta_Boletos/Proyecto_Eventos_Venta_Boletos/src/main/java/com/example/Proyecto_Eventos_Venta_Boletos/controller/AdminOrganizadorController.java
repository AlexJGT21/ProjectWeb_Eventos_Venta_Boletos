import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/organizadores")
public class AdminOrganizadorController {

    private final AdminService adminService;

    @Autowired
    public AdminOrganizadorController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Muestra la vista con la lista de organizadores
    @GetMapping
    public String listarOrganizadores(Model model) {
        model.addAttribute("organizadores", adminService.obtenerTodosLosOrganizadores());
        // Se envía un objeto vacío para el formulario de registro
        model.addAttribute("nuevoOrganizador", new Organizador());
        return "admin/organizadores";
    }

    // Procesa el formulario de registro
    @PostMapping("/registrar")
    public String registrarOrganizador(@ModelAttribute("nuevoOrganizador") Organizador organizador) {
        adminService.registrarOrganizador(organizador);
        // Se redirige a la ruta GET para evitar que al recargar la página se vuelva a enviar el formulario
        return "redirect:/admin/organizadores?exito";
    }

    // Procesa la solicitud de eliminación
    @PostMapping("/eliminar")
    public String eliminarOrganizador(@RequestParam("id") Long id) {
        adminService.eliminarOrganizador(id);
        // Se redirige a la ruta GET para actualizar la lista de forma segura
        return "redirect:/admin/organizadores?eliminado";
    }
}