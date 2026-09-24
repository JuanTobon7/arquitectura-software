#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Compilando nucleo, app, cliente y plugins principales..."
(cd "$root" && mvn clean install -DskipTests)

echo "==> Compilando plugins externos..."
for module in external-blur external-compression external-logging external-extra-filters; do
    echo "==> Compilando plugin externo: $module"
    (cd "$root/$module" && mvn clean package -DskipTests)
done

echo "==> Copiando JARs de plugins a plugins/..."
plugins_dir="$root/plugins"
mkdir -p "$plugins_dir"
rm -f "$plugins_dir"/*.jar

plugin_modules=(
    "images/target/images-1.0-SNAPSHOT.jar"
    "converter-binary/target/converter-binary-1.0-SNAPSHOT.jar"
    "converter-base64/target/converter-base64-1.0-SNAPSHOT.jar"
    "security/target/security-1.0-SNAPSHOT.jar"
    "persistence/target/persistence-1.0-SNAPSHOT.jar"
    "metadata/target/metadata-1.0-SNAPSHOT.jar"
    "external-blur/target/external-blur-1.0-SNAPSHOT.jar"
    "external-compression/target/external-compression-1.0-SNAPSHOT.jar"
    "external-logging/target/external-logging-1.0-SNAPSHOT.jar"
    "external-extra-filters/target/external-extra-filters-1.0-SNAPSHOT.jar"
)

for relative in "${plugin_modules[@]}"; do
    source="$root/$relative"
    if [[ -f "$source" ]]; then
        cp "$source" "$plugins_dir/"
        echo "    Copiado: $relative"
    else
        echo "    Advertencia: no se encontro $relative" >&2
    fi
done

echo "==> Todos los artefactos compilados. JARs de plugins en: $plugins_dir"
