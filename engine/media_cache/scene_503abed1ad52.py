from manim import *

class GraphVizScene(Scene):
    def construct(self):
        axes = Axes(
            x_range=[-4, 4, 1], y_range=[-2, 4, 1],
            x_length=8, y_length=5,
            axis_config={"color": GRAY, "include_numbers": True}
        )
        labels = axes.get_axis_labels(x_label="x", y_label="y")
        self.play(Create(axes), Write(labels))
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