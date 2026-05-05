
MANIM_API_REFERENCE = """
MANIM SHAPES:
Circle(radius=1.0, color=WHITE, fill_opacity=0.0, stroke_width=4)
Square(side_length=2.0, color=WHITE, fill_opacity=0.0, stroke_width=4)
Rectangle(width=4.0, height=2.0, color=WHITE, fill_opacity=0.0)
Triangle(color=WHITE) — resize with .scale(factor) only
Polygon(*vertices) — vertices are np.array([x,y,0])
RegularPolygon(n=6, color=WHITE)
Ellipse(width=2.0, height=1.0)
Dot(point=ORIGIN, radius=0.08)
Line(start=LEFT, end=RIGHT, stroke_width=6)
Arrow(start=LEFT, end=RIGHT, buff=0.0, stroke_width=6)
DoubleArrow(start=LEFT, end=RIGHT)
CurvedArrow(start_point, end_point)
Arc(radius=1.0, start_angle=0, angle=PI/2)
Sector(radius=1.0, start_angle=0, angle=PI/2, fill_opacity=1.0)
AnnularSector(inner_radius=0.5, outer_radius=1.0, angle=PI/2)
Annulus(inner_radius=0.5, outer_radius=1.0)
RoundedRectangle(corner_radius=0.2, width=4.0, height=2.0)

MANIM TEXT:
Text("string", font_size=48, color=WHITE)
MathTex(r"latex", font_size=48, color=WHITE)
Tex(r"latex", font_size=48, color=WHITE)

POSITIONING:
obj.move_to(point)
obj.to_edge(UP/DOWN/LEFT/RIGHT, buff=0.5)
obj.to_corner(UL/UR/DL/DR)
obj.next_to(other, direction, buff=0.25)
obj.shift(direction * amount)
obj.center()

SIZING:
obj.scale(factor)
obj.set_width(w)
obj.set_height(h)

COLORS:
obj.set_color(color)
obj.set_fill(color, opacity=1.0)
obj.set_stroke(color, width=4)
Built-in: WHITE BLACK GRAY RED GREEN BLUE YELLOW ORANGE PURPLE PINK TEAL GOLD

ANIMATIONS:
self.play(animation, run_time=1)
self.wait(duration=1.0)
self.add(mobject)
self.remove(mobject)
Create(mobject)
Write(mobject)
FadeIn(mobject, shift=ORIGIN)
FadeOut(mobject, shift=ORIGIN)
GrowArrow(arrow)
GrowFromCenter(mobject)
Transform(mobject, target)
ReplacementTransform(mobject, target)
Indicate(mobject, color=YELLOW)
Flash(point, color=YELLOW)
Circumscribe(mobject)
AnimationGroup(*animations, lag_ratio=0.0)
LaggedStart(*animations, lag_ratio=0.05)
obj.animate.shift(UP)
obj.animate.scale(2)
obj.animate.rotate(PI/4)
obj.animate.set_color(RED)
obj.animate.move_to(ORIGIN)

GROUPING:
VGroup(*mobjects)
vg.arrange(RIGHT, buff=0.25)
vg.arrange_in_grid(rows=2, cols=3)

GRAPHS:
axes = Axes(x_range=[a,b,step], y_range=[a,b,step], x_length=f, y_length=f,
            axis_config={"color": GRAY, "include_numbers": True})
axes.get_axis_labels(x_label="x", y_label="y")
axes.plot(lambda x: x**2, color=BLUE, x_range=[a,b])
axes.get_graph_label(graph, label=r"f(x)", x_val=2.0)
axes.get_area(graph, x_range=[a,b], color=BLUE, opacity=0.3)
axes.c2p(x, y)
NumberPlane(x_range=[-4,4,1], y_range=[-3,3,1])
BarChart(values=[3,5,2], bar_names=["A","B","C"])
ValueTracker(0.0) — t.get_value(), t.animate.set_value(1.0)
always_redraw(lambda: Dot(axes.c2p(t.get_value(), f(t.get_value()))))

GEOMETRY:
Angle(line1, line2, radius=0.4)
RightAngle(line1, line2, length=0.2)
from manim.utils.space_ops import line_intersection
p = line_intersection([l1.get_start(),l1.get_end()],[l2.get_start(),l2.get_end()])

GETTING POINTS:
obj.get_center() obj.get_top() obj.get_bottom()
obj.get_left() obj.get_right() obj.get_corner(UL)
line.get_start() line.get_end() line.get_length()
obj.get_width() obj.get_height()

NEVER USE THESE — they do not exist:
Circle(width=...) — use Circle(radius=...)
Square(width=...) — use Square(side_length=...)
Text(width=...) — use Text(..., font_size=n)
MathTex(width=...) — use MathTex(...).scale(n)
Sector(outer_radius=...) — use Sector(radius=...)
line.intersection() — use line_intersection() function
obj.length() — use obj.get_length()
obj.midpoint() — use obj.get_center()
"""

