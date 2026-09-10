import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../hooks/useAuth';
import estilos from './SoportePage.module.css';

const PREGUNTAS = [
  {
    pregunta: '¿Cómo reservo mis asientos?',
    respuesta:
      'Entra a Cartelera, elige película, día y horario. En el mapa de la sala selecciona hasta 8 butacas, confirma con tu nombre y recibirás un código único (MHK-XXXXXX).',
  },
  {
    pregunta: '¿Puedo cancelar una reserva?',
    respuesta:
      'Sí: justo después de confirmar verás el botón "Cancelar reserva". Al cancelar, tus asientos quedan libres de inmediato para otras personas.',
  },
  {
    pregunta: '¿Qué diferencia hay entre los formatos?',
    respuesta:
      '2D es proyección estándar; 3D usa gafas estereoscópicas; IMAX es pantalla gigante con sonido de precisión; IMAX 3D combina ambos; 4D añade butacas con movimiento, viento y aromas.',
  },
  {
    pregunta: '¿Necesito una cuenta para reservar?',
    respuesta:
      'No es obligatorio, pero con tu cuenta (botón ÚNETE) el formulario de reserva se llena solo con tu nombre y correo.',
  },
  {
    pregunta: '¿Las películas de la cartelera son reales?',
    respuesta:
      'No: MICOS HAKARI es un proyecto académico y todos los títulos, pósters y sinopsis son ficticios, creados para esta demostración.',
  },
];

export default function SoportePage() {
  const { usuario, salir } = useAuth();

  return (
    <>
      <Navbar usuario={usuario} onCerrarSesion={salir} />
      <main className={estilos.pagina}>
        <header className={estilos.cabecera}>
          <h2 className={estilos.titulo}>SOPORTE</h2>
          <span className="hud-label">CENTRO DE AYUDA · RED HAKARI</span>
        </header>

        <div className={estilos.contacto}>
          <article className={estilos.tarjetaContacto}>
            <span className={estilos.icono}>✉</span>
            <div>
              <h3>Escríbenos</h3>
              <p>info@micoshakari.cine</p>
            </div>
          </article>
          <article className={estilos.tarjetaContacto}>
            <span className={estilos.icono}>☏</span>
            <div>
              <h3>Llámanos</h3>
              <p>+34 900 01 01 01 · 24/7</p>
            </div>
          </article>
          <article className={estilos.tarjetaContacto}>
            <span className={estilos.icono}>⌖</span>
            <div>
              <h3>Visítanos</h3>
              <p>Distrito Neón 42, Megaciudad</p>
            </div>
          </article>
        </div>

        <section className={estilos.faq}>
          <h3 className={estilos.subtitulo}>PREGUNTAS FRECUENTES</h3>
          {PREGUNTAS.map((item) => (
            <details key={item.pregunta} className={estilos.detalle}>
              <summary className={estilos.pregunta}>{item.pregunta}</summary>
              <p className={estilos.respuesta}>{item.respuesta}</p>
            </details>
          ))}
        </section>
      </main>
      <Footer />
    </>
  );
}
