from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
DATA_PATH = BASE_DIR / "data" / "training_logs.csv"
MODEL_PATH = BASE_DIR / "models" / "log_classifier.joblib"
HISTORY_PATH = BASE_DIR / "data" / "applied_solutions.jsonl"
