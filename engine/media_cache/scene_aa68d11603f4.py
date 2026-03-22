from manim import *

class CalculusScene(Scene):
    def construct(self):
        title = Text("Calculus", font_size=48, color=WHITE)
        self.play(Write(title))
        self.wait(1)
        self.play(FadeOut(title))
        deriv_def = MathTex(
            r"f'(x) = \lim_{h \to 0} \frac{f(x+h) - f(x)}{h}",
            font_size=32
        )
        self.play(Write(deriv_def))
        self.wait(2)
        axes = Axes(
            x_range=[-3, 3, 1], y_range=[-1, 9, 1],
            x_length=6, y_length=5,
            axis_config={"color": GRAY}
        )
        labels = axes.get_axis_labels(x_label="x", y_label="y")
        curve = axes.plot(lambda x: x**2, color=BLUE, x_range=[-3, 3])
        self.play(FadeOut(deriv_def), Create(axes), Write(labels), Create(curve))
        tangent = axes.plot(lambda x: 2 * (x - 1) + 1, color=ORANGE, x_range=[-1, 3])
        self.play(Create(tangent))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])