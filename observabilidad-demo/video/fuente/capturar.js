// Captura las pantallas reales de Grafana y Prometheus con Edge headless.
// Uso: node capturar.js [nombre...]   (sin argumentos captura todas)
const { execFileSync } = require('child_process');
const path = require('path');

const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const SHOTS = path.join(__dirname, 'shots');
const now = Date.now();
const from = now - 6 * 60 * 1000;
const dash = (extra) => `http://localhost:3000/d/api-demo/?orgId=1&from=${from}&to=${now}&kiosk&theme=light${extra}`;

function explore(ds, ...qs) {
  const uid = ds === 'loki' ? 'loki' : 'prometheus';
  const queries = qs.map(([legendFormat, expr], i) => ({
    refId: String.fromCharCode(65 + i), expr, legendFormat, editorMode: 'code',
    datasource: { type: uid, uid }, ...(ds === 'loki' ? { queryType: 'range' } : {}),
  }));
  const panes = { a: { datasource: uid, queries, range: { from: 'now-6m', to: 'now' } } };
  return 'http://localhost:3000/explore?schemaVersion=1&orgId=1&theme=light&panes=' + encodeURIComponent(JSON.stringify(panes));
}

const pagos = 'uri="/api/pagos"';
const capturas = {
  'dashboard.png': [1920, 1080, dash('')],
  'panel-tomcat.png': [1600, 800, dash('&viewPanel=8')],
  'panel-sql.png': [1600, 800, dash('&viewPanel=5')],
  'explore-latencia.png': [1920, 1500, explore('prom',
    ['promedio', `sum(rate(http_server_requests_seconds_sum{${pagos}}[1m])) / sum(rate(http_server_requests_seconds_count{${pagos}}[1m]))`],
    ['p95', `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{${pagos}}[1m])))`],
    ['p99', `histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket{${pagos}}[1m])))`])],
  'explore-nmas1.png': [1920, 1300, explore('loki', ['', '{app="api-demo"} | json | mdc_sql_consultas > 50'])],
  'explore-errores.png': [1920, 1300, explore('loki', ['', '{app="api-demo", level="ERROR"}'])],
  'alertas.png': [1600, 900, 'http://localhost:9090/alerts'],
  'alertas-reto.png': [1600, 1100, 'http://localhost:9090/alerts'],
};

const pedidas = process.argv.slice(2);
for (const [nombre, [w, h, url]] of Object.entries(capturas)) {
  if (pedidas.length && !pedidas.includes(nombre)) continue;
  if (!pedidas.length && nombre === 'alertas-reto.png') continue;
  const out = execFileSync(EDGE, ['--headless=new', '--disable-gpu', '--hide-scrollbars', `--window-size=${w},${h}`,
    '--virtual-time-budget=20000', `--screenshot=${path.join(SHOTS, nombre)}`, url], { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
  console.log(nombre, /bytes written/.test(out) ? 'ok' : 'revisar');
}
