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
        "code": '''from manim import *

class CalculusScene(Scene):
    def construct(self):
        # Title
        title = Text("Calculus", font_size=48, color=WHITE)
        subtitle = Text("Derivatives & Integrals", font_size=24, color=GRAY)
        subtitle.next_to(title, DOWN, buff=0.3)
        self.play(Write(title), FadeIn(subtitle, shift=UP * 0.3))
        self.wait(1)
        self.play(FadeOut(title), FadeOut(subtitle))

        # Derivative definition
        deriv_title = MathTex(r"\\text{Derivative}", font_size=36, color=ORANGE)
        deriv_title.to_edge(UP, buff=0.5)
        deriv_def = MathTex(
            r"f\'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h}",
            font_size=32
        )
        self.play(Write(deriv_title))
        self.play(Write(deriv_def))
        self.wait(2)

        # Example: derivative of x squared
        example = MathTex(
            r"f(x) = x^2", r"\\Rightarrow", r"f\'(x) = 2x",
            font_size=32
        )
        example.set_color_by_tex("f(x) = x^2", BLUE)
        example.set_color_by_tex("f\'(x) = 2x", GREEN)
        example.next_to(deriv_def, DOWN, buff=0.8)
        self.play(FadeOut(deriv_def))
        self.play(Write(example))
        self.wait(1.5)
        self.play(FadeOut(deriv_title), FadeOut(example))

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
        curve_label = axes.get_graph_label(curve, label=r"f(x)=x^2", x_val=2)
        self.play(Create(axes), Write(labels))
        self.play(Create(curve), Write(curve_label))
        self.wait(1)

        # Tangent line at x=1
        x_val = 1
        slope = 2 * x_val
        tangent = axes.plot(
            lambda x: slope * (x - x_val) + x_val**2,
            color=ORANGE,
            x_range=[-1, 3]
        )
        tangent_label = Text("tangent at x=1", font_size=20, color=ORANGE)
        tangent_label.to_edge(DOWN)
        self.play(Create(tangent), Write(tangent_label))
        self.wait(2)

        # Integral
        self.play(
            FadeOut(curve), FadeOut(tangent),
            FadeOut(tangent_label), FadeOut(curve_label)
        )
        integral_title = MathTex(r"\\text{Integral}", font_size=36, color=ORANGE)
        integral_title.to_edge(UP, buff=0.5)
        integral_def = MathTex(
            r"\\int_a^b f(x)\\,dx = F(b) - F(a)",
            font_size=32
        )
        area_curve = axes.plot(lambda x: x**2, color=BLUE, x_range=[0, 2])
        area = axes.get_area(area_curve, x_range=[0, 2], color=BLUE, opacity=0.4)
        self.play(Write(integral_title), Write(integral_def))
        self.play(Create(area_curve), FadeIn(area))
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
        "code": '''from manim import *

