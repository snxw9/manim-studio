import pytest
from ai.prompt_builder import MANIM_SYSTEM_PROMPT, build_prompt

def test_system_prompt_builder():
    prompt = build_prompt(user_prompt="calculus")
    assert "calculus" in prompt
    assert "manim" in prompt.lower()
    assert MANIM_SYSTEM_PROMPT in prompt
