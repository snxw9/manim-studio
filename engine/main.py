from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, JSONResponse
from pydantic import BaseModel
import uvicorn
import os
from dotenv import load_dotenv
from ai.gemini_client import generate_manim_code
from renderer.manim_runner import run_manim, cleanup_previews
from renderer.preview_renderer import render_preview
from renderer.code_validator import validate_manim_code

# Load .env from root directory
load_dotenv(os.path.join(os.path.dirname(__file__), "..", ".env"))

app = FastAPI(title="Manim Studio Engine API")

class GenerateRequest(BaseModel):
    prompt: str
    template: str = "none"

class RenderRequest(BaseModel):
    code: str
    is_preview: bool = False
    quality: str = "1080p"
    format: str = "mp4"

class CleanupRequest(BaseModel):
    scene_name: str

@app.post("/generate")
async def generate_code(request: GenerateRequest):
    try:
        code = await generate_manim_code(request.prompt, request.template)
        return {"code": code}
    except Exception as e:
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
