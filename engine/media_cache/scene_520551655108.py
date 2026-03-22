from manim import *

class CarUpHill(Scene):
    def construct(self):
        hill = Rectangle(width=6, height=2, fill_color=BLUE, fill_opacity=0.5)
        hill.shift(UP * 2)
        hill.set_stroke(WHITE, 3)

        car = Rectangle(width=1, height=0.5, fill_color=RED, fill_opacity=1)
        car.shift(LEFT * 3)
        car.set_stroke(WHITE, 3)

        self.play(DrawBorderThenFill(hill))
        self.play(DrawBorderThenFill(car))

        for i in range(10):
            car.shift(RIGHT * 0.1)
            self.play(MoveToTarget(car))
            self.wait(0.1)

        self.play(car.animate.shift(RIGHT * 0.1), rate_func=linear)
        self.wait(0.1)