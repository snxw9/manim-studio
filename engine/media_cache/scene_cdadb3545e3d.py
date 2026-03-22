from manim import *

class MyScene(Scene):
    def construct(self):
        circle = Circle(radius=1.5, color=BLUE, fill_opacity=0.3)
self.play(Create(circle))
self.wait(1)