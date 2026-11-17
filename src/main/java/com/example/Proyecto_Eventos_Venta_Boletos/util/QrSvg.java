package com.example.Proyecto_Eventos_Venta_Boletos.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera un código QR como SVG (Java puro, sin librerías ni JavaScript).
 * Modo byte (UTF-8), corrección de errores nivel M, versiones 1 a 10 (hasta 213 caracteres).
 */
public final class QrSvg {

    private static final int[] ECC_POR_BLOQUE = {-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26};
    private static final int[] NUM_BLOQUES = {-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5};
    private static final int VERSION_MAX = 10;

    private QrSvg() {
    }

    /** Devuelve el QR como etiqueta &lt;svg&gt; lista para incrustar en el HTML. */
    public static String generar(String texto, String colorOscuro, String colorOjo) {
        boolean[][] m = crearMatriz(texto);
        int n = m.length;
        int margen = 1;
        int total = n + margen * 2;
        StringBuilder sb = new StringBuilder(n * n * 20);
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ").append(total).append(' ').append(total)
          .append("\" shape-rendering=\"crispEdges\" role=\"img\" aria-label=\"Código QR del boleto\">")
          .append("<rect width=\"").append(total).append("\" height=\"").append(total).append("\" fill=\"#FFFFFF\"/>");
        for (int f = 0; f < n; f++) {
            int c = 0;
            while (c < n) {
                if (m[f][c] && !esOjo(f, c, n)) {
                    int inicio = c;
                    while (c < n && m[f][c] && !esOjo(f, c, n)) {
                        c++;
                    }
                    sb.append("<rect x=\"").append(inicio + margen).append("\" y=\"").append(f + margen)
                      .append("\" width=\"").append(c - inicio).append("\" height=\"1\" fill=\"").append(colorOscuro).append("\"/>");
                } else {
                    c++;
                }
            }
        }
        ojo(sb, margen, margen, colorOscuro, colorOjo);
        ojo(sb, margen, n - 7 + margen, colorOscuro, colorOjo);
        ojo(sb, n - 7 + margen, margen, colorOscuro, colorOjo);
        return sb.append("</svg>").toString();
    }

    private static boolean esOjo(int f, int c, int n) {
        return (f < 7 && c < 7) || (f < 7 && c >= n - 7) || (f >= n - 7 && c < 7);
    }

