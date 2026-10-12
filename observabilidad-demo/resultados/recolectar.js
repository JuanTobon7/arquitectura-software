// Consulta Prometheus y Loki después de la prueba de carga y guarda los resultados en metricas.json.
// Uso: node resultados/recolectar.js <inicio_epoch> <fin_epoch>
const fs = require('fs');
const path = require('path');

const PROM = 'http://localhost:9090/api/v1';
const LOKI = 'http://localhost:3100/loki/api/v1';
const inicio = Number(process.argv[2]);
const fin = Number(process.argv[3]);
const ventana = `${fin - inicio}s`;
const F = 'uri!~"/actuator.*"';

async function prom(query) {
  const r = await fetch(`${PROM}/query?` + new URLSearchParams({ query, time: fin }));
  const j = await r.json();
  return j.data.result.map((x) => ({ labels: x.metric, valor: Number(x.value[1]) }));
}

async function promRango(query) {
  const r = await fetch(`${PROM}/query_range?` + new URLSearchParams({ query, start: inicio, end: fin, step: 10 }));
  const j = await r.json();
  return j.data.result.map((x) => ({ labels: x.metric, puntos: x.values.map(([t, v]) => [t - inicio, Number(v)]) }));
}

async function loki(query, extra = {}) {
  const params = { query, start: inicio * 1e9, end: fin * 1e9, limit: 10, ...extra };
  const tipo = extra.instant ? 'query' : 'query_range';
  if (extra.instant) { delete params.start; delete params.instant; params.time = fin * 1e9; }
  const r = await fetch(`${LOKI}/${tipo}?` + new URLSearchParams(params));
  return (await r.json()).data;
}

(async () => {
  const consultas = {
    p50: `histogram_quantile(0.50, sum by (le, uri) (increase(http_server_requests_seconds_bucket{${F}}[${ventana}])))`,
    p95: `histogram_quantile(0.95, sum by (le, uri) (increase(http_server_requests_seconds_bucket{${F}}[${ventana}])))`,
    p99: `histogram_quantile(0.99, sum by (le, uri) (increase(http_server_requests_seconds_bucket{${F}}[${ventana}])))`,
    rps: `sum by (uri) (rate(http_server_requests_seconds_count{${F}}[${ventana}]))`,
    errores_pct: `100 * sum by (uri) (increase(http_server_requests_seconds_count{status=~"5..",${F}}[${ventana}])) / sum by (uri) (increase(http_server_requests_seconds_count{${F}}[${ventana}]))`,
    sql_por_request: `sum by (uri) (increase(sql_consultas_por_request_sum{${F}}[${ventana}])) / sum by (uri) (increase(sql_consultas_por_request_count{${F}}[${ventana}]))`,
    total_requests: `sum by (uri) (increase(http_server_requests_seconds_count{${F}}[${ventana}]))`,
  };
  const instantaneas = {};
  for (const [k, q] of Object.entries(consultas)) instantaneas[k] = { promql: q, resultado: await prom(q) };

  const series = {
    p95: `histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{${F}}[1m])))`,
    rps: `sum by (uri) (rate(http_server_requests_seconds_count{${F}}[1m]))`,
    errores_pct: `100 * sum by (uri) (rate(http_server_requests_seconds_count{status=~"5..",${F}}[1m])) / sum by (uri) (rate(http_server_requests_seconds_count{${F}}[1m]))`,
    hilos_ocupados: 'tomcat_threads_busy_threads',
  };
  const rangos = {};
  for (const [k, q] of Object.entries(series)) rangos[k] = { promql: q, resultado: await promRango(q) };

  const logql = {
    lineas_por_nivel: `sum by (level) (count_over_time({app="api-demo"}[${ventana}]))`,
    requests_lentos_por_uri: `sum by (mdc_uri) (count_over_time({app="api-demo"} | json | mdc_duracion_ms > 500 [${ventana}]))`,
    duracion_promedio_por_uri: `avg by (mdc_uri) (avg_over_time({app="api-demo"} | json | unwrap mdc_duracion_ms [${ventana}]))`,
  };
  const logs = {};
  for (const [k, q] of Object.entries(logql)) {
    const d = await loki(q, { instant: true });
    logs[k] = { logql: q, resultado: d.result.map((x) => ({ labels: x.metric, valor: Number(x.value[1]) })) };
  }
  const errores = await loki('{app="api-demo", level="ERROR"}', { limit: 3 });
  const lentos = await loki('{app="api-demo"} | json | mdc_duracion_ms > 1000', { limit: 3 });
  logs.ejemplo_errores = { logql: '{app="api-demo", level="ERROR"}', lineas: errores.result.flatMap((s) => s.values.map((v) => v[1])).slice(0, 3) };
  logs.ejemplo_lentos = { logql: '{app="api-demo"} | json | mdc_duracion_ms > 1000', lineas: lentos.result.flatMap((s) => s.values.map((v) => v[1])).slice(0, 3) };

  const alertas = await (await fetch(`${PROM}/alerts`)).json();

  const salida = { inicio, fin, ventana, instantaneas, rangos, logs, alertas: alertas.data.alerts };
  fs.writeFileSync(path.join(__dirname, 'metricas.json'), JSON.stringify(salida, null, 2));

  const fmt = (r, f) => r.resultado.map((x) => `  ${x.labels.uri || x.labels.mdc_uri || x.labels.level}: ${f(x.valor)}`).join('\n');
  console.log('p95 (ms):\n' + fmt(instantaneas.p95, (v) => (v * 1000).toFixed(0)));
  console.log('p99 (ms):\n' + fmt(instantaneas.p99, (v) => (v * 1000).toFixed(0)));
  console.log('req/s:\n' + fmt(instantaneas.rps, (v) => v.toFixed(1)));
  console.log('errores %:\n' + fmt(instantaneas.errores_pct, (v) => v.toFixed(2)));
  console.log('SQL por request:\n' + fmt(instantaneas.sql_por_request, (v) => v.toFixed(1)));
  console.log('Logs por nivel:\n' + fmt(logs.lineas_por_nivel, (v) => v));
  console.log('Requests lentos (>500ms) en logs:\n' + fmt(logs.requests_lentos_por_uri, (v) => v));
  console.log('Alertas:', alertas.data.alerts.map((a) => `${a.labels.alertname}/${a.labels.uri}/${a.state}`).join(', '));
  console.log('Ejemplo error:', logs.ejemplo_errores.lineas[0]);
})();
