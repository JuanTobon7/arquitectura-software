# Observabilidad en Spring Boot: Prometheus + Loki + Grafana

Mini API Spring Boot instrumentada con Actuator, Micrometer y logs JSON enviados a Loki, más un stack
Prometheus / Loki / Grafana con datasources y dashboard ya provisionados.

## Requisitos

- Docker Desktop (la API se compila dentro de Docker con Maven y JDK 21, no hace falta Java local)
- Node.js (solo para `resultados/recolectar.js`)

## Levantar

```bash
docker compose up -d --build
```

| Servicio   | URL                                   |
|------------|---------------------------------------|
| API        | http://localhost:8080                 |
| Prometheus | http://localhost:9090 (ver *Alerts*)  |
| Loki       | http://localhost:3100                 |
| Tempo      | http://localhost:3200 (trazas)        |
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

Corre 4 minutos y deja el resumen en `resultados/k6-resumen.json`. Todo se ajusta desde la terminal
con `-e VARIABLE=valor` (detalle al inicio de `carga/carga.js`):

| Variable | Qué hace | Por defecto |
|----------|----------|-------------|
| `DURACION` | Duración de cada escenario | `4m` |
| `ENDPOINTS` | Escenarios a correr: `pagos,catalogo,n-mas-1,optimizado` | todos |
| `FACTOR` | Multiplica las tasas base (20, 10, 5 y 5 req/s) | `1` |
| `RPS_PAGOS`, `RPS_CATALOGO`, `RPS_N_MAS_1`, `RPS_OPTIMIZADO` | Tasa fija de un endpoint | base × `FACTOR` |
| `MAX_VUS` | Tope de usuarios virtuales para sostener la tasa | `100` |
| `USUARIOS` | Cambia a modo usuarios concurrentes (por escenario, sin pausa) | sin definir |
| `ITERACIONES` | Con `USUARIOS`: peticiones por usuario y termina | sin definir |
| `RAMPA` | Con `USUARIOS`: sube de 0 a `USUARIOS` en ese tiempo | sin definir |

```bash
# 5 veces el tráfico normal (unas 200 req/s)
docker compose --profile carga run --rm -e FACTOR=5 k6

# Colapsar el N+1: 200 usuarios concurrentes solo contra /api/pedidos/n-mas-1
docker compose --profile carga run --rm -e ENDPOINTS=n-mas-1 -e USUARIOS=200 -e DURACION=2m k6

# Lo mismo con el endpoint corregido, para comparar
docker compose --profile carga run --rm -e ENDPOINTS=optimizado -e USUARIOS=200 -e DURACION=2m k6

# Rampa: de 0 a 300 usuarios en 1 minuto y se sostiene 3 minutos
docker compose --profile carga run --rm -e ENDPOINTS=n-mas-1 -e USUARIOS=300 -e RAMPA=1m -e DURACION=3m k6
```

Para ver k6 en vivo en el navegador (http://localhost:5665), añade
`-p 5665:5665 -e K6_WEB_DASHBOARD=true -e K6_WEB_DASHBOARD_HOST=0.0.0.0`.

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

TraceQL (Grafana → Explore → Tempo):

```traceql
{ name = "llamada a pasarela de pagos" && duration > 1s }   # pagos lentos
{ status = error }                                          # pagos fallidos
{ name = "http get /api/pedidos/n-mas-1" }                  # 101 spans de consulta
```

Las trazas salen de Micrometer Tracing + OpenTelemetry (OTLP a Tempo). Cada log en Loki lleva su
`traceId`, y Grafana enlaza log → traza y span → logs.

## Resultados de la corrida (12-oct-2026, 4 min, 9.603 requests)

| Métrica                         | /api/pagos | /api/pedidos/n-mas-1 | /api/pedidos/optimizado |
|---------------------------------|-----------:|---------------------:|------------------------:|
| p95                             | 372 ms     | 224 ms               | 5 ms                    |
| p99                             | 1.574 ms   | 244 ms               | 7 ms                    |
| Errores 5xx                     | 4,4 %      | 0 %                  | 0 %                     |
| Consultas SQL por request       | 0          | 101                  | 1                       |

La alerta `TasaErroresAlta` se disparó para `/api/pagos`. Capturas en `resultados/capturas/`.

## Apagar

```bash
docker compose down
```
