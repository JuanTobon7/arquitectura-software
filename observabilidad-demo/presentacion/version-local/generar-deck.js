// Genera el deck (PPTX con notas del orador) a partir de los resultados reales de la demo.
// Uso: node generar-deck.js  (requiere pptxgenjs instalado: npm i pptxgenjs)
const path = require('path');
const PptxGenJS = require(process.env.PPTXGENJS || 'pptxgenjs');

const RAIZ = path.resolve(__dirname, '..', '..');
const m = require(path.join(RAIZ, 'resultados', 'metricas.json'));
const k6 = require(path.join(RAIZ, 'resultados', 'k6-resumen.json'));
const CAP = path.join(RAIZ, 'resultados', 'capturas');

// ---------- datos reales ----------
const porUri = (lista, uri) => lista.resultado.find((x) => (x.labels.uri || x.labels.mdc_uri) === uri)?.valor;
const ms = (s) => Math.round(s * 1000);
const pag = k6.metrics['http_req_duration{scenario:pagos}'].values;
const n1 = k6.metrics['http_req_duration{scenario:pedidos_n_mas_1}'].values;
const D = {
  totalReq: k6.metrics.http_reqs.values.count,
  rps: k6.metrics.http_reqs.values.rate,
  pagosAvg: Math.round(pag.avg), pagosMed: Math.round(pag.med),
  pagosP95: ms(porUri(m.instantaneas.p95, '/api/pagos')), pagosP99: ms(porUri(m.instantaneas.p99, '/api/pagos')),
  pagosMax: Math.round(pag.max),
  errPct: porUri(m.instantaneas.errores_pct, '/api/pagos'),
  errFallidos: k6.metrics['http_req_failed{scenario:pagos}'].values.passes,
  pagosTotal: k6.metrics['http_req_failed{scenario:pagos}'].values.passes + k6.metrics['http_req_failed{scenario:pagos}'].values.fails,
  n1Sql: porUri(m.instantaneas.sql_por_request, '/api/pedidos/n-mas-1'),
  optSql: porUri(m.instantaneas.sql_por_request, '/api/pedidos/optimizado'),
  n1P95: ms(porUri(m.instantaneas.p95, '/api/pedidos/n-mas-1')), optP95: ms(porUri(m.instantaneas.p95, '/api/pedidos/optimizado')),
  n1Avg: Math.round(n1.avg),
  optAvg: porUri(m.logs.duracion_promedio_por_uri, '/api/pedidos/optimizado'),
  logs: Object.fromEntries(m.logs.lineas_por_nivel.resultado.map((x) => [x.labels.level, x.valor])),
  lentos: porUri(m.logs.requests_lentos_por_uri, '/api/pagos'),
  alerta: m.alertas.find((a) => a.labels.alertname === 'TasaErroresAlta'),
};
const num = (n) => Math.round(n).toLocaleString('es-CO');
console.log(D);

// ---------- estilo ----------
const C = {
  oscuro: '0B1220', claro: 'F7F8FA', tinta: '111827', gris: '5B6475', linea: 'D9DEE7',
  naranja: 'F46800', azul: '2563EB', verde: '15803D', rojo: 'DC2626', amarillo: 'CA8A04', blanco: 'FFFFFF',
};
const FUENTE = 'Calibri';
const MONO = 'Consolas';

const pres = new PptxGenJS();
pres.layout = 'LAYOUT_WIDE'; // 13.33 x 7.5 in
pres.title = 'Observabilidad en Spring Boot con Prometheus, Loki y Grafana';
pres.author = 'Juan Tobón';

let n = 0;
function base(titulo, kicker) {
  const s = pres.addSlide();
  n++;
  s.background = { color: C.claro };
  s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 0.12, h: 7.5, fill: { color: C.naranja }, line: { color: C.naranja } });
  if (kicker) s.addText(kicker.toUpperCase(), { x: 0.6, y: 0.35, w: 10, h: 0.35, fontFace: FUENTE, fontSize: 12, bold: true, color: C.naranja, charSpacing: 2 });
  s.addText(titulo, { x: 0.6, y: 0.7, w: 12.1, h: 0.9, fontFace: FUENTE, fontSize: 32, bold: true, color: C.tinta, valign: 'top' });
  s.addText(String(n), { x: 12.5, y: 7.0, w: 0.6, h: 0.3, fontFace: FUENTE, fontSize: 10, color: C.gris, align: 'right' });
  return s;
}
function viñetas(s, items, o = {}) {
  s.addText(items.map((t) => (typeof t === 'string'
    ? { text: t, options: { bullet: { indent: 18 }, paraSpaceAfter: 10 } }
    : { text: t.t, options: { bullet: { indent: 18 }, paraSpaceAfter: 10, bold: t.b, color: t.c } })),
  { x: o.x ?? 0.6, y: o.y ?? 1.8, w: o.w ?? 12, h: o.h ?? 4.8, fontFace: FUENTE, fontSize: o.size ?? 20, color: C.tinta, valign: 'top' });
}
function tarjeta(s, x, y, w, h, titulo, cuerpo, color, o = {}) {
  s.addShape(pres.shapes.RECTANGLE, { x, y, w, h, fill: { color: C.blanco }, line: { color: C.linea, width: 1 } });
  s.addShape(pres.shapes.RECTANGLE, { x, y, w, h: 0.08, fill: { color }, line: { color } });
  s.addText(titulo, { x: x + 0.2, y: y + 0.2, w: w - 0.4, h: 0.5, fontFace: FUENTE, fontSize: o.tSize ?? 20, bold: true, color });
  s.addText(cuerpo, { x: x + 0.2, y: y + 0.75, w: w - 0.4, h: h - 0.9, fontFace: FUENTE, fontSize: o.size ?? 15, color: C.tinta, valign: 'top' });
}
function codigo(s, texto, x, y, w, h, size = 13) {
  s.addShape(pres.shapes.RECTANGLE, { x, y, w, h, fill: { color: '0F172A' }, line: { color: '0F172A' } });
  s.addText(texto, { x: x + 0.15, y: y + 0.1, w: w - 0.3, h: h - 0.2, fontFace: MONO, fontSize: size, color: 'E2E8F0', valign: 'top' });
}
function kpi(s, x, y, w, valor, etiqueta, color) {
  s.addText(valor, { x, y, w, h: 0.8, fontFace: FUENTE, fontSize: 40, bold: true, color });
  s.addText(etiqueta, { x, y: y + 0.8, w, h: 0.6, fontFace: FUENTE, fontSize: 14, color: C.gris, valign: 'top' });
}
function imagen(s, archivo, x, y, w, ancho, alto) {
  s.addImage({ path: path.join(CAP, archivo), x, y, w, h: w * alto / ancho });
  s.addShape(pres.shapes.RECTANGLE, { x, y, w, h: w * alto / ancho, fill: { type: 'none' }, line: { color: C.linea, width: 1 } });
}

