import { useEffect } from 'react';
import PropTypes from 'prop-types';
import estilos from './TrailerModal.module.css';

export default function TrailerModal({ abierto, onCerrar }) {

  useEffect(() => {
    if (!abierto) return undefined;
    const alTeclear = (e) => e.key === 'Escape' && onCerrar();
    window.addEventListener('keydown', alTeclear);
    return () => window.removeEventListener('keydown', alTeclear);
  }, [abierto, onCerrar]);

  if (!abierto) return null;

  return (
    <div className={estilos.fondo} onClick={onCerrar} role="presentation">
      <div
        className={estilos.reproductor}
        role="dialog"
        aria-modal="true"
        aria-label="Tráiler de Hakari: Protocolo Cero"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className={estilos.cerrar} onClick={onCerrar} aria-label="Cerrar tráiler">
          ✕
        </button>

        <div className={estilos.pantalla}>
          <span className={estilos.scanlines} aria-hidden="true" />

          <p className={`${estilos.escena} ${estilos.escena1}`}>MICOS HAKARI PRESENTA</p>
          <p className={`${estilos.escena} ${estilos.escena2}`}>
            EN UNA MEGACIUDAD SIN LEY…
          </p>
          <p className={`${estilos.escena} ${estilos.escena3}`}>
            …UNA UNIDAD SE REBELA
          </p>
          <div className={`${estilos.escena} ${estilos.escenaTitulo}`}>
            <span className={estilos.equis}>✕</span>
            <strong className={estilos.tituloGlitch} data-texto="HAKARI">
              HAKARI
            </strong>
            <span className={estilos.subtitulo}>PROTOCOLO CERO</span>
          </div>
          <p className={`${estilos.escena} ${estilos.escenaFinal}`}>
            PRÓXIMAMENTE · SOLO EN SALAS IMAX
          </p>
        </div>

        <div className={estilos.controles}>
          <span className={estilos.play}>▶</span>
          <span className={estilos.pista}>
            <span className={estilos.progreso} />
          </span>
          <span className={estilos.tiempo}>TEASER · 00:12</span>
        </div>
      </div>
    </div>
  );
}

TrailerModal.propTypes = {
  abierto: PropTypes.bool.isRequired,
  onCerrar: PropTypes.func.isRequired,
};
