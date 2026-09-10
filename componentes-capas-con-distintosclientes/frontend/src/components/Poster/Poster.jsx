import { useState } from 'react';
import PropTypes from 'prop-types';
import PosterPlaceholder from '../PosterPlaceholder/PosterPlaceholder';
import estilos from './Poster.module.css';

export default function Poster({ pelicula }) {
  const [fallo, setFallo] = useState(false);

  if (!pelicula.posterUrl || fallo) {
    return (
      <PosterPlaceholder
        titulo={pelicula.titulo}
        genero={pelicula.genero}
        colores={pelicula.coloresPoster}
      />
    );
  }

  return (
    <img
      className={estilos.poster}
      src={pelicula.posterUrl}
      alt={`Póster de ${pelicula.titulo}`}
      loading="lazy"
      onError={() => setFallo(true)}
    />
  );
}

Poster.propTypes = {
  pelicula: PropTypes.shape({
    titulo: PropTypes.string.isRequired,
    genero: PropTypes.string,
    posterUrl: PropTypes.string,
    coloresPoster: PropTypes.arrayOf(PropTypes.string),
  }).isRequired,
};
