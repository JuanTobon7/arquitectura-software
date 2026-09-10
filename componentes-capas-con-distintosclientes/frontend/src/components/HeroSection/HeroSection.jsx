import PropTypes from 'prop-types';
import estilos from './HeroSection.module.css';

export default function HeroSection({ onVerCartelera, onVerTrailer }) {
  return (
    <section className={estilos.hero}>
      <span className={estilos.tituloFondo} aria-hidden="true">
        HAKARI
      </span>

      <div className={estilos.contenido}>
        <span className="hud-label">— ENTRA A LA SIGUIENTE DIMENSIÓN —</span>

        <h1 className={estilos.titulo}>
          MICOS <span>HAKARI</span>
        </h1>

        <p className={estilos.tagline}>
          Donde la imaginación se encuentra con la pantalla. Cine 2D, 3D, IMAX y 4D
          en el corazón de la megaciudad.
        </p>

        <div className={estilos.ctas}>
          <button type="button" className={estilos.ctaPrimario} onClick={onVerCartelera}>
            VER CARTELERA ▸
          </button>
          <button type="button" className={estilos.ctaSecundario} onClick={onVerTrailer}>
            ▷ VER TRÁILER
          </button>
        </div>
      </div>

      <div className={estilos.hudEsquina}>
        <span>WHERE IMAGINATION</span>
        <span>MEETS REALITY</span>
        <strong>01 / 06</strong>
      </div>
    </section>
  );
}

HeroSection.propTypes = {
  onVerCartelera: PropTypes.func.isRequired,
  onVerTrailer: PropTypes.func,
};
