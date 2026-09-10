export function crearFuncion(dto) {
  const fecha = new Date(dto.fechaHora);
  return {
    id: dto.id,
    peliculaId: dto.peliculaId,
    fecha,
    formato: dto.formato,
    sala: dto.sala,
    precio: dto.precio,
    hora: fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
  };
}

export function agruparPorFormato(funciones) {
  const grupos = new Map();
  funciones.forEach((f) => {
    if (!grupos.has(f.formato)) grupos.set(f.formato, []);
    grupos.get(f.formato).push(f);
  });
  return [...grupos.entries()].map(([formato, fns]) => ({ formato, funciones: fns }));
}

export function esDelDia(funcion, fecha) {
  return (
    funcion.fecha.getFullYear() === fecha.getFullYear() &&
    funcion.fecha.getMonth() === fecha.getMonth() &&
    funcion.fecha.getDate() === fecha.getDate()
  );
}
