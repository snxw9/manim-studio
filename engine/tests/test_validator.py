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

def test_catches_sector_outer_radius():
    code = 'Sector(outer_radius=0.7, angle=PI/3)'
    result = validate_manim_code(code)
    assert not result["valid"]

def test_fixes_latex_escapes():
    code = 'MathTex("\\\\alpha + \\\\beta = 180^\\circ")'
    result = validate_manim_code(code)
    assert 'r"' in result["fixed_code"] or '\\\\circ' in result["fixed_code"]

def test_catches_camera_frame_in_scene():
    code = """from manim import *
class MyScene(Scene):
    def construct(self):
        self.play(self.camera.frame.animate.scale(0.5))
"""
    result = validate_manim_code(code)
    assert not result["valid"]
    assert any("MovingCameraScene" in e for e in result["errors"])

def test_auto_fixes_camera_frame():
    code = """from manim import *
class MyScene(Scene):
    def construct(self):
        self.play(self.camera.frame.animate.scale(0.5))
"""
    result = validate_manim_code(code)
    assert "MovingCameraScene" in result["fixed_code"]

def test_catches_performance_trap():
    code = """from manim import *
class MyScene(Scene):
    def construct(self):
        for i in range(1080):
            self.add(Dot())
            self.wait(1/60)
"""
    result = validate_manim_code(code)
    assert any("Performance trap" in w for w in result["warnings"])

def test_catches_3d_in_scene():
    code = """from manim import *
class MyScene(Scene):
    def construct(self):
        s = Sphere(radius=1)
        self.play(Create(s))
"""
    result = validate_manim_code(code)
    assert not result["valid"]

def test_catches_parametric_surface():
    code = "surface = ParametricSurface(lambda u, v: np.array([u, v, 0]))"
    result = validate_manim_code(code)
    assert not result["valid"]
    assert "Surface" in result["errors"][0]

def test_auto_fixes_parametric_surface():
    code = "surface = ParametricSurface(lambda u, v: np.array([u, v, 0]))"
    result = validate_manim_code(code)
    assert "ParametricSurface" not in result["fixed_code"]
    assert "Surface" in result["fixed_code"]

def test_catches_show_creation():
    code = "self.play(ShowCreation(circle))"
    result = validate_manim_code(code)
    assert not result["valid"]

def test_catches_graph_scene():
    code = "class MyScene(GraphScene):"
    result = validate_manim_code(code)
    assert not result["valid"]