// ---------- 1. Portada ----------
{
  const s = pres.addSlide(); n++;
  s.background = { color: C.oscuro };
  s.addShape(pres.shapes.RECTANGLE, { x: 0.8, y: 2.0, w: 1.2, h: 0.1, fill: { color: C.naranja }, line: { color: C.naranja } });
  s.addText('Observabilidad en Spring Boot', { x: 0.8, y: 2.3, w: 11.5, h: 1.0, fontFace: FUENTE, fontSize: 48, bold: true, color: C.blanco });
  s.addText('Prometheus · Loki · Grafana', { x: 0.8, y: 3.3, w: 11.5, h: 0.8, fontFace: FUENTE, fontSize: 30, color: C.naranja });
  s.addText('De "¿está caído?" a "¿por qué está lento, para quién y desde cuándo?"', { x: 0.8, y: 4.3, w: 11.5, h: 0.6, fontFace: FUENTE, fontSize: 20, color: 'CBD5E1', italic: true });
  s.addText('Arquitectura de Software · Con resultados reales de una demo instrumentada', { x: 0.8, y: 6.3, w: 11.5, h: 0.4, fontFace: FUENTE, fontSize: 14, color: '94A3B8' });
  s.addNotes(`Formato: 40 minutos. Unos 20 de explicación (slides 1 a 14, entre 1 y 2 minutos cada una) y 20 de taller guiado (slide 15 y la guía TALLER.md).
Abrir con una pregunta al público: "¿Cuántos han tenido una app que funciona en local pero en producción 'a veces' es lenta?". Casi todos levantan la mano.
Mensaje central de la charla: observabilidad no es tener dashboards bonitos, es poder responder preguntas nuevas sobre el sistema sin desplegar código nuevo.
Todo lo que van a ver en números (latencias, errores, consultas SQL) salió de una demo real: una API Spring Boot con Prometheus, Loki y Grafana corriendo en Docker, bajo ${num(D.totalReq)} requests de carga generada con k6.`);
}

// ---------- 2. Qué es ----------
{
  const s = base('¿Qué es observabilidad?', 'Definición');
  s.addText('La capacidad de entender el estado interno de un sistema a partir de las señales que emite, incluso ante preguntas que no anticipaste.',
    { x: 0.6, y: 1.75, w: 12, h: 1.1, fontFace: FUENTE, fontSize: 22, color: C.tinta, italic: true });
  tarjeta(s, 0.6, 3.2, 3.85, 3.4, 'Viene de control', 'Término de la teoría de control (Kalman, 1960): un sistema es observable si sus salidas permiten inferir su estado.', C.azul, { size: 18 });
  tarjeta(s, 4.75, 3.2, 3.85, 3.4, 'En software', 'Las salidas son métricas, logs y trazas. El "estado" es: qué request, qué usuario, qué dependencia, qué versión.', C.naranja, { size: 18 });
  tarjeta(s, 8.9, 3.2, 3.85, 3.4, 'La prueba', 'Si un problema nuevo te obliga a agregar un log y redesplegar para entenderlo, al sistema le falta observabilidad.', C.verde, { size: 18 });
  s.addNotes(`Definición: observabilidad es una propiedad del sistema, no una herramienta. Prometheus o Grafana no "son" observabilidad; ayudan a lograrla.
El origen es teoría de control: Rudolf Kalman definió que un sistema es observable si a partir de sus salidas puedes reconstruir su estado interno.
En software, las salidas son la telemetría. La pregunta clave es: ¿puedo explicar un comportamiento raro con la telemetría que ya tengo?
Ejemplo para el público: "el checkout está lento solo para usuarios de Bogotá con tarjeta Visa desde el deploy de ayer". Si para responder eso hay que agregar un log y redesplegar, el sistema no es observable.`);
}

// ---------- 3. Monitoreo vs observabilidad ----------
{
  const s = base('Monitoreo vs. observabilidad', 'No son lo mismo');
  const fila = (a, b, c, encabezado) => [a, b, c].map((t, i) => ({
    text: t, options: {
      bold: encabezado || i === 0, color: encabezado ? C.blanco : C.tinta,
      fill: { color: encabezado ? C.oscuro : (i === 0 ? 'EEF1F6' : C.blanco) }, fontSize: encabezado ? 17 : 16,
    },
  }));
  s.addTable([
    fila('', 'Monitoreo', 'Observabilidad', true),
    fila('Pregunta', '¿Está funcionando?', '¿Por qué se comporta así?'),
    fila('Tipo de problema', 'Conocidos ("known unknowns")', 'Nuevos ("unknown unknowns")'),
    fila('Artefacto', 'Checks, umbrales, alertas', 'Telemetría rica y consultable'),
    fila('Ejemplo', 'CPU > 80% → alerta', 'p99 de /pagos sube solo con la pasarela X'),
    fila('Relación', 'Es un subconjunto…', '…de una práctica más amplia'),
  ], { x: 0.6, y: 1.8, w: 12.1, colW: [2.6, 4.75, 4.75], rowH: 0.62, fontFace: FUENTE, border: { type: 'solid', color: C.linea, pt: 1 }, valign: 'middle' });
  s.addText('Monitoreo te dice QUE algo falla. Observabilidad te deja averiguar POR QUÉ.', { x: 0.6, y: 6.0, w: 12.1, h: 0.6, fontFace: FUENTE, fontSize: 20, bold: true, color: C.naranja });
  s.addNotes(`Monitoreo responde preguntas que ya sabías que debías hacer: ¿el servicio responde?, ¿la CPU pasó del 80%? Son "known unknowns".
Observabilidad sirve para lo que no anticipaste: "unknown unknowns". En sistemas distribuidos la mayoría de incidentes son de este tipo.
No compiten: el monitoreo (alertas) es la puerta de entrada y la observabilidad es lo que usas para investigar después de que suena la alerta.
En la demo lo van a ver: una alerta de Prometheus nos dice QUE hay errores en /api/pagos; los logs en Loki nos dicen POR QUÉ (timeout de la pasarela).`);
}

