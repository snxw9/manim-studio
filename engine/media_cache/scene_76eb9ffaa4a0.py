from manim import *

class WaterfallModel(Scene):
    def construct(self):
        # Create the base of the waterfall
        base = Rectangle(width=6, height=0.5, fill_color=WHITE, fill_opacity=1)
        base.to_edge(DOWN)

        # Create the waterfall
        waterfall = Rectangle(width=6, height=6, fill_color=BLUE, fill_opacity=0.5)
        waterfall.shift(UP)

        # Create the water flow
        water_flow = VGroup()
        for i in range(10):
            line = Line(width=0.2, color=BLUE)
            line.shift(UP * i)
            water_flow.add(line)

        # Create the rocks
        rocks = VGroup()
        for i in range(5):
            rock = Circle(radius=0.5, fill_color=GREY, fill_opacity=1)
            rock.shift(LEFT * (i + 1))
            rocks.add(rock)

        # Create the water level
        water_level = Line(width=0.2, color=BLUE)
        water_level.shift(UP * 5)

        # Create the animation
        self.play(FadeIn(base))
        self.play(FadeIn(waterfall))
        self.play(FadeIn(water_flow))
        self.play(FadeIn(rocks))
        self.play(FadeIn(water_level))
        self.wait(1)
        self.play(water_flow.animate.shift(UP * 5), rate_func=linear)
        self.wait(1)
        self.play(water_flow.animate.shift(UP * 5), rate_func=linear)
        self.wait(1)
        self.play(water_flow.animate.shift(UP * 5), rate_func=linear)
        self.wait(1)
        self.play(water_flow.animate.shift(UP * 5), rate_func=linear)
        self.wait(1)
        self.play(water_flow.animate.shift(UP * 5), rate_func=linear)
        self.wait(1)