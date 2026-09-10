# Sistema de Gestión de Cine 🎬

Proyecto de ejemplo en **Java 21 + Spring Boot 3** que implementa una
**arquitectura en capas con inversión de dependencias** (estilo *puertos y
adaptadores / hexagonal*), aplicando explícitamente los principios **SOLID**
y **GRASP** (cada clase señala en sus comentarios qué principios cumple y por qué).

## 1. Diagrama de la arquitectura

Flujo de dependencias (todas apuntan **hacia el dominio**):

```
Vista Consola ──▶ Servicio1/Servicio2 ──▶ Interface Dominio (puerto entrada)
                                                  │
                                             DOMINIO (gestores + entidades)
                                                  │
                                     Interface Persistencia (puerto salida)
                                                  ▲
                                 ┌────────────────┴────────────────┐
                        Adaptador MySQL (Películas)      Adaptador H2 (Reservas)
```

### Árbol de paquetes

```
sistema-gestion-cine/
├── pom.xml
├── src/main/resources/
│   ├── application.yml            # dos datasources: MySQL (películas) + H2 (reservas)
│   └── application-demo.yml       # perfil demo: todo en H2, corre sin MySQL
└── src/main/java/com/uni/cine/
    ├── CineApplication.java                        # arranque Spring Boot
    │
    ├── vista/                                      # CAPA VISTA
    │   └── VistaConsola.java                       #   menú interactivo (CommandLineRunner)
    │
    ├── aplicacion/                                 # CAPA APLICACIÓN
    │   ├── ServicioPelicula.java                   #   Servicio1 (GRASP Controlador)
    │   └── ServicioReserva.java                    #   Servicio2 (GRASP Controlador)
    │
    ├── dominio/                                    # CAPA DOMINIO (Java puro, sin Spring)
    │   ├── pelicula/                               #   MÓDULO 1: Películas
    │   │   ├── Pelicula.java                       #     entidad (Experto en Información)
    │   │   ├── GestorPeliculas.java                #     lógica de negocio
    │   │   └── puerto/
    │   │       ├── entrada/CatalogoPeliculas.java  #     Interface Dominio (puerto entrada)
    │   │       └── salida/RepositorioPeliculas.java#     Interface Persistencia (puerto salida)
    │   └── reserva/                                #   MÓDULO 2: Reservas
    │       ├── Reserva.java                        #     entidad (Experto en Información)
    │       ├── EstadoReserva.java                  #     enum ACTIVA/CANCELADA
    │       ├── Funcion.java                        #     objeto de valor: sala A1..E6
    │       ├── GestorReservas.java                 #     lógica de negocio
    │       └── puerto/
    │           ├── entrada/GestionReservas.java    #     Interface Dominio (puerto entrada)
    │           └── salida/RepositorioReservas.java #     Interface Persistencia (puerto salida)
    │
    └── infraestructura/                            # CAPA INFRAESTRUCTURA
        ├── config/
        │   ├── ConfiguracionDatasourcePeliculas.java  # DataSource+EMF+TX de MySQL
        │   ├── ConfiguracionDatasourceReservas.java   # DataSource+EMF+TX de H2
        │   └── ConfiguracionDominio.java              # registra gestores bajo sus puertos
        └── persistencia/
            ├── peliculas/                          #   ADAPTADOR MySQL
            │   ├── PeliculaJpaRepository.java      #     Spring Data JPA (detalle técnico)
            │   └── AdaptadorPersistenciaPeliculas.java # implementa RepositorioPeliculas
            └── reservas/                           #   ADAPTADOR H2
                ├── ReservaJpaRepository.java
                └── AdaptadorPersistenciaReservas.java  # implementa RepositorioReservas
```

**Claves del diseño**

- Los **puertos** (interfaces) viven **dentro del dominio**: los de *entrada*
  (`CatalogoPeliculas`, `GestionReservas`) son lo que el dominio *ofrece*; los de
  *salida* (`RepositorioPeliculas`, `RepositorioReservas`) son lo que el dominio *exige*.
