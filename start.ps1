# ============================================================
# OTNP Portail - Script de demarrage complet (1 commande)
# ============================================================
# Utilisation: powershell -ExecutionPolicy Bypass -File start.ps1
# ============================================================

param(
    [switch]$Build
)

$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$LOG  = Join-Path $ROOT "start.log"

function Log { param([string]$Msg) $t = Get-Date -Format "HH:mm:ss"; "$t $Msg" | Tee-Object -FilePath $LOG -Append }

function Wait-Url {
    param([string]$Url, [int]$Timeout = 60, [string]$Name = "service")
    $elapsed = 0
    while ($elapsed -lt $Timeout) {
        try { $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { Log "$Name OK"; return $true } } catch {}
        Start-Sleep -Seconds 2; $elapsed += 2
    }
    Log "ATTENTION: $Name non disponible apres ${Timeout}s"
    return $false
}

# ─── Prérequis ──────────────────────────────────────────────
Log "=== OTNP Portail - Demarrage ==="
Log "Verification des prerequis..."

$java = Get-Command "java" -ErrorAction SilentlyContinue
if (-not $java) { Log "ERREUR: Java 17+ introuvable. Installez Java JDK 17."; exit 1 }
$jv = & java -version 2>&1; Log "Java: $jv"

$node = Get-Command "node" -ErrorAction SilentlyContinue
if (-not $node) { Log "ERREUR: Node.js introuvable."; exit 1 }
Log "Node: $(node -v)"

$mvn = Get-Command "mvn" -ErrorAction SilentlyContinue
if (-not $mvn) { Log "ERREUR: Maven (mvn) introuvable."; exit 1 }
Log "Maven: $(mvn -v | Select-String 'Apache Maven' | ForEach-Object { $_.ToString().Split('/')[0].Trim() })"

$python = Get-Command "python" -ErrorAction SilentlyContinue
if (-not $python) { Log "ERREUR: Python introuvable."; exit 1 }
Log "Python: $(python -V 2>&1)"

# ─── 1. Wildfly / KIE Server ───────────────────────────────
$WF = $env:WILDFLY_HOME
if (-not $WF) { $WF = "C:\wildfly" }
if (Test-Path "$WF\bin\standalone.bat") {
    Log "Demarrage Wildfly depuis $WF ..."
    Start-Process -FilePath "$WF\bin\standalone.bat" -NoNewWindow
    Wait-Url "http://localhost:8080" -Timeout 120 -Name "Wildfly"
} else {
    Log "Wildfly non trouve dans $WF. Ignore (demarrez-le manuellement si necessaire)."
}

# ─── 2. Build (optionnel) ───────────────────────────────────
if ($Build.IsPresent) {
    Log "Build des projets Maven..."
    Push-Location (Join-Path $ROOT "monitoring-back\monitoring-back")
    & mvn clean package -DskipTests; if ($LASTEXITCODE -ne 0) { Log "ERREUR build monitoring-back"; Pop-Location; exit 1 }
    Pop-Location

    Push-Location (Join-Path $ROOT "OTNP_WS1")
    & mvn clean package -DskipTests; if ($LASTEXITCODE -ne 0) { Log "ERREUR build OTNP_WS1"; Pop-Location; exit 1 }
    Pop-Location

    Push-Location (Join-Path $ROOT "monitoring-front")
    npm install; npm run build; if ($LASTEXITCODE -ne 0) { Log "ERREUR build frontend"; Pop-Location; exit 1 }
    Pop-Location

    Log "Build termine."
}

# ─── 3. ai-service (Python FastAPI) ─────────────────────────
Log "Demarrage ai-service (Python FastAPI port 5001)..."
$AI_DIR = Join-Path $ROOT "ai-service"
Push-Location $AI_DIR
$aiProc = Start-Process -FilePath "python" -ArgumentList "-m uvicorn app.main:app --host 0.0.0.0 --port 5001" -NoNewWindow -PassThru
Pop-Location
Wait-Url "http://localhost:5001/health" -Timeout 30 -Name "ai-service"

# ─── 4. OTNP_WS1 (Spring Boot port 8089) ──────────────────
Log "Demarrage OTNP_WS1 (Spring Boot port 8089)..."
$WS1_DIR = Join-Path $ROOT "OTNP_WS1"
Push-Location $WS1_DIR
$ws1Proc = Start-Process -FilePath "mvnw" -ArgumentList "spring-boot:run" -NoNewWindow -PassThru
Pop-Location
Wait-Url "http://localhost:8089/api/monitoring/statistics/performance" -Timeout 120 -Name "OTNP_WS1"

# ─── 5. monitoring-back (Spring Boot port 8081) ────────────
Log "Demarrage monitoring-back (Spring Boot port 8081)..."
$MB_DIR = Join-Path $ROOT "monitoring-back\monitoring-back"
Push-Location $MB_DIR
$mbProc = Start-Process -FilePath "mvnw" -ArgumentList "spring-boot:run" -NoNewWindow -PassThru
Pop-Location
Wait-Url "http://localhost:8081/api/monitoring/kpis" -Timeout 120 -Name "monitoring-back"

# ─── 6. monitoring-front (React port 3000) ─────────────────
Log "Demarrage monitoring-front (React port 3000)..."
$FRONT_DIR = Join-Path $ROOT "monitoring-front"
Push-Location $FRONT_DIR
$frontProc = Start-Process -FilePath "npx" -ArgumentList "react-scripts start" -NoNewWindow -PassThru
Pop-Location
Wait-Url "http://localhost:3000" -Timeout 60 -Name "React"

# ─── Résumé ─────────────────────────────────────────────────
Log ""
Log "============================================"
Log "  OTNP Portail - DEMARRE !"
Log "============================================"
Log "  Frontend http://localhost:3000"
Log "  Monitoring API http://localhost:8081"
Log "  OTNP_WS1 API   http://localhost:8089"
Log "  AI Service     http://localhost:5001"
Log "  Wildfly        http://localhost:8080"
Log "============================================"
Log ""
Log "Pour arreter: fermez les fenetres ou tuez les processus Java/Python/npm"
