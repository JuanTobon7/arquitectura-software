import { useEffect, useMemo, useState } from 'react';
import { reservaService } from '../services/reservaService';
import { usePeliculas } from './usePeliculas';
import { generarSemana } from '../models/dias';
import { agruparPorFormato, esDelDia } from '../models/funcion';

export function useCartelera(formatoFiltro) {
  const { peliculas, cargando: cargandoPeliculas, error } = usePeliculas({ formato: formatoFiltro });

  const [funcionesPorPelicula, setFuncionesPorPelicula] = useState({});
  const [diaPorPelicula, setDiaPorPelicula] = useState({});
  const [cargandoFunciones, setCargandoFunciones] = useState(false);

  const dias = useMemo(() => generarSemana(), []);

  useEffect(() => {
    if (peliculas.length === 0) {
      setFuncionesPorPelicula({});
      return undefined;
    }
    let activo = true;
    setCargandoFunciones(true);

    Promise.all(
      peliculas.map((p) =>
        reservaService
          .funcionesDePelicula(p.id)
          .then((funciones) => [p.id, funciones])
          .catch(() => [p.id, []]),
      ),
    )
      .then((pares) => activo && setFuncionesPorPelicula(Object.fromEntries(pares)))
      .finally(() => activo && setCargandoFunciones(false));

    return () => {
      activo = false;
    };
  }, [peliculas]);

  const seleccionarDia = (peliculaId, indice) =>
    setDiaPorPelicula((prev) => ({ ...prev, [peliculaId]: indice }));

  const items = useMemo(
    () =>
      peliculas.map((pelicula) => {
        const indiceDia = diaPorPelicula[pelicula.id] ?? 0;
        const todas = funcionesPorPelicula[pelicula.id] ?? [];
        const delDia = todas.filter((f) => esDelDia(f, dias[indiceDia].fecha));
        const visibles =
          formatoFiltro && formatoFiltro !== 'Todas'
            ? delDia.filter((f) => f.formato === formatoFiltro)
            : delDia;
        return {
          pelicula,
          indiceDia,
          gruposFormato: agruparPorFormato(visibles),
        };
      }),
    [peliculas, funcionesPorPelicula, diaPorPelicula, dias, formatoFiltro],
  );

  return {
    items,
    dias,
    seleccionarDia,
    cargando: cargandoPeliculas || cargandoFunciones,
    error,
  };
}
