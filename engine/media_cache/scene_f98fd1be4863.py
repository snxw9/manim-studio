from manim import *

class Snowflake(Scene):
    def construct(self):
        name = Text("snxw").scale(3)
        self.play(FadeIn(name))
        self.wait()

        snowflake = VGroup()
        for i in range(6):
            branch = Line(ORIGIN, OUT, color=WHITE).scale(2)
            branch.rotate(i * PI / 3, about_point=ORIGIN)
            snowflake.add(branch)

        self.play(Transform(name, snowflake), run_time=2)
        self.wait()