// ---------- 4. Cuándo ----------
{
  const s = base('¿Cuándo invertir en observabilidad?', 'Contexto');
  tarjeta(s, 0.6, 1.8, 5.9, 4.6, 'Sí, desde el día uno', [
    '• Microservicios o sistemas distribuidos',
    '• Dependencias externas (pagos, APIs de terceros)',
    '• Tienes SLOs o contratos de disponibilidad',
    '• Varios equipos operan el mismo sistema',
    '• Despliegues frecuentes (CI/CD)',
  ].join('\n'), C.verde, { size: 18 });
  tarjeta(s, 6.8, 1.8, 5.9, 4.6, 'Con mesura', [
    '• Monolito pequeño o prototipo: Actuator + logs estructurados alcanzan',
    '• Scripts batch: métricas de éxito/duración del job',
    '• Cuidado con el costo: la telemetría también se paga (almacenamiento, cardinalidad)',
    '• Empieza por RED en los endpoints críticos',
  ].join('\n'), C.amarillo, { size: 18 });
  s.addNotes(`No todo necesita la pila completa. La regla práctica: cuanto más distribuido es el sistema y más dependencias externas tiene, más falta hace.
Si tienes un SLO, por ejemplo "99% de los pagos en menos de 500 ms", necesitas medirlo: sin observabilidad un SLO es un deseo.
Con mesura: en un prototipo, Spring Boot Actuator más logs en JSON ya te dan el 80% del valor con 5 minutos de configuración.
Recordar que la telemetría tiene costo. Loki y Prometheus cobran (en dinero o en disco) por cada etiqueta y cada línea. Volveremos sobre esto en errores comunes.`);
}

// ---------- 5. Tres pilares ----------
{
  const s = base('Los tres pilares de la telemetría', 'Señales');
  tarjeta(s, 0.6, 1.8, 3.9, 4.0, 'Métricas', 'Números agregados en el tiempo.\n\n¿Cuánto? ¿Qué tan rápido? ¿Qué porcentaje?\n\nBaratas, ideales para alertar.\n\nEn la demo: Micrometer → Prometheus', C.naranja, { size: 16 });
  tarjeta(s, 4.72, 1.8, 3.9, 4.0, 'Logs', 'Eventos discretos con contexto.\n\n¿Qué pasó exactamente en este request?\n\nRicos pero caros de buscar si no son estructurados.\n\nEn la demo: Logback JSON → Loki', C.azul, { size: 16 });
  tarjeta(s, 8.84, 1.8, 3.9, 4.0, 'Trazas', 'El recorrido de un request entre servicios.\n\n¿Dónde se fue el tiempo?\n\nClave en microservicios.\n\nSiguiente paso: Micrometer Tracing + Tempo', C.verde, { size: 16 });
  s.addText('El valor está en correlacionarlos: una métrica te lleva al log, el log a la traza.', { x: 0.6, y: 6.1, w: 12.1, h: 0.6, fontFace: FUENTE, fontSize: 19, bold: true, color: C.tinta });
  s.addNotes(`Métricas: series de tiempo numéricas. Son baratas porque se agregan: un contador de requests no crece con el tráfico. Sirven para dashboards y alertas.
Logs: un evento por cosa que pasa. Dan el detalle (qué request_id, qué mensaje de error) pero cuestan más. La clave es que sean estructurados (JSON), para poder filtrarlos como datos y no como texto.
Trazas: siguen un request por varios servicios con spans. En esta demo hay un solo servicio, así que no las incluimos; el siguiente paso natural sería Micrometer Tracing con OpenTelemetry y Grafana Tempo.
Hoy se habla también de un cuarto pilar, el profiling continuo (Pyroscope), pero los tres clásicos cubren la mayoría de los casos.
Lo importante: no son silos. En la demo un panel de métricas (errores 5xx) nos lleva a los logs de ERROR en Loki desde el mismo dashboard.`);
}

