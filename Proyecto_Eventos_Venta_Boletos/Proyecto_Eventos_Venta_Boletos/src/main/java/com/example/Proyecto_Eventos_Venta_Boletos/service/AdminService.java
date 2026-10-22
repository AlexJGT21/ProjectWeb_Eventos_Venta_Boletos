import com.example.Proyecto_Eventos_Venta_Boletos.repository.AdministradorRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdministradorRepository administradorRepository;
    private final OrganizadorRepository organizadorRepository;
    private final EventoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(AdministradorRepository administradorRepository,
                        OrganizadorRepository organizadorRepository,
                        EventoRepository eventoRepository,
                        PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.organizadorRepository = organizadorRepository;
        this.eventoRepository = eventoRepository;
        this.passwordEncoder = passwordEncoder; // Requerido para cifrar credenciales
    }

    // logica de admins
    public Administrador registrarAdministrador(Administrador admin) {
        // no se permiten correos duplicados
        if (administradorRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema.");
        }
        // las contraseñas se almacenan cifradas con BCrypt
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return administradorRepository.save(admin);
    }

    // logica de organizadores
    public Organizador registrarOrganizador(Organizador organizador) {
        return organizadorRepository.save(organizador);
    }

    public List<Organizador> obtenerTodosLosOrganizadores() {
        return organizadorRepository.findAll();
    }

    public void eliminarOrganizador(Long id) {
        organizadorRepository.deleteById(id);
    }

    // logica de ventos (control global)
    public List<Evento> obtenerTodosLosEventos() {
        return eventoRepository.findAll();
    }

    public void cancelarEventoPorIncumplimiento(Long idEvento) {
        Optional<Evento> eventoOpt = eventoRepository.findById(idEvento);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            // Descomenta y ajusta según tu atributo de estado en Evento.java
            // evento.setEstado(EstadoEvento.CANCELADO);
            eventoRepository.save(evento);
        } else {
            throw new IllegalArgumentException("El evento especificado no existe.");
        }
    }

    // metricas para el dashboard
    public long obtenerTotalOrganizadores() {
        return organizadorRepository.count();
    }

    public long obtenerTotalEventos() {
        return eventoRepository.count();
    }
}