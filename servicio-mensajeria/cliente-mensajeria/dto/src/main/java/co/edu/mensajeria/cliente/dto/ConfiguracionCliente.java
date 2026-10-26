package co.edu.mensajeria.cliente.dto;

import java.util.List;

/**
 * Configuración del cliente ({@code config/cliente.properties}). Inmutable.
 *
 * @param filtrosDisponibles filtros que el usuario puede elegir al enviar una imagen
 * @param tamanoMaximoBytes  tamaño máximo de un archivo; el mismo que acepta el servidor
 */
public record ConfiguracionCliente(String host, int puerto, String bdUrl, String bdUsuario, String bdContrasena,
                                   String directorioDescargas, List<String> filtrosDisponibles,
                                   int tamanoMaximoBytes) {

    public ConfiguracionCliente {
        filtrosDisponibles = filtrosDisponibles == null ? List.of() : List.copyOf(filtrosDisponibles);
    }
}