- Los servicios de aplicación dependen **solo de los puertos de entrada**;
  jamás nombran `GestorPeliculas`, `GestorReservas` ni nada de infraestructura (**DIP**).
- El dominio es **Java puro sin anotaciones de Spring**; sus gestores se registran
  como beans en `ConfiguracionDominio` (infraestructura), que es donde se "ensambla el hexágono".
- **Toda la inyección es por constructor** (nunca `@Autowired` en campos):
  dependencias `final`, objetos siempre completos y testeables sin contenedor.
- *Compromiso señalado*: las entidades de dominio llevan anotaciones
  `jakarta.persistence` para no duplicar el modelo (entidad JPA espejo + mapper).
  Son metadatos pasivos; la alternativa purista se comenta en `Pelicula.java`.

## 2. Dos datasources con Spring Data JPA

Con dos orígenes de datos la autoconfiguración de Spring Boot no alcanza, así que
cada módulo declara su tríada explícita **DataSource → EntityManagerFactory →
TransactionManager**:

| Módulo | BD | Config | Propiedades | Entidades escaneadas | Repositorios enlazados |
|---|---|---|---|---|---|
| Películas | **MySQL** | `ConfiguracionDatasourcePeliculas` | `app.datasource.peliculas.*` | `dominio.pelicula` | `infraestructura.persistencia.peliculas` |
| Reservas | **H2 (mem)** | `ConfiguracionDatasourceReservas` | `app.datasource.reservas.*` | `dominio.reserva` | `infraestructura.persistencia.reservas` |

Cada servicio delimita sus transacciones con el gestor de SU módulo
(`@Transactional("peliculasTransactionManager")` / `@Transactional("reservasTransactionManager")`).

## 3. Tabla resumen: Principio → Clase → Justificación

### SOLID

| Principio | Clase(s) donde se aplica | Justificación breve |
|---|---|---|
| **SRP** — Responsabilidad Única | `Pelicula`, `Reserva`, `GestorPeliculas`, `GestorReservas`, `ServicioPelicula`, `ServicioReserva`, `VistaConsola`, adaptadores y configs | Cada clase tiene una sola razón de cambio: la entidad modela e invariantes, el gestor reglas de negocio, el servicio orquesta casos de uso, la vista presenta, el adaptador traduce a JPA, la config cablea. |
| **OCP** — Abierto/Cerrado | `RepositorioPeliculas`, `RepositorioReservas` (+ sus adaptadores), `EstadoReserva` | Cambiar de BD = escribir OTRO adaptador del mismo puerto; agregar un estado = extender el enum. Nada existente se modifica. |
| **LSP** — Sustitución de Liskov | `GestorPeliculas`/`GestorReservas` respecto a sus puertos de entrada; `AdaptadorPersistencia*` respecto a los de salida | Toda implementación respeta el contrato observable del puerto (pre/postcondiciones); el cliente funciona igual con la implementación real, otra BD o un mock de test. |
| **ISP** — Segregación de Interfaces | `CatalogoPeliculas`, `GestionReservas`, `RepositorioPeliculas`, `RepositorioReservas` | Interfaces pequeñas y por módulo: quien consume películas no arrastra operaciones de reservas ni viceversa. |
| **DIP** — Inversión de Dependencias | `ServicioPelicula`→`CatalogoPeliculas`; `ServicioReserva`→`GestionReservas`; `Gestor*`→`Repositorio*`; adaptadores implementan puertos del dominio | Alto nivel y bajo nivel dependen de abstracciones definidas EN el dominio; la infraestructura depende del dominio y nunca al revés. |

### GRASP

