// Guion del video: cada escena es una imagen (tarjeta, terminal o captura real) + narración.
const URL_CS = 'codespaces.new/JuanTobon7/arquitectura-software';

module.exports = [
  {
    id: '01', tipo: 'portada',
    titulo: 'Taller de observabilidad', sub: 'Spring Boot · Prometheus · Loki · Grafana', pie: 'Guía paso a paso · 20 minutos',
    voz: 'Bienvenidos al taller de observabilidad. En unos veinte minutos van a levantar una mini tienda en Spring Boot con tres problemas escondidos, y los van a encontrar usando solo métricas, logs y dashboards. No necesitan instalar nada: basta un navegador y una cuenta gratuita de GitHub.',
  },
  {
    id: '02', tipo: 'lista', etiqueta: 'Plan del taller', titulo: 'Cuatro pasos',
    items: ['0 a 4 min · Levantar el entorno', '4 a 6 min · Generar tráfico con k6', '6 a 16 min · Tres misiones', '16 a 20 min · Reto: tu propia alerta'],
    voz: 'El taller tiene cuatro partes: levantar el entorno, generar tráfico, resolver tres misiones y un reto final. Pausen el video cuando lo necesiten.',
  },
  {
    id: '03', tipo: 'lista', etiqueta: 'Paso 1 · Opción A', titulo: 'Abrir GitHub Codespaces',
    items: [`Abre ${URL_CS} (el enlace completo está en la guía)`, 'Inicia sesión con tu cuenta de GitHub', 'Pulsa Create codespace y espera 1 a 2 minutos', '¿Prefieres tu PC? Opción B: Docker Desktop + git clone'],
    voz: 'Paso uno. Abran el enlace de Codespaces que está en la guía, inicien sesión con GitHub y pulsen Create codespace. En uno o dos minutos se abre Visual Studio Code en el navegador, ya ubicado en la carpeta del taller. Si prefieren trabajar en su computador con Docker Desktop, la guía también trae esa opción.',
  },
  {
    id: '04', tipo: 'terminal', etiqueta: 'Paso 1 · Levantar el stack',
    lineas: [
      ['$', 'docker compose up -d --build'],
      ['', ' observabilidad-demo-api  Built'],
      ['', ' Container loki        Started'],
      ['', ' Container prometheus  Started'],
      ['', ' Container api-demo    Started'],
      ['', ' Container grafana     Started'],
      ['$', 'docker ps --format "table {{.Names}}\\t{{.Status}}"'],
      ['', 'NAMES        STATUS'],
      ['', 'grafana      Up'],
      ['', 'api-demo     Up'],
      ['', 'loki         Up'],
      ['', 'prometheus   Up'],
    ],
    voz: 'En la terminal escriban: docker compose up, guion d, guion guion build. La primera vez compila la API con Maven dentro de Docker, así que tarda un par de minutos. Al final deben ver cuatro contenedores: api-demo, prometheus, loki y grafana.',
  },
  {
    id: '05', tipo: 'terminal', etiqueta: 'Paso 1 · Verificar la API',
    lineas: [
      ['$', 'curl -s localhost:8080/actuator/health'],
      ['', '{"status":"UP"}'],
      ['#', 'Si dice "connection refused", espera 20 segundos y repite'],
    ],
    voz: 'Verifiquen que la API respondió con curl a actuator health. Debe decir status UP. Si dice connection refused, esperen unos segundos y repitan.',
  },
  {
    id: '06', tipo: 'captura', etiqueta: 'Paso 1 · Abrir Grafana', img: 'dashboard.png',
    nota: 'Pestaña PORTS → puerto 3000 (Grafana). Sin usuario ni contraseña.',
    voz: 'Ahora abran la pestaña PORTS, junto a la terminal, y hagan clic en el globo del puerto tres mil. Se abre Grafana, sin usuario ni contraseña, directamente en el dashboard de la API.',
  },
  {
    id: '07', tipo: 'terminal', etiqueta: 'Paso 2 · Generar tráfico',
    lineas: [
      ['$', 'DURACION=6m docker compose --profile carga run -d --rm k6'],
      ['', '13934bf15261...'],
      ['#', '~40 peticiones/s durante 6 minutos, en segundo plano'],
      ['#', 'POST /api/pagos · GET /api/catalogo'],
      ['#', 'GET /api/pedidos/n-mas-1 · GET /api/pedidos/optimizado'],
    ],
    voz: 'Paso dos: generar tráfico. Ejecuten el comando de k6 que está en la guía. Lanza unas cuarenta peticiones por segundo durante seis minutos contra cuatro endpoints. Corre en segundo plano, así que pueden seguir trabajando.',
  },
  {
    id: '08', tipo: 'captura', etiqueta: 'Paso 2 · El dashboard con tráfico', img: 'dashboard.png',
    nota: 'Tráfico · Errores 5xx · Latencia p95 · Consultas SQL por request',
    voz: 'Esperen un minuto y miren el dashboard. Arriba está el tráfico y la tasa de errores; abajo, la latencia p95 y las consultas SQL por request. Ya se ve que algo raro pasa con pagos y con uno de los endpoints de pedidos. Vamos a investigarlo.',
  },
  {
    id: '09', tipo: 'captura', etiqueta: 'Misión 1 · ¿El promedio miente? (PromQL)', img: 'explore-latencia.png',
    nota: 'Explore → Prometheus → modo Code → las 3 consultas de la guía',
    voz: 'Misión uno: ¿el promedio miente? Abran Explore, elijan Prometheus, cambien a modo Code y peguen las tres consultas de la guía: el promedio, el percentil noventa y cinco y el percentil noventa y nueve de los pagos. Comparen las líneas: el promedio ronda los cien milisegundos, pero el p99 pasa de un segundo.',
  },
  {
    id: '10', tipo: 'captura', etiqueta: 'Misión 1 · ¿El servidor está saturado?', img: 'panel-tomcat.png',
    nota: 'Hilos Tomcat ocupados: casi en reposo. El problema es la pasarela externa.',
    voz: 'Antes de culpar al servidor, miren los hilos de Tomcat y la CPU. Están casi en reposo. El problema no es de capacidad: es la pasarela de pagos, una dependencia externa. Escalar servidores no lo arreglaría.',
  },
  {
    id: '11', tipo: 'terminal', etiqueta: 'Misión 2 · Cazar el N+1',
    lineas: [
      ['$', 'curl -s localhost:8080/api/pedidos/n-mas-1 | head -c 90'],
      ['', '[{"id":1,"total":580.0,"cliente":"cliente-1"},{"id":2,"total":602.0,...'],
      ['$', 'curl -s localhost:8080/api/pedidos/optimizado | head -c 90'],
      ['', '[{"id":1,"total":580.0,"cliente":"cliente-1"},{"id":2,"total":602.0,...'],
      ['#', 'Mismo JSON... ¿mismo costo?'],
    ],
    voz: 'Misión dos: cazar el N+1. Los dos endpoints de pedidos devuelven exactamente el mismo JSON. Compruébenlo con curl.',
  },
  {
    id: '12', tipo: 'captura', etiqueta: 'Misión 2 · Consultas SQL por request', img: 'panel-sql.png',
    nota: 'n-mas-1: 101 consultas · optimizado: 1 consulta',
    voz: 'Pero en el panel de consultas SQL por request, uno hace ciento una consultas y el otro solo una. Ciento una es una consulta para los pedidos, más una por cada uno de los cien pedidos: el clásico N+1.',
  },
  {
    id: '13', tipo: 'captura', etiqueta: 'Misión 2 · La evidencia en los logs (LogQL)', img: 'explore-nmas1.png',
    nota: '{app="api-demo"} | json | mdc_sql_consultas > 50',
    voz: 'Busquen la evidencia en los logs. En Explore elijan Loki y ejecuten la consulta de la guía. Cada línea trae el request id, la duración y el número de consultas. Después abran PedidoRepository y comparen findAll con findAllConItems.',
  },
  {
    id: '14', tipo: 'captura', etiqueta: 'Misión 3 · La alerta', img: 'alertas.png',
    nota: 'Prometheus (puerto 9090) → Alerts → TasaErroresAlta en FIRING',
    voz: 'Misión tres: de la alerta al log. Abran Prometheus en el puerto nueve mil noventa, menú Alerts. La alerta TasaErroresAlta está en FIRING para pagos.',
  },
  {
    id: '15', tipo: 'captura', etiqueta: 'Misión 3 · El log explica el porqué', img: 'explore-errores.png',
    nota: '{app="api-demo", level="ERROR"}',
    voz: 'En Loki filtren por nivel ERROR. Todos los mensajes dicen lo mismo: la pasarela de pagos respondió timeout. El problema es de la dependencia, no de nuestra base de datos.',
  },
  {
    id: '16', tipo: 'terminal', etiqueta: 'Reto · Tu propia alerta de p99',
    lineas: [
      ['#', 'infra/prometheus/alertas.yml  (al final de rules:)'],
      ['', '      - alert: LatenciaP99Alta'],
      ['', '        expr: |'],
      ['', '          histogram_quantile(0.99,'],
      ['', '            sum by (le, uri) (rate(http_server_requests_seconds_bucket{uri!~"/actuator.*"}[2m]))'],
      ['', '          ) > 1'],
      ['', '        for: 30s'],
      ['', '        labels:'],
      ['', '          severity: warning'],
      ['$', 'curl -X POST localhost:9090/-/reload'],
    ],
    voz: 'El reto final. La alerta de p95 no se dispara, aunque hay pagos de casi dos segundos. Abran el archivo alertas punto yml, peguen al final la regla LatenciaP99Alta de la guía respetando la indentación, y recarguen Prometheus con el curl que ven en pantalla.',
  },
  {
    id: '17', tipo: 'captura', etiqueta: 'Reto · La alerta se dispara', img: 'alertas-reto.png',
    nota: 'LatenciaP99Alta pasa de Pending a FIRING en menos de un minuto',
    voz: 'En menos de un minuto la alerta pasa de Pending a Firing. Elegir el percentil del objetivo de servicio cambia lo que ven.',
  },
  {
    id: '18', tipo: 'lista', etiqueta: 'Cierre', titulo: 'Antes de irte',
    items: ['Compara tus números con la tabla de referencia de la guía', 'Apaga todo: docker compose down', 'Detén el codespace en github.com/codespaces', 'Trae tus dudas a la sesión de preguntas'],
    voz: 'Para terminar, comparen sus números con la tabla de referencia al final de la guía. Luego apaguen todo con docker compose down y detengan el codespace para no gastar sus horas gratis. Eso es todo. ¡Gracias!',
  },
];
