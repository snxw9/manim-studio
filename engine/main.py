from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import FileResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
import os
import hashlib
from collections import defaultdict
from datetime import date
from dotenv import load_dotenv
from ai.model_router import generate_with_fallback, select_tier
from renderer.manim_runner import run_manim, cleanup_previews
from renderer.preview_renderer import render_preview
from renderer.code_validator import validate_manim_code

# Load .env from root directory
load_dotenv(os.path.join(os.path.dirname(__file__), "..", ".env"))

app = FastAPI(title="Manim Studio Engine API")

# Add CORS headers so Next.js can reach the engine
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory store: device_id -> {date, count}
_usage: dict[str, dict] = defaultdict(lambda: {"date": None, "count": 0})

FREE_DAILY_LIMIT = 10  # generations per device per day

def get_device_id(request: Request) -> str:
    # Use IP as anonymous device identifier
    ip = request.client.host
    return hashlib.md5(ip.encode()).hexdigest()[:16]

def check_rate_limit(device_id: str, has_own_key: bool) -> tuple[bool, int]:
    """Returns (is_allowed, remaining)"""
    if has_own_key:
        return True, 999  # Own key = unlimited
    
    today = date.today().isoformat()
    usage = _usage[device_id]
    
    if usage["date"] != today:
        usage["date"] = today
        usage["count"] = 0
    
    remaining = FREE_DAILY_LIMIT - usage["count"]
    if remaining <= 0:
        return False, 0
    
    usage["count"] += 1
    return True, remaining - 1

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

@app.get("/health")
async def health():
    return {"status": "ok", "version": "1.0"}

@app.post("/generate")
async def generate_code(request: GenerateRequest, http_request: Request):
    try:
        user_keys = request.user_keys or {}
        has_own_key = bool(user_keys.get('groq') or user_keys.get('gemini') or user_keys.get('openai'))
        
        device_id = get_device_id(http_request)
        allowed, remaining = check_rate_limit(device_id, has_own_key)
        
        if not allowed:
            raise HTTPException(status_code=429, detail={
                "error": "daily_limit_reached",
                "message": f"Free tier limit of {FREE_DAILY_LIMIT} generations/day reached.",
                "fix": "Add your own API key in Settings for unlimited use.",
                "reset": "Resets at midnight."
            })
        
        tier = select_tier(request.prompt)
        result = await generate_with_fallback(request.prompt, tier, user_keys)
        
        if "error" in result and "code" not in result:
            raise HTTPException(status_code=500, detail=result["error"])
        
        return {
            "code": result["code"],
            "provider": result.get("provider"),
            "model": result.get("model"),
            "tier": tier,
            "cached": result.get("cached", False),
            "remaining_today": remaining,
            "using_own_key": has_own_key,
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/preview")
async def preview(request: PreviewRequest):
    try:
        # Validate code before previewing
        validation = validate_manim_code(request.code)
        if not validation["valid"]:
            return JSONResponse(status_code=422, content={
                "error": "Code has errors — fix before previewing",
                "details": validation["errors"]
            })

        output_path = run_manim(
            validation["fixed_code"],
            preview=True,
            quality="480p",
            fmt="mp4"
        )
        
        if not os.path.exists(output_path):
            raise HTTPException(status_code=500, detail=f"Preview file not found at {output_path}")
            
        return {"videoPath": os.path.abspath(output_path)}
    except Exception as e:
        print(f"Error in /preview: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/render")
async def render_code(request: RenderRequest):
    try:
        # Validate code before rendering
        validation = validate_manim_code(request.code)
        if not validation["valid"]:
            return JSONResponse(status_code=422, content={
                "error": "Invalid Manim code",
                "details": validation["errors"],
                "suggestion": "Use the /generate endpoint to auto-fix this code"
            })

        print(f"Render requested: quality={request.quality}, format={request.format}, is_preview={request.is_preview}")
        output_path = run_manim(
            validation["fixed_code"], 
            preview=request.is_preview, 
            quality=request.quality, 
            fmt=request.format
        )
        
        if not os.path.exists(output_path):
             raise HTTPException(status_code=500, detail=f"Rendered file not found at {output_path}")

        media_type = "video/mp4"
        if request.format == "gif":
            media_type = "image/gif"
        elif request.format == "webm":
            media_type = "video/webm"
        elif request.format == "mov":
            media_type = "video/quicktime"

        return FileResponse(output_path, media_type=media_type, filename=os.path.basename(output_path))
    except Exception as e:
        print(f"Error in /render: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/cleanup")
async def cleanup(request: CleanupRequest):
    try:
        output_dir = os.path.abspath(os.getenv("MANIM_OUTPUT_DIR", "./outputs"))
        cleanup_previews(request.scene_name, output_dir)
        return {"status": "success"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
