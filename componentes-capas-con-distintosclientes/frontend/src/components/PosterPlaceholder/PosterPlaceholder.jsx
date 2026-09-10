import PropTypes from 'prop-types';
import estilos from './PosterPlaceholder.module.css';

export default function PosterPlaceholder({ titulo, genero, colores }) {
  const [desde, hasta] = colores;
  return (
    <div
      className={estilos.poster}
      style={{ background: `linear-gradient(160deg, ${desde} 0%, #0a0a0a 78%)` }}
    >
      <span className={estilos.scanlines} aria-hidden="true" />
      <span className={estilos.genero} style={{ color: hasta }}>
        {genero}
      </span>
      <strong className={estilos.titulo}>{titulo}</strong>
      <span className={estilos.marca}>MICOS HAKARI</span>
    </div>
  );
}

PosterPlaceholder.propTypes = {
  titulo: PropTypes.string.isRequired,
  genero: PropTypes.string,
  colores: PropTypes.arrayOf(PropTypes.string).isRequired,
};
