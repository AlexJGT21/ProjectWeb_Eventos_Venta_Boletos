package com.example.Proyecto_Eventos_Venta_Boletos.controller;

import com.example.Proyecto_Eventos_Venta_Boletos.dto.MetricasOrganizadorDTO;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.service.OrganizadorService;
import com.example.Proyecto_Eventos_Venta_Boletos.util.MapaRecinto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/organizador")
public class OrganizadorController {

    private final OrganizadorService organizadorService;

    @Autowired
    public OrganizadorController(OrganizadorService organizadorService) {
        this.organizadorService = organizadorService;
    }

    // Id del organizador que inició sesión (Spring Security solo deja entrar aquí con rol ORGANIZADOR)
    private Long organizadorId(Principal principal) {
        return organizadorService.obtenerPorCorreo(principal.getName()).getId();
    }

    // --- Panel Principal ---
    @GetMapping("/panel")
    public String panel(@RequestParam(name = "eventoId", required = false) Long eventoSolicitado,
                        @RequestParam(name = "dias", required = false, defaultValue = "30") int dias,
                        Model model, Principal principal) {
        Organizador organizador = organizadorService.obtenerPorCorreo(principal.getName());
        Long organizadorId = organizador.getId();
        List<Evento> eventos = organizadorService.obtenerEventosPorOrganizador(organizadorId);

        // Solo se aceptan eventos de este organizador; cualquier otro valor muestra "Todos mis eventos"
        boolean esSuyo = eventoSolicitado != null && eventos.stream().anyMatch(e -> e.getId().equals(eventoSolicitado));
        Long eventoId = esSuyo ? eventoSolicitado : null;
        if (dias != 7 && dias != 30 && dias != 90) {
            dias = 30;
        }

        model.addAttribute("organizador", organizador);
        model.addAttribute("eventos", eventos);
        model.addAttribute("eventoSeleccionadoId", eventoId);
        model.addAttribute("diasSeleccionados", dias);
        model.addAttribute("inventario", organizadorService.obtenerInventario(organizadorId, eventoId));
        model.addAttribute("curva", organizadorService.curvaVentas(organizadorId, eventoId, dias));
        model.addAttribute("diasProximoEvento", organizadorService.diasParaProximoEvento(organizadorId, eventoId));
        model.addAttribute("tipoTop", organizadorService.tipoConMasIngresos(organizadorId, eventoId).orElse(null));

        // Métricas/estimaciones calculadas en tiempo real a partir de los datos reales del organizador
        MetricasOrganizadorDTO metricas = organizadorService.calcularMetricas(organizadorId, eventoId);
        model.addAttribute("ingresosTotales", metricas.getIngresosTotales());
        model.addAttribute("boletosVendidos", metricas.getBoletosVendidos());
        model.addAttribute("capacidadTotal", metricas.getCapacidadTotal());
        model.addAttribute("boletosRestantes", metricas.getBoletosRestantes());
        model.addAttribute("porcentajeOcupacion", metricas.getPorcentajeOcupacion());
        model.addAttribute("ticketPromedio", metricas.getTicketPromedio());

        return "panelOrganizador";
    }

