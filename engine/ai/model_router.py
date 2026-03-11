import asyncio
import hashlib
import os
from typing import Literal

ModelTier = Literal["fast", "standard", "smart"]

# Ordered fallback chains — tries each model in order until one works
FALLBACK_CHAIN = {
    "fast": [
        {"provider": "gemini", "model": "gemini-2.0-flash"},
        {"provider": "gemini", "model": "gemini-1.5-flash"},
        {"provider": "openai",  "model": "gpt-4o-mini"},
        {"provider": "openai",  "model": "gpt-3.5-turbo"},
    ],
    "standard": [
        {"provider": "gemini", "model": "gemini-2.0-flash"},
        {"provider": "gemini", "model": "gemini-1.5-flash"},
        {"provider": "openai",  "model": "gpt-4o-mini"},
        {"provider": "openai",  "model": "gpt-3.5-turbo"},
    ],
    "smart": [
        {"provider": "gemini", "model": "gemini-2.5-pro-preview-05-06"},
        {"provider": "gemini", "model": "gemini-2.0-flash"},
        {"provider": "openai",  "model": "gpt-4o"},
        {"provider": "openai",  "model": "gpt-4o-mini"},
    ],
}

# In-memory cache to avoid re-generating identical prompts
_cache: dict[str, dict] = {}

def _cache_key(prompt: str) -> str:
    return hashlib.md5(prompt.strip().lower().encode()).hexdigest()

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

async def generate_with_fallback(prompt: str, tier: ModelTier = "standard") -> dict:
    """Try each model in the fallback chain sequentially. Stop at first success."""
    from ai.gemini_client import generate as gemini_generate
    from ai.openai_client import generate as openai_generate
    from renderer.code_validator import validate_manim_code

    # Check cache first
    key = _cache_key(prompt)
    if key in _cache:
        return {**_cache[key], "cached": True}

    chain = FALLBACK_CHAIN[tier]
    last_error = None

    for entry in chain:
        provider = entry["provider"]
        model = entry["model"]

        # Skip providers with missing API keys
        if provider == "gemini" and not os.getenv("GEMINI_API_KEY"):
            continue
        if provider == "openai" and not os.getenv("OPENAI_API_KEY"):
            continue

        try:
            print(f"[model_router] Trying {provider}/{model}...")

            if provider == "gemini":
                code = await gemini_generate(prompt, model=model)
            else:
                code = await openai_generate(prompt, model=model)

            validation = validate_manim_code(code)
            result = {
                "code": validation["fixed_code"],
                "provider": provider,
                "model": model,
                "valid": validation["valid"],
                "warnings": validation.get("errors", []),
            }

            # Cache successful results
            _cache[key] = result
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
        return "All AI providers failed. Check your API keys in engine/.env"
    
    err = last_error.get("error", "")
    
    if is_quota_error(err):
        return (
            "API quota exhausted on all providers. Options:\n"
            "1. Wait for quota reset (usually resets daily/per minute)\n"
            "2. Add billing to your Gemini account at https://aistudio.google.com\n"
            "3. Add billing to your OpenAI account at https://platform.openai.com/settings/billing\n"
            "4. Check your GEMINI_API_KEY and OPENAI_API_KEY are correct in engine/.env"
        )
    if is_auth_error(err):
        return "Invalid API key. Check GEMINI_API_KEY and OPENAI_API_KEY in engine/.env"
    
    return f"Generation failed: {err[:200]}"

# Keep old name as alias so existing imports don't break
async def race_models(prompt: str, tier: ModelTier = "standard") -> dict:
    return await generate_with_fallback(prompt, tier)
