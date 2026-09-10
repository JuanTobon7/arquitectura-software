import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import MapaAsientos from '../components/MapaAsientos/MapaAsientos';
import { useReserva } from '../hooks/useReserva';
import { useAuth } from '../hooks/useAuth';
import estilos from './ReservaPage.module.css';

export default function ReservaPage() {
  const { funcionId } = useParams();
  const navegar = useNavigate();
  const {
    disponibilidad,
    seleccionados,
    reserva,
    cargando,
    enviando,
    error,
    alternarAsiento,
    confirmar,
    cancelar,
  } = useReserva(funcionId);

  const { usuario, salir } = useAuth();
  const [nombre, setNombre] = useState(usuario?.nombre ?? '');
  const [email, setEmail] = useState(usuario?.email ?? '');

  const enviar = async (e) => {
    e.preventDefault();
    await confirmar({ nombreCliente: nombre, email });
  };

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} />
      <main className={estilos.pagina}>
        <header className={estilos.cabecera}>
          <div>
            <h2 className={estilos.titulo}>RESERVA DE ASIENTOS</h2>
            <span className="hud-label">FUNCIÓN #{funcionId} · SELECCIONA TU BUTACA</span>
          </div>
          <button type="button" className={estilos.volver} onClick={() => navegar('/cartelera')}>
            ← VOLVER A CARTELERA
          </button>
        </header>

        {cargando && <p className={estilos.estado}>Cargando sala…</p>}
        {error && <p className={estilos.error}>{error}</p>}

        {!cargando && disponibilidad && (
          <div className={estilos.panel}>
            <MapaAsientos
              filas={disponibilidad.filas}
              columnas={disponibilidad.columnas}
              ocupados={disponibilidad.ocupados}
              seleccionados={seleccionados}
              onToggle={alternarAsiento}
            />

            {reserva ? (
              <aside className={estilos.confirmacion}>
                <span className="hud-label">RESERVA CONFIRMADA</span>
                <strong className={estilos.codigo}>{reserva.codigo}</strong>
                <p>
                  {reserva.nombreCliente} · asientos{' '}
                  <b>{reserva.asientos.join(', ')}</b>
                </p>
                <button
                  type="button"
                  className={estilos.botonSecundario}
                  onClick={cancelar}
                  disabled={enviando}
                >
                  CANCELAR RESERVA
                </button>
              </aside>
            ) : (
              <form className={estilos.formulario} onSubmit={enviar}>
                <label className={estilos.campo}>
                  <span>NOMBRE</span>
                  <input
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    placeholder="Tu nombre"
                    required
                  />
                </label>
                <label className={estilos.campo}>
                  <span>EMAIL</span>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="tu@correo.com"
                  />
                </label>

                <p className={estilos.resumen}>
                  {seleccionados.length === 0
                    ? 'Ningún asiento seleccionado'
                    : `Asientos: ${seleccionados.join(', ')}`}
                </p>

                <button
                  type="submit"
                  className={estilos.botonPrimario}
                  disabled={seleccionados.length === 0 || enviando}
                >
                  {enviando ? 'RESERVANDO…' : 'CONFIRMAR RESERVA ▸'}
                </button>
              </form>
            )}
          </div>
        )}
      </main>
      <Footer />
    </>
  );
}
