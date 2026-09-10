import { Link } from 'react-router-dom';
import estilos from './Footer.module.css';

export default function Footer() {
  return (
    <footer className={estilos.pie} id="soporte">
      <div className={estilos.superior}>
        <Link to="/" className={estilos.logo}>
          <span>✕</span> MICOS <em>HAKARI</em>
        </Link>

        <nav className={estilos.enlaces}>
          <Link to="/">Home</Link>
          <Link to="/cartelera">Cartelera</Link>
          <Link to="/peliculas">Películas</Link>
          <Link to="/noticias">Noticias</Link>
          <Link to="/soporte">Soporte</Link>
        </nav>

        <div className={estilos.redes}>
          <a href="#redes" title="Facebook">f</a>
          <a href="#redes" title="X">x</a>
          <a href="#redes" title="Instagram">◉</a>
        </div>
      </div>

      <div className={estilos.inferior}>
        <p>
          info@micoshakari.cine · +34 900 01 01 01
        </p>
        <p>© {new Date().getFullYear()} MICOS HAKARI — todos los derechos reservados</p>
      </div>
    </footer>
  );
}
