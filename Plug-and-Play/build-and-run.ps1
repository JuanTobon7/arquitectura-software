#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Compila todos los módulos (incluyendo plugins externos) y ejecuta el cliente Swing.
    Uso: .\build-and-run.ps1
#>

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition

# 1. Compilar e instalar el núcleo y plugins principales
Write-Host "==> Compilando nucleo y plugins principales..." -ForegroundColor Green
& mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error compilando el nucleo y plugins principales."
    exit 1
}

# 2. Compilar plugins externos individualmente
$externals = @("external-blur", "external-compression", "external-logging", "external-extra-filters")
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

# 3. Ejecutar el cliente Swing
$clientJar = Join-Path $root "client\target\client-1.0-SNAPSHOT.jar"
if (-not (Test-Path $clientJar)) {
    Write-Error "No se encontro el JAR del cliente: $clientJar"
    exit 1
}

Write-Host "==> Ejecutando cliente Swing..." -ForegroundColor Green
& java -jar $clientJar
