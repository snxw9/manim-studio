# Calculus Template
# Provide basic setup for calculus scenes

template_code = """
from manim import *

class CalculusScene(Scene):
    def construct(self):
        axes = Axes(
            x_range=[-3, 3],
            y_range=[-5, 5],
            axis_config={"color": BLUE},
        )
        self.play(Create(axes))
"""
