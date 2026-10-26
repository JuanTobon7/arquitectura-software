package co.edu.mensajeria.cliente.utilerias;

import co.edu.mensajeria.cliente.dto.ConfiguracionCliente;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Lee y valida {@code cliente.properties}. Si el archivo no existe se usan los valores
 * por defecto, para que el cliente arranque aunque se copie solo el jar.
 */
public final class LectorConfiguracion {

    public static final String HOST_POR_DEFECTO = "localhost";
    public static final int PUERTO_POR_DEFECTO = 5000;
    public static final String BD_POR_DEFECTO = "jdbc:h2:./datos/historial;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
    public static final List<String> FILTROS_POR_DEFECTO =
            List.of("escala_grises", "sepia", "girar_180", "brillo", "reduccion");
    public static final int TAMANO_MAXIMO_POR_DEFECTO = 10 * 1024 * 1024;

    private LectorConfiguracion() {
    }

    public static ConfiguracionCliente leer(Path archivo) throws IOException {
        Properties propiedades = new Properties();
        if (Files.isRegularFile(archivo)) {
            try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                propiedades.load(lector);
            }
        }
        return desdePropiedades(propiedades);
    }

    public static ConfiguracionCliente desdePropiedades(Properties p) {
        String host = texto(p, "servidor.host", HOST_POR_DEFECTO);
        int puerto = entero(p, "servidor.puerto", PUERTO_POR_DEFECTO);
        String bdUrl = texto(p, "bd.url", BD_POR_DEFECTO);
        String bdUsuario = texto(p, "bd.usuario", "sa");
        String bdContrasena = p.getProperty("bd.contrasena", "");
        String descargas = texto(p, "descargas.directorio", "datos/descargas");
        List<String> filtros = lista(p.getProperty("filtros.disponibles", String.join(",", FILTROS_POR_DEFECTO)));
        int tamanoMaximo = entero(p, "archivos.tamanoMaximoBytes", TAMANO_MAXIMO_POR_DEFECTO);

        validar(!host.isBlank(), "servidor.host no puede estar vacío");
        validar(puerto > 0 && puerto <= 65535, "servidor.puerto debe estar entre 1 y 65535");
        validar(!bdUrl.isBlank(), "bd.url no puede estar vacío");
        validar(!descargas.isBlank(), "descargas.directorio no puede estar vacío");
        validar(tamanoMaximo > 0, "archivos.tamanoMaximoBytes debe ser mayor que 0");

        return new ConfiguracionCliente(host, puerto, bdUrl, bdUsuario, bdContrasena, descargas, filtros,
                tamanoMaximo);
    }

    private static String texto(Properties p, String clave, String porDefecto) {
        return p.getProperty(clave, porDefecto).trim();
    }

    private static int entero(Properties p, String clave, int porDefecto) {
        String valor = p.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Configuración inválida: " + clave + " debe ser un número entero: "
                    + valor, e);
        }
    }

    private static List<String> lista(String valor) {
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalArgumentException("Configuración inválida: " + mensaje);
        }
    }
}
