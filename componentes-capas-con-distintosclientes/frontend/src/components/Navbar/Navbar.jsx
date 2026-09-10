import PropTypes from 'prop-types';
import { Link, NavLink } from 'react-router-dom';
import estilos from './Navbar.module.css';

const ENLACES = [
  { a: '/', texto: 'Home', exacto: true },
  { a: '/cartelera', texto: 'Cartelera' },
  { a: '/peliculas', texto: 'Películas' },
  { a: '/noticias', texto: 'Noticias' },
  { a: '/soporte', texto: 'Soporte' },
];

export default function Navbar({ onBuscar, usuario, onCerrarSesion }) {
  return (
    <header className={estilos.barra}>
      <NavLink to="/" className={estilos.logo}>
        <span className={estilos.logoIcono}>✕</span>
        MICOS <em>HAKARI</em>
      </NavLink>

      <nav className={estilos.enlaces}>
        {ENLACES.map((enlace) => (
          <NavLink
            key={enlace.texto}
            to={enlace.a}
            className={({ isActive }) =>
              isActive ? `${estilos.enlace} ${estilos.enlaceActivo}` : estilos.enlace
            }
            end={enlace.exacto}
          >
            {enlace.texto}
          </NavLink>
        ))}
      </nav>

      <div className={estilos.acciones}>
        <input
          type="search"
          placeholder="Buscar género..."
          className={estilos.buscador}
          onChange={(e) => onBuscar?.(e.target.value)}
          aria-label="Buscar por género"
        />
        {usuario ? (
          <div className={estilos.sesion}>
            <span className={estilos.saludo} title={usuario.email}>
              HOLA, {usuario.nombre.split(' ')[0].toUpperCase()}
            </span>
            <button type="button" className={estilos.salir} onClick={onCerrarSesion}>
              SALIR
            </button>
          </div>
        ) : (
          <Link to="/cuenta" className={estilos.cta}>
            ÚNETE
          </Link>
        )}
      </div>
    </header>
  );
}

Navbar.propTypes = {
  onBuscar: PropTypes.func,
  usuario: PropTypes.shape({
    nombre: PropTypes.string.isRequired,
    email: PropTypes.string,
  }),
  onCerrarSesion: PropTypes.func,
};
