from manim import *

class WaterfallModel(Scene):
    def construct(self):
        title = Text("Waterfall Model").scale(1.5)
        self.play(Write(title))
        self.wait()

        phases = VGroup(
            Text("Analysis").scale(0.8),
            Text("Design").scale(0.8),
            Text("Implementation").scale(0.8),
            Text("Verification").scale(0.8),
            Text("Maintenance").scale(0.8)
        )
        phases.arrange(DOWN)
        self.play(Write(phases))
        self.wait()

        arrows = VGroup(
            Arrow(start=ORIGIN, end=DOWN * 2),
            Arrow(start=DOWN * 2, end=DOWN * 4),
            Arrow(start=DOWN * 4, end=DOWN * 6),
            Arrow(start=DOWN * 6, end=DOWN * 8)
        )
        arrows[0].set_color(YELLOW)
        arrows[1].set_color(YELLOW)
        arrows[2].set_color(YELLOW)
        arrows[3].set_color(YELLOW)
        self.play(Write(arrows))
        self.wait()

        self.play(FadeOut(arrows))
        self.wait()

        waterfall = VGroup(
            Text("Requirements").scale(0.8),
            Text("Analysis").scale(0.8),
            Text("Design").scale(0.8),
            Text("Implementation").scale(0.8),
            Text("Verification").scale(0.8),
            Text("Maintenance").scale(0.8)
        )
        waterfall.arrange(DOWN)
        waterfall[0].set_color(YELLOW)
        waterfall[1].set_color(YELLOW)
        waterfall[2].set_color(YELLOW)
        waterfall[3].set_color(YELLOW)
        waterfall[4].set_color(YELLOW)
        waterfall[5].set_color(YELLOW)
        self.play(Write(waterfall))
        self.wait()

        self.play(FadeOut(waterfall))
        self.wait()