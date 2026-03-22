from manim import *

class CarRidingUpHill(Scene):
    def construct(self):
        hill = Rectangle(width=6, height=2, fill_color=BLUE, fill_opacity=0.5)
        hill.shift(UP * 2)
        hill.set_stroke(width=0)

        car = Rectangle(width=1, height=0.5, fill_color=RED, fill_opacity=1)
        car.shift(LEFT * 3)
        car.set_stroke(width=0)

        car_wheel = Circle(radius=0.2, fill_color=RED, fill_opacity=1)
        car_wheel.shift(LEFT * 1.5 + DOWN * 0.25)

        car_wheel2 = Circle(radius=0.2, fill_color=RED, fill_opacity=1)
        car_wheel2.shift(LEFT * 1.5 + UP * 0.25)

        self.add(hill, car, car_wheel, car_wheel2)

        for i in range(10):
            car.shift(RIGHT * 0.1)
            car_wheel.shift(RIGHT * 0.1)
            car_wheel2.shift(RIGHT * 0.1)
            self.play(MoveToTarget(car), MoveToTarget(car_wheel), MoveToTarget(car_wheel2))
            self.wait(0.1)