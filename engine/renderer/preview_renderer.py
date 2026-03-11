from .manim_runner import run_manim

def render_preview(code: str) -> str:
    """Convenience wrapper for low-quality preview rendering"""
    return run_manim(code, preview=True)
