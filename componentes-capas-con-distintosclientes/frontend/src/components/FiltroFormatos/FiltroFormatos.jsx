import PropTypes from 'prop-types';
import estilos from './FiltroFormatos.module.css';

export default function FiltroFormatos({ opciones, activa, onSeleccionar }) {
  return (
    <div className={estilos.fila} role="tablist" aria-label="Filtrar por formato">
      {opciones.map((opcion) => (
        <button
          key={opcion}
          type="button"
          role="tab"
          aria-selected={opcion === activa}
          className={opcion === activa ? `${estilos.pill} ${estilos.activa}` : estilos.pill}
          onClick={() => onSeleccionar(opcion)}
        >
          {opcion === 'Todas' ? 'Todas las películas' : opcion}
        </button>
      ))}
    </div>
  );
}

FiltroFormatos.propTypes = {
  opciones: PropTypes.arrayOf(PropTypes.string).isRequired,
  activa: PropTypes.string.isRequired,
  onSeleccionar: PropTypes.func.isRequired,
};