// ---------- 6. Arquitectura ----------
{
  const s = base('Arquitectura: Spring → Prometheus / Loki → Grafana', 'Cómo fluye la telemetría');
  const caja = (x, y, w, h, titulo, sub, color) => {
    s.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w, h, rectRadius: 0.12, fill: { color: C.blanco }, line: { color, width: 2 } });
    s.addText(titulo, { x, y: y + 0.12, w, h: 0.45, fontFace: FUENTE, fontSize: 18, bold: true, color, align: 'center' });
    s.addText(sub, { x: x + 0.1, y: y + 0.55, w: w - 0.2, h: h - 0.65, fontFace: FUENTE, fontSize: 13, color: C.tinta, align: 'center', valign: 'top' });
  };
  const flecha = (x, y, w, h, color, etiqueta, ex, ey, o = {}) => {
    s.addShape(pres.shapes.LINE, { x, y, w, h, flipH: o.flipH, flipV: o.flipV, line: { color, width: 2.5, endArrowType: 'triangle' } });
    if (etiqueta) s.addText(etiqueta, { x: ex, y: ey, w: 2.8, h: 0.6, fontFace: FUENTE, fontSize: 12, color, italic: true, align: 'center' });
  };
  caja(0.5, 2.75, 2.2, 1.5, 'k6', 'Generador de carga\n~40 req/s', C.gris);
  caja(3.6, 2.0, 3.0, 3.0, 'API Spring Boot', 'Actuator + Micrometer\n/actuator/prometheus\n\nLogback + Loki4j\n(logs JSON)', C.verde);
  caja(7.8, 1.7, 2.3, 1.7, 'Prometheus', 'TSDB de métricas\nReglas de alerta', C.naranja);
  caja(7.8, 3.8, 2.3, 1.7, 'Loki', 'Logs indexados\nsolo por etiquetas', C.azul);
  caja(10.9, 2.85, 2.0, 1.5, 'Grafana', 'Dashboards\nExplore', 'B45309');
  flecha(2.7, 3.5, 0.9, 0, C.gris, 'HTTP', 2.6, 2.95);
  flecha(6.6, 2.55, 1.2, 0, C.naranja, '', 0, 0, { flipH: true });
  flecha(6.6, 4.65, 1.2, 0, C.azul, '', 0, 0);
  s.addText('pull: scrape\ncada 5 s', { x: 6.6, y: 2.62, w: 1.2, h: 0.6, fontFace: FUENTE, fontSize: 11, italic: true, color: C.naranja, align: 'center' });
  s.addText('push: HTTP\n/loki/api/v1/push', { x: 6.5, y: 3.98, w: 1.4, h: 0.6, fontFace: FUENTE, fontSize: 11, italic: true, color: C.azul, align: 'center' });
  flecha(10.1, 2.55, 0.8, 0.65, C.naranja, '', 0, 0);
  flecha(10.1, 4.0, 0.8, 0.65, C.azul, '', 0, 0, { flipV: true });
  s.addText('PromQL', { x: 10.2, y: 2.05, w: 1.2, h: 0.4, fontFace: FUENTE, fontSize: 12, italic: true, color: C.naranja });
  s.addText('LogQL', { x: 10.2, y: 4.7, w: 1.2, h: 0.4, fontFace: FUENTE, fontSize: 12, italic: true, color: C.azul });
  s.addText('Todo se levanta con un solo docker compose up: datasources y dashboard ya provisionados.', { x: 0.6, y: 6.3, w: 12.1, h: 0.5, fontFace: FUENTE, fontSize: 16, color: C.gris });
  s.addNotes(`Recorrer el diagrama de izquierda a derecha.
k6 genera tráfico constante contra la API (unos 40 requests por segundo).
La API expone métricas en /actuator/prometheus. Ojo: Prometheus funciona por PULL, es Prometheus quien va cada 5 segundos a "raspar" (scrape) el endpoint. Ventaja: si la app muere, Prometheus lo nota porque el scrape falla (métrica "up").
Los logs van por PUSH: el appender Loki4j de Logback agrupa las líneas y las envía por HTTP a Loki.
Loki es "Prometheus para logs": no indexa el contenido completo, solo las etiquetas (app, level). Por eso es barato, y por eso hay que tener pocas etiquetas.
Grafana no almacena nada: consulta a Prometheus con PromQL y a Loki con LogQL. Las fuentes de datos y el dashboard se provisionan como código desde archivos YAML/JSON, sin clics.
Alternativa en producción: OpenTelemetry Collector o Grafana Alloy como agente intermedio.`);
}

// ---------- 8. La demo ----------
{
  const s = base('La demo: una tienda con dos problemas escondidos', 'Escenario');
  const filas = [
    ['Endpoint', 'Qué simula', 'Carga k6'],
    ['POST /api/pagos', 'Pasarela externa: 90% rápida, 8% lenta, 2% muy lenta, ~4% timeouts', '20 req/s'],
    ['GET /api/pedidos/n-mas-1', 'JPA con relación LAZY: 1 consulta + 1 por cada pedido', '5 req/s'],
    ['GET /api/pedidos/optimizado', 'Mismo resultado con JOIN FETCH', '5 req/s'],
    ['GET /api/catalogo', 'Endpoint sano de referencia', '10 req/s'],
  ].map((f, i) => f.map((t, j) => ({ text: t, options: { bold: i === 0 || j === 0, color: i === 0 ? C.blanco : C.tinta, fill: { color: i === 0 ? C.oscuro : C.blanco }, fontFace: j === 0 && i > 0 ? MONO : FUENTE, fontSize: j === 0 && i > 0 ? 14 : 16 } })));
  s.addTable(filas, { x: 0.6, y: 1.8, w: 12.1, colW: [3.6, 6.8, 1.7], rowH: 0.6, border: { type: 'solid', color: C.linea, pt: 1 }, valign: 'middle' });
  kpi(s, 0.6, 5.1, 3.2, num(D.totalReq), 'requests en 4 minutos', C.naranja);
  kpi(s, 4.0, 5.1, 3.2, `${D.rps.toFixed(0)} req/s`, 'tráfico sostenido', C.azul);
  kpi(s, 7.4, 5.1, 5.3, '100 pedidos × 3 ítems', 'H2 en memoria + 2 ms simulados de red a la BD', C.verde);
  s.addNotes(`Contexto de la demo: una mini tienda. No le digo al público cuáles son los problemas todavía; la idea es que el dashboard los muestre.
Pagos simula una dependencia externa con latencia variable y algunos timeouts. Pedidos tiene el clásico problema N+1 de JPA y su versión corregida. Catálogo es el control: si catálogo también estuviera lento, el problema sería de infraestructura y no de un endpoint.
Detalle de honestidad técnica: la base es H2 en memoria, tan rápida que el N+1 no se notaría. Por eso cada sentencia SQL suma 2 ms, simulando la ida y vuelta de red a una base real. Es un valor conservador.
La carga: k6 con tasa de llegada constante durante 4 minutos, ${num(D.totalReq)} requests en total.`);
}

