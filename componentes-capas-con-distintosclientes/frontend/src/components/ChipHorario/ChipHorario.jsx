import PropTypes from 'prop-types';
import estilos from './ChipHorario.module.css';

export default function ChipHorario({ hora, seleccionado, onClick }) {
  return (
    <button
      type="button"
      className={seleccionado ? `${estilos.chip} ${estilos.seleccionado}` : estilos.chip}
      onClick={onClick}
    >
      {hora}
    </button>
  );
}

ChipHorario.propTypes = {
  hora: PropTypes.string.isRequired,
  seleccionado: PropTypes.bool,
  onClick: PropTypes.func.isRequired,
};
