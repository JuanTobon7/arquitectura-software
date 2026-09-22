# PipesFilters - Arquitectura de Plugins

Este proyecto implementa un pipeline modular de procesamiento de archivos bajo una
arquitectura **plug-and-play**. Cada módulo es un plugin independiente que recibe una
lista de `FileFrame`, transforma su contenido y devuelve otra lista.

---

## Estructura general

El proyecto es un multi-módulo Maven con la siguiente organización:

```
plug-and-play/
├── core/
├── images/
├── converter-binary/
├── converter-base64/
├── security/
└── persistence/
```

Cada plugin extiende `ConcurrenceBase` (definido en `core`) y sobrescribe el método
`processFile(FileFrame)`. La clase base se encarga de ejecutar el procesamiento en
paralelo usando un `ExecutorService`.

---

## Contrato entre plugins

La interfaz `Plugin` define el contrato básico:

```java
public interface Plugin {
    List<FileFrame> process(List<FileFrame> input);
}
```

La clase `FileFrame` representa un archivo genérico:

```java
public record FileFrame(String name, byte[] content) {
    public long size() {
        return content.length;
    }
}
```

---

## Módulos

### `core`

Es el módulo base compartido por todos los demás. Define las abstracciones y la
infraestructura de concurrencia.

| Clase / Interfaz | Descripción |
|------------------|-------------|
| `FileFrame`      | Record que agrupa el nombre y el contenido binario de un archivo. |
| `Plugin`         | Interfaz de todos los filtros. Define el método `process`. |
| `ConcurrenceBase`| Clase base abstracta que implementa `Plugin`. Ejecuta `processFile` en paralelo sobre cada elemento de la lista de entrada. |

#### Concurrencia

`ConcurrenceBase` utiliza un pool de hilos con tamaño igual al número de procesadores
disponibles. Para cada `FileFrame` de entrada, envía una tarea asíncrona que invoca
`processFile`. Al finalizar, recopila los resultados en el mismo orden de llegada y
devuelve la lista resultante.

---

### `images`

Plugin de procesamiento de imágenes.

| Clase | Descripción |
|-------|-------------|
| `ProcessorImages` | Recibe imágenes como `FileFrame`, las escala 5 veces en paralelo y devuelve un `FileFrame` especial con nombre `__DIRECTORY__` cuyo contenido es la ruta del directorio generado. |
| `HelperImage`     | Utilidad para extraer el formato de imagen soportado a partir de la extensión del archivo (`jpg`, `jpeg`, `png`). |

#### Comportamiento

Por cada imagen de entrada:

1. Detecta el formato soportado.
2. Crea un directorio de salida único.
3. Escribe 5 versiones de la misma imagen:
   - `nombre_1.ext`
   - `nombre_2.ext`
   - ...
   - `nombre_5.ext`

Estas escrituras se realizan en paralelo usando un pool de 5 hilos.

Además, el plugin expone `collectOutputFrames()` para obtener los archivos generados
como una lista de `FileFrame`, incluyendo el marcador de directorio al final.

---

### `converter-binary`

Plugin de codificación Base64.

| Clase | Descripción |
|-------|-------------|
| `BinaryToBase64` | Transforma el contenido binario de cada `FileFrame` en una cadena Base64. |

La salida mantiene el nombre original y el contenido pasa a ser el texto codificado
en UTF-8. El resultado se escribe en el directorio raíz compartido (`result/`).

---

### `converter-base64`

Plugin de decodificación Base64.

| Clase | Descripción |
|-------|-------------|
| `Base64ToBinary` | Convierte una cadena Base64 en su representación binaria original. |

Es el complemento de `BinaryToBase64`. Recibe texto codificado y devuelve bytes.

#### Uso típico

```
Entrada -> BinaryToBase64 -> Base64ToBinary -> ...
```

---

### `security`

Plugin de seguridad.

| Clase | Descripción |
|-------|-------------|
| `EncryptSha256` | Calcula el hash SHA-256 del contenido de cada `FileFrame` y devuelve el hash como texto hexadecimal. |

Aplica `MessageDigest` con el algoritmo `SHA-256` y formatea el resultado con
`HexFormat.of().formatHex(...)`.

---

### `persistence`

Plugin de persistencia de metadatos en base de datos H2.

| Clase | Descripción |
|-------|-------------|
| `Persistence`       | Recibe `FileFrame`, calcula SHA-256 y guarda sus metadatos en H2. |
| `Repository`        | Interfaz con la operación `save(ProcessedFile)`. |
| `H2RepositoryImpl`  | Implementación JDBC que inserta en la tabla `processed_files`. |
| `DatabaseAccess`    | Administra la conexión JDBC a `jdbc:h2:./data/pipesfilters` y crea la tabla si no existe. |
| `Factory`           | Fábrica que devuelve una instancia de `Repository`. |
| `ProcessedFile`     | Record con los campos persistidos: nombre, sha256, tamaño, origen y fecha. |

#### Esquema de base de datos

```sql
CREATE TABLE IF NOT EXISTS processed_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL,
    filter_origin VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
)
```

---

## Flujo de procesamiento

Un pipeline típico podría ser:

```
[imágenes de entrada]
        |
        v
+---------------+
| images        | -> divide imágenes en 5 versiones y devuelve directorio
+---------------+
        |
        v
+------------------+
| converter-binary | -> codifica cada archivo a Base64
+------------------+
        |
        v
+------------------+
| converter-base64 | -> decodifica de Base64 a binario
+------------------+
        |
        v
+-------------+
| security    | -> calcula SHA-256 de cada archivo
+-------------+
        |
        v
+----------------+
| persistence    | -> guarda metadatos en H2
+----------------+
```

Cada plugin solo conoce el contrato `List<FileFrame>` gracias a `core`, lo que permite
reordenarlos, reemplazarlos o agregar nuevos sin modificar el resto del sistema.

---

## Construcción

Comando para compilar todos los módulos:

```bash
mvn clean compile
```

---

## Notas

- No existen clases `Main` en los módulos: los plugins están diseñados para ser
  invocados programáticamente o desde un orquestador externo.
- Se eliminó todo el mecanismo de streams binarios `FpsReader` / `FpsWriter`; el
  intercambio de información entre plugins se realiza a través de colecciones
  en memoria.
- La concurrencia está centralizada en `ConcurrenceBase`, por lo que cualquier
  plugin nuevo obtiene procesamiento paralelo automáticamente.
