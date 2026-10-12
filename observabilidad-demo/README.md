# Observabilidad en Spring Boot: Prometheus + Loki + Grafana

Mini API Spring Boot instrumentada con Actuator, Micrometer y logs JSON enviados a Loki, más un stack
Prometheus / Loki / Grafana con datasources y dashboard ya provisionados. Sirve como demo para la
presentación de la carpeta `presentacion/`.

## Requisitos

- Docker Desktop (la API se compila dentro de Docker con Maven y JDK 21, no hace falta Java local)
- Node.js (solo para `resultados/recolectar.js` y `presentacion/generar-deck.js`)

## Levantar

```bash
docker compose up -d --build
```

| Servicio   | URL                                   |
|------------|---------------------------------------|
| API        | http://localhost:8080                 |
| Prometheus | http://localhost:9090 (ver *Alerts*)  |
| Loki       | http://localhost:3100                 |
| Grafana    | http://localhost:3000 (sin login)     |

El dashboard **API Demo - Observabilidad** es la página de inicio de Grafana.

## Endpoints

| Endpoint                      | Qué simula                                                            |
|-------------------------------|-----------------------------------------------------------------------|
| `POST /api/pagos`             | Pasarela externa: 90% 20-80 ms, 8% 200-500 ms, 2% 1,2-2 s, 4% errores |
| `GET /api/pedidos/n-mas-1`    | Problema N+1 con JPA (relación LAZY): 101 consultas por request       |
| `GET /api/pedidos/optimizado` | Misma respuesta con `JOIN FETCH`: 1 consulta                          |
| `GET /api/catalogo`           | Endpoint sano de referencia                                           |

Cada sentencia SQL suma 2 ms simulados de red (`ContadorSql`), porque H2 en memoria es tan rápida que
el N+1 no se notaría.

## Generar carga (k6)

```bash
docker compose --profile carga run --rm k6
```

Corre 4 minutos (cambiar con `DURACION=2m`) y deja el resumen en `resultados/k6-resumen.json`.

## Recolectar resultados

```bash
node resultados/recolectar.js <inicio_epoch> <fin_epoch>
```

Consulta Prometheus (PromQL) y Loki (LogQL) para la ventana de la prueba y guarda `resultados/metricas.json`.

## Consultas útiles

PromQL:

```promql
# Latencia p95 por endpoint
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{uri!~"/actuator.*"}[1m])))

# Tasa de errores 5xx (%)
100 * sum by (uri) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
    / sum by (uri) (rate(http_server_requests_seconds_count[1m]))

# Consultas SQL promedio por request
sum by (uri) (rate(sql_consultas_por_request_sum[1m])) / sum by (uri) (rate(sql_consultas_por_request_count[1m]))
```

LogQL:

```logql
{app="api-demo", level="ERROR"}
{app="api-demo"} | json | mdc_sql_consultas > 50
{app="api-demo"} | json | mdc_duracion_ms > 1000
sum by (level) (count_over_time({app="api-demo"}[1m]))
```

## Resultados de la corrida (12-oct-2026, 4 min, 9.603 requests)

| Métrica                         | /api/pagos | /api/pedidos/n-mas-1 | /api/pedidos/optimizado |
|---------------------------------|-----------:|---------------------:|------------------------:|
| p95                             | 372 ms     | 224 ms               | 5 ms                    |
| p99                             | 1.574 ms   | 244 ms               | 7 ms                    |
| Errores 5xx                     | 4,4 %      | 0 %                  | 0 %                     |
| Consultas SQL por request       | 0          | 101                  | 1                       |

La alerta `TasaErroresAlta` se disparó para `/api/pagos`. Capturas en `resultados/capturas/`.

## Taller y video guía

- Guía paso a paso del taller: [TALLER.md](TALLER.md) (también en `presentacion/guia-taller-observabilidad.pdf`).
- Video guía narrado: `video/taller-observabilidad.mp4` (subtítulos en `video/taller-observabilidad.srt`).
  Se regenera con `node video/fuente/capturar.js` (con el stack y la carga corriendo) y
  `node video/fuente/construir.js salida.mp4` (requiere Windows, Edge, ffmpeg y el paquete npm `jimp`; narra con la voz en español de Windows y `WIN_RATE=2`, o con Piper usando `TTS=piper`).

## Apagar

```bash
docker compose down
```
