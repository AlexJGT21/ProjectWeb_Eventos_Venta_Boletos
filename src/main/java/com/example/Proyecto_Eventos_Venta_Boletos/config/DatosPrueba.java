package com.example.Proyecto_Eventos_Venta_Boletos.config;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.service.AdminService;
import com.example.Proyecto_Eventos_Venta_Boletos.service.CompraService;
import com.example.Proyecto_Eventos_Venta_Boletos.service.EventoService;
import com.example.Proyecto_Eventos_Venta_Boletos.service.OrganizadorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carga eventos y compras de prueba la primera vez que arranca el sistema (cuando todavía no hay
 * ningún evento), para que el catálogo, la venta y los paneles se vean funcionando.
 *
 * Todo se crea a través de los servicios, con las mismas validaciones que usa la aplicación.
 * Se puede desactivar con konticket.datos-prueba=false en application.properties.
 */
@Component
@Order(3) // Después de OrganizadorInicial y AdministradorInicial
public class DatosPrueba implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosPrueba.class);

    private static final String CORREO_ORGANIZADOR = "organizador@konticket.mx";
    private static final String CORREO_SEGUNDO_ORGANIZADOR = "sonoralive@konticket.mx";
    private static final String METODO_PAGO = "Tarjeta de Crédito / Débito";

    private record Cliente(String nombre, String correo, String telefono) {}

    private static final List<Cliente> CLIENTES = List.of(
            new Cliente("Ana Torres Ríos", "ana.torres@example.com", "6441000001"),
            new Cliente("Luis Ramírez Soto", "luis.ramirez@example.com", "6441000002"),
            new Cliente("Sofía Méndez Lara", "sofia.mendez@example.com", "6441000003"),
            new Cliente("Diego Castro Félix", "diego.castro@example.com", "6441000004"),
            new Cliente("Valeria Ruiz Acosta", "valeria.ruiz@example.com", "6441000005"),
            new Cliente("Jorge Salazar Peña", "jorge.salazar@example.com", "6441000006"),
            new Cliente("Camila Ortega Valdez", "camila.ortega@example.com", "6441000007"),
            new Cliente("Andrés Navarro Gil", "andres.navarro@example.com", "6441000008"));

    private final boolean activo;
    private final EventoService eventoService;
    private final OrganizadorService organizadorService;
    private final AdminService adminService;
    private final CompraService compraService;

    private int compras = 0;

    public DatosPrueba(@Value("${konticket.datos-prueba:true}") boolean activo,
                       EventoService eventoService, OrganizadorService organizadorService,
                       AdminService adminService, CompraService compraService) {
        this.activo = activo;
        this.eventoService = eventoService;
        this.organizadorService = organizadorService;
        this.adminService = adminService;
        this.compraService = compraService;
    }

    @Override
    public void run(String... args) {
        if (!activo || eventoService.hayEventos()) {
            return;
        }
        try {
            cargarDatos();
            log.info("Datos de prueba cargados: 6 eventos y {} compras.", compras);
        } catch (RuntimeException e) {
            // Si algo falla, la aplicación arranca de todas formas (sin datos de prueba completos)
            log.error("No se pudieron cargar los datos de prueba", e);
        }
    }

    private void cargarDatos() {
        Organizador organizador = organizadorService.obtenerPorCorreo(CORREO_ORGANIZADOR);
        Organizador segundo = segundoOrganizador();

        // 1. Entrada general con dos tipos de boleto
        Evento neon = crearEvento(organizador, "Noche Indie: Neon Waves Fest",
                "La cumbre del synthwave y dream-pop latinoamericano con 6 bandas en vivo.",
                "Indie", "Desierto Bar · Cd. Obregón", 5, LocalTime.of(21, 0), "GENERAL",
                List.of("General", "VIP"), List.of(250.0, 450.0), List.of(300, 80),
                List.of("Acceso general de pie", "Zona frente al escenario y barra exclusiva"));
        comprar(neon, "General", null, 4, 25);
        comprar(neon, "General", null, 2, 20);
        comprar(neon, "VIP", null, 2, 18);
        comprar(neon, "General", null, 6, 14);
        comprar(neon, "General", null, 3, 9);
        comprar(neon, "VIP", null, 4, 7);
        comprar(neon, "General", null, 8, 5);
        comprar(neon, "VIP", null, 2, 3);
        comprar(neon, "General", null, 2, 2);
        comprar(neon, "General", null, 5, 1);
        comprar(neon, "VIP", null, 1, 0);

        // 2. Mapa numerado (los nombres de los boletos son zonas de la plantilla del recinto)
        Evento techno = crearEvento(organizador, "Techno Underground: Dark Beats",
                "Una noche inmersiva con DJs invitados y un sistema de audio envolvente de 360°.",
                "Electrónica", "Arena Metropolitana · Cd. Obregón", 12, LocalTime.of(22, 0), "NUMERADO",
                List.of("Zona Fan Pie", "Zona Fan Sillas", "Palco VIP", "General", "Preferente"),
                List.of(400.0, 650.0, 1200.0, 300.0, 500.0), List.of(1500, 0, 0, 0, 0),
                List.of("Cancha general de pie", "Sillas frente al escenario", "Palcos frente a la cancha",
                        "Gradería oriente", "Gradería preferente"));
        comprar(techno, "Zona Fan Pie", null, 6, 22);
        comprar(techno, "Zona Fan Sillas", "A1,A2,A3,A4", 0, 19);
        comprar(techno, "Zona Fan Pie", null, 8, 16);
        comprar(techno, "Palco VIP", "A1,A2", 0, 15);
        comprar(techno, "Zona Fan Sillas", "B5,B6", 0, 12);
        comprar(techno, "General", "D1,D2,D3", 0, 11);
        comprar(techno, "Zona Fan Pie", null, 4, 10);
        comprar(techno, "Palco VIP", "B3,B4,B5,B6", 0, 8);
        comprar(techno, "Zona Fan Sillas", "C10,C11,C12", 0, 6);
        comprar(techno, "Zona Fan Pie", null, 7, 4);
        comprar(techno, "Preferente", "E7,E8", 0, 3);
        comprar(techno, "Zona Fan Sillas", "A5,A6", 0, 2);
        comprar(techno, "Zona Fan Pie", null, 3, 1);

        // 3. Otro evento con mapa numerado
        Evento rock = crearEvento(organizador, "Rock en el Desierto",
                "Cinco bandas de rock del noroeste en un festival al aire libre bajo las estrellas.",
                "Rock", "Explanada del Estadio Municipal · Cd. Obregón", 20, LocalTime.of(19, 30), "NUMERADO",
                List.of("Lateral Azul", "Lateral Café", "Preferente", "General", "Zona Fan Pie"),
                List.of(350.0, 350.0, 550.0, 280.0, 450.0), List.of(0, 0, 0, 0, 800),
                List.of("Gradería lateral", "Gradería lateral", "Gradería preferente", "Gradería oriente",
                        "Cancha general de pie"));
        comprar(rock, "Lateral Azul", "A1,A2", 0, 13);
        comprar(rock, "Lateral Café", "B9,B10", 0, 9);
        comprar(rock, "Zona Fan Pie", null, 5, 7);
        comprar(rock, "Lateral Azul", "C4,C5,C6", 0, 6);
        comprar(rock, "Preferente", "A1,A2,A3,A4", 0, 5);
        comprar(rock, "Zona Fan Pie", null, 8, 3);
        comprar(rock, "General", "H1,H2", 0, 2);
        comprar(rock, "Zona Fan Pie", null, 2, 0);

        // 4. Evento del segundo organizador, con preventa
        Evento urbano = crearEvento(segundo, "Festival Urbano Sonora",
                "Rap, reggaetón y dancehall con artistas locales y food trucks durante toda la tarde.",
                "Urbano", "Plaza Álvaro Obregón · Cd. Obregón", 28, LocalTime.of(16, 0), "GENERAL",
                List.of("Preventa", "General", "VIP"), List.of(180.0, 260.0, 520.0), List.of(200, 800, 100),
                List.of("Precio especial por tiempo limitado", "Acceso general", "Acceso preferente y zona lounge"));
        comprar(urbano, "Preventa", null, 8, 27);
        comprar(urbano, "Preventa", null, 8, 24);
        comprar(urbano, "Preventa", null, 6, 21);
        comprar(urbano, "Preventa", null, 5, 17);
        comprar(urbano, "Preventa", null, 4, 10);
        comprar(urbano, "General", null, 3, 4);
        comprar(urbano, "VIP", null, 2, 1);

        // 5. Casi lleno (aparece como "POCOS BOLETOS" en el inventario del panel)
        Evento jazz = crearEvento(organizador, "Noche Acústica & Jazz Latino",
                "Un recorrido íntimo por los clásicos del bolero y el jazz moderno.",
                "Indie", "Teatro de la Ciudad · Cd. Obregón", 3, LocalTime.of(20, 0), "GENERAL",
                List.of("General", "Mesa VIP"), List.of(220.0, 480.0), List.of(60, 16),
                List.of("Butaca sin numerar", "Mesa para cuatro personas junto al escenario"));
        comprar(jazz, "General", null, 8, 20);
        comprar(jazz, "General", null, 8, 15);
        comprar(jazz, "General", null, 8, 11);
        comprar(jazz, "General", null, 8, 8);
        comprar(jazz, "Mesa VIP", null, 4, 6);
        comprar(jazz, "General", null, 6, 5);
        comprar(jazz, "General", null, 7, 2);
        comprar(jazz, "General", null, 4, 1);

        // 6. Agotado (aparece con el filtro "Agotados" del catálogo)
        Evento garage = crearEvento(segundo, "Distorsión Club: Garage & Riot Rock",
                "Tres bandas de garage y punk en un foro pequeño. Cupo muy limitado.",
                "Rock", "Foro Independiente · Cd. Obregón", 8, LocalTime.of(21, 30), "GENERAL",
                List.of("General"), List.of(190.0), List.of(40),
                List.of("Acceso general de pie"));
        comprar(garage, "General", null, 8, 26);
        comprar(garage, "General", null, 8, 19);
        comprar(garage, "General", null, 8, 12);
        comprar(garage, "General", null, 8, 6);
        comprar(garage, "General", null, 8, 1);
    }

    private Organizador segundoOrganizador() {
        try {
            return organizadorService.obtenerPorCorreo(CORREO_SEGUNDO_ORGANIZADOR);
        } catch (IllegalArgumentException e) {
            return adminService.registrarOrganizador(Organizador.builder()
                    .nombre("Sonora Live Producciones")
                    .telefono("6442000000")
                    .correo(CORREO_SEGUNDO_ORGANIZADOR)
                    .contrasenia("Konticket123")
                    .build());
        }
    }

    private Evento crearEvento(Organizador organizador, String nombre, String descripcion, String categoria,
                               String lugar, int diasParaEvento, LocalTime hora, String modalidad,
                               List<String> tipos, List<Double> precios, List<Integer> aforos,
                               List<String> descripciones) {
        Evento evento = Evento.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .lugar(lugar)
                .fechaHoraEvento(LocalDate.now().plusDays(diasParaEvento).atTime(hora))
                .estadoEvento(EstadoEventoENUM.PROGRAMADO)
                .modalidadAsientos(modalidad)
                .build();
        // Mismo método que usa el formulario del organizador (sin imágenes; el sitio muestra la imagen por defecto)
        List<Long> idsNuevos = new ArrayList<>(Collections.nCopies(tipos.size(), (Long) null));
        return organizadorService.procesarYGuardarEvento(evento, null, null, organizador.getId(),
                idsNuevos, tipos, precios, aforos, descripciones);
    }

    /** Registra una compra hecha hace {@code diasAtras} días (asientos solo en zonas numeradas). */
    private void comprar(Evento evento, String tipoNombre, String asientos, int cantidad, int diasAtras) {
        Boleto tipo = evento.getTiposBoleto().stream()
                .filter(t -> t.getTipoNombre().equals(tipoNombre))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el boleto " + tipoNombre + " en " + evento.getNombre()));
        Cliente cliente = CLIENTES.get(compras % CLIENTES.size());
        LocalDateTime fecha = LocalDateTime.now().minusDays(diasAtras).minusHours(compras % 6);

        compraService.registrarCompra(evento.getId(), tipo.getId(), asientos, cantidad,
                cliente.nombre(), cliente.correo(), cliente.telefono(), METODO_PAGO, fecha);
        compras++;
    }
}
