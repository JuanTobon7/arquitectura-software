// Generador de carga con k6. Todo se ajusta desde la terminal con variables de entorno:
//
//   DURACION    duración de cada escenario (por defecto 4m)
//   ENDPOINTS   escenarios a correr, separados por coma (por defecto todos):
//               pagos,catalogo,n-mas-1,optimizado
//
// Modo 1, tasa fija (por defecto): k6 envía N peticiones por segundo aunque la API se ponga lenta.
//   FACTOR      multiplica las tasas base (20, 10, 5 y 5 req/s). FACTOR=10 da unas 400 req/s.
//   RPS_PAGOS, RPS_CATALOGO, RPS_N_MAS_1, RPS_OPTIMIZADO   fijan la tasa de un endpoint a mano.
//   MAX_VUS     tope de usuarios virtuales que k6 puede abrir para sostener la tasa (por defecto 100).
//
// Modo 2, usuarios concurrentes: se activa al definir USUARIOS.
//   USUARIOS    usuarios virtuales por escenario, cada uno pide sin pausa (un usuario = un hilo)
//   ITERACIONES si se define, cada usuario hace ese número de peticiones y termina;
//               si no, los usuarios piden sin parar durante DURACION.
//   RAMPA       si se define (p. ej. 1m), los usuarios suben de 0 a USUARIOS en ese tiempo.
//
// Ejemplos (desde observabilidad-demo):
//   docker compose --profile carga run --rm -e FACTOR=5 k6
//   docker compose --profile carga run --rm -e ENDPOINTS=n-mas-1 -e USUARIOS=200 -e DURACION=2m k6
//   docker compose --profile carga run --rm -e USUARIOS=50 -e ITERACIONES=100 k6
import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const DURACION = __ENV.DURACION || '4m';
const FACTOR = Number(__ENV.FACTOR || 1);
const MAX_VUS = Number(__ENV.MAX_VUS || 100);
const USUARIOS = Number(__ENV.USUARIOS || 0);
const ITERACIONES = Number(__ENV.ITERACIONES || 0);
const RAMPA = __ENV.RAMPA || '';

const BASES = {
  pagos: { exec: 'pagos', rps: Number(__ENV.RPS_PAGOS || 20 * FACTOR) },
  catalogo: { exec: 'catalogo', rps: Number(__ENV.RPS_CATALOGO || 10 * FACTOR) },
  'n-mas-1': { exec: 'pedidosNMas1', rps: Number(__ENV.RPS_N_MAS_1 || 5 * FACTOR) },
  optimizado: { exec: 'pedidosOptimizado', rps: Number(__ENV.RPS_OPTIMIZADO || 5 * FACTOR) },
};

const elegidos = (__ENV.ENDPOINTS || Object.keys(BASES).join(','))
  .split(',').map((e) => e.trim()).filter((e) => BASES[e]);

function escenario({ exec, rps }) {
  if (USUARIOS > 0 && ITERACIONES > 0) {
    return { executor: 'per-vu-iterations', vus: USUARIOS, iterations: ITERACIONES, maxDuration: '30m', exec };
  }
  if (USUARIOS > 0 && RAMPA) {
    return {
      executor: 'ramping-vus', startVUs: 0, exec,
      stages: [{ duration: RAMPA, target: USUARIOS }, { duration: DURACION, target: USUARIOS }],
    };
  }
  if (USUARIOS > 0) {
    return { executor: 'constant-vus', vus: USUARIOS, duration: DURACION, exec };
  }
  return {
    executor: 'constant-arrival-rate', rate: Math.max(1, Math.round(rps)), timeUnit: '1s',
    duration: DURACION, preAllocatedVUs: Math.min(20, MAX_VUS), maxVUs: MAX_VUS, exec,
  };
}

const scenarios = {};
for (const nombre of elegidos) scenarios[nombre.replace(/-/g, '_')] = escenario(BASES[nombre]);

export const options = {
  scenarios,
  thresholds: {
    'http_req_duration{scenario:pagos}': ['p(95)<500'],
    'http_req_duration{scenario:n_mas_1}': ['p(95)<300'],
    'http_req_failed{scenario:pagos}': ['rate<0.02'],
  },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
  const modo = USUARIOS > 0
    ? `${USUARIOS} usuarios por escenario` + (ITERACIONES ? `, ${ITERACIONES} iteraciones cada uno` : `, ${RAMPA ? 'rampa ' + RAMPA + ' + ' : ''}${DURACION}`)
    : `tasa fija (FACTOR=${FACTOR}): ` + elegidos.map((e) => `${e}=${Math.round(BASES[e].rps)} req/s`).join(', ');
  console.log(`Carga contra ${BASE} -> ${modo}`);
}

export function pagos() {
  const r = http.post(`${BASE}/api/pagos`, null, { tags: { endpoint: 'pagos' } });
  check(r, { 'pago 200': (x) => x.status === 200 });
}

export function catalogo() {
  http.get(`${BASE}/api/catalogo`, { tags: { endpoint: 'catalogo' } });
}

export function pedidosNMas1() {
  http.get(`${BASE}/api/pedidos/n-mas-1`, { tags: { endpoint: 'n-mas-1' } });
}

export function pedidosOptimizado() {
  http.get(`${BASE}/api/pedidos/optimizado`, { tags: { endpoint: 'optimizado' } });
}

export function handleSummary(data) {
  return {
    '/resultados/k6-resumen.json': JSON.stringify(data, null, 2),
    stdout: '\nResumen guardado en resultados/k6-resumen.json\n',
  };
}
