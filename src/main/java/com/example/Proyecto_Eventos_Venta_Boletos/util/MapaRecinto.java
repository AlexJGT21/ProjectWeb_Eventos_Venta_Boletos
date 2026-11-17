package com.example.Proyecto_Eventos_Venta_Boletos.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Distribución del recinto de ejemplo que se dibuja en la venta de boletos (ver ventaBoletos.html).
 * Los códigos coinciden con TipoZonaENUM. Para que una zona se venda en un evento, el organizador
 * debe crear un tipo de boleto con el mismo nombre que la zona (ej. "Lateral Azul").
 */
public final class MapaRecinto {

    /**
     * @param codigo       código de la zona (igual que TipoZonaENUM)
     * @param nombre       nombre visible; también es el nombre que debe tener el tipo de boleto
     * @param descripcion  texto corto de la zona
     * @param color        color con el que se pinta en el mapa
     * @param filas        letras de las filas (vacío si la zona es de pie)
     * @param asientosPorFila asientos en cada fila
     * @param pasillos     número de asiento después del cual hay pasillo
     */
    public record Zona(String codigo, String nombre, String descripcion, String color,
                       List<String> filas, int asientosPorFila, List<Integer> pasillos) {

        public boolean isNumerada() {
            return !filas.isEmpty();
        }

        public int getTotalAsientos() {
            return filas.size() * asientosPorFila;
        }

        /** true si el código de asiento (ej. "D8") existe en esta zona. */
        public boolean existeAsiento(String codigoAsiento) {
            if (codigoAsiento == null || codigoAsiento.length() < 2 || !isNumerada()) return false;
            String fila = codigoAsiento.substring(0, 1);
            try {
                int numero = Integer.parseInt(codigoAsiento.substring(1));
                return filas.contains(fila) && numero >= 1 && numero <= asientosPorFila;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private static final List<String> FILAS_A_D = List.of("A", "B", "C", "D");
    private static final List<String> FILAS_A_H = List.of("A", "B", "C", "D", "E", "F", "G", "H");

    /** Nombre con el que se muestra esta plantilla de recinto al organizador. */
    public static final String NOMBRE_PLANTILLA = "Estadio con escenario lateral (plantilla Konticket)";

    public static final List<Zona> ZONAS = List.of(
            new Zona("PALCO_VIP", "Palco VIP", "Palcos frente a la cancha", "#F97316",
                    FILAS_A_D, 8, List.of(4)),
            new Zona("PALCO_ORO", "Palco Oro", "Palcos laterales a la cancha", "#10B981",
                    FILAS_A_D, 6, List.of(3)),
            new Zona("ZONA_FAN_SILLAS", "Zona Fan Sillas", "Sillas frente al escenario", "#7C3AED",
                    List.of("A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M"), 20, List.of(5, 10, 15)),
            new Zona("ZONA_FAN_PIE", "Zona Fan Pie", "Cancha general de pie", "#F5C542",
                    List.of(), 0, List.of()),
            new Zona("LATERAL_AZUL", "Lateral Azul", "Gradería lateral", "#60A5FA",
                    FILAS_A_H, 12, List.of(4, 8)),
            new Zona("LATERAL_CAFE", "Lateral Café", "Gradería lateral", "#5B3A29",
                    FILAS_A_H, 12, List.of(4, 8)),
            new Zona("PREFERENTE", "Preferente", "Gradería preferente", "#E11D74",
                    List.of("A", "B", "C", "D", "E", "F", "G", "H", "J", "K"), 12, List.of(4, 8)),
            new Zona("GENERAL", "General", "Gradería oriente", "#2563EB",
                    List.of("A", "B", "C", "D", "E", "F", "G", "H", "J", "K"), 14, List.of(4, 10))
    );

    private MapaRecinto() {
    }

    public static Optional<Zona> porCodigo(String codigo) {
        return ZONAS.stream().filter(z -> z.codigo().equals(codigo)).findFirst();
    }

    /** Zona cuyo nombre (o código) coincide con el nombre de un tipo de boleto, sin importar acentos ni mayúsculas. */
    public static Optional<Zona> porNombreDeBoleto(String tipoNombre) {
        String buscado = normalizar(tipoNombre);
        if (buscado.isEmpty()) return Optional.empty();
        return ZONAS.stream()
                .filter(z -> normalizar(z.nombre()).equals(buscado) || normalizar(z.codigo()).equals(buscado))
                .findFirst();
    }

    /** Minúsculas, sin acentos y solo letras/números: "Lateral Café" y "LATERAL_CAFE" quedan como "lateralcafe". */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