class GraphVizScene(Scene):
    def construct(self):
        title = Text("Graph Visualization", font_size=42, color=WHITE)
        self.play(Write(title))
        self.wait(0.8)
        self.play(FadeOut(title))

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

        sin_label = axes.get_graph_label(sin_curve, r"\\sin(x)", x_val=2.5, color=BLUE)
        cos_label = axes.get_graph_label(cos_curve, r"\\cos(x)", x_val=2.5, color=GREEN)
        quad_label = axes.get_graph_label(quad_curve, r"0.3x^2", x_val=2.8, color=ORANGE)

        self.play(Create(sin_curve), Write(sin_label))
        self.wait(0.5)
        self.play(Create(cos_curve), Write(cos_label))
        self.wait(0.5)
        self.play(Create(quad_curve), Write(quad_label))
        self.wait(2)

        # Highlight intersection
        dot = Dot(axes.c2p(0, 1), color=YELLOW, radius=0.12)
        intersect_label = Text("intersection", font_size=18, color=YELLOW)
        intersect_label.next_to(dot, UR, buff=0.15)
        self.play(FadeIn(dot, scale=2), Write(intersect_label))
        self.wait(1.5)

        # Transition to parametric
        self.play(*[FadeOut(m) for m in self.mobjects])
        param_title = Text("Parametric Curve", font_size=36, color=WHITE)
        self.play(Write(param_title))
        self.wait(0.5)
        self.play(FadeOut(param_title))

        axes2 = Axes(
            x_range=[-2, 2, 1], y_range=[-2, 2, 1],
            x_length=5, y_length=5,
            axis_config={"color": GRAY},
        )
        self.play(Create(axes2))
        t = ValueTracker(0)
        parametric = axes2.plot_parametric_curve(
            lambda t: np.array([np.cos(t), np.sin(t), 0]),
            t_range=[0, 2 * PI],
            color=ORANGE,
        )
        param_label = MathTex(
            r"(\\cos t, \\sin t)", font_size=28, color=ORANGE
        ).to_edge(DOWN)
        self.play(Create(parametric), run_time=2)
        self.play(Write(param_label))
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
        "code": '''from manim import *

class GeometryProofsScene(Scene):
    def construct(self):
        title = Text("Geometry Proofs", font_size=42, color=WHITE)
        self.play(Write(title))
        self.wait(0.8)
        self.play(FadeOut(title))

        # Theorem 1 — Sum of angles in a triangle = 180
        theorem_label = Text(
            "Triangle Angle Sum Theorem", font_size=30, color=ORANGE
        ).to_edge(UP, buff=0.4)
        self.play(Write(theorem_label))

        # Draw triangle
        A = np.array([-2.5, -1.5, 0])
        B = np.array([2.5, -1.5, 0])
        C = np.array([0.5, 1.8, 0])

        tri = Polygon(A, B, C, color=WHITE, stroke_width=2.5)
        self.play(Create(tri))

        # Label vertices
        a_label = Text("A", font_size=24).next_to(A, DL, buff=0.1)
        b_label = Text("B", font_size=24).next_to(B, DR, buff=0.1)
        c_label = Text("C", font_size=24).next_to(C, UP, buff=0.1)
        self.play(Write(a_label), Write(b_label), Write(c_label))

        # Draw and label angles
        angle_a = Angle(
            Line(A, B), Line(A, C), radius=0.45, color=BLUE
        )
        angle_b = Angle(
            Line(B, C), Line(B, A), radius=0.45, color=GREEN
        )
        angle_c = Angle(
            Line(C, A), Line(C, B), radius=0.45, color=RED
        )
        alpha = MathTex(r"\\alpha", font_size=22, color=BLUE).next_to(angle_a, buff=0.05)
        beta  = MathTex(r"\\beta",  font_size=22, color=GREEN).next_to(angle_b, buff=0.05)
        gamma = MathTex(r"\\gamma", font_size=22, color=RED).next_to(angle_c, buff=0.05)

        self.play(
            Create(angle_a), Create(angle_b), Create(angle_c),
            Write(alpha), Write(beta), Write(gamma)
        )
        self.wait(1)

        # Theorem equation
        theorem_eq = MathTex(
            r"\\alpha + \\beta + \\gamma = 180^\\circ",
            font_size=34
        ).to_edge(DOWN, buff=0.6)
        self.play(Write(theorem_eq))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])

        # Theorem 2 — Pythagorean theorem
        pyth_label = Text(
            "Pythagorean Theorem", font_size=30, color=ORANGE
        ).to_edge(UP, buff=0.4)
        self.play(Write(pyth_label))

        # Right triangle
        P1 = np.array([-1.5, -1.5, 0])
        P2 = np.array([1.5, -1.5, 0])
        P3 = np.array([-1.5, 1.5, 0])
        right_tri = Polygon(P1, P2, P3, color=WHITE, stroke_width=2.5)
        right_angle = RightAngle(Line(P1, P2), Line(P1, P3), length=0.25, color=WHITE)

        side_a = MathTex(r"a", font_size=28, color=BLUE).move_to(
            (P1 + P2) / 2 + DOWN * 0.3
        )
        side_b = MathTex(r"b", font_size=28, color=GREEN).move_to(
            (P1 + P3) / 2 + LEFT * 0.3
        )
        side_c = MathTex(r"c", font_size=28, color=RED).move_to(
            (P2 + P3) / 2 + RIGHT * 0.35 + UP * 0.2
        )

        self.play(Create(right_tri), Create(right_angle))
        self.play(Write(side_a), Write(side_b), Write(side_c))

        pyth_eq = MathTex(
            r"a^2 + b^2 = c^2",
            font_size=38
        ).to_edge(DOWN, buff=0.6)
        self.play(Write(pyth_eq))
        self.wait(2.5)
        self.play(*[FadeOut(m) for m in self.mobjects])
'''
    },

    "matrix_transforms": {
        "id": "matrix_transforms",
        "name": "Matrix Transforms",
        "description": "Linear transformations and eigenvectors",
        "category": "Linear Algebra",
        "duration": 16,
        "code": '''from manim import *

class MatrixTransformsScene(Scene):
    def construct(self):
        title = Text("Matrix Transformations", font_size=40, color=WHITE)
        self.play(Write(title))
        self.wait(0.8)
        self.play(FadeOut(title))

        # Show a 2x2 matrix
        matrix_label = Text("2×2 Transformation Matrix", font_size=28, color=ORANGE)
        matrix_label.to_edge(UP, buff=0.5)
        self.play(Write(matrix_label))

        matrix = Matrix(
            [["2", "1"], ["0", "3"]],
            h_buff=1.2, v_buff=0.9
        )
        matrix.set_color(WHITE)
        self.play(Write(matrix))
        self.wait(1)
        self.play(FadeOut(matrix), FadeOut(matrix_label))

        # Number plane transformation
        plane = NumberPlane(
            x_range=[-4, 4, 1], y_range=[-3, 3, 1],
            background_line_style={"stroke_color": BLUE_D, "stroke_opacity": 0.4}
        )
        self.play(Create(plane))

        # Original vectors
        v1 = Arrow(ORIGIN, RIGHT * 2, buff=0, color=GREEN, stroke_width=4)
        v2 = Arrow(ORIGIN, UP * 2, buff=0, color=RED, stroke_width=4)
        v1_label = MathTex(r"\\vec{v_1}", color=GREEN, font_size=24).next_to(v1.get_end(), RIGHT * 0.2)
        v2_label = MathTex(r"\\vec{v_2}", color=RED, font_size=24).next_to(v2.get_end(), UP * 0.2)

        self.play(GrowArrow(v1), GrowArrow(v2), Write(v1_label), Write(v2_label))
        self.wait(1)

        # Apply transformation matrix [[2,1],[0,3]]
        transform_label = MathTex(
            r"M = \\begin{bmatrix} 2 & 1 \\\\ 0 & 3 \\end{bmatrix}",
            font_size=30
        ).to_edge(DOWN, buff=0.5)
        self.play(Write(transform_label))

        self.play(
            plane.animate.apply_matrix([[2, 1], [0, 3]]),
            v1.animate.put_start_and_end_on(ORIGIN, np.array([4, 0, 0])),
            v2.animate.put_start_and_end_on(ORIGIN, np.array([2, 6, 0])),
            run_time=2
        )
        self.wait(1.5)
        self.play(*[FadeOut(m) for m in self.mobjects])

        # Eigenvector demo
        eigen_label = Text("Eigenvectors", font_size=32, color=ORANGE)
        eigen_label.to_edge(UP, buff=0.5)
        eigen_eq = MathTex(r"M\\vec{v} = \\lambda\\vec{v}", font_size=36)
        eigen_desc = Text(
            "Eigenvectors only scale — they don't rotate",
            font_size=20, color=GRAY
        ).next_to(eigen_eq, DOWN, buff=0.4)

        self.play(Write(eigen_label))
        self.play(Write(eigen_eq))
        self.play(FadeIn(eigen_desc, shift=UP * 0.2))
        self.wait(2.5)
        self.play(*[FadeOut(m) for m in self.mobjects])
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
