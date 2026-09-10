import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import TarjetaCatalogo from '../components/TarjetaCatalogo/TarjetaCatalogo';
import { usePeliculas } from '../hooks/usePeliculas';
import { useAuth } from '../hooks/useAuth';
import estilos from './PeliculasPage.module.css';

export default function PeliculasPage() {
  const navegar = useNavigate();
  const { usuario, salir } = useAuth();
  const { peliculas, cargando, error } = usePeliculas();
  const [busqueda, setBusqueda] = useState('');

  const visibles = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    if (!texto) return peliculas;
    return peliculas.filter(
      (p) =>
        p.titulo.toLowerCase().includes(texto) || p.genero.toLowerCase().includes(texto),
    );
  }, [peliculas, busqueda]);

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} onBuscar={setBusqueda} />
      <main className={estilos.pagina}>
        <header className={estilos.cabecera}>
          <h2 className={estilos.titulo}>PELÍCULAS</h2>
          <span className="hud-label">
            CATÁLOGO · {peliculas.length} TÍTULOS EN LA RED HAKARI
          </span>
        </header>

        {error && (
          <p className={estilos.error}>
            No se pudo conectar con el backend ({error}). ¿Está corriendo en el puerto 8080?
          </p>
        )}
        {cargando && <p className={estilos.estado}>Cargando catálogo…</p>}

        {!cargando && !error && visibles.length === 0 && (
          <p className={estilos.estado}>
            Nada coincide con «{busqueda}» — prueba con otro título o género.
          </p>
        )}

        <div className={estilos.rejilla}>
          {visibles.map((pelicula) => (
            <TarjetaCatalogo
              key={pelicula.id}
              pelicula={pelicula}
              onVerHorarios={() => navegar('/cartelera')}
            />
          ))}
        </div>
      </main>
      <Footer />
    </>
  );
}
