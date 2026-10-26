package co.edu.mensajeria.cliente.fachada;

import co.edu.mensajeria.cliente.comunicacion.ConexionServidorTCP;
import co.edu.mensajeria.cliente.comunicacion.IConexionServidor;
import co.edu.mensajeria.cliente.dto.ConfiguracionCliente;
import co.edu.mensajeria.cliente.persistencia.IRepositorioMensajesLocal;
import co.edu.mensajeria.cliente.persistencia.RepositorioMensajesH2;

import java.nio.file.Path;

/**
 * Arma el cliente (GRASP Creador). Es la única clase que conoce las implementaciones de
 * la comunicación y la persistencia; los demás reciben sus interfaces.
 */
public final class FabricaCliente {

    private FabricaCliente() {
    }

    /** Una fachada nueva por cada inicio de sesión: así una sesión no hereda observadores de otra. */
    public static IFachadaCliente crear(ConfiguracionCliente c) {
        IConexionServidor conexion = new ConexionServidorTCP();
        IRepositorioMensajesLocal repositorio = new RepositorioMensajesH2(c.bdUrl(), c.bdUsuario(), c.bdContrasena());
        return new FachadaCliente(conexion, repositorio, Path.of(c.directorioDescargas()), c.filtrosDisponibles(),
                c.tamanoMaximoBytes());
    }
}
