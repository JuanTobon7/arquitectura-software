import PropTypes from 'prop-types';
import estilos from './MapaAsientos.module.css';

export default function MapaAsientos({ filas, columnas, ocupados, seleccionados, onToggle }) {
  const etiquetasFila = Array.from({ length: filas }, (_, i) => String.fromCharCode(65 + i));

  return (
    <div className={estilos.contenedor}>
      <div className={estilos.pantalla}>PANTALLA</div>

      <div className={estilos.sala} style={{ '--columnas': columnas }}>
        {etiquetasFila.map((fila) => (
          <div key={fila} className={estilos.fila}>
            <span className={estilos.etiquetaFila}>{fila}</span>
            {Array.from({ length: columnas }, (_, c) => {
              const etiqueta = `${fila}${c + 1}`;
              const ocupado = ocupados.includes(etiqueta);
              const seleccionado = seleccionados.includes(etiqueta);
              const clase = ocupado
                ? `${estilos.asiento} ${estilos.ocupado}`
                : seleccionado
                  ? `${estilos.asiento} ${estilos.seleccionado}`
                  : estilos.asiento;
              return (
                <button
                  key={etiqueta}
                  type="button"
                  className={clase}
                  disabled={ocupado}
                  onClick={() => onToggle(etiqueta)}
                  aria-label={`Asiento ${etiqueta}${ocupado ? ' (ocupado)' : ''}`}
                  title={etiqueta}
                />
              );
            })}
            <span className={estilos.etiquetaFila}>{fila}</span>
          </div>
        ))}
      </div>

      <div className={estilos.leyenda}>
        <span><i className={estilos.puntoLibre} /> Libre</span>
        <span><i className={estilos.puntoSeleccion} /> Seleccionado</span>
        <span><i className={estilos.puntoOcupado} /> Ocupado</span>
      </div>
    </div>
  );
}

MapaAsientos.propTypes = {
  filas: PropTypes.number.isRequired,
  columnas: PropTypes.number.isRequired,
  ocupados: PropTypes.arrayOf(PropTypes.string).isRequired,
  seleccionados: PropTypes.arrayOf(PropTypes.string).isRequired,
  onToggle: PropTypes.func.isRequired,
};
