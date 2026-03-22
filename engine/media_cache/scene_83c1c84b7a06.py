from manim import *

class MyScene(Scene):
    def construct(self):
        pi_symbol = MathTex(r"\pi", font_size=72, color=ORANGE)
self.play(Write(pi_symbol))
self.wait(1)