// ---------- 9. Dashboard ----------
{
  const s = base('El dashboard RED en Grafana (datos reales)', 'Resultado de la carga');
  imagen(s, 'recorte-red.png', 0.6, 1.65, 12.1, 1780, 650);
  s.addText('Tráfico · Errores 5xx · Latencia p95 · Consultas SQL por request — todo provisionado como código.', { x: 0.6, y: 6.3, w: 12.1, h: 0.5, fontFace: FUENTE, fontSize: 15, color: C.gris });
  s.addNotes(`Este es el dashboard real durante la prueba de carga. Leerlo como lo haría alguien de guardia:
Arriba a la izquierda, tráfico: estable, coincide con lo que manda k6. Si el tráfico cae a cero, el problema puede estar antes de la app (balanceador, DNS).
Arriba a la derecha, errores: SOLO /api/pagos tiene errores, entre 3,5% y 5,5%. Los demás endpoints están en cero, así que no es un problema general.
Abajo a la izquierda, latencia p95: pagos ronda 350-410 ms y n-mas-1 se queda en unos 224 ms, mientras optimizado y catálogo están casi en cero.
Abajo a la derecha, la pista definitiva: n-mas-1 hace 101 consultas SQL por request; optimizado hace 1.
En menos de un minuto ya sabemos qué endpoints mirar y qué tipo de problema tiene cada uno. Ahora vamos a cada cuello de botella.`);
}

// ---------- 10. Cuello 1: cola de latencia ----------
{
  const s = base('Cuello de botella 1: el promedio miente', '/api/pagos · latencia');
  s.addChart(pres.charts.BAR, [{
    name: 'Latencia /api/pagos (ms)',
    labels: ['Promedio', 'Mediana (p50)', 'p95', 'p99', 'Máximo'],
    values: [D.pagosAvg, D.pagosMed, D.pagosP95, D.pagosP99, D.pagosMax],
  }], {
    x: 0.6, y: 1.7, w: 6.6, h: 4.6, barDir: 'col', chartColors: ['94A3B8', '94A3B8', C.amarillo, C.rojo, '7F1D1D'],
    showValue: true, dataLabelFontSize: 14, dataLabelColor: C.tinta, dataLabelFormatCode: '#,##0" ms"',
    catAxisLabelFontSize: 13, valAxisLabelFontSize: 11, valAxisLabelFormatCode: '#,##0', valGridLine: { color: 'E5E7EB', size: 0.5 },
    showLegend: false, showTitle: false, catAxisLabelColor: C.tinta,
  });
  s.addText([
    { text: `Promedio ${D.pagosAvg} ms`, options: { bold: true } },
    { text: ` parece sano. Pero 1 de cada 100 pagos tarda ` },
    { text: `más de ${(Math.floor(D.pagosP99 / 100) / 10).toFixed(1).replace('.', ',')} s`, options: { bold: true, color: C.rojo } },
    { text: `. A 20 pagos por segundo, eso es un cliente frustrado cada 5 segundos.` },
  ], { x: 7.5, y: 1.7, w: 5.2, h: 1.7, fontFace: FUENTE, fontSize: 17, color: C.tinta, valign: 'top' });
  s.addText('PromQL', { x: 7.5, y: 3.5, w: 5, h: 0.35, fontFace: FUENTE, fontSize: 13, bold: true, color: C.gris });
  codigo(s, 'histogram_quantile(0.99,\n  sum by (le, uri) (\n    rate(http_server_requests_seconds_bucket\n         {uri="/api/pagos"}[1m])))', 7.5, 3.85, 5.2, 1.45, 12);
  s.addText(`k6 midió del lado del cliente p95 = ${Math.round(pag['p(95)'])} ms y p99 = ${num(pag['p(99)'])} ms: coincide con Prometheus.`, { x: 7.5, y: 5.45, w: 5.2, h: 0.9, fontFace: FUENTE, fontSize: 14, color: C.verde, italic: true });
  s.addNotes(`Este es el primer hallazgo y el más contraintuitivo para los estudiantes.
Datos reales de la prueba: promedio ${D.pagosAvg} ms, mediana ${D.pagosMed} ms, p95 ${D.pagosP95} ms, p99 ${num(D.pagosP99)} ms y máximo ${num(D.pagosMax)} ms.
Si solo miráramos el promedio diríamos "100 ms, todo bien". La distribución tiene una cola larga: un 2% de requests tarda más de un segundo.
Por eso se usan percentiles. El p95 dice: el 95% de los usuarios espera esto o menos. El p99 mira la cola, que suele ser donde están los clientes más valiosos (los que tienen más datos, más ítems en el carrito).
Ojo con un detalle: nuestra alerta de p95 > 500 ms NO se disparó (p95 = ${D.pagosP95} ms). Eligiendo solo p95 nos habríamos perdido la cola. Por eso conviene definir SLOs en varios percentiles.
Validación cruzada: k6 midió desde el cliente p95 ${Math.round(pag['p(95)'])} ms y p99 ${num(pag['p(99)'])} ms; Prometheus calculó desde el servidor ${D.pagosP95} y ${num(D.pagosP99)} ms. Coinciden, así que confiamos en la instrumentación.
Causa: la dependencia externa. La API no está saturada (máximo 5 de 50 hilos de Tomcat ocupados, CPU < 6%). Solución: timeouts agresivos, circuit breaker (Resilience4j) y medir la latencia de la dependencia por separado.`);
}

