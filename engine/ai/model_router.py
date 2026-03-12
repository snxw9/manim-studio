import asyncio
import hashlib
import json
import os
from pathlib import Path
from typing import Literal

from ai.provider_pool import get_pool, TIMEOUT_SECONDS, Provider
from renderer.code_validator import validate_manim_code

ModelTier = Literal["fast", "standard", "smart"]

MODELS: dict[str, dict[str, str]] = {
    "groq":   {"fast": "llama-3.1-8b-instant",      "standard": "llama-3.3-70b-versatile", "smart": "llama-3.3-70b-versatile"},
    "gemini": {"fast": "gemini-2.0-flash",           "standard": "gemini-2.0-flash",        "smart": "gemini-2.5-pro-preview-05-06"},
    "openai": {"fast": "gpt-4o-mini",                "standard": "gpt-4o-mini",             "smart": "gpt-4o"},
}

# Disk-based prompt cache
CACHE_DIR = Path(__file__).parent.parent / "cache"
CACHE_DIR.mkdir(exist_ok=True)

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
    if "code" in result:
        path = CACHE_DIR / f"{key}.json"
        path.write_text(json.dumps({k: v for k, v in result.items() if k != "cached"}))

def select_tier(prompt: str) -> ModelTier:
    words = len(prompt.split())
    return "fast" if words < 15 else "smart" if words > 50 else "standard"

def is_quota_error(err: str) -> bool:
    return any(s in err.lower() for s in ["429", "quota", "rate_limit", "insufficient_quota", "exceeded"])

def is_auth_error(err: str) -> bool:
    return any(s in err.lower() for s in ["401", "403", "invalid_api_key", "authentication"])

def is_timeout_error(err: str) -> bool:
    return any(s in err.lower() for s in ["timeout", "timed out", "asyncio.timeouterror"])

async def _call_provider(provider: Provider, prompt: str, model: str, api_key: str) -> str:
    """Call a single provider with a hard timeout."""
    from ai.groq_client import generate as groq_gen
    from ai.gemini_client import generate as gemini_gen
    from ai.openai_client import generate as openai_gen

    call_map = {
        "groq":   lambda: groq_gen(prompt, model=model, api_key=api_key),
        "gemini": lambda: gemini_gen(prompt, model=model, api_key=api_key),
        "openai": lambda: openai_gen(prompt, model=model, api_key=api_key),
    }

    return await asyncio.wait_for(call_map[provider](), timeout=TIMEOUT_SECONDS)

async def generate_with_fallback(
    prompt: str,
    tier: ModelTier = "standard",
    user_keys: dict | None = None,
) -> dict:
    """
    If user provides their own keys: use only those, no pool involved.
    Otherwise: use the developer pool with rotation and circuit breaking.
    """

    # Check cache first — zero network either way
    cache_key = _cache_key(prompt)
    cached = _load_cache(cache_key)
    if cached:
        print(f"[router] Cache hit — no API call needed")
        return {**cached, "cached": True}

    # USER'S OWN KEYS — bypass pool entirely
    if user_keys and any(user_keys.values()):
        return await _generate_with_user_keys(prompt, tier, user_keys, cache_key)

    # DEVELOPER POOL — rotation + circuit breaker
    return await _generate_with_pool(prompt, tier, cache_key)

async def _generate_with_user_keys(
    prompt: str,
    tier: ModelTier,
    user_keys: dict,
    cache_key: str,
) -> dict:
    """Use user's own keys sequentially. No rotation, no pool tracking."""
    provider_order: list[Provider] = ["groq", "gemini", "openai"]

    for provider in provider_order:
        api_key = user_keys.get(provider, "").strip()
        if not api_key:
            continue

        model = MODELS[provider][tier]
        print(f"[router] User key — trying {provider}/{model}")

        try:
            code = await _call_provider(provider, prompt, model, api_key)
            validation = validate_manim_code(code)
            result = {
                "code": validation["fixed_code"],
                "provider": provider,
                "model": model,
                "source": "user_key",
            }
            _save_cache(cache_key, result)
            return result

        except asyncio.TimeoutError:
            print(f"[router] {provider} timed out — trying next")
            continue
        except Exception as e:
            err = str(e)
            if is_quota_error(err):
                print(f"[router] User's {provider} key quota exceeded — trying next")
            else:
                print(f"[router] {provider} error: {err[:80]}")
            continue

    return {"error": "All your API keys failed or are exhausted. Check your keys in Settings."}

async def _generate_with_pool(
    prompt: str,
    tier: ModelTier,
    cache_key: str,
) -> dict:
    """Use developer pool with rotation and circuit breaking."""
    pool = get_pool()

    if pool.all_exhausted():
        return {
            "error": (
                "Daily generation limit reached on all providers.\n"
                "Options:\n"
                "• Add your own free Groq key in Settings for unlimited use\n"
                "• Wait until midnight for the daily quota to reset\n"
                "• Get a free key at console.groq.com — takes 2 minutes"
            )
        }

    candidates = pool.get_ordered_providers()

    if not candidates:
        return {"error": "No providers available right now. Try again in a minute."}

    for entry in candidates:
        provider: Provider = entry["provider"]
        api_key: str = entry["api_key"]
        model = MODELS[provider][tier]

        print(f"[router] Pool — trying {provider}/{model}")

        try:
            code = await _call_provider(provider, prompt, model, api_key)
            validation = validate_manim_code(code)
            result = {
                "code": validation["fixed_code"],
                "provider": provider,
                "model": model,
                "source": "developer_pool",
            }
            pool.record_success(provider)
            _save_cache(cache_key, result)
            return result

        except asyncio.TimeoutError:
            print(f"[router] {provider} timed out after {TIMEOUT_SECONDS}s — opening circuit")
            pool.record_timeout(provider)
            continue

        except Exception as e:
            err = str(e)
            if is_quota_error(err) or is_auth_error(err):
                print(f"[router] {provider} quota/auth error — exhausting")
                pool.record_quota_error(provider)
            else:
                print(f"[router] {provider} error: {err[:80]}")
            continue

    return {
        "error": (
            "All providers failed for this request.\n"
            "Add your own free Groq key in Settings to bypass this."
        )
    }

# Keep alias
async def race_models(prompt: str, tier: ModelTier = "standard", user_keys: dict | None = None) -> dict:
    return await generate_with_fallback(prompt, tier, user_keys)
