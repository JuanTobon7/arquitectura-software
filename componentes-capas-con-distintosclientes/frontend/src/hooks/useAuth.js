import { useState } from 'react';
import { usuarioService } from '../services/usuarioService';

const CLAVE_SESION = 'mh_usuario';

function leerSesion() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_SESION));
  } catch {
    return null;
  }
}

function guardarSesion(usuario) {
  try {
    if (usuario) localStorage.setItem(CLAVE_SESION, JSON.stringify(usuario));
    else localStorage.removeItem(CLAVE_SESION);
  } catch {

  }
}

export function useAuth() {
  const [usuario, setUsuario] = useState(leerSesion);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  const ejecutar = async (accion) => {
    setEnviando(true);
    setError(null);
    try {
      const perfil = await accion();
      setUsuario(perfil);
      guardarSesion(perfil);
      return perfil;
    } catch (e) {
      setError(e.message);
      return null;
    } finally {
      setEnviando(false);
    }
  };

  const registrar = (datos) => ejecutar(() => usuarioService.registrar(datos));
  const entrar = (credenciales) => ejecutar(() => usuarioService.login(credenciales));

  const salir = () => {
    setUsuario(null);
    guardarSesion(null);
  };

  return { usuario, enviando, error, registrar, entrar, salir };
}