// ---------- 11. Cuello 2: N+1 ----------
{
  const s = base('Cuello de botella 2: el problema N+1', '/api/pedidos · base de datos');
  kpi(s, 0.6, 1.7, 2.9, `${Math.round(D.n1Sql)}`, 'consultas SQL por request (N+1)', C.rojo);
  kpi(s, 3.6, 1.7, 2.9, `${Math.round(D.optSql)}`, 'consulta con JOIN FETCH', C.verde);
  s.addChart(pres.charts.BAR, [{
    name: 'p95 (ms)', labels: ['N+1 (LAZY)', 'JOIN FETCH'], values: [D.n1P95, D.optP95],
  }], {
    x: 0.6, y: 3.3, w: 5.9, h: 3.1, barDir: 'bar', chartColors: [C.rojo, C.verde], showValue: true, dataLabelFormatCode: '#,##0" ms"',
    dataLabelFontSize: 14, catAxisLabelFontSize: 14, valAxisHidden: true, valGridLine: { style: 'none' }, showLegend: false,
    showTitle: true, title: 'Latencia p95', titleFontSize: 14, titleColor: C.gris,
  });
  s.addText('El log estructurado lo delata (LogQL)', { x: 6.9, y: 1.7, w: 5.8, h: 0.35, fontFace: FUENTE, fontSize: 13, bold: true, color: C.gris });
  codigo(s, '{app="api-demo"}\n  | json\n  | mdc_sql_consultas > 50', 6.9, 2.05, 5.8, 1.05, 13);
  codigo(s, `"message":"request GET /api/pedidos/n-mas-1\n  -> 200 en 217 ms (101 consultas SQL)",\n"mdc_request_id":"20efa79a",\n"mdc_sql_consultas":"101"`, 6.9, 3.25, 5.8, 1.4, 11);
  s.addText('Arreglo: una línea en el repositorio', { x: 6.9, y: 4.8, w: 5.8, h: 0.35, fontFace: FUENTE, fontSize: 13, bold: true, color: C.gris });
  codigo(s, '@Query("select distinct p from Pedido p\n        left join fetch p.items")', 6.9, 5.15, 5.8, 0.8, 12);
  s.addText(`≈ ${Math.round(D.n1P95 / D.optP95)}× más rápido con el mismo resultado`, { x: 6.9, y: 6.05, w: 5.8, h: 0.45, fontFace: FUENTE, fontSize: 17, bold: true, color: C.verde });
  s.addNotes(`El N+1 es el bug de rendimiento más común con JPA/Hibernate y es invisible en desarrollo, porque con 5 registros en local no se nota.
Qué pasa: findAll() trae los 100 pedidos en 1 consulta, y al recorrer pedido.getItems() Hibernate lanza 1 consulta por pedido porque la relación es LAZY. Total: 1 + 100 = 101.
Resultados reales: N+1 con p95 de ${D.n1P95} ms y ${Math.round(D.n1Sql)} consultas por request. La versión con JOIN FETCH: p95 de ${D.optP95} ms y 1 consulta. Unas ${Math.round(D.n1P95 / D.optP95)} veces más rápido.
¿Por qué lo detectamos? Porque instrumentamos una métrica propia (consultas SQL por request) con un StatementInspector de Hibernate. La latencia sola te dice que es lento; la métrica de consultas te dice POR QUÉ.
Y el log estructurado en Loki tiene el request_id y el número de consultas, así que con LogQL filtramos exactamente los requests sospechosos.
Lo peligroso: el N+1 escala con los datos. Con 100 pedidos son 220 ms; con 10.000 pedidos serían segundos. Observabilidad detecta la tendencia antes de que sea incidente.`);
}

// ---------- 12. Errores ----------
{
  const s = base('De la alerta al log: la tasa de errores', '/api/pagos · errores');
  imagen(s, 'recorte-errores.png', 0.6, 1.65, 6.3, 1345, 547);
  kpi(s, 0.6, 4.5, 2.9, `${D.errPct.toFixed(1).replace('.', ',')}%`, `de los pagos fallan (${D.errFallidos} de ${num(D.pagosTotal)})`, C.rojo);
  kpi(s, 3.7, 4.5, 3.2, 'FIRING', 'alerta TasaErroresAlta (> 2% durante 1 min)', C.rojo);
  s.addText('1. La métrica alerta (PromQL)', { x: 7.2, y: 1.65, w: 5.5, h: 0.35, fontFace: FUENTE, fontSize: 13, bold: true, color: C.gris });
  codigo(s, 'sum by (uri)(rate(http_server_requests_seconds_count\n  {status=~"5.."}[2m]))\n/ sum by (uri)(rate(http_server_requests_seconds_count[2m]))\n> 0.02', 7.2, 2.0, 5.5, 1.35, 11);
  s.addText('2. El log explica (LogQL)', { x: 7.2, y: 3.5, w: 5.5, h: 0.35, fontFace: FUENTE, fontSize: 13, bold: true, color: C.gris });
  codigo(s, '{app="api-demo", level="ERROR"}', 7.2, 3.85, 5.5, 0.5, 12);
  codigo(s, '"level":"ERROR",\n"message":"Error procesando pago:\n  La pasarela de pagos respondio timeout",\n"mdc_request_id":"ef1851b4"', 7.2, 4.45, 5.5, 1.35, 11);
  s.addText(`Loki: ${num(D.logs.INFO)} INFO · ${D.logs.WARN} WARN (todos requests lentos de /pagos) · ${D.logs.ERROR} ERROR`, { x: 7.2, y: 5.95, w: 5.5, h: 0.6, fontFace: FUENTE, fontSize: 14, color: C.tinta });
  s.addNotes(`Flujo típico de un incidente, en orden:
1. La alerta. Prometheus evalúa la regla TasaErroresAlta: más del 2% de respuestas 5xx durante 1 minuto. En la prueba se disparó (estado FIRING) con ${D.errPct.toFixed(1)}% de errores en /api/pagos.
2. El dashboard acota: solo /api/pagos tiene errores; catálogo y pedidos están en cero. No es la base de datos ni la red en general.
3. Los logs explican: con LogQL filtramos level="ERROR" y todas las líneas dicen lo mismo: timeout de la pasarela de pagos. Cada línea trae request_id, así que podemos buscar ese request puntual.
Números de Loki durante la prueba: ${num(D.logs.INFO)} líneas INFO, ${D.logs.WARN} WARN y ${D.logs.ERROR} ERROR. Los ${D.logs.WARN} WARN son requests de más de 500 ms y TODOS son de /api/pagos: otra confirmación del hallazgo anterior.
k6 contó ${D.errFallidos} pagos fallidos de ${num(D.pagosTotal)}, y su umbral de error < 2% falló. En un pipeline de CI eso bloquearía el despliegue.
Nota: la alerta es por síntoma (errores que ve el usuario), no por causa (CPU). Eso es buena práctica.`);
}

