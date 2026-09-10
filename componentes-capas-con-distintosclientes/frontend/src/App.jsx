import { BrowserRouter, Route, Routes } from 'react-router-dom';
import HomePage from './views/HomePage';
import CarteleraPage from './views/CarteleraPage';
import ReservaPage from './views/ReservaPage';
import CuentaPage from './views/CuentaPage';
import PeliculasPage from './views/PeliculasPage';
import NoticiasPage from './views/NoticiasPage';
import SoportePage from './views/SoportePage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/cartelera" element={<CarteleraPage />} />
        <Route path="/reserva/:funcionId" element={<ReservaPage />} />
        <Route path="/cuenta" element={<CuentaPage />} />
        <Route path="/peliculas" element={<PeliculasPage />} />
        <Route path="/noticias" element={<NoticiasPage />} />
        <Route path="/soporte" element={<SoportePage />} />
      </Routes>
    </BrowserRouter>
  );
}
