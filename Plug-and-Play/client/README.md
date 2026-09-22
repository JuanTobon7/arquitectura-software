# Cliente Swing - Arquitectura Plugin

Este módulo contiene una interfaz gráfica (Swing) para ejecutar los plugins de
procesamiento de imágenes de forma visual.

## Ejecución

### Desde el JAR empaquetado

```bash
java -jar client/target/client-1.0-SNAPSHOT.jar
```

### Desde Maven

```bash
mvn -pl client exec:java -Dexec.mainClass=com.pipesfilters.client.ImageFilterClient
```

## Funcionalidades

- **Cargar Imagen**: selecciona una imagen PNG/JPG para procesar.
- **Cargar componentes (filtros)**: carga un JAR externo con plugins en tiempo de ejecución.
- **Lista de plugins**: muestra todos los plugins disponibles, incluyendo los recién cargados.
- **Ejecutar filtro seleccionado**: aplica el plugin seleccionado sobre la imagen.
- Salida de mensajes con logs y errores en tiempo real.

## Plugins cargados por defecto

Los plugins del classpath se descubren automáticamente vía `ServiceLoader`:

- `grayscale`
- `rotate`
- `sepia`
- `invert`
- `metadata`
- `security`
- `persistence`
- `binary-to-base64`
- `base64-to-binary`

## Cargar plugins externos

Copia cualquier JAR generado de los módulos `external-*` y selecciónalo con el botón
**Cargar componentes (filtros)**. Sus plugins aparecerán inmediatamente en la lista.

Ejemplo:

```bash
cp external-blur/target/external-blur-1.0-SNAPSHOT.jar result/plugins/
java -jar client/target/client-1.0-SNAPSHOT.jar
```
