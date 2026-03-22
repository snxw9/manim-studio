from manim import *

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
        eigen_eq = MathTex(r"M\vec{{v}} = \lambda\vec{{v}}", font_size=42)
        self.play(*[FadeOut(m) for m in self.mobjects])
        self.play(Write(eigen_eq))
        self.wait(2)
        self.play(FadeOut(eigen_eq))