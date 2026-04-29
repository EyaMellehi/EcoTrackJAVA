# EcoTrack – AI Face Login (FastAPI)

Ce dossier contient un microservice Python (FastAPI) utilisé par EcoTrack (Symfony) pour l’authentification par visage.

## 1) Prérequis
- Python 3.11.x (recommandé)
- Symfony EcoTrack lancé sur http://127.0.0.1:8000
- API FastAPI sur http://127.0.0.1:5000

## 2) Installation (Windows)
Ouvrir PowerShell dans `EcoTrack/ai-face-api/` :

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt

## 3) Lancer l’API
cd EcoTrack\ai-face-api
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
python -m uvicorn face_api:app --host 127.0.0.1 --port 5000 --reload