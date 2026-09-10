import { useCallback, useEffect, useState } from 'react';
import { reservaService } from '../services/reservaService';

export function useReserva(funcionId) {
  const [disponibilidad, setDisponibilidad] = useState(null);
  const [seleccionados, setSeleccionados] = useState([]);
  const [reserva, setReserva] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  const cargar = useCallback(() => {
    setCargando(true);
    setError(null);
    reservaService
      .disponibilidad(funcionId)
      .then(setDisponibilidad)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false));
  }, [funcionId]);

  useEffect(() => {
    cargar();
    setSeleccionados([]);
    setReserva(null);
  }, [cargar]);

  const alternarAsiento = (etiqueta) => {
    if (disponibilidad?.ocupados.includes(etiqueta)) return;
    setSeleccionados((prev) =>
      prev.includes(etiqueta) ? prev.filter((a) => a !== etiqueta) : [...prev, etiqueta],
    );
  };

  const confirmar = async ({ nombreCliente, email }) => {
    setEnviando(true);
    setError(null);
    try {
      const creada = await reservaService.crear({
        funcionId: Number(funcionId),
        nombreCliente,
        email,
        asientos: seleccionados,
      });
      setReserva(creada);
      setSeleccionados([]);
      cargar();
      return creada;
    } catch (e) {
      setError(e.message);
      return null;
    } finally {
      setEnviando(false);
    }
  };

  const cancelar = async () => {
    if (!reserva) return;
    setEnviando(true);
    setError(null);
    try {
      await reservaService.cancelar(reserva.id);
      setReserva(null);
      cargar();
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  };

  return {
    disponibilidad,
    seleccionados,
    reserva,
    cargando,
    enviando,
    error,
    alternarAsiento,
    confirmar,
    cancelar,
  };
}
