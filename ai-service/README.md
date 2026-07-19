# Service IA Monitoring OTNP

Microservice FastAPI dedie au monitoring. Il centralise l'analyse IA du PFE: le frontend appelle `monitoring-back`, puis `monitoring-back` appelle ce service Python.

## Demarrage

```powershell
cd C:\Users\lenovo\Desktop\OTNP-Portail1\ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m app.train_model
uvicorn app.main:app --host 0.0.0.0 --port 5000
```

## Endpoints

- `GET /health` : etat du service et du modele.
- `POST /api/analyze` : classification ML d'un log + recommandations.
- `POST /api/history` : memorise une solution appliquee pour enrichir les cas similaires.

Le modele est un pipeline scikit-learn `TfidfVectorizer + LinearSVC`, entraine sur des exemples de logs jBPM/OTNP puis sauvegarde dans `models/log_classifier.joblib`.
