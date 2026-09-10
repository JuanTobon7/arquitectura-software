import estilos from './SocialSidebar.module.css';

const REDES = [
  { nombre: 'YT', titulo: 'YouTube' },
  { nombre: 'IG', titulo: 'Instagram' },
  { nombre: 'X', titulo: 'X' },
  { nombre: 'TW', titulo: 'Twitch' },
];

export default function SocialSidebar() {
  return (
    <aside className={estilos.lateral}>
      <span className={estilos.texto}>SÍGUENOS</span>
      <span className={estilos.linea} />
      {REDES.map((red) => (
        <a key={red.nombre} href="#redes" title={red.titulo} className={estilos.icono}>
          {red.nombre}
        </a>
      ))}
      <span className={estilos.linea} />
      <span className={estilos.texto}>SCROLL</span>
    </aside>
  );
}
