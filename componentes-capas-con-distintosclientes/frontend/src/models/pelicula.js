export const FORMATOS = ['Todas', '2D', '3D', 'IMAX', 'IMAX 3D', '4D'];

const COLORES_GENERO = {
  accion: ['#3b0d17', '#ff2d55'],
  'ciencia ficcion': ['#0d1b3b', '#3d7bff'],
  animacion: ['#3b2a0d', '#ffb42d'],
  aventura: ['#0d3b2a', '#2dffb4'],
  terror: ['#1c0d3b', '#8a2dff'],
  comedia: ['#0d2e3b', '#2dc9ff'],
};

function normalizar(texto) {
  return (texto ?? '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
}

export function crearPelicula(dto) {
  return {
    id: dto.id,
    titulo: dto.titulo,
    sinopsis: dto.sinopsis,
    genero: dto.genero,
    clasificacion: dto.clasificacion,
    duracionMinutos: dto.duracionMinutos,
    rating: dto.rating,
    formatos: dto.formatos ?? [],
    posterUrl: dto.posterUrl ?? null,
    coloresPoster: COLORES_GENERO[normalizar(dto.genero)] ?? ['#1a1a1e', '#ff2d55'],
  };
}
