from renderer.manim_runner import render_scene
result = render_scene('''
from manim import *
class TestMath(Scene):
    def construct(self):
        eq = MathTex(r"\alpha + \beta", font_size=48)
        self.play(Write(eq))
        self.wait(1)
''', quality='480p', fmt='mp4')
print('SUCCESS size:', result['size'])
