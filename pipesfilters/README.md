# PipesFilters

Este proyecto ejecuta un pipeline de procesamiento de archivos a partir de una línea de comando.

## Sintaxis

```text
<ruta_o_archivo> > <comando_1> | <comando_2> | ... | <comando_n>
```

Ejemplo:

```text
"C:\Users\usuario\Pictures\Screenshots" > image png | base64 | security | save
```

## Comandos soportados

### 1. image

Aplica procesamiento de imágenes.

```text
image png
```

- Sirve para indicar el formato o extensión esperado para imágenes.
- Es el primer filtro típico para archivos de imagen.

### 2. base64

Convierte contenido binario a Base64.

```text
base64
```

### 3. binary

Convierte Base64 a binario.

```text
binary
```

### 4. security

Genera hash SHA-256 del contenido actual.

```text
security
```

### 5. save

Guarda el resultado del pipeline en la base de datos H2 local.

```text
save
```

## Orden de los filtros

El orden del pipeline debe ir de menor a mayor nivel de transformación, nunca al revés.

En este proyecto la secuencia natural es:

```text
image -> base64 -> binary -> security -> save
```

La regla general es:

- `image`: entrada / validación de imagen
- `base64`: codificación/decodificación textual
- `binary`: conversión a contenido binario
- `security`: generación de hash o seguridad
- `save`: persistencia final

No debe hacerse algo como:

```text
save | security
```

ni tampoco:

```text
binary | base64
```

porque el pipeline está pensado para ejecutar desde un nivel más primitivo hasta el final persistente.

## Ejemplos válidos

### Procesar todas las imágenes de una carpeta y guardarlas

```text
"C:\Users\usuario\Pictures\Screenshots" > image png | base64 | security | save
```

### Convertir Base64 a binario y guardar

```text
"archivo.txt" > binary | save
```

### Procesar imagen y guardarla

```text
"C:\Users\usuario\Pictures\foto.png" > image png | save
```

## Base de datos

La persistencia usa H2 embebido, guardado localmente en:

```text
pipesfilters/data/pipesfilters.mv.db
```

La URL usada por la aplicación es:

```text
jdbc:h2:./data/pipesfilters
```

Y la tabla principal es:

```sql
PROCESSED_FILES
```

## Observación importante

El pipeline debe ejecutarse sin abrir otra conexión H2 concurrente al mismo archivo de base de datos, porque H2 embebido puede bloquear el archivo `.mv.db` si hay varias conexiones abiertas al mismo tiempo.

Por eso, si quieres inspeccionar la base, es mejor cerrar la app antes de abrir el cliente H2.

## Resumen

La idea general del proyecto es:

1. indicar la entrada
2. definir el pipeline
3. ejecutar filtros en orden lógico
4. guardar el resultado final

La estructura es simple:

```text
entrada > filtro1 | filtro2 | ... | save
```

y el orden debe respetar una progresión natural del contenido hacia su persistencia final.
