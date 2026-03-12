import asyncio
import hashlib
import json
import os
from pathlib import Path
from typing import Literal

ModelTier = Literal["fast", "standard", "smart"]

CACHE_DIR = Path(__file__).parent.parent / "cache"
CACHE_DIR.mkdir(exist_ok=True)

# Ordered fallback chains — tries each model in order until one works
FALLBACK_CHAIN = {
    "fast": [
        {"provider": "groq",   "model": "llama-3.1-8b-instant"},
        {"provider": "gemini", "model": "gemini-2.0-flash"},
        {"provider": "openai", "model": "gpt-4o-mini"},
    ],
    "standard": [
        {"provider": "groq",   "model": "llama-3.3-70b-versatile"},
        {"provider": "gemini", "model": "gemini-2.0-flash"},
        {"provider": "openai", "model": "gpt-4o-mini"},
    ],
    "smart": [
        {"provider": "groq",   "model": "llama-3.3-70b-versatile"},
        {"provider": "gemini", "model": "gemini-2.5-pro-preview-05-06"},
        {"provider": "openai", "model": "gpt-4o"},
    ],
}

def _cache_key(prompt: str) -> str:
    return hashlib.md5(prompt.strip().lower().encode()).hexdigest()

def _load_cache(key: str) -> dict | None:
    path = CACHE_DIR / f"{key}.json"
    if path.exists():
        try:
            return json.loads(path.read_text())
        except:
            return None
    return None

def _save_cache(key: str, result: dict):
    path = CACHE_DIR / f"{key}.json"
    # Don't cache errors
    if "error" in result:
        return
    path.write_text(json.dumps(result))

def select_tier(prompt: str) -> ModelTier:
    word_count = len(prompt.split())
    if word_count < 15:
        return "fast"
    elif word_count < 50:
        return "standard"
    else:
        return "smart"

def is_quota_error(error_str: str) -> bool:
    quota_signals = ["429", "quota", "rate limit", "insufficient_quota", "exceeded"]
    return any(s.lower() in error_str.lower() for s in quota_signals)

def is_auth_error(error_str: str) -> bool:
    return any(s in error_str.lower() for s in ["401", "403", "api key", "authentication", "invalid_api_key"])

def get_api_key(provider: str, request_key: str | None = None) -> str | None:
    """
    Priority order:
    1. Key passed directly in the API request (user brings their own key)
    2. Key from engine .env file (developer's key — personal/hosted use)
    3. None — will skip this provider
    """
    if request_key:
        return request_key
    
    env_map = {
        "groq":   "GROQ_API_KEY",
        "gemini": "GEMINI_API_KEY",
        "openai": "OPENAI_API_KEY",
    }
    
    env_key = env_map.get(provider)
    if env_key:
        return os.getenv(env_key)
    
    return None

async def generate_with_fallback(
    prompt: str, 
    tier: ModelTier = "standard",
    user_keys: dict | None = None,  # {"groq": "key...", "gemini": "key..."}
) -> dict:
    """Try each model in the fallback chain sequentially. Stop at first success."""
    from ai.gemini_client import generate as gemini_generate
    from ai.openai_client import generate as openai_generate
    try:
        from ai.groq_client import generate as groq_generate
    except ImportError:
        groq_generate = None
    from renderer.code_validator import validate_manim_code

    user_keys = user_keys or {}

    # Check disk cache first
    key = _cache_key(prompt)
    cached = _load_cache(key)
    if cached:
        print(f"[cache] Hit — skipping network entirely")
        return {**cached, "cached": True}

    chain = FALLBACK_CHAIN[tier]
    last_error = None

    for entry in chain:
        provider = entry["provider"]
        model = entry["model"]

        # Resolve key — user key takes priority over env key
        api_key = get_api_key(provider, user_keys.get(provider))
        
        if not api_key and provider != "ollama":
            continue  # No key available for this provider — skip it

        try:
            print(f"[model_router] Trying {provider}/{model}...")

            if provider == "groq" and groq_generate:
                code = await groq_generate(prompt, model=model, api_key=api_key)
            elif provider == "gemini":
                code = await gemini_generate(prompt, model=model, api_key=api_key)
            elif provider == "openai":
                code = await openai_generate(prompt, model=model, api_key=api_key)
            else:
                continue

            validation = validate_manim_code(code)
            result = {
                "code": validation["fixed_code"],
                "provider": provider,
                "model": model,
                "valid": validation["valid"],
                "warnings": validation.get("errors", []),
            }

            # Cache successful results
            _save_cache(key, result)
            print(f"[model_router] Success with {provider}/{model}")
            return result

        except Exception as e:
            err_str = str(e)
            last_error = {"provider": provider, "model": model, "error": err_str}

            if is_auth_error(err_str):
                print(f"[model_router] Auth error for {provider} — check your API key")
                # Skip all models from this provider
                chain = [m for m in chain if m["provider"] != provider]
                continue

            if is_quota_error(err_str):
                print(f"[model_router] Quota exceeded for {provider}/{model} — trying next...")
                continue

            # Unknown error — still try next
            print(f"[model_router] Error with {provider}/{model}: {err_str[:100]}")
            continue

    # All models failed
    return {
        "error": _build_user_error(last_error),
        "technical_detail": str(last_error),
    }

def _build_user_error(last_error: dict | None) -> str:
    if not last_error:
        return "All AI providers failed. Check your API keys in engine/.env or Settings."
    
    err = last_error.get("error", "")
    
    if is_quota_error(err):
        return (
            "API quota exhausted on all providers. Options:\n"
            "1. Wait for quota reset\n"
            "2. Add billing to your Groq/Gemini/OpenAI account\n"
            "3. Add your own API key in Settings for unlimited use."
        )
    if is_auth_error(err):
        return "Invalid API key. Check engine/.env or your own keys in Settings."
    
    return f"Generation failed: {err[:200]}"

# Keep old name as alias so existing imports don't break
async def race_models(prompt: str, tier: ModelTier = "standard", user_keys: dict | None = None) -> dict:
    return await generate_with_fallback(prompt, tier, user_keys)
