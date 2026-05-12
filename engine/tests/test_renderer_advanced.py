import pytest
import os
import shutil
from renderer.manim_runner import render_scene
from renderer.code_validator import validate_manim_code

# Sample Manim code that should work
VALID_CODE = """
from manim import *

class TestScene(Scene):
    def construct(self):
        circle = Circle()
        self.play(Create(circle))
"""

# Sample Manim code with a syntax error
INVALID_SYNTAX_CODE = """
from manim import *

class TestScene(Scene)
    def construct(self):
        circle = Circle()
        self.play(Create(circle))
"""

# Sample Manim code with a banned method
BANNED_METHOD_CODE = """
from manim import *

class TestScene(Scene):
    def construct(self):
        l1 = Line(LEFT, RIGHT)
        l2 = Line(UP, DOWN)
        # .intersection is banned
        inter = l1.intersection(l2)
"""

@pytest.fixture
def output_dir(tmp_path):
    """Fixture to provide a temporary output directory and set the environment variable."""
    # render_scene uses Path(__file__).parent.parent / "outputs"
    # so we don't really need to set MANIM_OUTPUT_DIR, but we'll keep it for other tests if any.
    yield str(tmp_path)

def test_validate_valid_code():
    result = validate_manim_code(VALID_CODE)
    assert result["valid"] is True
    assert len(result["errors"]) == 0

def test_validate_invalid_syntax():
    result = validate_manim_code(INVALID_SYNTAX_CODE)
    assert result["valid"] is False
    assert any("syntax error" in err.lower() for err in result["errors"])

def test_validate_banned_method():
    result = validate_manim_code(BANNED_METHOD_CODE)
    assert result["valid"] is False
    assert any("Invalid Manim method" in err for err in result["errors"])

@pytest.mark.skipif(shutil.which("manim") is None, reason="manim CLI not found")
def test_render_scene_success(output_dir):
    # This test actually runs manim, so it might be slow.
    result = render_scene(VALID_CODE, quality="480p")
    assert "video" in result
    assert result["mimeType"] == "video/mp4"
    assert result["className"] == "TestScene"

@pytest.mark.skipif(shutil.which("manim") is None, reason="manim CLI not found")
def test_render_scene_invalid_code(output_dir):
    with pytest.raises(RuntimeError):
        invalid_manim = """
from manim import *
class FailScene(Scene):
    def construct(self):
        circle = Circle()
        # Non-existent manim function
        self.play(NonExistentEffect(circle))
"""
        render_scene(invalid_manim, quality="480p")
