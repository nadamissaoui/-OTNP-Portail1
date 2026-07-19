# ============================================================
# OTNP Portail - Script de déploiement Wildfly
# ============================================================
# Usage: powershell -ExecutionPolicy Bypass -File deploy.ps1
# ============================================================

param(
    [switch]$Build
)

$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$LOG  = Join-Path $ROOT "deploy.log"
$WF   = $env:WILDFLY_HOME

if (-not $WF) {
    $WF = "C:\wildfly"
    Write-Host "WILDFLY_HOME non défini. Utilisation: $WF" -ForegroundColor Yellow
}

if (-not (Test-Path $WF)) {
    Write-Host "ERREUR: Wildfly introuvable dans $WF" -ForegroundColor Red
    Write-Host "Définissez WILDFLY_HOME ou installez Wildfly dans C:\wildfly" -ForegroundColor Red
    exit 1
}

Write-Host "=== Déploiement OTNP ===" -ForegroundColor Cyan
Write-Host "Wildfly: $WF"

# 1. Build des projets
if ($Build.IsPresent) {
    Write-Host "`n[1/3] Build des projets..." -ForegroundColor Green
    
    Write-Host "  -> monitoring-back..." -NoNewline
    Push-Location (Join-Path $ROOT "monitoring-back\monitoring-back")
    & mvnw clean package -DskipTests -q
    if ($LASTEXITCODE -eq 0) { Write-Host " OK" -ForegroundColor Green } else { Write-Host " FAIL" -ForegroundColor Red; exit 1 }
    Pop-Location

    Write-Host "  -> OTNP_WS1..." -NoNewline
    Push-Location (Join-Path $ROOT "OTNP_WS1")
    & mvnw clean package -DskipTests -q
    if ($LASTEXITCODE -eq 0) { Write-Host " OK" -ForegroundColor Green } else { Write-Host " FAIL" -ForegroundColor Red; exit 1 }
    Pop-Location

    Write-Host "  -> frontend..." -NoNewline
    Push-Location (Join-Path $ROOT "monitoring-front")
    npm run build -s
    if ($LASTEXITCODE -eq 0) { Write-Host " OK" -ForegroundColor Green } else { Write-Host " FAIL" -ForegroundColor Red; exit 1 }
    Pop-Location
}

# 2. Copie des WARs/JARs dans Wildfly
Write-Host "`n[2/3] Copie vers Wildfly..." -ForegroundColor Green

$jarBack = Join-Path $ROOT "monitoring-back\monitoring-back\target\monitoring-back-*.jar"
$jarWs1  = Join-Path $ROOT "OTNP_WS1\target\OTNP_WS1-*.jar"

if (Test-Path $jarBack) {
    Copy-Item $jarBack -Destination (Join-Path $WF "standalone\deployments\") -Force
    Write-Host "  -> monitoring-back.jar copié" -ForegroundColor Green
} else {
    Write-Host "  -> monitoring-back.jar introuvable" -ForegroundColor Yellow
}

if (Test-Path $jarWs1) {
    Copy-Item $jarWs1 -Destination (Join-Path $WF "standalone\deployments\") -Force
    Write-Host "  -> OTNP_WS1.jar copié" -ForegroundColor Green
} else {
    Write-Host "  -> OTNP_WS1.jar introuvable" -ForegroundColor Yellow
}

# 3. Redémarrage Wildfly
Write-Host "`n[3/3] Redémarrage Wildfly..." -ForegroundColor Green
$wfProc = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "wildfly" -or $_.CommandLine -match "jboss" }
if ($wfProc) {
    Write-Host "  -> Arrêt Wildfly en cours..." -NoNewline
    & "$WF\bin\jboss-cli.bat" --connect command=:shutdown 2>$null
    Start-Sleep -Seconds 5
    Write-Host " OK" -ForegroundColor Green
}

Write-Host "  -> Démarrage Wildfly..." -NoNewline
Start-Process -FilePath "$WF\bin\standalone.bat" -NoNewWindow
Start-Sleep -Seconds 3
Write-Host " OK" -ForegroundColor Green

Write-Host "`n=== Déploiement terminé ===" -ForegroundColor Cyan
Write-Host "Wildfly: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:3000 (npm start)" -ForegroundColor Cyan
