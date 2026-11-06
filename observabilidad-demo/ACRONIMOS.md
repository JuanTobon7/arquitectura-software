# Acrónimos del taller de observabilidad

## Observabilidad y confiabilidad

| Acrónimo | Significado | En pocas palabras |
|---|---|---|
| **RED** | Rate, Errors, Duration | Método para medir servicios: tasa de peticiones, errores y duración. |
| **USE** | Utilization, Saturation, Errors | Método para medir recursos como CPU, memoria e hilos. |
| **SLI** | Service Level Indicator | Lo que se mide. Ejemplo: % de pagos que responden en menos de 500 ms. |
| **SLO** | Service Level Objective | La meta sobre ese indicador. Ejemplo: 99 % en 30 días. |
| **SLA** | Service Level Agreement | El contrato con el cliente, con consecuencias si no se cumple. |
| **SRE** | Site Reliability Engineering | Disciplina que aplica ingeniería de software a la operación. |
| **MTTD** | Mean Time To Detect | Tiempo medio en detectar un problema. |
| **MTTR** | Mean Time To Recover | Tiempo medio en recuperar el servicio. |
| **APM** | Application Performance Monitoring | Plataformas de monitoreo de aplicaciones, como Datadog o New Relic. |
| **TSDB** | Time Series Database | Base de datos de series de tiempo. Prometheus usa una. |
| **ELK** | Elasticsearch, Logstash, Kibana | Pila clásica de logs, alternativa a Loki. |
| **PromQL** | Prometheus Query Language | Lenguaje de consultas de métricas en Prometheus. |
| **LogQL** | Log Query Language | Lenguaje de consultas de logs en Loki. |

## Desarrollo y Java

| Acrónimo | Significado | En pocas palabras |
|---|---|---|
| **API** | Application Programming Interface | Interfaz para que un programa use a otro. |
| **JPA** | Jakarta Persistence API | Estándar de Java para trabajar con bases de datos. Hibernate lo implementa. |
| **JVM** | Java Virtual Machine | Máquina virtual que ejecuta Java. |
| **JDK** | Java Development Kit | Herramientas para compilar y ejecutar Java. |
| **SQL** | Structured Query Language | Lenguaje de consultas de bases de datos relacionales. |
| **DTO** | Data Transfer Object | Objeto que solo transporta datos entre capas. |
| **MDC** | Mapped Diagnostic Context | Contexto que se agrega a cada log. De ahí salen los campos `mdc_*`. |

## Web, datos e infraestructura

| Acrónimo | Significado | En pocas palabras |
|---|---|---|
| **HTTP** | HyperText Transfer Protocol | Protocolo de las peticiones web. |
| **URI** | Uniform Resource Identifier | Identificador de un recurso, como `/api/pagos`. |
| **URL** | Uniform Resource Locator | Dirección web completa. |
| **DNS** | Domain Name System | Traduce nombres de dominio a direcciones IP. |
| **JSON** | JavaScript Object Notation | Formato de texto para datos. Los logs de la demo lo usan. |
| **YAML** | YAML Ain't Markup Language | Formato de configuración, como `alertas.yml`. |
| **CPU** | Central Processing Unit | Procesador. |
| **CI/CD** | Continuous Integration / Continuous Delivery | Integrar y entregar cambios de forma automática. |
| **MiB / GiB** | Mebibyte / Gibibyte | Unidades de memoria en base 2. |
| **QR** | Quick Response | Código que se escanea para abrir el repositorio. |

## No son acrónimos, pero te los pueden preguntar

| Término | Qué es |
|---|---|
| **p50, p95, p99** | Percentiles. El p99 es el valor que el 99 % de las peticiones no supera. |
| **5xx** | Códigos HTTP de error del servidor, como el 500. |
| **N+1** | 1 consulta para traer N registros, más 1 consulta adicional por cada uno. |
| **JOIN FETCH** | Instrucción de JPA que trae los datos relacionados en una sola consulta. |
| **LAZY** | Carga perezosa: los datos relacionados se consultan solo cuando se usan. |
| **k6** | Herramienta que genera la carga de la demo. |
| **H2** | Base de datos en memoria que usa la demo. |
| **FIRING** | Estado de una alerta de Prometheus que está activa. |
| **Cardinalidad** | Cantidad de combinaciones distintas de etiquetas de una métrica. |
