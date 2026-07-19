from __future__ import annotations

import csv
import json
import os
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

import joblib
import requests
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sklearn.metrics.pairwise import cosine_similarity

from .config import DATA_PATH, HISTORY_PATH, MODEL_PATH
from .knowledge_base import node_specific_steps, solution_for
from .train_model import train
from .chat_service import chat_service

app = FastAPI(title="OTNP Monitoring AI Service", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

_model = None
_training_cache: List[Dict[str, str]] = []


class AnalyzeRequest(BaseModel):
    log_content: str = Field(..., min_length=3)
    process_id: Optional[str] = None
    workflow_type: Optional[str] = None
    node_name: Optional[str] = None
    node_type: Optional[str] = None


class AppliedSolution(BaseModel):
    process_id: Optional[str] = None
    error_type: Optional[str] = None
    message: Optional[str] = None
    workflow_type: Optional[str] = None
    timestamp: Optional[str] = None
    solution: str


def load_model():
    global _model
    if _model is None:
        if not MODEL_PATH.exists():
            train()
        _model = joblib.load(MODEL_PATH)
    return _model


def load_training_cache() -> List[Dict[str, str]]:
    global _training_cache
    if _training_cache:
        return _training_cache
    with DATA_PATH.open("r", encoding="utf-8") as csv_file:
        _training_cache = list(csv.DictReader(csv_file))
    return _training_cache


def confidence_for(model, text: str, predicted: str) -> float:
    classifier = model.named_steps["classifier"]
    if hasattr(classifier, "predict_proba"):
        probabilities = model.predict_proba([text])[0]
        classes = list(classifier.classes_)
        if predicted in classes:
            return float(probabilities[classes.index(predicted)])
    return 0.62


def similar_examples(model, text: str, limit: int = 3) -> List[Dict[str, Any]]:
    rows = load_training_cache()
    if not rows:
        return []
    vectorizer = model.named_steps["tfidf"]
    matrix = vectorizer.transform([row["log_content"] for row in rows])
    query = vectorizer.transform([text])
    scores = cosine_similarity(query, matrix)[0]
    ranked = sorted(enumerate(scores), key=lambda item: item[1], reverse=True)[:limit]
    return [
        {
            "process_id": "training-case",
            "error_type": rows[index]["error_type"],
            "message": rows[index]["log_content"],
            "applied_solution": None,
            "similarity": round(float(score), 3),
        }
        for index, score in ranked
        if score > 0
    ]


def load_nv_token() -> Optional[str]:
    token = os.environ.get("NV_API_KEY")
    if token:
        return token
    try:
        nv_path = os.path.join(os.path.dirname(__file__), "..", ".nv_token")
        with open(nv_path) as f:
            return f.read().strip()
    except Exception:
        return None


def nvidia_solution(log_text: str, error_type: str, cause: str, kb_steps: list, node_name: str = None) -> Optional[str]:
    token = load_nv_token()
    if not token:
        return None
    try:
        prompt = (
            f"Tu es un expert OTNP. Reponds en 3-4 actions concretes MAX.\n\n"
            f"Contexte: noeud={node_name or 'N/A'}, type_erreur={error_type}\n"
            f"Erreur: {log_text[:300]}\n"
            f"Cause: {cause}\n\n"
            f"Donne la solution en 3-4 puces courtes, actionnables."
        )
        resp = requests.post(
            "https://integrate.api.nvidia.com/v1/chat/completions",
            headers={
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/json"
            },
            json={
                "model": "meta/llama-3.2-3b-instruct",
                "messages": [{"role": "user", "content": prompt}],
                "temperature": 0.1,
                "max_tokens": 300
            },
            timeout=30
        )
        if resp.status_code == 200:
            data = resp.json()
            return data["choices"][0]["message"]["content"].strip()
    except Exception as e:
        print(f"NVIDIA analyze error: {e}")
    return None


def load_resolved_history() -> List[Dict[str, Any]]:
    if not HISTORY_PATH.exists():
        return []
    results = []
    try:
        with HISTORY_PATH.open("r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line:
                    try:
                        results.append(json.loads(line))
                    except json.JSONDecodeError:
                        continue
    except Exception:
        return []
    return results


def find_similar_resolved(log_text: str, limit: int = 3) -> List[Dict[str, Any]]:
    history = load_resolved_history()
    if not history:
        return []
    log_lower = log_text.lower()
    scored = []
    for entry in history:
        msg = (entry.get("message", "") or "").lower()
        score = 0
        words = set(log_lower.split())
        if words:
            common = sum(1 for w in words if w in msg and len(w) > 3)
            score = common / len(words) if words else 0
        if entry.get("error_type", "").lower() in log_lower:
            score += 0.3
        if score > 0.05:
            scored.append((score, entry))
    scored.sort(key=lambda x: x[0], reverse=True)
    return [
        {
            "process_id": s.get("process_id", "inconnu"),
            "error_type": s.get("error_type", "N/A"),
            "message": s.get("message", "")[:200],
            "applied_solution": s.get("solution", ""),
            "similarity": round(score, 3),
            "resolved_at": s.get("saved_at", s.get("timestamp", "N/A")),
        }
        for score, s in scored[:limit]
    ]


@app.get("/health")
def health() -> Dict[str, Any]:
    return {"status": "UP", "modelReady": MODEL_PATH.exists(), "modelPath": str(MODEL_PATH)}


@app.post("/api/analyze")
def analyze(request: AnalyzeRequest) -> List[Dict[str, Any]]:
    model = load_model()
    text = " ".join(filter(None, [request.workflow_type, request.node_name, request.node_type, request.log_content]))
    predicted = str(model.predict([text])[0])
    confidence = confidence_for(model, text, predicted)

    node_name = (request.node_name or "").lower()
    node_type = (request.node_type or "").lower()

    # Override UnknownWorkflowError with node-based heuristics
    if predicted == "UnknownWorkflowError":
        if "signal" in node_name or "timer" in node_name:
            predicted = "JmsSignalTimeout"
        elif "human" in node_type or "manual" in node_type:
            predicted = "HumanTaskBlocked"
        elif "soap" in node_name or "integration" in node_name:
            predicted = "SoapIntegration"
        elif "eligibilite" in node_name or "eligibility" in node_name:
            predicted = "BusinessRuleRejection"
        elif "kie" in node_name or "connection" in node_name:
            predicted = "KieServerConnection"
        elif "sql" in node_name or "bdd" in node_name or "database" in node_name:
            predicted = "SQLException"

    kb = solution_for(predicted)
    steps = node_specific_steps(request.node_name, request.node_type) + list(kb["steps"])

    nv_solution = nvidia_solution(request.log_content, predicted, kb["cause"], steps, request.node_name)

    return [{
        "success": True,
        "model": "scikit-learn + NVIDIA NIM",
        "error_type": predicted,
        "severity": "HIGH" if confidence >= 0.75 else "MEDIUM",
        "probable_cause": kb["cause"],
        "confidence_score": round(confidence, 3),
        "suggestions": [{
            "suggestion": nv_solution or "\n".join(f"{i+1}. {s}" for i, s in enumerate(steps)),
            "google_search_url": f"https://www.google.com/search?q=jBPM+{predicted}",
            "source": "NVIDIA NIM" if nv_solution else "Base de connaissance OTNP",
            "confidence_score": round(confidence, 3),
        }],
        "similar_errors": similar_examples(model, text),
        "resolved_cases": find_similar_resolved(request.log_content),
        "metadata": {
            "process_id": request.process_id,
            "workflow_type": request.workflow_type,
            "node_name": request.node_name,
            "node_type": request.node_type,
        },
    }]


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1)
    history: Optional[List[Dict[str, Any]]] = None
    context: Optional[Dict[str, Any]] = None

@app.post("/api/chat")
def chat_endpoint(req: ChatRequest) -> Dict[str, Any]:
    return chat_service.chat(req.message, req.history or [], req.context)

@app.post("/api/history")
def save_history(solution: AppliedSolution) -> Dict[str, Any]:
    HISTORY_PATH.parent.mkdir(parents=True, exist_ok=True)
    payload = solution.model_dump()
    payload["saved_at"] = datetime.now(timezone.utc).isoformat()
    with HISTORY_PATH.open("a", encoding="utf-8") as history_file:
        history_file.write(json.dumps(payload, ensure_ascii=True) + "\n")
    return {"success": True, "message": "Solution memorisee"}
