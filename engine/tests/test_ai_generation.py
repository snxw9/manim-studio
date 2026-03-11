import pytest
from ai.prompt_builder import get_system_prompt

def test_system_prompt_builder():
    prompt = get_system_prompt(template_name="calculus")
    assert "calculus" in prompt
    assert "manim" in prompt.lower()