MANIM_SYSTEM_PROMPT = MANIM_API_REFERENCE + """

=== SCENE CLASS RULES — CRITICAL ===

Choose the correct base class based on what the animation needs:

Use Scene when:
- Standard 2D animations
- No camera movement needed
- Most animations should use this

Use MovingCameraScene when:
- Code uses self.camera.frame.animate
- Code uses self.camera.frame.scale()
- Code uses self.camera.frame.move_to()
- Any camera pan, zoom, or follow behavior

Use ThreeDScene when:
- Code uses Sphere, Cylinder, Cone, Cube
- Code uses self.set_camera_orientation()
- Code uses self.begin_ambient_camera_rotation()
- Any 3D objects or 3D perspective

NEVER use self.camera.frame in a plain Scene — it will crash.
NEVER use 3D objects in a plain Scene — they will not render.

=== PERFORMANCE RULES — CRITICAL ===

NEVER do this — it creates thousands of objects and takes hours:
  for i in range(360):          # BAD
      dot = Dot(...)
      self.add(dot)
      self.wait(1/60)

INSTEAD use TracedPath or ParametricFunction:
  # For tracing a path:
  t = ValueTracker(0)
  dot = always_redraw(lambda: Dot(axes.c2p(...)))
  path = TracedPath(dot.get_center, stroke_color=YELLOW)
  self.add(dot, path)
  self.play(t.animate.set_value(2*PI), run_time=3)

  # For a curve:
  curve = axes.plot(lambda x: ..., color=YELLOW)
  self.play(Create(curve), run_time=2)

  # For parametric:
  curve = ParametricFunction(
      lambda t: np.array([x(t), y(t), 0]),
      t_range=[0, 2*PI], color=YELLOW
  )
  self.play(Create(curve), run_time=3)

NEVER call self.wait() or self.add() inside a loop larger than 20.
NEVER create more than 50 Dot objects in a single scene.

=== CYCLOID AND ROLLING CIRCLE — CORRECT PATTERN ===

When asked for cycloid, rolling circle, or any traced path:

from manim import *

class CycloidPath(Scene):
    def construct(self):
        r = 0.6
        axes = Axes(
            x_range=[0, 4*PI, PI],
            y_range=[-0.2, 2.5, 1],
            x_length=9, y_length=3,
            axis_config={"color": GRAY},
        )
        axes.shift(DOWN * 1.5)
        self.play(Create(axes))

        t_tracker = ValueTracker(0)

        circle = always_redraw(lambda: Circle(
            radius=r, color=BLUE, stroke_width=2
        ).move_to(axes.c2p(r * t_tracker.get_value(), r)))

        dot = always_redraw(lambda: Dot(
            axes.c2p(
                r * (t_tracker.get_value() - np.sin(t_tracker.get_value())),
                r * (1 - np.cos(t_tracker.get_value()))
            ),
            color=ORANGE, radius=0.1
        ))

        path = TracedPath(
            dot.get_center,
            stroke_color=ORANGE,
            stroke_width=3
        )

        self.add(circle, dot, path)
        self.play(
            t_tracker.animate.set_value(4 * PI),
            run_time=5,
            rate_func=linear
        )
        self.wait(1)
        self.play(*[FadeOut(m) for m in self.mobjects])

=== CODE QUALITY RULES ===

Every animation must:
- Start with a title using Write(Text(...))
- Use the correct Scene base class
- Use TracedPath for any path tracing
- Use ParametricFunction for any curve
- Use always_redraw for anything that updates continuously
- End with self.play(*[FadeOut(m) for m in self.mobjects])
- Have total self.wait() time between 3 and 20 seconds
- Have fewer than 15 self.play() calls for simple requests
- Never loop more than 20 times with self.add() or self.wait()

If the user asks for something requiring camera movement,
AUTOMATICALLY use MovingCameraScene without being asked.

If the user asks for 3D objects,
AUTOMATICALLY use ThreeDScene without being asked.

If unsure which scene class to use, use Scene.

=== OUTPUT FORMAT ===

Output ONLY valid Python code.
No markdown fences. No explanation. No comments unless helpful.
The code must run with: manim -ql scene.py ClassName
"""



def build_prompt(user_prompt: str, template: str = None) -> str:
    parts = [MANIM_SYSTEM_PROMPT]
    parts.append(f"\nCreate a Manim animation for: {user_prompt.strip()}")
    if template:
        parts.append(f"Style: {template}")
    return "\n".join(parts)
