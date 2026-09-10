import PropTypes from 'prop-types';
import Poster from '../Poster/Poster';
import SelectorDias from '../SelectorDias/SelectorDias';
import ChipHorario from '../ChipHorario/ChipHorario';
import estilos from './TarjetaPelicula.module.css';

export default function TarjetaPelicula({
  pelicula,
  dias,
  indiceDia,
  gruposFormato,
  onCambiarDia,
  onSeleccionarHorario,
}) {
  return (
    <article className={estilos.tarjeta}>
      <div className={estilos.principal}>
        <Poster pelicula={pelicula} />

        <div className={estilos.info}>
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

          <SelectorDias dias={dias} indiceActivo={indiceDia} onSeleccionar={onCambiarDia} />
        </div>
      </div>

      <div className={estilos.horarios}>
        {gruposFormato.length === 0 && (
          <p className={estilos.sinFunciones}>Sin funciones para este día</p>
        )}
        {gruposFormato.map((grupo) => (
          <div key={grupo.formato} className={estilos.filaFormato}>
            <span className={estilos.etiquetaFormato}>{grupo.formato}</span>
            <div className={estilos.chips}>
              {grupo.funciones.map((funcion) => (
                <ChipHorario
                  key={funcion.id}
                  hora={funcion.hora}
                  onClick={() => onSeleccionarHorario(funcion)}
                />
              ))}
            </div>
          </div>
        ))}
      </div>
    </article>
  );
}

TarjetaPelicula.propTypes = {
  pelicula: PropTypes.shape({
    id: PropTypes.number.isRequired,
    titulo: PropTypes.string.isRequired,
    sinopsis: PropTypes.string,
    genero: PropTypes.string,
    clasificacion: PropTypes.string,
    duracionMinutos: PropTypes.number,
    rating: PropTypes.number,
    coloresPoster: PropTypes.arrayOf(PropTypes.string),
  }).isRequired,
  dias: PropTypes.array.isRequired,
  indiceDia: PropTypes.number.isRequired,
  gruposFormato: PropTypes.arrayOf(
    PropTypes.shape({
      formato: PropTypes.string.isRequired,
      funciones: PropTypes.array.isRequired,
    }),
  ).isRequired,
  onCambiarDia: PropTypes.func.isRequired,
  onSeleccionarHorario: PropTypes.func.isRequired,
};
