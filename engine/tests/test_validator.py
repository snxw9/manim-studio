from renderer.code_validator import validate_manim_code

def test_catches_intersection_method():
    code = "line1.intersection(line2)"
    result = validate_manim_code(code)
    assert not result["valid"]
    assert any("intersection" in e for e in result["errors"])

def test_catches_missing_construct():
    code = "from manim import *\nclass MyScene(Scene):\n    pass"
    result = validate_manim_code(code)
    assert not result["valid"]

def test_valid_code_passes():
    code = """
from manim import *
class MyScene(Scene):
    def construct(self):
        self.play(Create(Circle()))
"""
    result = validate_manim_code(code)
    assert result["valid"]

def test_auto_adds_import():
    code = "class MyScene(Scene):\n    def construct(self):\n        pass"
    result = validate_manim_code(code)
    assert "from manim import *" in result["fixed_code"]
