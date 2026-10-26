package co.edu.mensajeria.cliente.soporte;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.persistencia.IRepositorioMensajesLocal;

import java.util.ArrayList;
import java.util.List;

/** Doble de prueba del historial: una lista en memoria. */
public final class RepositorioEnMemoria implements IRepositorioMensajesLocal {

    public final List<MensajeChat> guardados = new ArrayList<>();

    @Override
    public synchronized void guardar(MensajeChat m) {
        guardados.removeIf(g -> g.propietario().equals(m.propietario()) && g.id().equals(m.id()));
        guardados.add(m);
    }

    @Override
    public synchronized List<MensajeChat> conversacion(String propietario, String contacto) {
        return guardados.stream()
                .filter(m -> m.propietario().equals(propietario) && m.contacto().equals(contacto))
                .toList();
    }
}
