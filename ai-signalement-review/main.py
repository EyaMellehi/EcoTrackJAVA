from fastapi import FastAPI, Request
from starlette.datastructures import UploadFile
from typing import List
from PIL import Image
import torch
from detoxify import Detoxify
from transformers import CLIPProcessor, CLIPModel

app = FastAPI()

tox_model = Detoxify("original")

device = "cuda" if torch.cuda.is_available() else "cpu"
clip_model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32").to(device)
clip_processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")

POS_PROMPTS = [
    "trash", "garbage", "waste", "litter", "dumpster",
    "illegal dumping", "pollution", "water pollution", "air pollution",
    "burning trash", "smoke pollution", "environmental incident"
]
NEG_PROMPTS = [
    "a baby", "a child", "a person", "a face", "a selfie", "portrait",
    "food", "cat", "dog", "car", "document", "screenshot", "phone"
]

def clip_score(img: Image.Image) -> float:
    texts = POS_PROMPTS + NEG_PROMPTS
    inputs = clip_processor(text=texts, images=img, return_tensors="pt", padding=True)
    inputs = {k: v.to(device) for k, v in inputs.items()}

    with torch.no_grad():
        outputs = clip_model(**inputs)
        logits = outputs.logits_per_image.squeeze(0)

    pos = logits[:len(POS_PROMPTS)].max().item()
    neg = logits[len(POS_PROMPTS):].max().item()
    return float(pos - neg)

@app.post("/review")
async def review(request: Request):
    form = await request.form()

    titre = (form.get("titre") or "").strip()
    description = (form.get("description") or "").strip()
    type_ = (form.get("type") or "").strip()

    reasons = []
    scores = {}

    text = f"{titre}\n{description}".strip()

    tox = tox_model.predict(text)
    scores["toxicity"] = float(tox.get("toxicity", 0.0))
    scores["insult"]   = float(tox.get("insult", 0.0))
    scores["threat"]   = float(tox.get("threat", 0.0))

    if scores["toxicity"] >= 0.30 or scores["insult"] >= 0.30 or scores["threat"] >= 0.25:
        reasons.append("insultes_ou_toxicite")

    if len(description) < 20:
        reasons.append("texte_trop_vague")

    files: List[UploadFile] = []
    for key in form.keys():
        for item in form.getlist(key):
            if isinstance(item, UploadFile):
                files.append(item)

    img_scores = []
    for f in files:
        try:
            f.file.seek(0)
            img = Image.open(f.file).convert("RGB")
            s = clip_score(img)
            img_scores.append(s)
        except Exception:
            reasons.append("photo_hors_sujet")

    scores["images_count"] = len(img_scores)

    if img_scores:
        scores["clip_score_avg"] = float(sum(img_scores) / len(img_scores))
        scores["clip_score_min"] = float(min(img_scores))

        if scores["clip_score_min"] < 0.10:
            reasons.append("photo_hors_sujet")
    else:
        scores["clip_score_avg"] = None
        scores["clip_score_min"] = None

    decision = "ACCEPT" if not reasons else "REJECT"

    print("DEBUG_KEYS:", list(form.keys()))
    print("DEBUG_FILES:", [getattr(x, "filename", None) for x in files])
    print("DEBUG:", {"scores": scores, "reasons": reasons})

    return {"decision": decision, "reasons": reasons, "scores": scores, "model": "detoxify+clip"}

@app.post("/debug")
async def debug_endpoint(request: Request):
    form = await request.form()
    keys = list(form.keys())
    files = []
    for k in keys:
        for item in form.getlist(k):
            if hasattr(item, "filename"):
                files.append({"field": k, "filename": item.filename})
    return {"keys": keys, "files": files}