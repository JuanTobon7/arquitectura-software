import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import FiltroFormatos from '../components/FiltroFormatos/FiltroFormatos';
import TarjetaPelicula from '../components/TarjetaPelicula/TarjetaPelicula';
import { useCartelera } from '../hooks/useCartelera';
import { useAuth } from '../hooks/useAuth';
import { FORMATOS } from '../models/pelicula';
import estilos from './CarteleraPage.module.css';

export default function CarteleraPage() {
  const navegar = useNavigate();
  const [formato, setFormato] = useState('Todas');
  const { items, dias, seleccionarDia, cargando, error } = useCartelera(formato);
  const { usuario, salir } = useAuth();

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} />
      <main className={estilos.pagina}>
        <header className={estilos.cabecera}>
          <h2 className={estilos.titulo}>CARTELERA</h2>
          <span className="hud-label">PROGRAMACIÓN · PRÓXIMOS 7 DÍAS</span>
        </header>

        <FiltroFormatos opciones={FORMATOS} activa={formato} onSeleccionar={setFormato} />

        {error && (
          <p className={estilos.error}>
            No se pudo conectar con el backend ({error}). ¿Está corriendo en el puerto 8080?
          </p>
        )}

        {cargando && <p className={estilos.estado}>Cargando cartelera…</p>}

        {!cargando && !error && items.length === 0 && (
          <p className={estilos.estado}>No hay películas para el filtro seleccionado.</p>
        )}

        <div className={estilos.lista}>
          {items.map(({ pelicula, indiceDia, gruposFormato }) => (
            <TarjetaPelicula
              key={pelicula.id}
              pelicula={pelicula}
              dias={dias}
              indiceDia={indiceDia}
              gruposFormato={gruposFormato}
              onCambiarDia={(i) => seleccionarDia(pelicula.id, i)}
              onSeleccionarHorario={(funcion) => navegar(`/reserva/${funcion.id}`)}
            />
          ))}
        </div>
      </main>
      <Footer />
    </>
  );
}
