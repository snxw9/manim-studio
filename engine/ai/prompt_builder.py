MANIM_SYSTEM_PROMPT = """Manim Community Edition v0.18 expert. Output ONLY valid Python code.

RULES:
- from manim import *
- class NAME(Scene): def construct(self):
- Raw strings for LaTeX: r"\\alpha" never "\\alpha"  
- Sector(radius=r) never Sector(outer_radius=r)
- line_intersection([l1.get_start(),l1.get_end()],[l2.get_start(),l2.get_end()]) never line.intersection()
- self.play() for all animations, self.wait() for pacing
- No explanation, no markdown fences, no comments unless helpful"""

def build_prompt(user_prompt: str, template: str = None) -> str:
    # Keep total prompt under 500 tokens = ~375 words = tiny bandwidth
    parts = [MANIM_SYSTEM_PROMPT, f"\nAnimate: {user_prompt.strip()}"]
    if template:
        parts.append(f"Style: {template}")
    return "\n".join(parts)