    // Descarga del reporte (mismo filtro de evento que el panel)
    @GetMapping("/reporte.csv")
    public ResponseEntity<byte[]> reporteCsv(@RequestParam(name = "eventoId", required = false) Long eventoId,
                                             Principal principal) {
        Long organizadorId = organizadorId(principal);
        boolean esSuyo = eventoId == null || organizadorService.obtenerEventosPorOrganizador(organizadorId)
                .stream().anyMatch(e -> e.getId().equals(eventoId));
        byte[] csv = organizadorService.generarReporteCsv(organizadorId, esSuyo ? eventoId : null)
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("reporte-konticket-" + LocalDate.now() + ".csv").build().toString())
                .body(csv);
    }

    // --- Gestión de Perfil ---
    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {
        model.addAttribute("organizador", organizadorService.obtenerPorCorreo(principal.getName()));
        return "perfilOrganizador";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute Organizador organizador, Principal principal) {
        organizadorService.actualizarPerfil(organizadorId(principal), organizador);
        return "redirect:/organizador/perfil?exito";
    }

    // --- Gestión de Eventos ---
    @GetMapping("/evento/nuevo")
    public String nuevoEvento(Model model, Principal principal) {
        model.addAttribute("evento", new Evento());
        prepararFormulario(model, principal, false, List.of(), 1);
        return "formEvento";
    }

    // Edición de un evento ya creado (mismo formulario, con los datos y boletos cargados)
    @GetMapping("/evento/{id}/editar")
    public String editarEvento(@PathVariable Long id,
                               @RequestParam(name = "paso", required = false, defaultValue = "1") int paso,
                               Model model, Principal principal) {
        Evento evento;
        try {
            evento = organizadorService.obtenerEventoParaEditar(id, organizadorId(principal));
        } catch (IllegalArgumentException | SecurityException e) {
            return "redirect:/organizador/panel";
        }
        model.addAttribute("evento", evento);
        prepararFormulario(model, principal, true, evento.getTiposBoleto(), paso);
        return "formEvento";
    }

    private void prepararFormulario(Model model, Principal principal, boolean edicion, List<Boleto> tipos, int paso) {
        model.addAttribute("organizadorActual", organizadorService.obtenerPorCorreo(principal.getName()));
        model.addAttribute("modoEdicion", edicion);
        model.addAttribute("tiposExistentes", tipos);
        model.addAttribute("pasoInicial", paso >= 1 && paso <= 4 ? paso : 1);
        model.addAttribute("nombrePlantilla", MapaRecinto.NOMBRE_PLANTILLA);

        // Zonas de la plantilla del recinto para la modalidad "Mapa Numerado"
        List<Map<String, Object>> zonas = new ArrayList<>();
        for (MapaRecinto.Zona zona : MapaRecinto.ZONAS) {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("codigo", zona.codigo());
            datos.put("nombre", zona.nombre());
            datos.put("descripcion", zona.descripcion());
            datos.put("color", zona.color());
            datos.put("numerada", zona.isNumerada());
            datos.put("asientos", zona.getTotalAsientos());
            datos.put("filas", zona.isNumerada()
                    ? zona.filas().size() + " filas × " + zona.asientosPorFila() + " asientos"
                    : "De pie, sin asiento asignado");
            zonas.add(datos);
        }
        model.addAttribute("zonasMapa", zonas);
    }

    @PostMapping("/evento/guardar")
    public String guardarEvento(@ModelAttribute Evento evento,
                                 @RequestParam(name = "imagenFile", required = false) MultipartFile imagenFile,
                                 @RequestParam(name = "croquisFile", required = false) MultipartFile croquisFile,
                                 @RequestParam(name = "tipoId", required = false) List<String> tipoId,
                                 @RequestParam(name = "tipoNombre", required = false) List<String> tipoNombre,
                                 @RequestParam(name = "tipoPrecio", required = false) List<Double> tipoPrecio,
                                 @RequestParam(name = "tipoAforo", required = false) List<Integer> tipoAforo,
                                 @RequestParam(name = "tipoDescripcion", required = false) List<String> tipoDescripcion,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        boolean edicion = evento.getId() != null;
        try {
            Evento guardado = organizadorService.procesarYGuardarEvento(evento, imagenFile, croquisFile, organizadorId(principal),
                    convertirIds(tipoId), tipoNombre, tipoPrecio, tipoAforo, tipoDescripcion);
            return edicion
                    ? "redirect:/organizador/panel?eventoActualizado&eventoId=" + guardado.getId()
                    : "redirect:/organizador/panel?eventoCreado";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return edicion
                    ? "redirect:/organizador/evento/" + evento.getId() + "/editar?paso=3"
                    : "redirect:/organizador/evento/nuevo";
        } catch (IllegalArgumentException | SecurityException e) {
            return "redirect:/organizador/panel";
        }
    }

    // "" (boleto nuevo) -> null; así cada id queda alineado con su nombre/precio/aforo
    private static List<Long> convertirIds(List<String> ids) {
        List<Long> resultado = new ArrayList<>();
        if (ids == null) return resultado;
        for (String id : ids) {
            try {
                resultado.add(id == null || id.isBlank() ? null : Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                resultado.add(null);
            }
        }
        return resultado;
    }

    @PostMapping("/evento/eliminar")
    public String eliminarEvento(@RequestParam("id") Long eventoId, Principal principal) {
        organizadorService.eliminarEvento(eventoId, organizadorId(principal));
        return "redirect:/organizador/panel?eventoEliminado";
    }
}
