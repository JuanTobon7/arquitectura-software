import { httpClient } from './httpClient';
import { crearPelicula } from '../models/pelicula';

export function crearPeliculaService(cliente = httpClient) {
  return {

    async listar({ genero, formato } = {}) {
      const dtos = await cliente.get('/peliculas', {
        genero,
        formato: formato && formato !== 'Todas' ? formato : undefined,
      });
      return dtos.map(crearPelicula);
    },

    async registrar(datos) {
      const dto = await cliente.post('/peliculas', datos);
      return crearPelicula(dto);
    },
  };
}

export const peliculaService = crearPeliculaService();