    private static void ojo(StringBuilder sb, int y, int x, String oscuro, String centro) {
        sb.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"7\" height=\"7\" fill=\"").append(oscuro).append("\"/>")
          .append("<rect x=\"").append(x + 1).append("\" y=\"").append(y + 1).append("\" width=\"5\" height=\"5\" fill=\"#FFFFFF\"/>")
          .append("<rect x=\"").append(x + 2).append("\" y=\"").append(y + 2).append("\" width=\"3\" height=\"3\" fill=\"").append(centro).append("\"/>");
    }

    // ------------------------------------------------------------------ construcción de la matriz

    static boolean[][] crearMatriz(String texto) {
        byte[] datos = texto.getBytes(StandardCharsets.UTF_8);

        int version = 1;
        while (version <= VERSION_MAX) {
            int capacidadBits = totalCodewordsDatos(version) * 8;
            int bitsNecesarios = 4 + 8 + datos.length * 8;
            if (bitsNecesarios <= capacidadBits) {
                break;
            }
            version++;
        }
        if (version > VERSION_MAX) {
            throw new IllegalArgumentException("Texto demasiado largo para el QR");
        }

        // Segmento de datos: modo byte (0100) + longitud (8 bits) + bytes
        List<Boolean> bits = new ArrayList<>();
        agregarBits(bits, 0b0100, 4);
        agregarBits(bits, datos.length, 8);
        for (byte b : datos) {
            agregarBits(bits, b & 0xFF, 8);
        }
        int capacidad = totalCodewordsDatos(version) * 8;
        agregarBits(bits, 0, Math.min(4, capacidad - bits.size()));
        while (bits.size() % 8 != 0) {
            bits.add(false);
        }
        for (int relleno = 0xEC; bits.size() < capacidad; relleno ^= 0xEC ^ 0x11) {
            agregarBits(bits, relleno, 8);
        }
        byte[] codewords = new byte[bits.size() / 8];
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i)) {
                codewords[i >>> 3] |= 1 << (7 - (i & 7));
            }
        }

        byte[] finales = agregarEccEntrelazado(codewords, version);
        int size = version * 4 + 17;
        boolean[][] modulos = new boolean[size][size];
        boolean[][] funcion = new boolean[size][size];
        dibujarFuncion(modulos, funcion, version);
        dibujarCodewords(modulos, funcion, finales);

        int mejorMascara = 0;
        int mejorPenal = Integer.MAX_VALUE;
        for (int mask = 0; mask < 8; mask++) {
            aplicarMascara(modulos, funcion, mask);
            dibujarFormato(modulos, funcion, mask);
            int penal = penalizacion(modulos);
            if (penal < mejorPenal) {
                mejorPenal = penal;
                mejorMascara = mask;
            }
            aplicarMascara(modulos, funcion, mask); // deshacer (XOR)
        }
        aplicarMascara(modulos, funcion, mejorMascara);
        dibujarFormato(modulos, funcion, mejorMascara);
        return modulos;
    }

    private static void agregarBits(List<Boolean> lista, int valor, int cantidad) {
        for (int i = cantidad - 1; i >= 0; i--) {
            lista.add(((valor >>> i) & 1) != 0);
        }
    }

    private static int modulosDatosBrutos(int ver) {
        int r = (16 * ver + 128) * ver + 64;
        if (ver >= 2) {
            int numAlign = ver / 7 + 2;
            r -= (25 * numAlign - 10) * numAlign - 55;
            if (ver >= 7) {
                r -= 36;
            }
        }
        return r;
    }

    private static int totalCodewordsDatos(int ver) {
        return modulosDatosBrutos(ver) / 8 - ECC_POR_BLOQUE[ver] * NUM_BLOQUES[ver];
    }

    // ------------------------------------------------------------------ Reed-Solomon

    private static byte[] agregarEccEntrelazado(byte[] datos, int ver) {
        int numBloques = NUM_BLOQUES[ver];
        int eccLen = ECC_POR_BLOQUE[ver];
        int rawCodewords = modulosDatosBrutos(ver) / 8;
        int numBloquesCortos = numBloques - rawCodewords % numBloques;
        int largoBloqueCorto = rawCodewords / numBloques;

        byte[][] bloques = new byte[numBloques][];
        byte[] divisor = divisorRs(eccLen);
        for (int i = 0, k = 0; i < numBloques; i++) {
            int largoDatos = largoBloqueCorto - eccLen + (i < numBloquesCortos ? 0 : 1);
            byte[] dat = new byte[largoDatos];
            System.arraycopy(datos, k, dat, 0, largoDatos);
            k += largoDatos;
            byte[] ecc = restoRs(dat, divisor);
            byte[] bloque = new byte[largoBloqueCorto + 1];
            System.arraycopy(dat, 0, bloque, 0, dat.length);
            if (i < numBloquesCortos) {
                // posición vacía para igualar largos
                System.arraycopy(ecc, 0, bloque, largoBloqueCorto - eccLen + 1, eccLen);
            } else {
                System.arraycopy(ecc, 0, bloque, largoBloqueCorto - eccLen + 1, eccLen);
            }
            bloques[i] = bloque;
        }
        byte[] resultado = new byte[rawCodewords];
        for (int i = 0, k = 0; i < bloques[0].length; i++) {
            for (int j = 0; j < bloques.length; j++) {
                if (i != largoBloqueCorto - eccLen || j >= numBloquesCortos) {
                    resultado[k++] = bloques[j][i];
                }
            }
        }
        return resultado;
    }

    private static byte[] divisorRs(int grado) {
        byte[] resultado = new byte[grado];
        resultado[grado - 1] = 1;
        int raiz = 1;
        for (int i = 0; i < grado; i++) {
            for (int j = 0; j < resultado.length; j++) {
                resultado[j] = (byte) multiplicarGf(resultado[j] & 0xFF, raiz);
                if (j + 1 < resultado.length) {
                    resultado[j] ^= resultado[j + 1];
                }
            }
            raiz = multiplicarGf(raiz, 0x02);
        }
        return resultado;
    }

    private static byte[] restoRs(byte[] datos, byte[] divisor) {
        byte[] resultado = new byte[divisor.length];
        for (byte b : datos) {
            int factor = (b ^ resultado[0]) & 0xFF;
            System.arraycopy(resultado, 1, resultado, 0, resultado.length - 1);
            resultado[resultado.length - 1] = 0;
            for (int i = 0; i < resultado.length; i++) {
                resultado[i] ^= multiplicarGf(divisor[i] & 0xFF, factor);
            }
        }
        return resultado;
    }

    private static int multiplicarGf(int x, int y) {
        int z = 0;
        for (int i = 7; i >= 0; i--) {
            z = (z << 1) ^ ((z >>> 7) * 0x11D);
            z ^= ((y >>> i) & 1) * x;
        }
        return z;
    }

    // ------------------------------------------------------------------ dibujo

    private static void dibujarFuncion(boolean[][] m, boolean[][] fn, int ver) {
        int size = m.length;
        for (int i = 0; i < size; i++) {
            poner(m, fn, 6, i, i % 2 == 0);
            poner(m, fn, i, 6, i % 2 == 0);
        }
        dibujarBuscador(m, fn, 3, 3);
        dibujarBuscador(m, fn, size - 4, 3);
        dibujarBuscador(m, fn, 3, size - 4);

        int[] pos = posicionesAlineacion(ver, size);
        int num = pos.length;
        for (int i = 0; i < num; i++) {
            for (int j = 0; j < num; j++) {
                if (!((i == 0 && j == 0) || (i == 0 && j == num - 1) || (i == num - 1 && j == 0))) {
                    dibujarAlineacion(m, fn, pos[i], pos[j]);
                }
            }
        }
        dibujarFormato(m, fn, 0);
        dibujarVersion(m, fn, ver);
    }

    private static void dibujarFormato(boolean[][] m, boolean[][] fn, int mascara) {
        int size = m.length;
        int datos = (0 << 3) | mascara; // nivel M = 00
        int resto = datos;
        for (int i = 0; i < 10; i++) {
            resto = (resto << 1) ^ ((resto >>> 9) * 0x537);
        }
        int bits = ((datos << 10) | resto) ^ 0x5412;
        for (int i = 0; i <= 5; i++) {
            poner(m, fn, 8, i, bit(bits, i));
        }
        poner(m, fn, 8, 7, bit(bits, 6));
        poner(m, fn, 8, 8, bit(bits, 7));
        poner(m, fn, 7, 8, bit(bits, 8));
        for (int i = 9; i < 15; i++) {
            poner(m, fn, 14 - i, 8, bit(bits, i));
        }
        for (int i = 0; i < 8; i++) {
            poner(m, fn, size - 1 - i, 8, bit(bits, i));
        }
        for (int i = 8; i < 15; i++) {
            poner(m, fn, 8, size - 15 + i, bit(bits, i));
        }
        poner(m, fn, 8, size - 8, true);
    }

    private static void dibujarVersion(boolean[][] m, boolean[][] fn, int ver) {
        if (ver < 7) {
            return;
        }
        int size = m.length;
        int resto = ver;
        for (int i = 0; i < 12; i++) {
            resto = (resto << 1) ^ ((resto >>> 11) * 0x1F25);
        }
        int bits = (ver << 12) | resto;
        for (int i = 0; i < 18; i++) {
            boolean b = bit(bits, i);
            int a = size - 11 + i % 3;
            int c = i / 3;
            poner(m, fn, a, c, b);
            poner(m, fn, c, a, b);
        }
    }

    private static void dibujarBuscador(boolean[][] m, boolean[][] fn, int cx, int cy) {
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dy));
                int x = cx + dx;
                int y = cy + dy;
                if (x >= 0 && x < m.length && y >= 0 && y < m.length) {
                    poner(m, fn, x, y, dist != 2 && dist != 4);
                }
            }
        }
    }

    private static void dibujarAlineacion(boolean[][] m, boolean[][] fn, int cx, int cy) {
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                poner(m, fn, cx + dx, cy + dy, Math.max(Math.abs(dx), Math.abs(dy)) != 1);
            }
        }
    }

    private static int[] posicionesAlineacion(int ver, int size) {
        if (ver == 1) {
            return new int[0];
        }
        int num = ver / 7 + 2;
        int paso = (ver * 8 + num * 3 + 5) / (num * 4 - 4) * 2;
        int[] r = new int[num];
        r[0] = 6;
        for (int i = num - 1, pos = size - 7; i >= 1; i--, pos -= paso) {
            r[i] = pos;
        }
        return r;
    }

    private static void poner(boolean[][] m, boolean[][] fn, int x, int y, boolean oscuro) {
        m[y][x] = oscuro;
        fn[y][x] = true;
    }

    private static boolean bit(int x, int i) {
        return ((x >>> i) & 1) != 0;
    }

    private static void dibujarCodewords(boolean[][] m, boolean[][] fn, byte[] datos) {
        int size = m.length;
        int i = 0;
        for (int derecha = size - 1; derecha >= 1; derecha -= 2) {
            if (derecha == 6) {
                derecha = 5;
            }
            for (int vert = 0; vert < size; vert++) {
                for (int j = 0; j < 2; j++) {
                    int x = derecha - j;
                    boolean arriba = ((derecha + 1) & 2) == 0;
                    int y = arriba ? size - 1 - vert : vert;
                    if (!fn[y][x] && i < datos.length * 8) {
                        m[y][x] = bit(datos[i >>> 3] & 0xFF, 7 - (i & 7));
                        i++;
                    }
                }
            }
        }
    }

    private static void aplicarMascara(boolean[][] m, boolean[][] fn, int mask) {
        int size = m.length;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean invertir;
                switch (mask) {
                    case 0: invertir = (x + y) % 2 == 0; break;
                    case 1: invertir = y % 2 == 0; break;
                    case 2: invertir = x % 3 == 0; break;
                    case 3: invertir = (x + y) % 3 == 0; break;
                    case 4: invertir = (x / 3 + y / 2) % 2 == 0; break;
                    case 5: invertir = x * y % 2 + x * y % 3 == 0; break;
                    case 6: invertir = (x * y % 2 + x * y % 3) % 2 == 0; break;
                    default: invertir = ((x + y) % 2 + x * y % 3) % 2 == 0; break;
                }
                if (!fn[y][x] && invertir) {
                    m[y][x] = !m[y][x];
                }
            }
        }
    }

    // ------------------------------------------------------------------ penalización (elige la mejor máscara)

    private static int penalizacion(boolean[][] m) {
        int size = m.length;
        int resultado = 0;
        for (int y = 0; y < size; y++) {
            int racha = 1;
            for (int x = 1; x < size; x++) {
                if (m[y][x] == m[y][x - 1]) {
                    racha++;
                    if (racha == 5) resultado += 3;
                    else if (racha > 5) resultado++;
                } else {
                    racha = 1;
                }
            }
        }
        for (int x = 0; x < size; x++) {
            int racha = 1;
            for (int y = 1; y < size; y++) {
                if (m[y][x] == m[y - 1][x]) {
                    racha++;
                    if (racha == 5) resultado += 3;
                    else if (racha > 5) resultado++;
                } else {
                    racha = 1;
                }
            }
        }
        for (int y = 0; y < size - 1; y++) {
            for (int x = 0; x < size - 1; x++) {
                boolean c = m[y][x];
                if (c == m[y][x + 1] && c == m[y + 1][x] && c == m[y + 1][x + 1]) {
                    resultado += 3;
                }
            }
        }
        int oscuros = 0;
        for (boolean[] fila : m) {
            for (boolean b : fila) {
                if (b) oscuros++;
            }
        }
        int total = size * size;
        int k = (Math.abs(oscuros * 20 - total * 10) + total - 1) / total - 1;
        resultado += k * 10;
        return resultado;
    }
}
