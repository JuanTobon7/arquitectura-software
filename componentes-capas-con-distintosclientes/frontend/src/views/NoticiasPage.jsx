import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import NoticiasSection from '../components/NoticiasSection/NoticiasSection';
import { useAuth } from '../hooks/useAuth';
import estilos from './NoticiasPage.module.css';

const NOTICIAS = [
  {
    etiqueta: 'ESTRENO',
    titulo: 'Llega "Hakari: Protocolo Cero" a la sala IMAX',
    resumen:
      'La saga insignia de la casa aterriza con proyección IMAX 3D y sonido envolvente de 64 canales. Preventa abierta para la primera semana.',
    fecha: 'ESTA SEMANA',
  },
  {
    etiqueta: 'COMEDIA',
    titulo: 'Triple estreno: Felipe, Diego y Julián',
    resumen:
      'La trilogía cómica del semestre llega en bloque: grasa, migajas y polas en 2D, 3D y 4D. Combo especial para las tres funciones.',
    fecha: 'YA EN CARTELERA',
  },
  {
    etiqueta: 'SALAS',
    titulo: 'La sala 4D estrena asientos con háptica',
    resumen:
      'Nueva generación de butacas con vibración sincronizada, viento y aromas para las funciones 4D.',
    fecha: 'PRÓXIMAMENTE',
  },
  {
    etiqueta: 'COMUNIDAD',
    titulo: 'Maratón de medianoche: ciclo de terror',
    resumen:
      '"Ecos del Subsuelo" encabeza el ciclo nocturno de octubre. Entradas 2x1 para miembros registrados de la red.',
    fecha: 'OCTUBRE',
  },
  {
    etiqueta: 'TECNOLOGÍA',
    titulo: 'Reserva de asientos en tiempo real',
    resumen:
      'El nuevo sistema de reservas muestra la ocupación de la sala al instante y confirma tu butaca con un código único MHK.',
    fecha: 'YA DISPONIBLE',
  },
  {
    etiqueta: 'ANIMACIÓN',
    titulo: '"El Último Píxel" tendrá función especial retro',
    resumen:
      'Proyección con maratón de videojuegos clásicos en el lobby y torneo arcade antes de la función de las 17:00.',
    fecha: 'FIN DE MES',
  },
];

export default function NoticiasPage() {
  const { usuario, salir } = useAuth();

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} />
      <main className={estilos.pagina}>
        <NoticiasSection noticias={NOTICIAS} />
      </main>
      <Footer />
    </>
  );
}
