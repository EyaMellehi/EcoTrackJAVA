from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from deepface import DeepFace
import os, shutil, tempfile, re

app = FastAPI()

FACE_DB_DIR = r"C:\Users\eyame\IdeaProjects\EcoTrack\var\faces"
MODEL_NAME = "VGG-Face"
DETECTOR = "opencv"

def save_upload_to_temp(upload: UploadFile) -> str:
    suffix = os.path.splitext(upload.filename)[1].lower() or ".jpg"
    fd, tmp_path = tempfile.mkstemp(suffix=suffix)
    os.close(fd)
    with open(tmp_path, "wb") as f:
        shutil.copyfileobj(upload.file, f)
    return tmp_path

@app.post("/verify")
def verify(file: UploadFile = File(...), reference_path: str = Form(...)):
    tmp = None
    try:
        tmp = save_upload_to_temp(file)
        result = DeepFace.verify(
            img1_path=tmp,
            img2_path=reference_path,
            model_name=MODEL_NAME,
            detector_backend=DETECTOR,
            enforce_detection=False,   
            distance_metric="cosine"
        )
        return JSONResponse({
            "ok": True,
            "verified": bool(result.get("verified")),
            "distance": float(result.get("distance", 999)),
            "raw": result
        })
    except Exception as e:
        return JSONResponse({"ok": False, "error": str(e)}, status_code=200)
    finally:
        if tmp and os.path.exists(tmp):
            os.remove(tmp)

@app.post("/identify")
def identify(file: UploadFile = File(...)):
    """
    Compare la photo reçue avec tous les fichiers user_*.jpg dans FACE_DB_DIR
    Retourne matched + user_id si distance < threshold
    """
    tmp = None
    try:
        if not os.path.isdir(FACE_DB_DIR):
            return JSONResponse({"ok": False, "error": "FACE_DB_DIR not found"}, status_code=500)

        tmp = save_upload_to_temp(file)

        dfs = DeepFace.find(
            img_path=tmp,
            db_path=FACE_DB_DIR,
            model_name=MODEL_NAME,
            detector_backend=DETECTOR,
            enforce_detection=False,
            distance_metric="cosine"
        )

        if not dfs or len(dfs) == 0 or dfs[0].empty:
            return JSONResponse({"ok": True, "matched": False})

        best = dfs[0].iloc[0]
        identity_path = str(best["identity"])
        distance = float(best["distance"])

        # (plus petit = plus strict)


        
        threshold = 0.50

        m = re.search(r"user_(\d+)\.", os.path.basename(identity_path))
        if not m:
            return JSONResponse({"ok": False, "error": "Bad filename format"}, status_code=500)

        user_id = int(m.group(1))
        matched = distance <= threshold

        return JSONResponse({
            "ok": True,
            "matched": matched,
            "user_id": user_id if matched else None,
            "distance": distance,
            "threshold": threshold,
            "identity": identity_path
        })

    except Exception as e:
        return JSONResponse({"ok": False, "error": str(e)}, status_code=200)
    finally:
        if tmp and os.path.exists(tmp):
            os.remove(tmp)