// ---------- 13. RED y USE ----------
{
  const s = base('Dos métodos para no perderse: RED y USE', 'Qué medir');
  tarjeta(s, 0.6, 1.8, 5.9, 3.9, 'RED · para servicios', 'Rate: requests por segundo\nErrors: requests fallidos\nDuration: distribución de latencia (p50/p95/p99)\n\nPerspectiva del usuario. Tom Wilkie (Grafana Labs).', C.naranja, { size: 18 });
  tarjeta(s, 6.8, 1.8, 5.9, 3.9, 'USE · para recursos', 'Utilization: % de tiempo ocupado\nSaturation: trabajo en cola\nErrors: errores del recurso\n\nCPU, memoria, hilos, pool de conexiones. Brendan Gregg.', C.azul, { size: 18 });
  s.addText([
    { text: 'En la demo: ', options: { bold: true } },
    { text: `RED marcó /pagos y /pedidos; USE mostró la JVM sana (heap ≤ 145 MiB de 5,6 GiB, máx. 5 de 50 hilos Tomcat, CPU < 6%). ` },
    { text: 'Conclusión: el cuello no es de capacidad, es de diseño y de dependencias. Escalar servidores no lo habría arreglado.', options: { bold: true, color: C.rojo } },
  ], { x: 0.6, y: 5.9, w: 12.1, h: 1.0, fontFace: FUENTE, fontSize: 16, color: C.tinta, valign: 'top' });
  s.addNotes(`Dos recetas complementarias para decidir qué medir sin ahogarse en métricas.
RED, propuesto por Tom Wilkie, es para servicios orientados a requests: tasa, errores y duración. Es la vista del usuario. Nuestro dashboard tiene exactamente esas filas.
USE, de Brendan Gregg, es para recursos: utilización, saturación y errores de CPU, memoria, hilos, conexiones.
Usarlos juntos evita un error clásico. En la demo, RED mostró dos endpoints con problemas, y USE mostró que la JVM estaba tranquila: heap bajo, máximo 5 de 50 hilos de Tomcat ocupados, CPU por debajo del 6%.
Conclusión: si la reacción hubiera sido "subamos más réplicas", habríamos gastado plata sin arreglar nada. El problema era un patrón de acceso a datos y una dependencia externa.
También mencionar las Golden Signals de Google SRE (latencia, tráfico, errores, saturación): son básicamente RED + la S de USE.`);
}

// ---------- 14. Buenas prácticas ----------
{
  const s = base('Buenas prácticas', 'Lo que haría un equipo senior');
  const items = [
    ['Logs estructurados (JSON) con contexto', 'request_id, uri, duración, usuario anonimizado. Se consultan como datos.'],
    ['Percentiles e histogramas, no promedios', 'Activa percentiles-histogram y define SLOs en p95 y p99.'],
    ['Alertar por síntomas, investigar por causas', 'Errores y latencia que ve el usuario; la CPU va al dashboard, no al pager.'],
    ['Etiquetas de baja cardinalidad', 'uri como plantilla (/pedidos/{id}), nunca el id. En Loki: pocas etiquetas.'],
    ['Métricas de negocio', 'pagos aprobados/fallidos, carritos abandonados: lo que le importa al negocio.'],
    ['Observabilidad como código', 'Dashboards, datasources y alertas versionados en Git (como esta demo).'],
  ];
  items.forEach(([t, d], i) => {
    const x = 0.6 + (i % 2) * 6.2, y = 1.75 + Math.floor(i / 2) * 1.6;
    s.addShape(pres.shapes.OVAL, { x, y: y + 0.08, w: 0.42, h: 0.42, fill: { color: C.verde }, line: { color: C.verde } });
    s.addText(String(i + 1), { x, y: y + 0.08, w: 0.42, h: 0.42, fontFace: FUENTE, fontSize: 14, bold: true, color: C.blanco, align: 'center', valign: 'middle' });
    s.addText(t, { x: x + 0.6, y, w: 5.3, h: 0.5, fontFace: FUENTE, fontSize: 18, bold: true, color: C.tinta });
    s.addText(d, { x: x + 0.6, y: y + 0.5, w: 5.3, h: 0.9, fontFace: FUENTE, fontSize: 14, color: C.gris, valign: 'top' });
  });
  s.addNotes(`Recorrer rápido, cada punto conectado con la demo.
1. Logs estructurados: gracias al JSON pudimos filtrar con | json | mdc_sql_consultas > 50. Con texto plano habría sido una expresión regular frágil.
2. Percentiles: lo vimos en el slide del promedio que miente.
3. Alertar por síntomas: nuestra alerta es de tasa de errores, no de CPU. Al que está de guardia lo despiertas solo si el usuario sufre.
4. Cardinalidad: Micrometer etiqueta con la plantilla de la URI, no con la URI real. Si pusieras el id de pedido como etiqueta, tendrías una serie por pedido y Prometheus se cae.
5. Métricas de negocio: el contador pagos.procesados le habla al product owner, no solo al ingeniero.
6. Como código: todo el stack de esta demo se levanta con docker compose y queda versionado. Un dashboard hecho a mano en producción se pierde.`);
}

