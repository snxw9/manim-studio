"""
Manim Studio — Built-in Template Library
All templates are complete, tested, ready-to-render Manim scenes.
No AI generation required.
"""

TEMPLATES: dict[str, dict] = {

    "calculus": {
        "id": "calculus",
        "name": "Calculus",
        "description": "Derivatives, integrals, and limits",
        "category": "Mathematics",
        "duration": 15,
        "code": r'''from manim import *

class CalculusScene(Scene):
    def construct(self):
        # Title
        title = Text("Calculus", font_size=48, color=WHITE)
        self.play(Write(title))
        self.wait(1)
        self.play(FadeOut(title))

        # Derivative definition
        deriv_def = MathTex(
            r"f'(x) = \lim_{h \to 0} \frac{f(x+h) - f(x)}{h}",
            font_size=32
        )
        self.play(Write(deriv_def))
        self.wait(2)

        # Axes and curve
        axes = Axes(
            x_range=[-3, 3, 1],
            y_range=[-1, 9, 1],
            x_length=6,
            y_length=5,
            axis_config={"color": GRAY},
        )
        labels = axes.get_axis_labels(x_label="x", y_label="y")
        curve = axes.plot(lambda x: x**2, color=BLUE, x_range=[-3, 3])
        self.play(FadeOut(deriv_def), Create(axes), Write(labels), Create(curve))
        
        # Tangent
        tangent = axes.plot(lambda x: 2 * (x - 1) + 1, color=ORANGE, x_range=[-1, 3])
        self.play(Create(tangent))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])
'''
    },

    "graph_viz": {
        "id": "graph_viz",
        "name": "Graph Viz",
        "description": "Function graphs and parametric curves",
        "category": "Visualization",
        "duration": 12,
        "code": r'''from manim import *

class GraphVizScene(Scene):
    def construct(self):
        # Axes setup
        axes = Axes(
            x_range=[-4, 4, 1],
            y_range=[-2, 4, 1],
            x_length=8,
            y_length=5,
            axis_config={"color": GRAY, "include_numbers": True},
        )
        labels = axes.get_axis_labels(x_label="x", y_label="y")
        self.play(Create(axes), Write(labels))

        # Plot multiple functions
        sin_curve = axes.plot(lambda x: np.sin(x), color=BLUE, x_range=[-4, 4])
        cos_curve = axes.plot(lambda x: np.cos(x), color=GREEN, x_range=[-4, 4])
        quad_curve = axes.plot(lambda x: 0.3 * x**2, color=ORANGE, x_range=[-3.6, 3.6])

        sin_label = axes.get_graph_label(sin_curve, r"\sin(x)", x_val=2.5, color=BLUE)
        cos_label = axes.get_graph_label(cos_curve, r"\cos(x)", x_val=2.5, color=GREEN)
        quad_label = axes.get_graph_label(quad_curve, r"0.3x^2", x_val=2.8, color=ORANGE)

        self.play(Create(sin_curve), Write(sin_label))
        self.play(Create(cos_curve), Write(cos_label))
        self.play(Create(quad_curve), Write(quad_label))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])
'''
    },

    "geometry_proofs": {
        "id": "geometry_proofs",
        "name": "Geometry Proofs",
        "description": "Angles, polygons, and theorems",
        "category": "Geometry",
        "duration": 18,
        "code": r'''from manim import *

class GeometryProofsScene(Scene):
    def construct(self):
        # Triangle
        A = np.array([-2.5, -1.5, 0])
        B = np.array([2.5, -1.5, 0])
        C = np.array([0.5, 1.8, 0])
        tri = Polygon(A, B, C, color=WHITE, stroke_width=2.5)
        self.play(Create(tri))
        
        a_label = Text("A", font_size=24).next_to(A, DL, buff=0.1)
        b_label = Text("B", font_size=24).next_to(B, DR, buff=0.1)
        c_label = Text("C", font_size=24).next_to(C, UP, buff=0.1)
        self.play(Write(a_label), Write(b_label), Write(c_label))
        
        angle_a = Angle(Line(A, B), Line(A, C), radius=0.45, color=BLUE)
        angle_b = Angle(Line(B, C), Line(B, A), radius=0.45, color=GREEN)
        angle_c = Angle(Line(C, A), Line(C, B), radius=0.45, color=RED)
        self.play(Create(angle_a), Create(angle_b), Create(angle_c))
        
        theorem = MathTex(
            r"\alpha + \beta + \gamma = 180^\circ",
            font_size=36
        ).to_edge(DOWN, buff=0.6)
        self.play(Write(theorem))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])
        
        # Right triangle
        P1 = np.array([-1.5, -1.5, 0])
        P2 = np.array([1.5, -1.5, 0])
        P3 = np.array([-1.5, 1.5, 0])
        right_tri = Polygon(P1, P2, P3, color=WHITE, stroke_width=2.5)
        right_angle = RightAngle(Line(P1, P2), Line(P1, P3), length=0.25)
        self.play(Create(right_tri), Create(right_angle))
        
        pyth = MathTex(r"a^2 + b^2 = c^2", font_size=42).to_edge(DOWN, buff=0.6)
        self.play(Write(pyth))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])
'''
    },

    "matrix_transforms": {
        "id": "matrix_transforms",
        "name": "Matrix Transforms",
        "description": "Linear transformations and eigenvectors",
        "category": "Linear Algebra",
        "duration": 16,
        "code": r'''from manim import *

class MatrixTransformsScene(Scene):
    def construct(self):
        label = Text("Transformation Matrix", font_size=28, color=ORANGE).to_edge(UP)
        matrix = Matrix([["2", "1"], ["0", "3"]], h_buff=1.2, v_buff=0.9)
        self.play(Write(label), Write(matrix))
        self.wait(1.5)
        self.play(FadeOut(label), FadeOut(matrix))
        
        plane = NumberPlane(
            x_range=[-4, 4, 1], y_range=[-3, 3, 1],
            background_line_style={"stroke_color": BLUE_D, "stroke_opacity": 0.4}
        )
        v1 = Arrow(ORIGIN, RIGHT * 2, buff=0, color=GREEN, stroke_width=4)
        v2 = Arrow(ORIGIN, UP * 2, buff=0, color=RED, stroke_width=4)
        self.play(Create(plane), GrowArrow(v1), GrowArrow(v2))
        
        self.play(
            plane.animate.apply_matrix([[2, 1], [0, 3]]),
            v1.animate.put_start_and_end_on(ORIGIN, np.array([4, 0, 0])),
            v2.animate.put_start_and_end_on(ORIGIN, np.array([2, 6, 0])),
            run_time=2
        )
        self.wait(2)
        
        eigen_eq = MathTex(r"M\vec{v} = \lambda\vec{v}", font_size=42)
        self.play(*[FadeOut(m) for m in self.mobjects])
        self.play(Write(eigen_eq))
        self.wait(2)
        self.play(FadeOut(eigen_eq))
'''
    },
}

def get_template(template_id: str) -> dict | None:
    return TEMPLATES.get(template_id)

def list_templates() -> list[dict]:
    return [
        {k: v for k, v in t.items() if k != "code"}
        for t in TEMPLATES.values()
    ]
