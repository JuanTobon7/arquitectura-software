# Guía técnica: Refactor hacia una arquitectura Microkernel / Plug-and-Play real

Este documento es un paso a paso para llevar `PipesFilters` de "plug-and-play declarado en el
README" a "plug-and-play mecánicamente garantizado". Cada paso indica **qué módulo tocar**,
**por qué** y **el código concreto**.

---

## Índice

1. [Nuevo contrato central: `ProcessingContext`](#1-nuevo-contrato-central-processingcontext)
2. [Nueva interfaz `Plugin` y `ConcurrenceBase`](#2-nueva-interfaz-plugin-y-concurrencebase)
3. [Refactor de `images`: eliminar el marcador `__DIRECTORY__`](#3-refactor-de-images)
4. [Refactor de `converter-binary` y `converter-base64`](#4-refactor-de-converter-binary-y-converter-base64)
5. [Refactor de `security`: metadata en vez de sobrescritura](#5-refactor-de-security)
6. [Refactor de `persistence`: reutilizar metadata](#6-refactor-de-persistence)
7. [Descubrimiento dinámico con `ServiceLoader`](#7-descubrimiento-dinámico-con-serviceloader)
8. [Ejecución en paralelo (fan-out/fan-in) con `CompletableFuture.allOf()`](#8-ejecución-en-paralelo-fan-outfan-in-con-completablefutureallof)
9. [Ajustes de `pom.xml`](#9-ajustes-de-pomxml)
10. [Validación](#10-validación)
11. [Checklist final](#11-checklist-final)

---

## 1. Nuevo contrato central: `ProcessingContext`

**Módulo:** `core`
**Problema que resuelve:** `FileFrame` mezcla el payload con el estado del pipeline; no hay
dónde guardar metadata derivada (hash, tamaño original) sin destruir el contenido.

Crear `core/src/main/java/com/pipesfilters/core/ProcessingContext.java`:

```java
package com.pipesfilters.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProcessingContext {

    private final String name;
    private byte[] payload;
    private final Map<String, Object> metadata;

    public ProcessingContext(String name, byte[] payload) {
        this.name = name;
        this.payload = payload;
        this.metadata = new ConcurrentHashMap<>();
        this.metadata.put("originalSize", (long) payload.length);
    }

    public String name() {
        return name;
    }

    public byte[] payload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long originalSize() {
        return (long) metadata.get("originalSize");
    }

    public <T> Optional<T> getMetadata(String key, Class<T> type) {
        return Optional.ofNullable(metadata.get(key)).map(type::cast);
    }

    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Copia defensiva: cada rama paralela (ver sección 8) trabaja sobre su propia copia
     * para no pisar el payload de otra rama que corre al mismo tiempo.
     */
    public ProcessingContext copy() {
        ProcessingContext copy = new ProcessingContext(this.name, this.payload.clone());
        copy.metadata.putAll(this.metadata);
        return copy;
    }

    /**
     * Usado en la fase de fan-in: vuelca la metadata generada por una rama (por ejemplo
     * "sha256" de security) sobre el contexto final que verá "persistence".
     */
    public void mergeMetadataFrom(ProcessingContext other) {
        this.metadata.putAll(other.metadata);
    }
}
```

**Nota:** `FileFrame` puede quedar como record legado (deprecado) para no romper compilación
mientras migras módulo por módulo, o eliminarse directamente si haces el refactor de una sola vez.

**Nota sobre concurrencia:** como ahora varios plugins pueden leer/transformar el mismo archivo
al mismo tiempo (ver sección 8), ningún plugin debe mutar el `payload` de un `ProcessingContext`
compartido entre ramas. Por eso el orquestador le entrega a cada rama una copia (`copy()`), y
solo la metadata —que sí es segura para escritura concurrente porque vive en un
`ConcurrentHashMap`— se fusiona de vuelta al final.

---

## 2. Nueva interfaz `Plugin` y `ConcurrenceBase`

**Módulo:** `core`
**Problema que resuelve:** el registro dinámico (paso 7) necesita que cada plugin se identifique
a sí mismo; y `images` necesita el pool de hilos del núcleo en vez de crear uno propio.

`core/src/main/java/com/pipesfilters/core/Plugin.java`:

```java
package com.pipesfilters.core;

import java.util.List;

public interface Plugin {
    String id();
    List<ProcessingContext> process(List<ProcessingContext> input);
}
```

Además, una interfaz marcadora para el (o los) plugin(s) que deben ejecutarse **después** de que
todos los demás terminen, en vez de en paralelo con ellos (típicamente `persistence`, el "save"):

```java
package com.pipesfilters.core;

public interface TerminalPlugin extends Plugin {
    // Sin métodos nuevos: es un marcador para que el orquestador (sección 8)
    // lo excluya del fan-out y lo ejecute recién cuando CompletableFuture.allOf()
    // señala que el resto de los plugins ya completó.
}
```

`core/src/main/java/com/pipesfilters/core/ConcurrenceBase.java`:

```java
package com.pipesfilters.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class ConcurrenceBase implements Plugin {

    protected final ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    protected ExecutorService sharedExecutor() {
        return executor;
    }

    protected abstract ProcessingContext processFile(ProcessingContext ctx);

    @Override
    public List<ProcessingContext> process(List<ProcessingContext> input) {
        List<CompletableFuture<ProcessingContext>> futures = input.stream()
            .map(ctx -> CompletableFuture.supplyAsync(() -> processFile(ctx), executor))
            .collect(Collectors.toList());

        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
}
```

---

## 3. Refactor de `images`

**Problema que resuelve:** el `FileFrame` con nombre `__DIRECTORY__` es un valor centinela que
obliga a cada plugin downstream a conocer un caso especial no declarado en el contrato.

Paso 3.1 — Crear un tipo de resultado explícito en `images`:

```java
package com.pipesfilters.images;

import com.pipesfilters.core.ProcessingContext;
import java.nio.file.Path;
import java.util.List;

public record ImageProcessingResult(
    List<ProcessingContext> generatedFiles,
    Path outputDirectory
) {}
```

Paso 3.2 — `ProcessorImages` deja de devolver la lista mezclada:

```java
public class ProcessorImages extends ConcurrenceBase {

    @Override
    public String id() {
        return "images";
    }

    // process() sigue devolviendo List<ProcessingContext> con SOLO archivos reales

    public ImageProcessingResult collectOutputFrames(List<ProcessingContext> processed, Path dir) {
        return new ImageProcessingResult(processed, dir);
    }
}
```

Paso 3.3 — Reemplazar el pool propio de 5 hilos por `sharedExecutor()` del núcleo:

```java
List<CompletableFuture<Void>> writes = List.of(1, 2, 3, 4, 5).stream()
    .map(i -> CompletableFuture.runAsync(() -> writeVersion(ctx, i), sharedExecutor()))
    .toList();
writes.forEach(CompletableFuture::join);
```

Si el pipeline necesita el `outputDirectory` más adelante (por ejemplo para logging o para
`persistence`), pásalo como argumento explícito del orquestador (paso 8), **no** dentro de la
lista de `ProcessingContext`.

---

## 4. Refactor de `converter-binary` y `converter-base64`

**Problema que resuelve:** nombres ambiguos y pérdida de `originalSize`.

Paso 4.1 — Renombrar (opcional pero recomendado):
- `converter-binary` → conceptualmente "binary-to-base64"
- `converter-base64` → conceptualmente "base64-to-binary"

Si no quieres tocar los `artifactId` de Maven todavía, al menos cambia el `id()` del plugin:

```java
// BinaryToBase64.java
@Override
public String id() {
    return "binary-to-base64";
}

@Override
protected ProcessingContext processFile(ProcessingContext ctx) {
    String encoded = Base64.getEncoder().encodeToString(ctx.payload());
    ctx.setPayload(encoded.getBytes(StandardCharsets.UTF_8));
    // originalSize ya quedó protegido en el ProcessingContext desde su creación
    return ctx;
}
```

```java
// Base64ToBinary.java
@Override
public String id() {
    return "base64-to-binary";
}

@Override
protected ProcessingContext processFile(ProcessingContext ctx) {
    byte[] decoded = Base64.getDecoder().decode(ctx.payload());
    ctx.setPayload(decoded);
    return ctx;
}
```

---

## 5. Refactor de `security`

**Problema que resuelve:** hoy el hash **reemplaza** el contenido; si `persistence` viene después
y vuelve a calcular SHA-256, estaría hasheando el hash, no el archivo original.

```java
package com.pipesfilters.security;

@Override
public String id() {
    return "security";
}

@Override
protected ProcessingContext processFile(ProcessingContext ctx) {
    String hash = sha256Hex(ctx.payload());
    ctx.putMetadata("sha256", hash);   // ya NO se sobrescribe el payload
    return ctx;
}
```

Si tu caso de uso realmente necesita que el payload final sea el hash (por ejemplo, para un
pipeline de solo verificación), deja esa opción como un **segundo** plugin explícito
(`HashOnlyOutput`) en vez de que `security` haga las dos cosas a la vez.

---

## 6. Refactor de `persistence`

**Problema que resuelve:** duplicaba el cálculo de SHA-256 en vez de reutilizar el de `security`.

```java
package com.pipesfilters.persistence;

public class Persistence extends ConcurrenceBase implements TerminalPlugin {

    @Override
    public String id() {
        return "persistence";
    }

    @Override
    protected ProcessingContext processFile(ProcessingContext ctx) {
        String hash = ctx.getMetadata("sha256", String.class)
            .orElseGet(() -> sha256Hex(ctx.payload())); // fallback si "security" no corrió, o no está en el registro

        ProcessedFile record = new ProcessedFile(
            ctx.name(),
            hash,
            ctx.originalSize(),   // tamaño real, ya protegido en el ProcessingContext
            id(),
            Instant.now()
        );

        repository.save(record);
        return ctx;
    }
}
```

Al implementar `TerminalPlugin`, este plugin queda automáticamente excluido del fan-out paralelo
del orquestador (sección 8) y se ejecuta solo cuando `CompletableFuture.allOf()` confirma que
`images`, `security`, `binary-to-base64` y `base64-to-binary` ya terminaron.

---

## 7. Descubrimiento dinámico con `ServiceLoader`

**Problema que resuelve:** hoy "agregar un plugin sin modificar el resto del sistema" es una
promesa sin mecanismo. `ServiceLoader` la hace real.

Paso 7.1 — En **cada módulo de plugin** (`images`, `converter-binary`, `converter-base64`,
`security`, `persistence`), crear:

```
src/main/resources/META-INF/services/com.pipesfilters.core.Plugin
```

Con una sola línea dentro (ejemplo para `security`):

```
com.pipesfilters.security.EncryptSha256
```

Paso 7.2 — Crear el registro en `core`:

```java
package com.pipesfilters.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class PluginRegistry {
    public static Map<String, Plugin> discover() {
        Map<String, Plugin> plugins = new LinkedHashMap<>();
        ServiceLoader.load(Plugin.class).forEach(p -> plugins.put(p.id(), p));
        return plugins;
    }
}
```

Con esto, un plugin nuevo (por ejemplo `compression`) solo necesita:
1. Implementar `Plugin`.
2. Declarar su clase en `META-INF/services/com.pipesfilters.core.Plugin`.
3. Estar en el classpath.

Ningún otro módulo, ni `core`, se modifica.

---

## 8. Ejecución en paralelo (fan-out/fan-in) con `CompletableFuture.allOf()`

**Módulo:** nuevo módulo `orchestrator` (o `app`)
**Cambio de enfoque:** ya no hay un orden de "pipeline" — no hace falta que `images` termine
antes que `security`, ni que `security` termine antes que `binary-to-base64`. Todos los plugins
que no sean `TerminalPlugin` corren **al mismo tiempo** sobre el input. Lo único que debe esperar
es el guardado (`persistence`), porque necesita la metadata que los demás generan (por ejemplo
`sha256` de `security`).

```
                    ┌──────────────┐
            ┌──────▶│ images       │───┐
            │       └──────────────┘   │
            │       ┌──────────────┐   │
[input] ────┼──────▶│ binary-to-b64│───┤── CompletableFuture.allOf() ──▶ persistence (save)
            │       └──────────────┘   │
            │       ┌──────────────┐   │
            ├──────▶│ base64-to-bin│───┤
            │       └──────────────┘   │
            │       ┌──────────────┐   │
            └──────▶│ security     │───┘
                    └──────────────┘
```

Paso 8.1 — `ParallelOrchestrator`:

```java
package com.pipesfilters.orchestrator;

import com.pipesfilters.core.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ParallelOrchestrator {

    public List<ProcessingContext> run(List<ProcessingContext> input) {
        Map<String, Plugin> discovered = PluginRegistry.discover();

        List<Plugin> processingPlugins = discovered.values().stream()
            .filter(p -> !(p instanceof TerminalPlugin))
            .toList();

        TerminalPlugin savePlugin = discovered.values().stream()
            .filter(p -> p instanceof TerminalPlugin)
            .map(p -> (TerminalPlugin) p)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No hay plugin terminal (save) registrado"));

        // FAN-OUT: cada plugin recibe su propia copia del input y corre en paralelo
        List<CompletableFuture<List<ProcessingContext>>> branches = processingPlugins.stream()
            .map(plugin -> CompletableFuture.supplyAsync(() -> plugin.process(copyAll(input))))
            .toList();

        // Barrera: no se ejecuta el save hasta que TODAS las ramas terminaron
        CompletableFuture<Void> allDone =
            CompletableFuture.allOf(branches.toArray(new CompletableFuture[0]));

        // FAN-IN: se fusiona la metadata de todas las ramas y recién ahí corre persistence
        return allDone.thenApply(v -> {
            List<ProcessingContext> merged = mergeBranches(input, branches);
            return savePlugin.process(merged);
        }).join();
    }

    private List<ProcessingContext> copyAll(List<ProcessingContext> input) {
        return input.stream().map(ProcessingContext::copy).toList();
    }

    private List<ProcessingContext> mergeBranches(
            List<ProcessingContext> original,
            List<CompletableFuture<List<ProcessingContext>>> branches) {

        Map<String, ProcessingContext> byName = new LinkedHashMap<>();
        original.forEach(ctx -> byName.put(ctx.name(), ctx.copy()));

        for (CompletableFuture<List<ProcessingContext>> branch : branches) {
            for (ProcessingContext result : branch.join()) {
                byName.get(result.name()).mergeMetadataFrom(result);
            }
        }
        return new ArrayList<>(byName.values());
    }
}
```

**Por qué esto sigue siendo plug-and-play:** agregar un plugin nuevo (por ejemplo, `compression`)
solo requiere que implemente `Plugin` y se declare en `META-INF/services` (paso 7). El
`ParallelOrchestrator` lo recoge automáticamente en el `discovered.values()` y lo suma al
fan-out sin que se toque ni una línea de este archivo. Si en cambio el plugin nuevo debe ser
otro "sink" que también dependa de que todo termine, basta con que implemente `TerminalPlugin`
en vez de `Plugin` — aunque en ese caso conviene generalizar `savePlugin` a una lista de
terminales en vez de `findFirst()`.

**Nota importante sobre `converter-binary` y `converter-base64`:** en el diseño anterior (
sección "Uso típico" del README original) uno alimentaba al otro en secuencia. Al pasar a
fan-out, ambos reciben el mismo input original y corren de forma independiente — ya no tiene
sentido pensar en ellos como "codificador seguido de decodificador" dentro del mismo pipeline,
sino como dos transformaciones distintas que producen sus propios resultados en paralelo. Si tu
caso de uso realmente necesita que uno consuma la salida del otro, esa dependencia deja de ser
responsabilidad del orquestador y pasa a ser una composición explícita dentro de un único plugin
(o una llamada directa de un plugin al otro como colaborador interno, no como dos pasos de un
pipeline).

**Nota sobre el pool de hilos:** `CompletableFuture.supplyAsync(...)` sin executor explícito usa
el `ForkJoinPool.commonPool()`. Como cada rama, a su vez, puede disparar su propio trabajo
paralelo interno vía `ConcurrenceBase.sharedExecutor()` (sección 2), en una carga alta conviene
pasar un único `ExecutorService` compartido tanto al `ParallelOrchestrator` como a cada instancia
de `ConcurrenceBase`, para no terminar con dos niveles de pools compitiendo por CPU sin control
central.

---

## 9. Ajustes de `pom.xml`

- Cada módulo de plugin (`images`, `converter-binary`, `converter-base64`, `security`,
  `persistence`) debe depender de `core` (ya debería ser así) y **no** depender entre sí.
- Verificar que el recurso `META-INF/services/...` quede empaquetado: por defecto Maven ya
  incluye todo `src/main/resources` en el JAR, así que no necesitas plugins adicionales salvo
  que uses shading; si usas `maven-shade-plugin` para un JAR único del orquestador, agrega el
  transformer de servicios para que no se pisen los archivos `META-INF/services` de distintos
  módulos:

```xml
<transformers>
    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
</transformers>
```

- El módulo `orchestrator` debe depender en tiempo de ejecución de **todos** los módulos de
  plugins (para que estén en el classpath y `ServiceLoader` los encuentre), pero `core` sigue
  sin depender de ninguno de ellos.

---

## 10. Validación

1. **Prueba de contrato:** correr el `ParallelOrchestrator` con `images`, `binary-to-base64`,
   `base64-to-binary` y `security` disparados en paralelo, y verificar en la tabla
   `processed_files` (guardada por `persistence` al final) que `size_bytes` coincide con el
   tamaño real del archivo original, no con el del texto Base64 generado por alguna rama.
2. **Prueba de reutilización de metadata:** loggear si `persistence` usó el hash de `security`
   (`ctx.getMetadata` presente) o tuvo que recalcularlo, para confirmar que el flujo normal no
   duplica trabajo.
3. **Prueba de extensibilidad:** crear un plugin de prueba mínimo (`NoOpPlugin`) en un módulo
   nuevo, declararlo en `META-INF/services`, y confirmar que el `ParallelOrchestrator` lo suma al
   fan-out automáticamente, sin tocar ningún otro módulo ni el propio orquestador.
4. **Prueba de fan-out/fan-in:** instrumentar cada plugin de transformación con un log de
   timestamp al empezar y terminar, y confirmar que `persistence.process()` arranca **después**
   del timestamp más tardío de todos ellos (es decir, que `CompletableFuture.allOf()` realmente
   está bloqueando el save hasta el final, y no antes).
5. **Prueba de aislamiento de payload:** confirmar que dos ramas que mutan `payload` (por ejemplo
   `binary-to-base64` y `base64-to-binary` corriendo al mismo tiempo) no interfieren entre sí —
   es decir, que cada una recibió su propia copia vía `ProcessingContext.copy()` y no comparten
   el mismo arreglo de bytes.

---

## 11. Checklist final

- [ ] `ProcessingContext` reemplaza a `FileFrame` en todos los módulos.
- [ ] `images` ya no emite el `FileFrame` `__DIRECTORY__`; usa `ImageProcessingResult`.
- [ ] `images` reutiliza `sharedExecutor()` de `ConcurrenceBase` en vez de un pool propio.
- [ ] `security` usa `putMetadata("sha256", ...)` en vez de sobrescribir el payload.
- [ ] `persistence` reutiliza `sha256` de metadata si existe, antes de recalcularlo.
- [ ] Cada plugin tiene su archivo `META-INF/services/com.pipesfilters.core.Plugin`.
- [ ] Existe `PluginRegistry.discover()` en `core`.
- [ ] Existe la interfaz marcadora `TerminalPlugin` y `Persistence` la implementa.
- [ ] `ProcessingContext` tiene `copy()` y `mergeMetadataFrom(...)`.
- [ ] Existe `ParallelOrchestrator` que hace fan-out con `CompletableFuture.supplyAsync(...)`,
      espera con `CompletableFuture.allOf(...)` y recién entonces ejecuta el `TerminalPlugin`.
- [ ] Ya no existe `pipeline.yaml` ni ningún concepto de "orden de plugins".
- [ ] Ningún módulo de plugin depende de otro módulo de plugin (solo de `core`).
- [ ] Se agregó un plugin de prueba (`NoOpPlugin`) para validar el flujo de descubrimiento sin
      tocar código existente.
- [ ] Se verificó que ninguna rama paralela mutue el `payload` de un `ProcessingContext`
      compartido (todas trabajan sobre su propia copia).
