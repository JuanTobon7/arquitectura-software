#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Compila todos los modulos y deja todos los JARs de plugins en la carpeta plugins/.
    Uso: .\build-all.ps1
#>

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition

# 1. Compilar e instalar el nucleo, app, cliente y plugins principales
Write-Host "==> Compilando nucleo, app, cliente y plugins principales..." -ForegroundColor Green
& mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error compilando el nucleo y plugins principales."
    exit 1
}

# 2. Compilar plugins externos individualmente
$externals = @("external-blur", "external-compression", "external-logging", "external-extra-filters", "external-metadata")
foreach ($module in $externals) {
    Write-Host "==> Compilando plugin externo: $module" -ForegroundColor Green
    $path = Join-Path $root $module
    Set-Location $path
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Error compilando el plugin externo $module."
        exit 1
    }
}

Set-Location $root

# 3. Copiar todos los JARs de plugins a una sola carpeta
$pluginsDir = Join-Path $root "plugins"
if (Test-Path $pluginsDir) {
    Remove-Item -Path "$pluginsDir\*" -Include "*.jar" -Force
} else {
    New-Item -ItemType Directory -Path $pluginsDir | Out-Null
}

$pluginModules = @(
    "images\target\images-1.0-SNAPSHOT.jar",
    "converter-binary\target\converter-binary-1.0-SNAPSHOT.jar",
    "converter-base64\target\converter-base64-1.0-SNAPSHOT.jar",
    "security\target\security-1.0-SNAPSHOT.jar",
    "persistence\target\persistence-1.0-SNAPSHOT.jar",
    "external-blur\target\external-blur-1.0-SNAPSHOT.jar",
    "external-compression\target\external-compression-1.0-SNAPSHOT.jar",
    "external-logging\target\external-logging-1.0-SNAPSHOT.jar",
    "external-extra-filters\target\external-extra-filters-1.0-SNAPSHOT.jar",
    "external-metadata\target\external-metadata-1.0-SNAPSHOT.jar"
)

foreach ($relative in $pluginModules) {
    $source = Join-Path $root $relative
    if (Test-Path $source) {
        Copy-Item -Path $source -Destination $pluginsDir -Force
        Write-Host "    Copiado: $relative" -ForegroundColor DarkGray
    } else {
        Write-Warning "No se encontro el JAR: $relative"
    }
}

Write-Host "==> Todos los artefactos compilados. JARs de plugins en: $pluginsDir" -ForegroundColor Green
