from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import FileResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import uvicorn
import os
import hashlib
import base64
import glob
import re
import subprocess
import tempfile
import shutil
import asyncio
from collections import defaultdict
from datetime import date
from dotenv import load_dotenv
from ai.model_router import generate_with_fallback, select_tier
from renderer.manim_runner import run_manim, cleanup_previews
from renderer.preview_renderer import render_preview
from renderer.code_validator import validate_manim_code
from templates.library import get_template, list_templates
from templates.assets import get_asset, list_assets, get_assets_by_category

# Load .env from root directory
load_dotenv(os.path.join(os.path.dirname(__file__), "..", ".env"))

app = FastAPI(title="Manim Studio Engine API")

# Add CORS headers so Next.js can reach the engine
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Allow all for now to avoid CORS issues during debug
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Ensure outputs directory exists and mount it
OUTPUT_DIR_PATH = os.path.abspath(os.getenv("MANIM_OUTPUT_DIR", "./outputs"))
os.makedirs(OUTPUT_DIR_PATH, exist_ok=True)
app.mount("/outputs", StaticFiles(directory=OUTPUT_DIR_PATH), name="outputs")

# Persistent media cache for LaTeX and partial renders
MEDIA_CACHE = os.path.abspath(os.path.join(os.path.dirname(__file__), "media_cache"))
os.makedirs(MEDIA_CACHE, exist_ok=True)

# In-memory store: device_id -> {date, count}
_usage: dict[str, dict] = defaultdict(lambda: {"date": None, "count": 0})

FREE_DAILY_LIMIT = 20  # generations per device per day

def get_device_id(request: Request) -> str:
    # Use IP as anonymous device identifier
    ip = request.client.host
    return hashlib.md5(ip.encode()).hexdigest()[:16]

def check_rate_limit(device_id: str) -> tuple[bool, int]:
    """Returns (is_env_allowed, remaining)"""
    today = date.today().isoformat()
    usage = _usage[device_id]
    
    if usage["date"] != today:
        usage["date"] = today
        usage["count"] = 0
    
    remaining = FREE_DAILY_LIMIT - usage["count"]
    if remaining <= 0:
        return False, 0
    
    return True, remaining

def increment_usage(device_id: str):
    _usage[device_id]["count"] += 1

class GenerateRequest(BaseModel):
    prompt: str
    template: str | None = None
    user_keys: dict | None = None  # Optional user-provided API keys

class RenderRequest(BaseModel):
    code: str
    is_preview: bool = False
    quality: str = "1080p"
    format: str = "mp4"

class PreviewRequest(BaseModel):
    code: str

class CleanupRequest(BaseModel):
    scene_name: str

from ai.provider_pool import get_pool

@app.get("/pool/status")
async def pool_status():
    """Shows current provider rotation state — useful for debugging."""
    return get_pool().status()

@app.get("/templates")
async def get_templates():
    return {"templates": list_templates()}

@app.get("/templates/{template_id}")
async def get_template_code(template_id: str):
    template = get_template(template_id)
    if not template:
        raise HTTPException(status_code=404, detail=f"Template '{template_id}' not found")
    return template

@app.get("/assets")
async def get_assets():
    return {"assets": list_assets(), "by_category": get_assets_by_category()}

@app.get("/assets/{asset_id}")
async def get_asset_snippet(asset_id: str):
    asset = get_asset(asset_id)
    if not asset:
        raise HTTPException(status_code=404, detail=f"Asset '{asset_id}' not found")
    return asset

@app.get("/health")
async def health():
    return {"status": "ok", "version": "1.0"}

