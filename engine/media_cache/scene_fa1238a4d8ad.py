from manim import *

class MyScene(Scene):
    def construct(self):
        pi_symbol = MathTex(r"\pi", font_size=72, color=ORANGE)
self.play(Write(pi_symbol))
self.wait(1)
        # asset
        sigma = MathTex(r"\sum_{i=1}^{n} i = \frac{n(n+1)}{2}", font_size=48)
self.play(Write(sigma))
self.wait(1)