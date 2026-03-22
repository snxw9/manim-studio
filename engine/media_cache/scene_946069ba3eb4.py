from manim import *

class GeometryProofsScene(Scene):
    def construct(self):
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