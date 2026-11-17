package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Administrador;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.AdministradorRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.CompraRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Lógica del panel de administración: métricas globales, alta y baja de organizadores
 * y control de los eventos publicados por cualquier organizador.
 */
@Service
public class AdminService {

    private static final String PATRON_CORREO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final AdministradorRepository administradorRepository;
    private final OrganizadorRepository organizadorRepository;
    private final EventoRepository eventoRepository;
    private final CompraRepository compraRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdministradorRepository administradorRepository,
                        OrganizadorRepository organizadorRepository,
                        EventoRepository eventoRepository,
                        CompraRepository compraRepository,
                        PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.organizadorRepository = organizadorRepository;
        this.eventoRepository = eventoRepository;
        this.compraRepository = compraRepository;
        this.passwordEncoder = passwordEncoder; // Requerido para cifrar credenciales
    }

    // --- Administradores ---

    @Transactional
    public Administrador registrarAdministrador(Administrador admin) {
        String correo = normalizarCorreo(admin.getCorreo());
        // No se permiten correos duplicados (ni repetidos con un organizador)
        if (correoRegistrado(correo)) {
            throw new IllegalStateException("El correo ya está registrado en el sistema.");
        }
        admin.setCorreo(correo);
        // Las contraseñas se almacenan cifradas con BCrypt
        admin.setContrasenia(passwordEncoder.encode(admin.getContrasenia()));
        return administradorRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public boolean existeAdministrador(String correo) {
        return administradorRepository.existsByCorreo(normalizarCorreo(correo));
    }

    /** Nombre del administrador con sesión (para el header y el menú lateral). */
    @Transactional(readOnly = true)
    public Optional<String> buscarNombrePorCorreo(String correo) {
        return administradorRepository.findByCorreo(correo).map(Administrador::getNombre);
    }

    // --- Organizadores ---

    @Transactional(readOnly = true)
    public List<Organizador> obtenerTodosLosOrganizadores() {
        return organizadorRepository.findAll(Sort.by("nombre"));
    }

    /**
     * Da de alta un organizador. Solo se toman los campos del formulario (nunca el id),
     * para que no se pueda sobrescribir a un organizador existente.
     */
    @Transactional
    public Organizador registrarOrganizador(Organizador datos) {
        String nombre = datos.getNombre() == null ? "" : datos.getNombre().trim();
        String telefono = datos.getTelefono() == null ? "" : datos.getTelefono().trim();
        String correo = normalizarCorreo(datos.getCorreo());
        String contrasenia = datos.getContrasenia() == null ? "" : datos.getContrasenia();

        if (nombre.length() < 3 || nombre.length() > 50) {
            throw new IllegalStateException("El nombre debe tener entre 3 y 50 caracteres.");
        }
        if (telefono.isEmpty()) {
            throw new IllegalStateException("Escribe un teléfono de contacto.");
        }
        if (!correo.matches(PATRON_CORREO)) {
            throw new IllegalStateException("El correo electrónico no es válido.");
        }
        if (correoRegistrado(correo)) {
            throw new IllegalStateException("Ya existe un usuario con el correo " + correo + ".");
        }
        if (contrasenia.length() < 8) {
            throw new IllegalStateException("La contraseña debe tener al menos 8 caracteres.");
        }

        Organizador organizador = Organizador.builder()
                .nombre(nombre)
                .telefono(telefono)
                .correo(correo)
                .contrasenia(passwordEncoder.encode(contrasenia))
                .build();
        return organizadorRepository.save(organizador);
    }

    /** Solo se puede eliminar un organizador sin eventos (borrarlo eliminaría también sus eventos y ventas). */
    @Transactional
    public void eliminarOrganizador(Long id) {
        Organizador organizador = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("El organizador no existe."));
        long eventos = eventoRepository.countByOrganizadorId(id);
        if (eventos > 0) {
            throw new IllegalStateException("No se puede eliminar a «" + organizador.getNombre() + "» porque tiene "
                    + eventos + (eventos == 1 ? " evento registrado." : " eventos registrados."));
        }
        organizadorRepository.delete(organizador);
    }

    // --- Eventos (control global) ---

    @Transactional(readOnly = true)
    public List<Evento> obtenerTodosLosEventos() {
        return eventoRepository.findAll(Sort.by("fechaHoraEvento"));
    }

    /** Próximos eventos (de cualquier organizador) para el resumen del dashboard. */
    @Transactional(readOnly = true)
    public List<Evento> obtenerProximosEventos(int cantidad) {
        LocalDateTime ahora = LocalDateTime.now();
        return obtenerTodosLosEventos().stream()
                .filter(e -> e.getFechaHoraEvento() != null && e.getFechaHoraEvento().isAfter(ahora))
                .limit(cantidad)
                .toList();
    }

    /** Cancela un evento por incumplimiento: deja de mostrarse en el catálogo y ya no vende boletos. */
    @Transactional
    public void cancelarEventoPorIncumplimiento(Long idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalStateException("El evento especificado no existe."));
        if (evento.getEstadoEvento() == EstadoEventoENUM.CANCELADO) {
            throw new IllegalStateException("El evento «" + evento.getNombre() + "» ya estaba cancelado.");
        }
        evento.setEstadoEvento(EstadoEventoENUM.CANCELADO);
        eventoRepository.save(evento);
    }

    /** Revierte una cancelación: el evento vuelve a quedar programado. */
    @Transactional
    public void reactivarEvento(Long idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalStateException("El evento especificado no existe."));
        if (evento.getEstadoEvento() != EstadoEventoENUM.CANCELADO) {
            throw new IllegalStateException("Solo se pueden reactivar eventos cancelados.");
        }
        evento.setEstadoEvento(EstadoEventoENUM.PROGRAMADO);
        eventoRepository.save(evento);
    }

    // --- Métricas para el dashboard ---

    @Transactional(readOnly = true)
    public long obtenerTotalOrganizadores() {
        return organizadorRepository.count();
    }

    @Transactional(readOnly = true)
    public long obtenerTotalEventos() {
        return eventoRepository.count();
    }

    @Transactional(readOnly = true)
    public long obtenerEventosCancelados() {
        return eventoRepository.countByEstadoEvento(EstadoEventoENUM.CANCELADO);
    }

    @Transactional(readOnly = true)
    public long obtenerTotalCompras() {
        return compraRepository.count();
    }

    @Transactional(readOnly = true)
    public long obtenerBoletosVendidos() {
        Long total = compraRepository.sumarBoletosVendidos();
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public double obtenerIngresosTotales() {
        Double total = compraRepository.sumarIngresos();
        return total != null ? total : 0.0;
    }

    // --- Auxiliares ---

    private boolean correoRegistrado(String correo) {
        return administradorRepository.existsByCorreo(correo) || organizadorRepository.existsByCorreo(correo);
    }

    private static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }
}
