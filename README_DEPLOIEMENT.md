# OTNP Portail - Guide de déploiement

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    monitoring-front                     │
│                   React 19 + Recharts                   │
│                    Port 3000                            │
└────────────────────┬────────────────────────────────────┘
                     │ REST
    ┌────────────────┼────────────────────┐
    ▼                ▼                     ▼
┌──────────┐  ┌──────────────┐  ┌──────────────────┐
│monitoring│  │   OTNP_WS1   │  │   ai-service     │
│  -back   │  │ Spring Boot  │  │ Python FastAPI   │
│Port 8081 │  │  Port 8089   │  │   Port 5001      │
└────┬─────┘  └──────┬───────┘  └────────┬─────────┘
     │               │                    │
     ▼               ▼                    │
┌──────────┐  ┌──────────────┐            │
│  MySQL   │  │   Wildfly    │            │
│monitoring│  │ Port 8080    │            │
│   _db    │  │ KIE Server   │            │
└──────────┘  │ jBPM IN/OUT  │            │
              └──────────────┘            │
                                          ▼
                                  ┌──────────────┐
                                  │   NVIDIA NIM  │
                                  │ build.nvidia  │
                                  │   .com (API)  │
                                  └──────────────┘
```

## Prérequis

| Outil | Version | Chemin |
|-------|---------|--------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| Node.js | 18+ | `node -v` |
| Python | 3.10+ | `python -V` |
| MySQL | 8+ | Port 3306 |
| Wildfly | 26+ | `%WILDFLY_HOME%` |

## 1. Base de données MySQL

```sql
CREATE DATABASE monitoring_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Application properties** (`monitoring-back/src/main/resources/application.properties`) :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/monitoring_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=admin
```

## 2. Wildfly / KIE Server

1. Télécharger Wildfly 26+ avec KIE Server
2. Déployer les KJARs PortaIN et PortaOUT
3. Configurer `kie-server` user :
   ```
   $WILDFLY_HOME/bin/add-user.bat -a -u wbadmin -p wbadmin -g admin,kie-server
   ```
4. Démarrer :
   ```
   $WILDFLY_HOME/bin/standalone.bat
   ```
5. Vérifier : http://localhost:8080/kie-server/services/rest/server

## 3. Services Spring Boot

### monitoring-back (port 8081)

```bash
cd monitoring-back\monitoring-back
mvnw spring-boot:run
```

API : http://localhost:8081

### OTNP_WS1 (port 8089)

```bash
cd OTNP_WS1
mvnw spring-boot:run
```

API : http://localhost:8089

## 4. AI Service Python (port 5001)

```bash
cd ai-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 5001
```

API : http://localhost:5001

### NVIDIA NIM (optionnel, recommandé)

Pour activer le chatbot intelligent (Llama 3 70B) :

1. Créer un compte gratuit sur https://build.nvidia.com
2. Générer une clé API dans le menu "API"
3. Éditer `ai-service/.nv_token` :
   ```
   nvapi-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```
   Ou définir la variable d'environnement `NV_API_KEY`

Le chatbot utilise automatiquement : Ollama (local) -> NVIDIA NIM (cloud) -> Base connaissances

### Modèle ML scikit-learn

Le modèle de classification d'erreurs (`TfidfVectorizer + LinearSVC`) s'entraîne automatiquement au premier démarrage avec `ai-service/data/training_logs.csv`.

## 5. Frontend React (port 3000)

```bash
cd monitoring-front
npm install
npm start
```

## 6. Démarrage en 1 commande

```powershell
# Mode dev (utilise mvnw spring-boot:run)
powershell -ExecutionPolicy Bypass -File start.ps1

# Build + démarrage
powershell -ExecutionPolicy Bypass -File start.ps1 -Build
```

## 7. Build production

```bash
# Backends
cd monitoring-back\monitoring-back && mvnw clean package -DskipTests
cd OTNP_WS1 && mvnw clean package -DskipTests

# Frontend
cd monitoring-front && npm run build

# Lancer les JARs
java -jar monitoring-back\monitoring-back\target\monitoring-back-0.0.1-SNAPSHOT.jar
java -jar OTNP_WS1\target\OTNP_WS1-0.0.1-SNAPSHOT.jar
```

## Points d'accès

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | Interface React |
| Monitoring API | http://localhost:8081 | API métier + KPI |
| OTNP API | http://localhost:8089 | API jBPM + SOAP |
| AI Service | http://localhost:5001 | Analyse d'erreurs + Chatbot |
| KIE Server | http://localhost:8080 | Moteur jBPM |
| MySQL | localhost:3306 | Base de données |

## Dépannage

| Problème | Cause | Solution |
|----------|-------|----------|
| KIE Server 404 | Wildfly pas démarré | Lancer `%WILDFLY_HOME%/bin/standalone.bat` |
| monitoring-back ne démarre pas | MySQL pas connecté | Vérifier credentials dans application.properties |
| npm start erreur | Node_modules manquant | `cd monitoring-front && npm install` |
| Chatbot répond "Assistant OTNP" | NVIDIA pas configuré | Mettre la clé API dans `.nv_token` |
| Prédiction : "Données insuffisantes" | < 2 mois de données | Attendre que des instances soient créées |
