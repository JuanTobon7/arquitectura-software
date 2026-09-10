import { httpClient } from './httpClient';
import { crearFuncion } from '../models/funcion';

export function crearReservaService(cliente = httpClient) {
  return {

    async funcionesDePelicula(peliculaId) {
      const dtos = await cliente.get(`/funciones/${peliculaId}`);
      return dtos.map(crearFuncion);
    },

    disponibilidad(funcionId) {
      return cliente.get(`/funciones/${funcionId}/asientos`);
    },

    crear({ funcionId, nombreCliente, email, asientos }) {
      return cliente.post('/reservas', { funcionId, nombreCliente, email, asientos });
    },

    cancelar(reservaId) {
      return cliente.delete(`/reservas/${reservaId}`);
    },
  };
}

export const reservaService = crearReservaService();
