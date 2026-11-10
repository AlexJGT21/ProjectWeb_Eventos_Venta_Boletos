package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.BoletoEstadoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.ResumenCompraDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.dto.ZonaVentaDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Asistente;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Compra;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.AsistenteRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.BoletoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.CompraRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.util.MapaRecinto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Venta de boletos: arma las opciones de compra de un evento (zonas del mapa o tipos de boleto),
 * cotiza en el servidor y registra la compra guardando un Boleto por cada asiento/entrada vendida.
 */
@Service
public class CompraService {

    public static final BigDecimal TASA_SERVICIO = new BigDecimal("0.12");
    public static final BigDecimal TASA_IVA = new BigDecimal("0.16");
    public static final int MAXIMO_POR_COMPRA = 8;

    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EventoRepository eventoRepository;
    private final BoletoRepository boletoRepository;
    private final CompraRepository compraRepository;
    private final AsistenteRepository asistenteRepository;
    private final PasswordEncoder passwordEncoder;

    public CompraService(EventoRepository eventoRepository, BoletoRepository boletoRepository,
                         CompraRepository compraRepository, AsistenteRepository asistenteRepository,
                         PasswordEncoder passwordEncoder) {
        this.eventoRepository = eventoRepository;
        this.boletoRepository = boletoRepository;
        this.compraRepository = compraRepository;
        this.asistenteRepository = asistenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------------- Opciones de venta

    /** El evento usa el mapa si es de "Mapa Numerado" y al menos un tipo de boleto coincide con una zona. */
    public boolean usaMapa(Evento evento) {
        return "NUMERADO".equals(evento.getModalidadAsientos())
                && evento.getTiposBoleto().stream()
                        .anyMatch(t -> MapaRecinto.porNombreDeBoleto(t.getTipoNombre()).isPresent());
    }

    @Transactional(readOnly = true)
    public List<ZonaVentaDTO> opcionesDeVenta(Evento evento) {
        List<ZonaVentaDTO> opciones = new ArrayList<>();
        // Evento pasado, pausado, cancelado o agotado: se muestran las zonas pero nada está a la venta
        boolean abierto = evento.isDisponible();

        if (usaMapa(evento)) {
            for (MapaRecinto.Zona zona : MapaRecinto.ZONAS) {
                Boleto tipo = tipoDeZona(evento, zona).orElse(null);
                List<String> ocupados = (tipo != null && zona.isNumerada())
                        ? boletoRepository.asientosOcupados(evento.getId(), zona.codigo())
                        : List.of();

                int disponibles = 0;
                if (abierto && tipo != null && tipo.getEstado() == BoletoEstadoENUM.DISPONIBLE) {
                    disponibles = tipo.getRestantes();
                    if (zona.isNumerada()) {
                        disponibles = Math.min(disponibles, zona.getTotalAsientos() - ocupados.size());
                    }
                }

                opciones.add(ZonaVentaDTO.builder()
                        .codigo(zona.codigo())
                        .nombre(zona.nombre())
                        .descripcion(zona.descripcion())
                        .color(zona.color())
                        .iniciales(iniciales(zona.nombre()))
                        .tipoId(tipo != null ? tipo.getId() : null)
                        .precio(tipo != null ? tipo.getPrecio() : null)
                        .disponibles(Math.max(disponibles, 0))
                        .numerada(zona.isNumerada())
                        .filas(String.join(",", zona.filas()))
                        .asientosPorFila(zona.asientosPorFila())
                        .pasillos(String.join(",", zona.pasillos().stream().map(String::valueOf).toList()))
                        .ocupados(String.join(",", ocupados))
                        .totalAsientos(zona.getTotalAsientos())
                        .build());
            }
        } else {
            // Entrada general: cada tipo de boleto es una opción, sin asientos
            for (Boleto tipo : evento.getTiposBoleto()) {
                String nombre = tipo.getTipoNombre() != null ? tipo.getTipoNombre() : "General";
                opciones.add(ZonaVentaDTO.builder()
                        .codigo("TIPO_" + tipo.getId())
                        .nombre(nombre)
                        .descripcion(tipo.getDescripcion() != null ? tipo.getDescripcion() : "Admisión general")
                        .color("#E6005C")
                        .iniciales(iniciales(nombre))
                        .tipoId(tipo.getId())
                        .precio(tipo.getPrecio())
                        .disponibles(abierto && tipo.getEstado() == BoletoEstadoENUM.DISPONIBLE ? tipo.getRestantes() : 0)
                        .numerada(false)
                        .filas("")
                        .pasillos("")
                        .ocupados("")
                        .build());
            }
        }
        return opciones;
    }

    private Optional<Boleto> tipoDeZona(Evento evento, MapaRecinto.Zona zona) {
        return evento.getTiposBoleto().stream()
                .filter(t -> MapaRecinto.porNombreDeBoleto(t.getTipoNombre())
                        .map(z -> z.codigo().equals(zona.codigo()))
                        .orElse(false))
                .findFirst();
    }

    private static String iniciales(String nombre) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : nombre.trim().split("\\s+")) {
            if (!palabra.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(palabra.charAt(0)));
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- Cotización

    @Transactional(readOnly = true)
    public ResumenCompraDTO cotizar(Long eventoId, Long tipoId, String asientos, Integer cantidad) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalStateException("El evento no existe."));
        Boleto tipo = boletoRepository.findById(tipoId)
                .orElseThrow(() -> new IllegalStateException("Selecciona una zona o tipo de boleto."));
        return validarYCalcular(evento, tipo, separarAsientos(asientos), cantidad != null ? cantidad : 0);
    }

    private ResumenCompraDTO validarYCalcular(Evento evento, Boleto tipo, List<String> asientos, int cantidad) {
        if (!tipo.isTipo() || tipo.getEvento() == null || !tipo.getEvento().getId().equals(evento.getId())) {
            throw new IllegalStateException("El boleto seleccionado no pertenece a este evento.");
        }
        if (!evento.isDisponible()) {
            throw new IllegalStateException("Este evento ya no tiene boletos a la venta.");
        }
        if (tipo.getEstado() != BoletoEstadoENUM.DISPONIBLE || tipo.getRestantes() == 0) {
            throw new IllegalStateException("Los boletos «" + tipo.getTipoNombre() + "» están agotados.");
        }

        Optional<MapaRecinto.Zona> zona = usaMapa(evento)
                ? MapaRecinto.porNombreDeBoleto(tipo.getTipoNombre())
                : Optional.empty();

        List<String> asientosFinales = List.of();
        if (zona.isPresent() && zona.get().isNumerada()) {
            if (asientos.isEmpty()) {
                throw new IllegalStateException("Selecciona al menos un asiento en el mapa.");
            }
            for (String asiento : asientos) {
                if (!zona.get().existeAsiento(asiento)) {
                    throw new IllegalStateException("El asiento " + asiento + " no existe en " + zona.get().nombre() + ".");
                }
            }
            Set<String> ocupados = new HashSet<>(boletoRepository.asientosOcupados(evento.getId(), zona.get().codigo()));
            for (String asiento : asientos) {
                if (ocupados.contains(asiento)) {
                    throw new IllegalStateException("El asiento " + asiento + " ya fue vendido. Elige otro.");
                }
            }
            asientosFinales = asientos;
            cantidad = asientos.size();
        }

        if (cantidad < 1) {
            throw new IllegalStateException("Selecciona al menos un boleto.");
        }
        if (cantidad > MAXIMO_POR_COMPRA) {
            throw new IllegalStateException("Puedes comprar máximo " + MAXIMO_POR_COMPRA + " boletos por compra.");
        }
        if (cantidad > tipo.getRestantes()) {
            throw new IllegalStateException("Solo quedan " + tipo.getRestantes() + " boletos de «" + tipo.getTipoNombre() + "».");
        }

        BigDecimal precio = BigDecimal.valueOf(tipo.getPrecio() != null ? tipo.getPrecio() : 0.0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal cargo = subtotal.multiply(TASA_SERVICIO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotal.add(cargo).multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);

        return ResumenCompraDTO.builder()
                .evento(evento)
                .tipo(tipo)
                .zonaCodigo(zona.map(MapaRecinto.Zona::codigo).orElse(null))
                .zonaNombre(zona.map(MapaRecinto.Zona::nombre).orElse(tipo.getTipoNombre()))
                .asientos(asientosFinales)
                .cantidad(cantidad)
                .precioUnitario(precio)
                .subtotal(subtotal)
                .cargoServicio(cargo)
                .iva(iva)
                .total(subtotal.add(cargo).add(iva))
                .build();
    }

    /** "d8, E8,e8" -> [D8, E8] ordenados por fila y número, sin repetidos. */
    private static List<String> separarAsientos(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        return java.util.Arrays.stream(texto.split(","))
                .map(a -> a.trim().toUpperCase(Locale.ROOT))
                .filter(a -> !a.isEmpty())
                .distinct()
                .sorted(Comparator.comparing((String a) -> a.substring(0, 1))
                        .thenComparingInt(a -> numeroDeAsiento(a)))
                .toList();
    }

    private static int numeroDeAsiento(String asiento) {
        try {
            return Integer.parseInt(asiento.substring(1));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0;
        }
    }

    // ---------------------------------------------------------------- Registro de la compra

    @Transactional
    public Compra registrarCompra(Long eventoId, Long tipoId, String asientos, Integer cantidad,
                                  String nombreTitular, String correo, String telefono, String metodoPago) {
        String nombre = nombreTitular == null ? "" : nombreTitular.trim().replaceAll("\\s+", " ");
        String correoNormalizado = correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
        if (nombre.length() < 3) {
            throw new IllegalStateException("Escribe el nombre completo del titular.");
        }
        if (!correoNormalizado.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalStateException("El correo electrónico no es válido.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalStateException("Escribe un teléfono de contacto.");
        }

        // Bloquea el tipo de boleto hasta que termine la transacción
        Boleto tipo = boletoRepository.buscarParaComprar(tipoId)
                .orElseThrow(() -> new IllegalStateException("El boleto seleccionado ya no existe."));
        Evento evento = tipo.getEvento();
        if (evento == null || !evento.getId().equals(eventoId)) {
            throw new IllegalStateException("El boleto seleccionado no pertenece a este evento.");
        }
        ResumenCompraDTO resumen = validarYCalcular(evento, tipo, separarAsientos(asientos), cantidad != null ? cantidad : 0);

        // El asistente se identifica por su correo; si es la primera vez que compra, se registra
        Asistente asistente = asistenteRepository.findByCorreo(correoNormalizado)
                .orElseGet(() -> asistenteRepository.save(Asistente.builder()
                        .nombre(nombre.length() > 50 ? nombre.substring(0, 50) : nombre)
                        .correo(correoNormalizado)
                        .telefono(telefono.trim())
                        // Contraseña aleatoria: la cuenta queda creada y podrá activarse al registrarse
                        .contrasenia(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .build()));

        Compra compra = Compra.builder()
                .asistente(asistente)
                .fechaHora(LocalDateTime.now())
                .totalVendidos(resumen.getCantidad())
                .montoTotal(resumen.getTotal().doubleValue())
                .codigoPedido(generarCodigoPedido())
                .metodoPago(metodoPago != null && !metodoPago.isBlank() ? metodoPago : "Tarjeta de Crédito / Débito")
                .build();

        // Un Boleto vendido por cada asiento (o por cada entrada en zonas sin asientos)
        List<Boleto> vendidos = new ArrayList<>();
        for (int i = 0; i < resumen.getCantidad(); i++) {
            vendidos.add(Boleto.builder()
                    .evento(evento)
                    .compra(compra)
                    .tipoNombre(tipo.getTipoNombre())
                    .descripcion(tipo.getDescripcion())
                    .precio(tipo.getPrecio())
                    .estado(BoletoEstadoENUM.VENDIDO)
                    .cantidad(1)
                    .zonaCodigo(resumen.getZonaCodigo())
                    .codigoAsiento(resumen.isConAsientos() ? resumen.getAsientos().get(i) : null)
                    .build());
        }
        compra.setBoletos(vendidos);

        // Descontar del inventario del tipo de boleto
        int vendidosTipo = (tipo.getCantidadVendida() != null ? tipo.getCantidadVendida() : 0) + resumen.getCantidad();
        tipo.setCantidadVendida(vendidosTipo);
        if (tipo.getRestantes() == 0) {
            tipo.setEstado(BoletoEstadoENUM.AGOTADO);
        }

        return compraRepository.save(compra);
    }

    private String generarCodigoPedido() {
        for (int intento = 0; intento < 10; intento++) {
            StringBuilder codigo = new StringBuilder("KT-" + Year.now().getValue() + "-");
            for (int i = 0; i < 6; i++) {
                codigo.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
            }
            if (!compraRepository.existsByCodigoPedido(codigo.toString())) {
                return codigo.toString();
            }
        }
        throw new IllegalStateException("No se pudo generar el código de pedido, intenta de nuevo.");
    }

    // ---------------------------------------------------------------- Confirmación

    /** Datos que necesita confirmacionPago.html, tomados de la compra guardada. */
    @Transactional(readOnly = true)
    public Map<String, Object> datosConfirmacion(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalStateException("La compra no existe."));
        List<Boleto> boletos = compra.getBoletos() != null ? compra.getBoletos() : List.of();
        Boleto primero = boletos.isEmpty() ? null : boletos.get(0);
        Evento evento = primero != null ? primero.getEvento() : null;

        List<String> asientos = boletos.stream().map(Boleto::getCodigoAsiento).filter(a -> a != null).sorted().toList();
        String zona = primero == null ? "" : MapaRecinto.porCodigo(primero.getZonaCodigo())
                .map(MapaRecinto.Zona::nombre).orElse(primero.getTipoNombre());

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("evento", evento);
        datos.put("titularNombre", compra.getAsistente().getNombre());
        datos.put("titularEmail", compra.getAsistente().getCorreo());
        datos.put("titularTelefono", compra.getAsistente().getTelefono());
        datos.put("codigoPedido", compra.getCodigoPedido());
        datos.put("codigoAcceso", compra.getCodigoPedido() + "-ACC");
        datos.put("fechaCompra", compra.getFechaHora().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm 'hrs'", Locale.forLanguageTag("es-MX"))));
        datos.put("metodoPago", compra.getMetodoPago());
        datos.put("seccion", zona);
        datos.put("fila", asientos.isEmpty() ? "—" : String.join(", ",
                asientos.stream().map(a -> a.substring(0, 1)).distinct().toList()));
        datos.put("asientos", asientos.isEmpty() ? "Admisión general" : String.join(", ", asientos));
        datos.put("cantidadBoletos", compra.getTotalVendidos());
        datos.put("totalPagado", compra.getMontoTotal());
        return datos;
    }
}