// ---------- 15. Errores comunes ----------
{
  const s = base('Errores comunes (antipatrones)', 'Lo que no hay que hacer');
  const items = [
    ['Explosión de cardinalidad', 'userId, requestId o email como etiqueta en Prometheus o Loki.'],
    ['Dashboards que nadie mira', '40 paneles sin pregunta detrás. Cada panel debe responder algo.'],
    ['Fatiga de alertas', 'Alertas sin acción clara → el equipo las silencia → se pierde la importante.'],
    ['Loguear todo (o nada)', 'DEBUG en producción, datos personales o contraseñas en logs.'],
    ['Confiar en el promedio', `Oculta la cola: ${D.pagosAvg} ms de promedio con un p99 de ${num(D.pagosP99)} ms en la demo.`],
    ['Instrumentar después del incidente', 'La telemetría que no existía cuando pasó, no sirve para explicarlo.'],
  ];
  items.forEach(([t, d], i) => {
    const x = 0.6 + (i % 2) * 6.2, y = 1.75 + Math.floor(i / 2) * 1.6;
    s.addShape(pres.shapes.OVAL, { x, y: y + 0.08, w: 0.42, h: 0.42, fill: { color: C.rojo }, line: { color: C.rojo } });
    s.addText('✕', { x, y: y + 0.08, w: 0.42, h: 0.42, fontFace: FUENTE, fontSize: 14, bold: true, color: C.blanco, align: 'center', valign: 'middle' });
    s.addText(t, { x: x + 0.6, y, w: 5.3, h: 0.5, fontFace: FUENTE, fontSize: 18, bold: true, color: C.tinta });
    s.addText(d, { x: x + 0.6, y: y + 0.5, w: 5.3, h: 0.9, fontFace: FUENTE, fontSize: 14, color: C.gris, valign: 'top' });
  });
  s.addNotes(`El espejo de la slide anterior.
Cardinalidad: es el error número uno que tumba Prometheus. Cada combinación única de etiquetas es una serie de tiempo. Un userId como etiqueta con un millón de usuarios son un millón de series.
En Loki pasa igual: por eso en la demo solo usamos app y level como etiquetas, y el resto (uri, duración, request_id) va dentro del JSON y se filtra con | json.
Fatiga de alertas: si una alerta suena y la respuesta es "ah, esa siempre suena", hay que borrarla o arreglarla.
Datos personales en logs: además de riesgo de seguridad, en Colombia la Ley 1581 de protección de datos aplica también a los logs.
Instrumentar después: el mejor momento para instrumentar es antes del primer incidente, porque no puedes analizar retroactivamente datos que nunca recolectaste.`);
}

// ---------- 15. Taller ----------
{
  const s = base('Taller guiado: ahora lo encuentran ustedes', '20 minutos · manos a la obra');
  const pasos = [
    ['0-4 min', 'Levantar', 'Codespace (o Docker local) y un solo comando: docker compose up', C.gris],
    ['4-6 min', 'Carga', 'k6 genera tráfico 6 min mientras exploran', C.azul],
    ['6-16 min', '3 misiones', '1. La cola de latencia (PromQL)\n2. Cazar el N+1 (métrica + LogQL)\n3. Del error al request_id (alerta + logs)', C.naranja],
    ['16-20 min', 'Reto', 'Crear una alerta de p99 > 1 s y verla disparar', C.verde],
  ];
  pasos.forEach(([t, titulo, d, color], i) => {
    const x = 0.6 + i * 3.08;
    s.addShape(pres.shapes.RECTANGLE, { x, y: 1.85, w: 2.88, h: 3.6, fill: { color: C.blanco }, line: { color: C.linea, width: 1 } });
    s.addShape(pres.shapes.RECTANGLE, { x, y: 1.85, w: 2.88, h: 0.08, fill: { color }, line: { color } });
    s.addText(t, { x: x + 0.2, y: 2.05, w: 2.5, h: 0.4, fontFace: FUENTE, fontSize: 14, bold: true, color });
    s.addText(titulo, { x: x + 0.2, y: 2.45, w: 2.5, h: 0.6, fontFace: FUENTE, fontSize: 24, bold: true, color: C.tinta });
    s.addText(d, { x: x + 0.2, y: 3.15, w: 2.5, h: 2.2, fontFace: FUENTE, fontSize: 15, color: C.tinta, valign: 'top' });
  });
  s.addText([
    { text: 'Guía paso a paso: ', options: { bold: true } },
    { text: 'observabilidad-demo/TALLER.md (también en PDF). Nada que instalar: abren un GitHub Codespace desde el navegador.' },
  ], { x: 0.6, y: 5.75, w: 12.1, h: 0.6, fontFace: FUENTE, fontSize: 17, color: C.tinta });
  s.addText('Al final: preguntas y comparamos sus números con los de esta presentación.', { x: 0.6, y: 6.35, w: 12.1, h: 0.45, fontFace: FUENTE, fontSize: 15, color: C.gris, italic: true });
  s.addNotes(`Transición al taller (20 minutos). Compartir el enlace de la guía TALLER.md y del repositorio antes de esta slide, por ejemplo en el chat del curso.
Ritmo: 4 minutos para que todos tengan Grafana abierto. Quien no lo logre se empareja con un compañero; no detener al grupo.
Las tres misiones repiten lo que acabamos de ver, pero ahora ellos escriben las consultas. Los números no van a ser idénticos a los de las slides (la latencia es aleatoria), pero deben quedar en el mismo orden de magnitud: p99 de pagos > 1 s, 101 consultas en n-mas-1, ~4% de errores.
Reto final: agregar una regla de alerta de p99 > 1 s en infra/prometheus/alertas.yml, recargar Prometheus y verla pasar a FIRING.

Preguntas probables para el cierre:
Prometheus vs APM comercial (Datadog, New Relic): los conceptos (RED, percentiles, logs estructurados) son los mismos. La diferencia es quién opera la plataforma y cuánto cuesta. OpenTelemetry/Micrometer permite cambiar de backend sin tocar el código.
¿Por qué pull? Si el servicio muere, Prometheus lo sabe porque la métrica up pasa a 0. Para trabajos batch cortos existe el Pushgateway.
Loki vs Elasticsearch: Elasticsearch indexa todo el texto; Loki solo las etiquetas y comprime los chunks. Más barato; a cambio, la búsqueda de texto libre en mucho volumen es más lenta.
Overhead: el de Micrometer es despreciable; lo que cuesta es el volumen de logs y la cardinalidad.
Trazas: siguiente paso natural con Micrometer Tracing + OpenTelemetry + Grafana Tempo; el traceId va en el log para saltar de log a traza.`);
}

const salida = path.join(__dirname, 'observabilidad-spring-grafana.pptx');
pres.writeFile({ fileName: salida }).then(() => console.log('Deck generado:', salida, n, 'slides'));
