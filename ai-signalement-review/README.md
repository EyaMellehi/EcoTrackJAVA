# AI Signalement Review Service

Microservice FastAPI used by EcoTrack JavaFX to review a signalement before saving it.

## Features
- Text toxicity detection with Detoxify
- Image relevance detection with CLIP
- Rejects a signalement if:
    - the text is toxic or insulting
    - the description is too vague
    - one or more images are off-topic

## Endpoints

### POST /review
Receives a multipart/form-data request with:
- titre
- description
- type
- one or more image files

Returns:
- decision: ACCEPT or REJECT
- reasons
- scores
- model

### POST /debug
Returns received keys and uploaded files for debugging.

## Setup

### 1. Create virtual environment
```bash
python -m venv .venv