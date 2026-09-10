import PropTypes from 'prop-types';
import estilos from './SelectorDias.module.css';

export default function SelectorDias({ dias, indiceActivo, onSeleccionar }) {
  const mover = (delta) => {
    const siguiente = indiceActivo + delta;
    if (siguiente >= 0 && siguiente < dias.length) onSeleccionar(siguiente);
  };

  return (
    <div className={estilos.contenedor}>
      <button
        type="button"
        className={estilos.flecha}
        onClick={() => mover(-1)}
        disabled={indiceActivo === 0}
        aria-label="Día anterior"
      >
        ←
      </button>

      <div className={estilos.dias}>
        {dias.map((dia, i) => (
          <button
            key={dia.fecha.toISOString()}
            type="button"
            className={i === indiceActivo ? `${estilos.dia} ${estilos.activo}` : estilos.dia}
            onClick={() => onSeleccionar(i)}
          >
            <span className={estilos.nombre}>{dia.nombre}</span>
            <span className={estilos.numero}>{dia.numero}</span>
            <span className={estilos.mes}>{dia.mes}</span>
          </button>
        ))}
      </div>

      <button
        type="button"
        className={estilos.flecha}
        onClick={() => mover(1)}
        disabled={indiceActivo === dias.length - 1}
        aria-label="Día siguiente"
      >
        →
      </button>
    </div>
  );
}

SelectorDias.propTypes = {
  dias: PropTypes.arrayOf(
    PropTypes.shape({
      fecha: PropTypes.instanceOf(Date).isRequired,
      nombre: PropTypes.string.isRequired,
      numero: PropTypes.number.isRequired,
      mes: PropTypes.string.isRequired,
    }),
  ).isRequired,
  indiceActivo: PropTypes.number.isRequired,
  onSeleccionar: PropTypes.func.isRequired,
};
