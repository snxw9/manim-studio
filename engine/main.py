from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import uvicorn
import os
import hashlib
from collections import defaultdict
from datetime import date
from dotenv import load_dotenv
from pathlib import Path

from ai.model_router import generate_with_fallback, select_tier
from renderer.manim_runner import pre_validate, render_scene
from templates.library import list_templates
from templates.assets import list_assets

# Load .env from root directory
load_dotenv(os.path.join(os.path.dirname(__file__), "..", ".env"))

# Self-check on startup — catch common issues early
def startup_check():
    issues = []
    
    # Check manim is importable
    try:
        import manim
    except ImportError:
        issues.append("manim not installed — run: pip install manim")
    
    # Check API keys
    if not os.getenv("GROQ_API_KEY") and not os.getenv("GEMINI_API_KEY") and not os.getenv("OPENAI_API_KEY"):
        issues.append("No API keys found in .env — AI generation will fail")
    
    # Check output dirs exist
    Path("outputs").mkdir(exist_ok=True)
    Path("media_cache").mkdir(exist_ok=True)
    
    if issues:
        print("\n[STARTUP WARNINGS]")
        for issue in issues:
            print(f"  ⚠ {issue}")
        print()

startup_check()

app = FastAPI(title="Manim Studio Engine API")

# Add CORS headers so Next.js can reach the engine
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Ensure outputs directory exists and mount it
OUTPUT_DIR_PATH = os.path.abspath(os.getenv("MANIM_OUTPUT_DIR", "./outputs"))
os.makedirs(OUTPUT_DIR_PATH, exist_ok=True)
app.mount("/outputs", StaticFiles(directory=OUTPUT_DIR_PATH), name="outputs")

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
    user_keys: dict | None = None

class RenderRequest(BaseModel):
    code: str
    quality: str = "720p"
    format: str = "mp4"

@app.get("/health")
async def health():
    return {"status": "ok", "version": "1.0"}

@app.post("/generate")
async def generate_code(request: GenerateRequest, http_request: Request):
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
            user_keys
        )
        
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
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/render")
async def render_code(request: RenderRequest):
    if not request.code or not request.code.strip():
        raise HTTPException(status_code=400, detail="No code provided")
    error = pre_validate(request.code)
    if error:
        raise HTTPException(status_code=422, detail=error)
    try:
        import asyncio
        result = await asyncio.to_thread(
            render_scene,
            request.code,
            request.quality or "720p",
            request.format or "mp4",
        )
        return result
    except RuntimeError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

from ai.provider_pool import get_pool

@app.get("/pool/status")
async def pool_status():
    return get_pool().status()

@app.post("/render/cancel")
async def cancel_render(request: Request):
    return {"cancelled": False}

@app.get("/templates")
async def get_templates():
    return {"templates": list_templates()}

@app.get("/assets")
async def get_assets():
    return {"assets": list_assets()}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
