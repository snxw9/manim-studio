import asyncio
import os
import hashlib
from typing import Literal

ModelTier = Literal["fast", "standard", "smart"]

GEMINI_MODELS = {
    "fast": "gemini-2.0-flash-lite",
    "standard": "gemini-2.0-flash",
    "smart": "gemini-2.0-pro-exp-02-05",
}

OPENAI_MODELS = {
    "fast": "gpt-4o-mini",
    "standard": "gpt-4o-mini", 
    "smart": "gpt-4o",
}

_cache: dict[str, dict] = {}

def _cache_key(prompt: str) -> str:
    return hashlib.md5(prompt.strip().lower().encode()).hexdigest()

async def race_models(prompt: str, tier: ModelTier = "standard") -> dict:
    """
    Send the same prompt to Gemini and OpenAI simultaneously.
    Return whichever responds first with valid Manim code.
    """
    key = _cache_key(prompt)
    if key in _cache:
        print(f"[cache hit] {key[:8]}...")
        return {**_cache[key], "cached": True}
    
    result = await _race_models_internal(prompt, tier)
    if "code" in result and "error" not in result:
        _cache[key] = result
    return result

async def _race_models_internal(prompt: str, tier: ModelTier) -> dict:
    from ai.gemini_client import generate_manim_code as gemini_generate
    from ai.openai_client import generate as openai_generate
    from renderer.code_validator import validate_manim_code

    gemini_model = GEMINI_MODELS[tier]
    openai_model = OPENAI_MODELS[tier]

    async def try_gemini():
        try:
            code = await gemini_generate(prompt, model=gemini_model)
            validation = validate_manim_code(code)
            return {"code": validation["fixed_code"], "provider": "gemini", 
                    "model": gemini_model, "valid": validation["valid"]}
        except Exception as e:
            return {"error": str(e), "provider": "gemini"}

    async def try_openai():
        try:
            code = await openai_generate(prompt, model=openai_model)
            validation = validate_manim_code(code)
            return {"code": validation["fixed_code"], "provider": "openai",
                    "model": openai_model, "valid": validation["valid"]}
        except Exception as e:
            return {"error": str(e), "provider": "openai"}

    # Race both — first valid response wins
    tasks = [asyncio.create_task(try_gemini()), asyncio.create_task(try_openai())]
    
    errors = []
    # Use as_completed to get the first one that finishes
    for coro in asyncio.as_completed(tasks):
        result = await coro
        if "error" not in result and result.get("valid"):
            # Cancel the others
            for task in tasks:
                if not task.done():
                    task.cancel()
            return result
        elif "error" in result:
            errors.append(result)
    
    # Fallback: if none were valid, try to return one that has code at least
    for task in tasks:
        if task.done():
            res = task.result()
            if "code" in res:
                return res
    
    return {"error": f"Both providers failed: {errors}"}

def select_tier(prompt: str) -> ModelTier:
    """Choose model tier based on prompt complexity."""
    word_count = len(prompt.split())
    if word_count < 15:
        return "fast"
    elif word_count < 50:
        return "standard"
    else:
        return "smart"
