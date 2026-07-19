from __future__ import annotations

import csv
from pathlib import Path

import joblib
from sklearn.calibration import CalibratedClassifierCV
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import classification_report
from sklearn.pipeline import Pipeline
from sklearn.svm import LinearSVC

from .config import DATA_PATH, MODEL_PATH


def load_training_data(path: Path = DATA_PATH):
    texts, labels = [], []
    with path.open("r", encoding="utf-8") as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            texts.append(row["log_content"])
            labels.append(row["error_type"])
    return texts, labels


def build_pipeline() -> Pipeline:
    classifier = CalibratedClassifierCV(LinearSVC(class_weight="balanced"), cv=2)
    return Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1, max_features=4000)),
        ("classifier", classifier),
    ])


def train() -> Pipeline:
    texts, labels = load_training_data()
    model = build_pipeline()
    model.fit(texts, labels)
    MODEL_PATH.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(model, MODEL_PATH)
    predictions = model.predict(texts)
    print(classification_report(labels, predictions, zero_division=0))
    print(f"Model saved to {MODEL_PATH}")
    return model


if __name__ == "__main__":
    train()
