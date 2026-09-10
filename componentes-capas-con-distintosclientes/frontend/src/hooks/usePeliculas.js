import { useEffect, useState } from 'react';
import { peliculaService } from '../services/peliculaService';

export function usePeliculas({ genero, formato } = {}) {
  const [peliculas, setPeliculas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setError(null);

    peliculaService
      .listar({ genero, formato })
      .then((datos) => activo && setPeliculas(datos))
      .catch((e) => activo && setError(e.message))
      .finally(() => activo && setCargando(false));

    return () => {
      activo = false;
    };
  }, [genero, formato]);

  return { peliculas, cargando, error };
}