@app.post("/generate")
async def generate_code(request: GenerateRequest, http_request: Request):
    print(f"[generate] prompt='{request.prompt[:80]}' keys={list((request.user_keys or {}).keys())}")
    
    if not request.prompt.strip():
        raise HTTPException(status_code=400, detail="Prompt is empty")
    
    try:
        device_id = get_device_id(http_request)
        env_allowed, remaining = check_rate_limit(device_id)
        
        user_keys = request.user_keys or {}
        has_any_user_key = any(val and val.strip() for val in user_keys.values())

        if not env_allowed and not has_any_user_key:
            raise HTTPException(status_code=429, detail={
                "error": "daily_limit_reached",
                "message": f"Free tier limit of {FREE_DAILY_LIMIT} generations/day reached.",
                "fix": "Add your own API key in Settings for unlimited use.",
                "reset": "Resets at midnight."
            })

        from ai.prompt_builder import build_prompt
        full_prompt = build_prompt(request.prompt, request.template)
        
        tier = select_tier(request.prompt)
        result = await generate_with_fallback(
            full_prompt, 
            tier, 
            user_keys, 
            allow_env_fallback=env_allowed
        )
        
        print(f"[generate] result keys: {list(result.keys())}")
        
        if "error" in result and "code" not in result:
            if "Free limit reached" in result["error"]:
                raise HTTPException(status_code=429, detail={
                    "error": "daily_limit_reached",
                    "message": result["error"]
                })
            raise HTTPException(status_code=500, detail=result["error"])
        
        used_own_key = result.get("using_own_key", False)
        
        if not used_own_key:
            increment_usage(device_id)
            remaining -= 1

        return {
            "code": result.get("code", ""),
            "provider": result.get("provider"),
            "model": result.get("model"),
            "tier": tier,
            "cached": result.get("cached", False),
            "remaining_today": max(0, remaining),
            "using_own_key": used_own_key,
        }
    except HTTPException:
        raise
    except Exception as e:
        print(f"[generate] exception: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/preview")
async def preview(request: PreviewRequest):
    if not request.code.strip():
        raise HTTPException(status_code=400, detail="No code provided")
    try:
        result = await asyncio.to_thread(render_preview, request.code)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except Exception as e:
        print(f"[preview] exception: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/render")
async def render_code(request: RenderRequest):
    code = request.code.strip()
    if not code:
        raise HTTPException(status_code=400, detail="No code provided")

    # Extract class name
    match = re.search(r'class\s+(\w+)\s*\(', code)
    if not match:
        raise HTTPException(status_code=400, detail="No Scene class found in code")
    class_name = match.group(1)

    # Quality map
    quality_map = {
        "480p": "-ql",
        "720p": "-qm",
        "1080p": "-qh",
        "2160p": "-qk"
    }
    q_flag = quality_map.get(request.quality, "-qh")

    # Format
    fmt = request.format if request.format != "mov" else "mp4"

    # Use persistent file named by code hash
    code_hash = hashlib.md5(code.encode()).hexdigest()[:12]
    code_file = os.path.join(MEDIA_CACHE, f"scene_{code_hash}.py")
    with open(code_file, "w", encoding="utf-8") as f:
        f.write(code)

    cmd = [
        "manim", q_flag,
        "--format", fmt,
        "--media_dir", MEDIA_CACHE,
        "--progress_bar", "none",
        code_file,
        class_name,
    ]

    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    if result.returncode != 0:
        raise HTTPException(status_code=422, detail=f"Manim error: {result.stderr[-1000:]}")

    pattern = os.path.join(MEDIA_CACHE, "**", f"*.{fmt}")
    matches = glob.glob(pattern, recursive=True)
    if not matches:
        raise HTTPException(status_code=500, detail="Output file not found")

    orig_path = max(matches, key=os.path.getctime)
    filename = f"{class_name}_{request.quality}.{request.format}"
    final_path = os.path.join(OUTPUT_DIR_PATH, filename)
    
    shutil.copy2(orig_path, final_path)

    return {
        "videoUrl": f"/outputs/{filename}",
        "filename": filename,
        "size": os.path.getsize(final_path)
    }

@app.post("/cleanup")
async def cleanup(request: CleanupRequest):
    try:
        cleanup_previews(request.scene_name, OUTPUT_DIR_PATH)
        return {"status": "success"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
