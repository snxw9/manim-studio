from manim import *

class WaterfallModel(Scene):
    def construct(self):
        title = Text("Waterfall Model").scale(1.5)
        self.play(Write(title))
        self.wait()

        self.play(FadeIn(Text("Input").shift(2*LEFT), Text("Process").shift(2*RIGHT), Text("Output").shift(2*DOWN)))
        self.wait()

        self.play(FadeIn(Text("Input").shift(2*LEFT).set_color(RED), Text("Process").shift(2*RIGHT).set_color(YELLOW), Text("Output").shift(2*DOWN).set_color(BLUE)))
        self.wait()

        self.play(FadeOut(Text("Input").shift(2*LEFT), Text("Process").shift(2*RIGHT), Text("Output").shift(2*DOWN)))
        self.wait()

        self.play(FadeIn(Text("Input").shift(2*LEFT), Text("Process").shift(2*RIGHT), Text("Output").shift(2*DOWN)))
        self.wait()

        self.play(FadeIn(Text("Input").shift(2*LEFT).set_color(RED), Text("Process").shift(2*RIGHT).set_color(YELLOW), Text("Output").shift(2*DOWN).set_color(BLUE)))
        self.wait()

        self.play(FadeOut(Text("Input").shift(2*LEFT), Text("Process").shift(2*RIGHT), Text("Output").shift(2*DOWN)))
        self.wait()

        self.play(FadeIn(Text("Input").shift(2*LEFT), Text("Process").shift(2*RIGHT), Text("Output").shift(2*DOWN)))
        self.wait()

        self.play(FadeIn(Text("Input").shift(2*LEFT).set_color(RED), Text("Process").shift(2*RIGHT).set_color(YELLOW), Text("Output").shift(2*DOWN).set_color(BLUE)))
        self.wait()