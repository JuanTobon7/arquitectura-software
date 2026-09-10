export function generarSemana(desde = new Date()) {
  return Array.from({ length: 7 }, (_, i) => {
    const fecha = new Date(desde.getFullYear(), desde.getMonth(), desde.getDate() + i);
    return {
      fecha,
      nombre: fecha.toLocaleDateString('es-ES', { weekday: 'long' }),
      numero: fecha.getDate(),
      mes: fecha.toLocaleDateString('es-ES', { month: 'short' }).replace('.', ''),
    };
  });
}
