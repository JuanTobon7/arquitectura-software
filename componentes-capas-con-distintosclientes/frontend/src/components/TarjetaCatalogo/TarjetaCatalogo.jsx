import PropTypes from 'prop-types';
import Poster from '../Poster/Poster';
import estilos from './TarjetaCatalogo.module.css';

export default function TarjetaCatalogo({ pelicula, onVerHorarios }) {
  return (
    <article className={estilos.tarjeta}>
      <Poster pelicula={pelicula} />

      <div className={estilos.contenido}>
        <header className={estilos.cabecera}>
          <h3 className={estilos.titulo}>
            {pelicula.titulo}
            <span className={estilos.clasificacion}>{pelicula.clasificacion}</span>
          </h3>
          <span className={estilos.rating}>★ {pelicula.rating.toFixed(1)}</span>
        </header>

        <p className={estilos.meta}>
          {pelicula.duracionMinutos} min <i>|</i> {pelicula.genero.toUpperCase()}
        </p>

        <p className={estilos.sinopsis}>{pelicula.sinopsis}</p>

        <div className={estilos.pie}>
          <div className={estilos.formatos}>
            {pelicula.formatos.map((formato) => (
              <span key={formato} className={estilos.formato}>
                {formato}
              </span>
            ))}
          </div>
          <button type="button" className={estilos.boton} onClick={onVerHorarios}>
            VER HORARIOS ▸
          </button>
        </div>
      </div>
    </article>
  );
}

TarjetaCatalogo.propTypes = {
  pelicula: PropTypes.shape({
    id: PropTypes.number.isRequired,
    titulo: PropTypes.string.isRequired,
    sinopsis: PropTypes.string,
    genero: PropTypes.string,
    clasificacion: PropTypes.string,
    duracionMinutos: PropTypes.number,
    rating: PropTypes.number,
    formatos: PropTypes.arrayOf(PropTypes.string),
    coloresPoster: PropTypes.arrayOf(PropTypes.string),
  }).isRequired,
  onVerHorarios: PropTypes.func.isRequired,
};
