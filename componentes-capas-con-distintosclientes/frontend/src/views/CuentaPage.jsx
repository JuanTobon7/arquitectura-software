import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../hooks/useAuth';
import estilos from './CuentaPage.module.css';

export default function CuentaPage() {
  const navegar = useNavigate();
  const { usuario, enviando, error, registrar, entrar, salir } = useAuth();

  const [modo, setModo] = useState('login');
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [clave, setClave] = useState('');

  const enviar = async (e) => {
    e.preventDefault();
    const perfil =
      modo === 'registro'
        ? await registrar({ nombre, email, clave })
        : await entrar({ email, clave });
    if (perfil) navegar('/');
  };

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} />
      <main className={estilos.pagina}>
        <section className={estilos.panel}>
          <span className="hud-label">— ACCESO A LA RED HAKARI —</span>
          <h2 className={estilos.titulo}>
            {modo === 'login' ? 'INICIAR SESIÓN' : 'CREAR CUENTA'}
          </h2>

          <div className={estilos.pestanas} role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={modo === 'login'}
              className={modo === 'login' ? `${estilos.pestana} ${estilos.activa}` : estilos.pestana}
              onClick={() => setModo('login')}
            >
              LOGIN
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={modo === 'registro'}
              className={
                modo === 'registro' ? `${estilos.pestana} ${estilos.activa}` : estilos.pestana
              }
              onClick={() => setModo('registro')}
            >
              REGISTRO
            </button>
          </div>

          <form className={estilos.formulario} onSubmit={enviar}>
            {modo === 'registro' && (
              <label className={estilos.campo}>
                <span>NOMBRE</span>
                <input
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder="Tu nombre"
                  required
                />
              </label>
            )}

            <label className={estilos.campo}>
              <span>EMAIL</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="tu@correo.com"
                required
              />
            </label>

            <label className={estilos.campo}>
              <span>CONTRASEÑA</span>
              <input
                type="password"
                value={clave}
                onChange={(e) => setClave(e.target.value)}
                placeholder={modo === 'registro' ? 'Mínimo 6 caracteres' : '••••••••'}
                minLength={modo === 'registro' ? 6 : undefined}
                required
              />
            </label>

            {error && <p className={estilos.error}>{error}</p>}

            <button type="submit" className={estilos.boton} disabled={enviando}>
              {enviando
                ? 'CONECTANDO…'
                : modo === 'login'
                  ? 'ENTRAR ▸'
                  : 'ÚNETE A LA RED ▸'}
            </button>
          </form>

          <p className={estilos.alterno}>
            {modo === 'login' ? '¿Aún no tienes cuenta?' : '¿Ya tienes cuenta?'}{' '}
            <button
              type="button"
              className={estilos.enlaceAlterno}
              onClick={() => setModo(modo === 'login' ? 'registro' : 'login')}
            >
              {modo === 'login' ? 'Regístrate' : 'Inicia sesión'}
            </button>
          </p>
        </section>
      </main>
      <Footer />
    </>
  );
}
