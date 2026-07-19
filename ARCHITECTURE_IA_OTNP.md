# Architecture IA OTNP

Decision: un seul composant porte l'IA du PFE.

- `monitoring-front` appelle `monitoring-back` (`http://localhost:8081`).
- `monitoring-back` centralise les endpoints d'analyse, l'historique et les suggestions.
- `ai-service` est le microservice Python ML appele par `monitoring-back` sur `http://localhost:5000`.
- `OTNP_WS1` reste un web service metier SOAP/REST lie a jBPM et ne contient plus d'appel OpenAI/IA.

Cette separation evite la duplication entre WS1 et monitoring, protege les cles/secrets cote backend, et donne une vraie partie IA demonstrable: pipeline scikit-learn, entrainement, inference, cas similaires et memorisation des solutions appliquees.

## Ordre de lancement

1. Lancer MySQL, KIE Server et les services metier necessaires.
2. Lancer le service IA Python:
   ```powershell
   cd C:\Users\lenovo\Desktop\OTNP-Portail1\ai-service
   python -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   python -m app.train_model
   uvicorn app.main:app --host 0.0.0.0 --port 5000
   ```
3. Lancer `monitoring-back` sur `8081`.
4. Lancer `monitoring-front`.
5. Lancer `OTNP_WS1` sur `8089` seulement pour les workflows/metiers, pas pour l'IA.
