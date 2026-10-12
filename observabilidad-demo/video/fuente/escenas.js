// Guion del video guía (versión larga, ~10 min): cada escena es una imagen + narración.
module.exports = [
  // ---------- Introducción ----------
  {
    id: '01', tipo: 'portada', titulo: 'Taller de observabilidad', sub: 'Spring Boot · Prometheus · Loki · Grafana', pie: 'Guía paso a paso',
    voz: 'Hola, bienvenidos al taller de observabilidad. En este video vamos a recorrer, paso a paso, todo lo que van a hacer en los próximos veinte minutos. No necesitan instalar nada en su computador: con un navegador y una cuenta gratuita de GitHub es suficiente. Pausen el video cada vez que lo necesiten.',
  },
  {
    id: '02', tipo: 'lista', etiqueta: 'Objetivo', titulo: 'Qué vas a lograr',
    items: ['Levantar una API Spring Boot instrumentada', 'Leer un dashboard con el método RED', 'Escribir consultas PromQL y LogQL', 'Encontrar 3 problemas solo con telemetría', 'Crear tu propia alerta'],
    voz: 'Al terminar van a saber levantar una API de Spring Boot ya instrumentada, leer un dashboard con el método RED, escribir consultas en PromQL para métricas y en LogQL para logs, y, lo más importante, encontrar tres problemas reales sin leer el código primero, solo mirando la telemetría. Al final crearán su propia alerta.',
  },
  {
    id: '03', tipo: 'lista', etiqueta: 'La demo', titulo: 'Una tienda con trampas',
    items: ['POST /api/pagos · llama a una pasarela externa', 'GET /api/pedidos/n-mas-1 · lista 100 pedidos', 'GET /api/pedidos/optimizado · los mismos 100 pedidos', 'GET /api/catalogo · endpoint sano de referencia'],
    voz: 'La aplicación es una mini tienda con cuatro endpoints. Pagos llama a una pasarela de pagos externa. Hay dos endpoints de pedidos que devuelven exactamente la misma información. Y catálogo es un endpoint sano, que nos sirve como punto de comparación. Escondimos tres problemas a propósito. Su trabajo es descubrirlos.',
  },
  {
    id: '04', tipo: 'captura', etiqueta: 'Arquitectura', img: 'arquitectura.png',
    nota: 'Prometheus hace pull de las métricas · los logs van por push a Loki · Grafana consulta ambos',
    voz: 'Así fluye la telemetría. La API de Spring Boot expone sus métricas gracias a Actuator y Micrometer. Prometheus pasa cada cinco segundos a recogerlas. Los logs salen en formato JSON y se envían a Loki. Y Grafana no guarda nada: solo consulta a Prometheus y a Loki para dibujar los paneles. Un generador de carga llamado k6 simula a los usuarios.',
  },
  {
    id: '05', tipo: 'lista', etiqueta: 'Plan', titulo: 'Cuatro pasos, veinte minutos',
    items: ['0 a 4 min · Levantar el entorno', '4 a 6 min · Generar tráfico con k6', '6 a 16 min · Tres misiones', '16 a 20 min · Reto: tu propia alerta'],
    voz: 'El taller tiene cuatro pasos. Primero levantamos el entorno. Después generamos tráfico. Luego vienen tres misiones de investigación, y cerramos con un reto. Vamos con el primero.',
  },
  // ---------- Paso 1 ----------
  {
    id: '06', tipo: 'lista', etiqueta: 'Paso 1 · Opción A', titulo: 'Abrir GitHub Codespaces',
    items: ['Abre el enlace de Codespaces de la guía', 'Inicia sesión con tu cuenta de GitHub', 'Pulsa «Create codespace»', 'Espera 1 a 2 minutos'],
    voz: 'Abran el enlace de Codespaces que está al inicio de la guía. Inicien sesión con su cuenta de GitHub y pulsen el botón Create codespace. GitHub les presta una máquina en la nube con Docker ya instalado. La primera vez tarda uno o dos minutos en prepararse, así que tengan paciencia.',
  },
  {
    id: '07', tipo: 'lista', etiqueta: 'Paso 1 · Opción A', titulo: 'Qué vas a ver',
    items: ['VS Code dentro del navegador', 'La carpeta observabilidad-demo ya abierta', 'Una terminal en la parte de abajo', 'La pestaña PORTS junto a la terminal'],
    voz: 'Cuando termine, verán Visual Studio Code dentro del navegador. La carpeta del taller ya está abierta y abajo tienen una terminal lista. Fíjense también en la pestaña PORTS, al lado de la terminal: desde ahí vamos a abrir Grafana en un momento.',
  },
  {
    id: '08', tipo: 'terminal', etiqueta: 'Paso 1 · Opción B (tu computador)',
    lineas: [
      ['#', 'Requiere Docker Desktop instalado'],
      ['$', 'git clone https://github.com/JuanTobon7/arquitectura-software.git'],
      ['$', 'cd arquitectura-software/observabilidad-demo'],
      ['#', 'Grafana: http://localhost:3000 · Prometheus: http://localhost:9090'],
    ],
    voz: 'Si prefieren trabajar en su propio computador y ya tienen Docker Desktop, clonen el repositorio y entren a la carpeta observabilidad-demo. Todo lo demás es idéntico; la única diferencia es que Grafana queda en localhost, puerto tres mil, y Prometheus en el nueve mil noventa.',
  },
  {
    id: '09', tipo: 'terminal', etiqueta: 'Paso 1 · Levantar el stack',
    lineas: [
      ['$', 'docker compose up -d --build'],
      ['', ' observabilidad-demo-api  Built'],
      ['', ' Container loki        Started'],
      ['', ' Container prometheus  Started'],
      ['', ' Container api-demo    Started'],
      ['', ' Container grafana     Started'],
    ],
    voz: 'Ahora, en la terminal, escriban: docker compose up, guion d, guion guion build. La opción guion d deja todo corriendo en segundo plano, y build compila la API. La primera vez Maven descarga dependencias dentro de Docker, así que puede tardar un par de minutos. Al final verán que se iniciaron cuatro contenedores.',
  },
  {
    id: '10', tipo: 'lista', etiqueta: 'Paso 1 · Qué se levantó', titulo: 'Cuatro contenedores',
    items: ['api-demo · Spring Boot en el puerto 8080', 'prometheus · métricas y reglas de alerta', 'loki · logs indexados por etiquetas', 'grafana · dashboard ya provisionado'],
    voz: 'Esos cuatro contenedores son: la API en el puerto ocho mil ochenta, Prometheus con sus reglas de alerta, Loki para los logs, y Grafana. Las fuentes de datos y el dashboard de Grafana se crean solos desde archivos de configuración. Eso se llama observabilidad como código, y es una buena práctica.',
  },
  {
    id: '11', tipo: 'terminal', etiqueta: 'Paso 1 · Verificar la API',
    lineas: [
      ['$', 'curl -s localhost:8080/actuator/health'],
      ['', '{"status":"UP"}'],
      ['#', 'Si dice "connection refused", espera 20 segundos y repite'],
    ],
    voz: 'Antes de seguir, verifiquemos que la API arrancó. Ejecuten curl a localhost, puerto ocho mil ochenta, barra actuator, barra health. La respuesta debe decir status UP. Si les sale connection refused, no se preocupen: Spring Boot aún está arrancando. Esperen unos veinte segundos y repitan.',
  },
  {
    id: '12', tipo: 'captura', etiqueta: 'Paso 1 · Abrir Grafana', img: 'dashboard.png',
    nota: 'Pestaña PORTS → globo del puerto 3000. Sin usuario ni contraseña.',
    voz: 'Ahora abran la pestaña PORTS y hagan clic en el ícono del globo del puerto tres mil. Se abre Grafana en otra pestaña, sin pedir usuario ni contraseña, directamente en el dashboard API Demo. Al principio estará casi vacío; cuando generemos tráfico se verá como en esta imagen.',
  },
  // ---------- Paso 2 ----------
  {
    id: '13', tipo: 'terminal', etiqueta: 'Paso 2 · Generar tráfico',
    lineas: [
      ['$', 'DURACION=6m docker compose --profile carga run -d --rm k6'],
      ['', '13934bf15261...'],
      ['#', '~40 peticiones por segundo durante 6 minutos, en segundo plano'],
      ['#', 'pagos 20/s · catálogo 10/s · n-mas-1 5/s · optimizado 5/s'],
    ],
    voz: 'Paso dos: vamos a simular usuarios. Copien de la guía el comando de k6 y ejecútenlo. Lanza unas cuarenta peticiones por segundo durante seis minutos: veinte a pagos, diez a catálogo y cinco a cada endpoint de pedidos. Como corre en segundo plano, la terminal queda libre.',
  },
  {
    id: '14', tipo: 'captura', etiqueta: 'Paso 2 · Leer el dashboard: R de Rate', img: 'panel-trafico.png',
    nota: 'Tráfico (req/s) por endpoint: coincide con lo que envía k6',
    voz: 'Esperen un minuto y volvamos a Grafana. El dashboard está organizado con el método RED: Rate, Errors y Duration. El primer panel es la tasa, las peticiones por segundo de cada endpoint. Coincide con lo que envía k6, así que el tráfico está llegando bien.',
  },
  {
    id: '15', tipo: 'captura', etiqueta: 'Paso 2 · Leer el dashboard: E de Errors', img: 'panel-errores.png',
    nota: 'Tasa de errores 5xx: solo aparece /api/pagos',
    voz: 'El segundo panel es la tasa de errores del servidor, los códigos quinientos. Fíjense que solo aparece pagos, con algo entre tres y seis por ciento. Los demás endpoints están en cero. Primera pista: no es un problema general, es algo de pagos.',
  },
  {
    id: '16', tipo: 'captura', etiqueta: 'Paso 2 · Leer el dashboard: D de Duration', img: 'panel-p95.png',
    nota: 'Latencia p95 por endpoint',
    voz: 'El tercer panel es la duración, medida con el percentil noventa y cinco. Pagos ronda los cuatrocientos milisegundos, y pedidos n-mas-1 se queda en unos doscientos veinte, mientras que la versión optimizada y catálogo están casi en cero. Segunda pista: algo pasa con ese endpoint de pedidos.',
  },
  // ---------- Misión 1 ----------
  {
    id: '17', tipo: 'captura', etiqueta: 'Misión 1 · ¿El promedio miente?', img: 'consultas-promql.png',
    nota: 'Explore → Prometheus → modo Code → pega las 3 consultas de la guía',
    voz: 'Empecemos la misión uno: ¿el promedio miente? En el menú de la izquierda de Grafana entren a Explore, que es el ícono de la brújula. Arriba elijan la fuente de datos Prometheus y cambien el editor a modo Code. Ahí van a pegar tres consultas de la guía, una debajo de otra, con el botón Add query.',
  },
  {
    id: '18', tipo: 'terminal', etiqueta: 'Misión 1 · Las tres consultas',
    lineas: [
      ['#', 'Promedio = suma de duraciones / número de requests'],
      ['', 'sum(rate(http_server_requests_seconds_sum{uri="/api/pagos"}[1m]))'],
      ['', '  / sum(rate(http_server_requests_seconds_count{uri="/api/pagos"}[1m]))'],
      ['#', 'Percentil 95 y 99 a partir del histograma'],
      ['', 'histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{uri="/api/pagos"}[1m])))'],
      ['', 'histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket{uri="/api/pagos"}[1m])))'],
    ],
    voz: 'La primera calcula el promedio: la suma de todas las duraciones dividida entre el número de peticiones. Las otras dos usan histogram quantile para calcular los percentiles noventa y cinco y noventa y nueve a partir de los buckets del histograma. El percentil noventa y nueve responde: ¿cuánto espera el uno por ciento de usuarios más lentos?',
  },
  {
    id: '19', tipo: 'captura', etiqueta: 'Misión 1 · El resultado', img: 'explore-latencia.png',
    nota: 'promedio ≈ 0,1 s · p95 ≈ 0,4 s · p99 ≈ 1,5 s',
    voz: 'Miren la gráfica. La línea verde, el promedio, está alrededor de cero coma uno segundos: parece sano. La amarilla, el p95, ronda cero coma cuatro. Pero la azul, el p99, pasa de uno coma cinco segundos. Es decir, uno de cada cien pagos tarda quince veces más que el promedio. Si solo miráramos el promedio, nunca lo habríamos visto.',
  },
  {
    id: '20', tipo: 'captura', etiqueta: 'Misión 1 · ¿El servidor está saturado?', img: 'panel-tomcat.png',
    nota: 'Hilos de Tomcat ocupados: casi en reposo (máximo 50)',
    voz: 'Antes de culpar al servidor, revisen en el dashboard los paneles de recursos: hilos de Tomcat ocupados y CPU. Esto es el método USE: utilización, saturación y errores. De cincuenta hilos disponibles casi no se usan, y la CPU está muy baja. El servidor está tranquilo.',
  },
  {
    id: '21', tipo: 'lista', etiqueta: 'Misión 1 · Conclusión', titulo: 'La cola es de la pasarela',
    items: ['La API no está saturada', 'La lentitud viene de la dependencia externa', 'Escalar servidores no lo arregla', 'Sí ayudan: timeouts, circuit breaker y SLOs en p99'],
    voz: 'Conclusión de la misión uno: la lentitud no viene de nuestra API, viene de la pasarela de pagos, que es una dependencia externa. Agregar más servidores no arreglaría nada. Lo que sí ayuda son timeouts, un circuit breaker, y definir los objetivos de servicio en el p99 y no en el promedio.',
  },
  // ---------- Misión 2 ----------
  {
    id: '22', tipo: 'terminal', etiqueta: 'Misión 2 · Cazar el N+1',
    lineas: [
      ['$', 'curl -s localhost:8080/api/pedidos/n-mas-1 | head -c 90'],
      ['', '[{"id":1,"total":580.0,"cliente":"cliente-1"},{"id":2,"total":602.0,...'],
      ['$', 'curl -s localhost:8080/api/pedidos/optimizado | head -c 90'],
      ['', '[{"id":1,"total":580.0,"cliente":"cliente-1"},{"id":2,"total":602.0,...'],
      ['#', 'Mismo JSON... ¿mismo costo?'],
    ],
    voz: 'Misión dos: cazar el N más uno. Vuelvan a la terminal y llamen con curl a los dos endpoints de pedidos. Van a ver que devuelven exactamente el mismo JSON: los mismos cien pedidos con su total. Entonces, ¿por qué uno es mucho más lento que el otro?',
  },
  {
    id: '23', tipo: 'captura', etiqueta: 'Misión 2 · Consultas SQL por request', img: 'panel-sql.png',
    nota: 'n-mas-1: 101 consultas por request · optimizado: 1 consulta',
    voz: 'La respuesta está en el panel de consultas SQL por request, una métrica que agregamos nosotros. El endpoint n-mas-1 hace ciento una consultas a la base de datos en cada petición. El optimizado hace solo una. Ciento una es una consulta para traer los pedidos, más una por cada uno de los cien pedidos. Ese es el famoso problema N más uno.',
  },
  {
    id: '24', tipo: 'captura', etiqueta: 'Misión 2 · La evidencia en los logs', img: 'consulta-logql.png',
    nota: '{app="api-demo"} | json | mdc_sql_consultas > 50',
    voz: 'Ahora busquemos la evidencia en los logs. En Explore cambien la fuente de datos a Loki y peguen esta consulta. Se lee así: entre los logs de la app api-demo, interpreta cada línea como JSON, y quédate solo con las que hicieron más de cincuenta consultas SQL.',
  },
  {
    id: '25', tipo: 'captura', etiqueta: 'Misión 2 · Cada request, con contexto', img: 'explore-nmas1.png',
    nota: 'Cada línea trae request_id, uri, duración y número de consultas',
    voz: 'Cada línea es un request real. Abran una y miren sus campos: el request id para rastrearlo, la uri, la duración en milisegundos y las ciento una consultas. Esto funciona porque los logs están estructurados en JSON. Con logs de texto plano tendríamos que pelear con expresiones regulares.',
  },
  {
    id: '26', tipo: 'terminal', etiqueta: 'Misión 2 · El código (PedidoRepository.java)',
    lineas: [
      ['#', 'n-mas-1: findAll() trae los pedidos; cada getItems() es otra consulta (LAZY)'],
      ['', 'repositorio.findAll()'],
      ['#', 'optimizado: una sola consulta con JOIN FETCH'],
      ['', '@Query("select distinct p from Pedido p left join fetch p.items")'],
      ['', 'List<Pedido> findAllConItems();'],
    ],
    voz: 'Ahora sí, abran el código: PedidoRepository y PedidoController. El endpoint lento usa findAll, que trae los pedidos, y luego, al pedir los ítems de cada uno, Hibernate lanza una consulta extra porque la relación es perezosa. El optimizado usa join fetch y trae todo en una sola consulta. Es unas cuarenta y cinco veces más rápido.',
  },
  // ---------- Misión 3 ----------
  {
    id: '27', tipo: 'captura', etiqueta: 'Misión 3 · La alerta', img: 'alertas.png',
    nota: 'Prometheus (puerto 9090) → Alerts → TasaErroresAlta en FIRING',
    voz: 'Misión tres: de la alerta al log. Abran Prometheus desde la pestaña PORTS, puerto nueve mil noventa, y entren al menú Alerts. Verán la regla TasaErroresAlta en rojo, en estado FIRING. Ábranla: está disparada para el endpoint de pagos.',
  },
  {
    id: '28', tipo: 'terminal', etiqueta: 'Misión 3 · La regla (infra/prometheus/alertas.yml)',
    lineas: [
      ['', '- alert: TasaErroresAlta'],
      ['', '  expr: |'],
      ['', '    sum by (uri) (rate(http_server_requests_seconds_count{status=~"5.."}[2m]))'],
      ['', '      / sum by (uri) (rate(http_server_requests_seconds_count[2m])) > 0.02'],
      ['', '  for: 1m'],
    ],
    voz: 'Así está escrita la regla. Divide las respuestas con error quinientos entre el total de respuestas de cada endpoint. Si pasa del dos por ciento durante un minuto, se dispara. Fíjense que alerta por un síntoma que siente el usuario, los errores, y no por una causa como la CPU.',
  },
  {
    id: '29', tipo: 'captura', etiqueta: 'Misión 3 · El log explica el porqué', img: 'explore-errores.png',
    nota: '{app="api-demo", level="ERROR"}',
    voz: 'La alerta nos dice que algo falla, pero no por qué. Para eso vamos a Loki y filtramos por nivel ERROR. Todas las líneas dicen lo mismo: error procesando pago, la pasarela de pagos respondió timeout. El problema es de la dependencia externa, no de nuestra base de datos ni de la red.',
  },
  {
    id: '30', tipo: 'terminal', etiqueta: 'Misión 3 · Contar errores solo con logs',
    lineas: [
      ['', 'sum(count_over_time({app="api-demo", level="ERROR"}[1m]))'],
      ['#', 'Convierte logs en una serie de tiempo: errores por minuto'],
    ],
    voz: 'Un truco extra: con count over time pueden convertir los logs en una gráfica, por ejemplo cuántos errores hay por minuto. Así, métricas y logs se complementan. La métrica avisa, el log explica.',
  },
  // ---------- Reto ----------
  {
    id: '31', tipo: 'terminal', etiqueta: 'Reto · Tu propia alerta de p99',
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
    ],
    voz: 'Ahora el reto. El proyecto trae una alerta de latencia basada en el p95 mayor a quinientos milisegundos, y nunca se dispara, aunque hay pagos de casi dos segundos. Vamos a crear una sobre el p99. Abran el archivo alertas punto yml dentro de infra, prometheus, y al final de la lista de reglas peguen esta regla de la guía. Cuidado con la indentación: debe quedar alineada con las otras reglas.',
  },
  {
    id: '32', tipo: 'terminal', etiqueta: 'Reto · Recargar Prometheus',
    lineas: [
      ['$', 'curl -X POST localhost:9090/-/reload'],
      ['#', 'Prometheus relee alertas.yml sin reiniciarse'],
    ],
    voz: 'Guarden el archivo y, en la terminal, recarguen la configuración de Prometheus con este curl. No hace falta reiniciar nada: Prometheus vuelve a leer sus reglas en caliente.',
  },
  {
    id: '33', tipo: 'captura', etiqueta: 'Reto · La alerta se dispara', img: 'alertas-reto.png',
    nota: 'LatenciaP99Alta: Pending → FIRING en menos de un minuto',
    voz: 'Vuelvan a la página de alertas. Su nueva regla, LatenciaP99Alta, primero aparece como Pending y, después de treinta segundos, pasa a FIRING. La lección: el percentil que eligen para su objetivo de servicio decide qué problemas pueden ver.',
  },
  // ---------- Cierre ----------
  {
    id: '34', tipo: 'lista', etiqueta: 'Para comparar', titulo: 'Resultados de referencia',
    items: ['Pagos: promedio 103 ms · p99 1.574 ms · 4,4 % de errores', 'n-mas-1: 101 consultas · p95 224 ms', 'optimizado: 1 consulta · p95 5 ms', 'Tus números variarán, pero en el mismo orden'],
    voz: 'Comparen sus números con los de referencia. En pagos, un promedio de ciento tres milisegundos con un p99 de más de un segundo y medio, y cerca de cuatro por ciento de errores. En pedidos, ciento una consultas contra una. Sus valores serán un poco distintos porque la latencia es aleatoria, pero deben estar en el mismo orden.',
  },
  {
    id: '35', tipo: 'lista', etiqueta: 'Si algo falla', titulo: 'Problemas comunes',
    items: ['«connection refused»: la API aún arranca, espera 20 s', 'Paneles vacíos: ¿lanzaste k6? espera 1 minuto', 'Loki sin líneas: los logs llegan cada pocos segundos', 'La alerta no aparece: revisa la indentación del YAML'],
    voz: 'Si algo no les funciona, revisen esto. Connection refused: la API todavía está arrancando. Paneles vacíos: verifiquen que lanzaron k6 y esperen un minuto. Loki sin resultados: los logs se envían por lotes, denle unos segundos. Y si la alerta del reto no aparece, casi siempre es la indentación del archivo YAML.',
  },
  {
    id: '36', tipo: 'lista', etiqueta: 'Cierre', titulo: 'Antes de irte',
    items: ['Apaga todo: docker compose down', 'Detén el codespace en github.com/codespaces', 'Trae tus dudas a la sesión de preguntas'],
    voz: 'Para terminar, apaguen todo con docker compose down y detengan su codespace desde la página de GitHub para no gastar sus horas gratuitas. Hoy encontraron una cola de latencia, un N más uno y una dependencia que falla, solo con métricas, logs y alertas. Eso es observabilidad. ¡Gracias, y nos vemos en la sesión de preguntas!',
  },
];
