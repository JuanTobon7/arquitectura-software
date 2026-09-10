import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import SocialSidebar from '../components/SocialSidebar/SocialSidebar';
import HeroSection from '../components/HeroSection/HeroSection';
import StatsBar from '../components/StatsBar/StatsBar';
import TrailerModal from '../components/TrailerModal/TrailerModal';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../hooks/useAuth';

const ESTADISTICAS = [
  { titulo: 'Usuarios activos', valor: '+15', unidad: 'MIL', detalle: 'Cinéfilos conectados a la red HAKARI' },
  { titulo: 'Funciones hoy', valor: '+25', unidad: 'SALAS', detalle: '2D · 3D · IMAX · IMAX 3D · 4D' },
  { titulo: 'Comunidad global', valor: '98%', unidad: 'SCORE', detalle: 'Valoración media de la experiencia' },
];

export default function HomePage() {
  const navegar = useNavigate();
  const { usuario, salir } = useAuth();
  const [trailerAbierto, setTrailerAbierto] = useState(false);

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} />
      <SocialSidebar />
      <main>
        <HeroSection
          onVerCartelera={() => navegar('/cartelera')}
          onVerTrailer={() => setTrailerAbierto(true)}
        />
        <StatsBar estadisticas={ESTADISTICAS} />
      </main>
      <TrailerModal abierto={trailerAbierto} onCerrar={() => setTrailerAbierto(false)} />
      <Footer />
    </>
  );
}
