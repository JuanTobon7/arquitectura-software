// Generador de carga con k6: tráfico constante sobre los cuatro endpoints.
import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const DURACION = __ENV.DURACION || '4m';

function escenario(rps, exec) {
  return {
    executor: 'constant-arrival-rate',
    rate: rps,
    timeUnit: '1s',
    duration: DURACION,
    preAllocatedVUs: 20,
    maxVUs: 100,
    exec,
  };
}

export const options = {
  scenarios: {
    pagos: escenario(20, 'pagos'),
    catalogo: escenario(10, 'catalogo'),
    pedidos_n_mas_1: escenario(5, 'pedidosNMas1'),
    pedidos_optimizado: escenario(5, 'pedidosOptimizado'),
  },
  thresholds: {
    'http_req_duration{scenario:pagos}': ['p(95)<500'],
    'http_req_duration{scenario:pedidos_n_mas_1}': ['p(95)<300'],
    'http_req_failed{scenario:pagos}': ['rate<0.02'],
  },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

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
