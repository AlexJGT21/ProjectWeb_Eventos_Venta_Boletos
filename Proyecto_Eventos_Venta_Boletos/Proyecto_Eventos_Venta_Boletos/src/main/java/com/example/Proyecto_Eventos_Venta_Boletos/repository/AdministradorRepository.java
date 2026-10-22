import com.example.Proyecto_Eventos_Venta_Boletos.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    // Método esencial para buscar al administrador por su usuario/correo durante el login
    Optional<Administrador> findByEmail(String email);
}