| Patrón | Clase(s) donde se aplica | Justificación breve |
|---|---|---|
| **Experto en Información** | `Pelicula` (invariantes, `perteneceAlGenero`), `Reserva` (`cancelar`, `ocupaAsiento`), `Funcion` (mapa de asientos), `GestorReservas` (disponibilidad) | Cada responsabilidad se asigna a quien posee la información para cumplirla. |
| **Creador** | `GestorPeliculas` crea `Pelicula`; `GestorReservas` crea `Reserva` y `Funcion`; `ConfiguracionDominio` crea los gestores | Crea B quien agrega/usa estrechamente a B y posee sus datos de inicialización. |
| **Controlador** | `ServicioPelicula`, `ServicioReserva` | Reciben los eventos del actor (la consola) y los dirigen al dominio; la vista no decide, solo captura entrada. |
| **Bajo Acoplamiento** | Toda la cadena Vista→Servicio→Puerto→Gestor→Puerto→Adaptador | Cada capa conoce solo la abstracción siguiente; cambiar MySQL/H2 no toca dominio, servicios ni vista (lo demuestra el perfil `demo`). |
| **Alta Cohesión** | `GestorPeliculas`, `GestorReservas`, `VistaConsola`, `Funcion` | Métodos de cada clase giran alrededor de un único concepto; nada mezcla presentación, negocio y SQL. |
| **Polimorfismo** | `AdaptadorPersistenciaPeliculas` (MySQL) y `AdaptadorPersistenciaReservas` (H2) tras puertos análogos | La variación por tecnología de BD se resuelve con implementaciones intercambiables del mismo contrato, sin `if (tipoBD)`. |
| **Indirección** | Puertos de entrada/salida, adaptadores y el contenedor de Spring (`ConfiguracionDominio`) | Objetos intermedios desacoplan a los participantes: servicio↔gestor y gestor↔BD nunca se conocen directamente. |

## 4. Instrucciones de ejecución

### Requisitos
- JDK 21+
- Maven 3.9+
- MySQL 8 (solo para el modo completo; ver perfil `demo` más abajo)

### Opción A — Modo completo (Películas en MySQL, Reservas en H2)

1. Levantar MySQL, por ejemplo con Docker:
   ```bash
   docker run --name cine-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8
   ```
   La base `cine_db` se crea sola (`createDatabaseIfNotExist=true`) y Hibernate
   genera la tabla `peliculas` (`hbm2ddl.auto=update`). Si tu MySQL usa otras
   credenciales, ajusta `app.datasource.peliculas.*` en `application.yml`.

2. Ejecutar:
   ```bash
   cd sistema-gestion-cine
   mvn spring-boot:run
   ```

### Opción B — Perfil `demo` (sin MySQL: todo en H2)

```bash
cd sistema-gestion-cine
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Gracias al DIP este cambio es **solo configuración** (`application-demo.yml`):
ni el dominio, ni los servicios, ni la vista se modifican.

### Uso del menú

```
--------- MENÚ PRINCIPAL ---------
  PELÍCULAS (MySQL)
    1. Registrar película
    2. Listar películas
    3. Buscar películas por género
  RESERVAS (H2 en memoria)
    4. Crear reserva de asiento
    5. Cancelar reserva
    6. Consultar disponibilidad de una función
    7. Listar reservas
  0. Salir
```

- Las funciones se identifican por código libre (ej. `F1`) y la sala es fija:
  filas `A..E` × asientos `1..6` (30 asientos).
- Reglas de negocio demostrables: título de película único, asiento único por
  función mientras la reserva esté ACTIVA, cancelación libera el asiento,
  asiento inexistente (`Z9`) rechazado.
- Las películas persisten entre ejecuciones (MySQL); las reservas se pierden al
  salir (H2 en memoria, `create-drop`) — elección deliberada para contrastar
  ambos adaptadores.

### Empaquetar

```bash
mvn clean package
java -jar target/sistema-gestion-cine-1.0.0.jar            # modo completo
java -jar target/sistema-gestion-cine-1.0.0.jar --spring.profiles.active=demo
```

## Validar el flujo de capas

En la raíz del repositorio hay un validador que dibuja el diagrama del tablero
(Vista Consola → Servicio1/Servicio2 → Interface Dominio → Dominio →
Persistencia MySQL/H2) con cada flecha verificada contra el código, y audita
que ninguna capa importe fuera de su caja vecina:

```powershell
# Windows (desde la raíz del repo)
.\validar-capas.ps1
```

```bash
# Linux / Mac
./validar-capas.sh
```
