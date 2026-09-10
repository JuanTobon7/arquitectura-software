import { httpClient } from './httpClient';

export function crearUsuarioService(cliente = httpClient) {
  return {

    registrar({ nombre, email, clave }) {
      return cliente.post('/usuarios/registro', { nombre, email, clave });
    },

    login({ email, clave }) {
      return cliente.post('/usuarios/login', { email, clave });
    },
  };
}

export const usuarioService = crearUsuarioService();
