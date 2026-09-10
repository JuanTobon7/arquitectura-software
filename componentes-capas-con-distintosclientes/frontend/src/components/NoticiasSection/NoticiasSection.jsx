import PropTypes from 'prop-types';
import estilos from './NoticiasSection.module.css';

export default function NoticiasSection({ noticias }) {
  return (
    <section className={estilos.seccion} id="noticias">
      <header className={estilos.cabecera}>
        <h2 className={estilos.titulo}>NOTICIAS</h2>
        <span className="hud-label">TRANSMISIÓN · RED HAKARI</span>
      </header>

      <div className={estilos.rejilla}>
        {noticias.map((noticia) => (
          <article key={noticia.titulo} className={estilos.tarjeta}>
            <span className={estilos.etiqueta}>{noticia.etiqueta}</span>
            <h3 className={estilos.tituloNoticia}>{noticia.titulo}</h3>
            <p className={estilos.resumen}>{noticia.resumen}</p>
            <span className={estilos.fecha}>{noticia.fecha}</span>
          </article>
        ))}
      </div>
    </section>
  );
}

NoticiasSection.propTypes = {
  noticias: PropTypes.arrayOf(
    PropTypes.shape({
      etiqueta: PropTypes.string,
      titulo: PropTypes.string.isRequired,
      resumen: PropTypes.string,
      fecha: PropTypes.string,
    }),
  ).isRequired,
};
