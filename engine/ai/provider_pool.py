import json
import os
import time
from pathlib import Path
from dataclasses import dataclass, field, asdict
from typing import Literal

Provider = Literal["groq", "gemini", "openai"]

PROVIDER_ORDER: list[Provider] = ["groq", "gemini", "openai"]
USES_PER_PROVIDER = 10
TIMEOUT_SECONDS = 8
CIRCUIT_OPEN_SECONDS = 60  # How long to skip a timed-out provider

STATE_FILE = Path(__file__).parent.parent / "cache" / "provider_state.json"
STATE_FILE.parent.mkdir(exist_ok=True)

@dataclass
class ProviderState:
    uses: int = 0                    # Uses on current active provider
    current_index: int = 0           # Index into PROVIDER_ORDER
    exhausted: list[str] = field(default_factory=list)  # Permanently exhausted today
    circuit_open: dict[str, float] = field(default_factory=dict)  # provider -> timestamp when skipped
    reset_date: str = ""             # Date string — resets counts daily

class ProviderPool:
    def __init__(self):
        self.state = self._load()

    def _load(self) -> ProviderState:
        if STATE_FILE.exists():
            try:
                data = json.loads(STATE_FILE.read_text())
                state = ProviderState(**data)
                # Reset daily
                today = time.strftime("%Y-%m-%d")
                if state.reset_date != today:
                    return ProviderState(reset_date=today)
                return state
            except:
                pass
        return ProviderState(reset_date=time.strftime("%Y-%m-%d"))

    def _save(self):
        STATE_FILE.write_text(json.dumps(asdict(self.state)))

    def _get_env_key(self, provider: Provider) -> str | None:
        env_map = {
            "groq":   "GROQ_API_KEY",
            "gemini": "GEMINI_API_KEY",
            "openai": "OPENAI_API_KEY",
        }
        key = os.getenv(env_map[provider])
        if key and "your_" in key: # Don't use placeholder keys
            return None
        return key or None

    def _is_circuit_open(self, provider: Provider) -> bool:
        """Returns True if this provider was recently slow — skip it."""
        opened_at = self.state.circuit_open.get(provider)
        if not opened_at:
            return False
        if time.time() - opened_at > CIRCUIT_OPEN_SECONDS:
            # Circuit cooled down — allow retrying
            del self.state.circuit_open[provider]
            self._save()
            return False
        return True

    def _open_circuit(self, provider: Provider):
        """Mark provider as temporarily slow."""
        print(f"[pool] Circuit opened for {provider} — skipping for {CIRCUIT_OPEN_SECONDS}s")
        self.state.circuit_open[provider] = time.time()
        self._save()

    def _exhaust(self, provider: Provider):
        """Mark provider as permanently exhausted for today."""
        if provider not in self.state.exhausted:
            print(f"[pool] {provider} exhausted — removed from today's rotation")
            self.state.exhausted.append(provider)
            self._save()

    def _advance(self):
        """Move to next provider in rotation."""
        self.state.uses = 0
        self.state.current_index = (self.state.current_index + 1) % len(PROVIDER_ORDER)
        self._save()
        print(f"[pool] Rotated to {PROVIDER_ORDER[self.state.current_index]}")

    def get_ordered_providers(self) -> list[dict]:
        """
        Returns providers to try for this request, in order.
        Starts from current provider, wraps around.
        Skips exhausted and circuit-open providers.
        """
        candidates = []
        total = len(PROVIDER_ORDER)

        for offset in range(total):
            idx = (self.state.current_index + offset) % total
            provider = PROVIDER_ORDER[idx]

            if provider in self.state.exhausted:
                continue
            if self._is_circuit_open(provider):
                continue

            key = self._get_env_key(provider)
            if not key:
                continue

            candidates.append({
                "provider": provider,
                "api_key": key,
                "is_current": offset == 0,
            })

        return candidates

    def record_success(self, provider: Provider):
        """Called after a successful generation."""
        if provider == PROVIDER_ORDER[self.state.current_index]:
            self.state.uses += 1
            print(f"[pool] {provider} use {self.state.uses}/{USES_PER_PROVIDER}")
            if self.state.uses >= USES_PER_PROVIDER:
                print(f"[pool] {provider} reached {USES_PER_PROVIDER} uses — rotating")
                self._advance()
            else:
                self._save()

    def record_timeout(self, provider: Provider):
        """Called when a provider was too slow."""
        self._open_circuit(provider)
        if provider == PROVIDER_ORDER[self.state.current_index]:
            self._advance()

    def record_quota_error(self, provider: Provider):
        """Called when a provider returns 429 or auth error."""
        self._exhaust(provider)
        if provider == PROVIDER_ORDER[self.state.current_index]:
            self._advance()

    def all_exhausted(self) -> bool:
        available = [
            p for p in PROVIDER_ORDER
            if p not in self.state.exhausted
            and not self._is_circuit_open(p)
            and self._get_env_key(p)
        ]
        return len(available) == 0

    def status(self) -> dict:
        return {
            "current_provider": PROVIDER_ORDER[self.state.current_index],
            "uses_on_current": self.state.uses,
            "uses_remaining": USES_PER_PROVIDER - self.state.uses,
            "exhausted": self.state.exhausted,
            "circuit_open": list(self.state.circuit_open.keys()),
            "reset_date": self.state.reset_date,
        }

# Singleton — one pool shared across all requests
_pool = ProviderPool()

def get_pool() -> ProviderPool:
    return _pool
