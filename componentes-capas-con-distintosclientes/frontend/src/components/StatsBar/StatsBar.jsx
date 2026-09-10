import PropTypes from 'prop-types';
import estilos from './StatsBar.module.css';

export default function StatsBar({ estadisticas }) {
  return (
    <section className={estilos.franja}>
      {estadisticas.map((stat) => (
        <article key={stat.titulo} className={estilos.tarjeta}>
          <span className={estilos.anillo} aria-hidden="true" />
          <div>
            <h3 className={estilos.tituloStat}>{stat.titulo}</h3>
            <p className={estilos.valor}>
              {stat.valor} <small>{stat.unidad}</small>
            </p>
            <p className={estilos.detalle}>{stat.detalle}</p>
          </div>
        </article>
      ))}
    </section>
  );
}

StatsBar.propTypes = {
  estadisticas: PropTypes.arrayOf(
    PropTypes.shape({
      titulo: PropTypes.string.isRequired,
      valor: PropTypes.string.isRequired,
      unidad: PropTypes.string,
      detalle: PropTypes.string,
    }),
  ).isRequired,
};
