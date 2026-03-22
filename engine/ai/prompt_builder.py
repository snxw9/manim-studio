MANIM_API_REFERENCE = """
=== MANIM COMMUNITY EDITION v0.18 — COMPLETE API REFERENCE ===

── SHAPES ──────────────────────────────────────────────────────
Circle(radius=1.0, color=WHITE, fill_opacity=0.0, stroke_width=4)
Square(side_length=2.0, color=WHITE, fill_opacity=0.0, stroke_width=4)
Rectangle(width=4.0, height=2.0, color=WHITE, fill_opacity=0.0)
Triangle(color=WHITE, fill_opacity=0.0)
  → resize with: .scale(factor)
  → NO width= height= on Circle/Square/Triangle/Polygon
Polygon(*vertices, color=WHITE)
  → vertices are numpy arrays: np.array([x, y, 0])
RegularPolygon(n=6, color=WHITE)
Ellipse(width=2.0, height=1.0, color=WHITE)
Dot(point=ORIGIN, radius=0.08, color=WHITE)
Line(start=LEFT, end=RIGHT, color=WHITE, stroke_width=6)
Arrow(start=LEFT, end=RIGHT, color=WHITE, buff=0.0, stroke_width=6)
DoubleArrow(start=LEFT, end=RIGHT, color=WHITE, buff=0.0)
CurvedArrow(start_point, end_point, color=WHITE)
Arc(radius=1.0, start_angle=0, angle=PI/2, color=WHITE)
Sector(radius=1.0, start_angle=0, angle=PI/2, color=WHITE, fill_opacity=1.0)
  → Sector NEVER takes outer_radius= or inner_radius=
AnnularSector(inner_radius=0.5, outer_radius=1.0, angle=PI/2)
Annulus(inner_radius=0.5, outer_radius=1.0, color=WHITE)
RoundedRectangle(corner_radius=0.2, width=4.0, height=2.0)

── TEXT ────────────────────────────────────────────────────────
Text("string", font_size=48, color=WHITE, font="", weight=NORMAL)
  → NO width= height= in Text constructor
  → resize with: .scale(factor)
MathTex(r"latex", font_size=48, color=WHITE)
  → ALWAYS use raw strings: r"\alpha" NEVER "\alpha"
  → NO width= height= in MathTex constructor
Tex(r"latex text", font_size=48, color=WHITE)
Title("string", color=WHITE)
Paragraph("line1", "line2", color=WHITE)
  → multi-line text with separate strings

── POSITIONING ─────────────────────────────────────────────────
obj.move_to(point)             # point = np.array([x,y,0]) or UP/DOWN/LEFT/RIGHT
obj.to_edge(direction, buff=0.5)  # direction = UP/DOWN/LEFT/RIGHT
obj.to_corner(direction, buff=0.25)  # direction = UL/UR/DL/DR
obj.next_to(other, direction, buff=0.25)
obj.shift(direction * amount)
obj.align_to(other, direction)
obj.center()

Constants: UP, DOWN, LEFT, RIGHT, ORIGIN, UL, UR, DL, DR
           IN, OUT (for 3D)
           PI, TAU, DEGREES

── SIZING ──────────────────────────────────────────────────────
obj.scale(factor)
obj.set_width(width, stretch=False)
obj.set_height(height, stretch=False)
obj.stretch_to_fit_width(width)
obj.stretch_to_fit_height(height)

── COLORS ──────────────────────────────────────────────────────
obj.set_color(color)
obj.set_fill(color, opacity=1.0)
obj.set_stroke(color, width=4, opacity=1.0)
obj.set_opacity(opacity)

Built-in colors: WHITE, BLACK, GRAY, GREY, RED, GREEN, BLUE,
YELLOW, ORANGE, PURPLE, PINK, TEAL, GOLD, MAROON,
RED_A through RED_E, BLUE_A through BLUE_E, etc.
Custom: ManimColor("#ff6600") or "#ff6600" directly

── ANIMATIONS ──────────────────────────────────────────────────
self.play(animation, run_time=1, rate_func=smooth)
self.wait(duration=1.0)
self.add(mobject)          # instant add, no animation
self.remove(mobject)       # instant remove

Create(mobject)            # draws the shape
Write(mobject)             # writes text stroke by stroke
FadeIn(mobject, shift=ORIGIN, scale=1.0)
FadeOut(mobject, shift=ORIGIN, scale=1.0)
GrowArrow(arrow)
GrowFromCenter(mobject)
GrowFromEdge(mobject, direction)
SpinInFromNothing(mobject)
Transform(mobject, target)           # morphs mobject into target
ReplacementTransform(mobject, target) # replaces mobject with target
TransformFromCopy(mobject, target)
MoveToTarget(mobject)               # use after mobject.generate_target()
Indicate(mobject, color=YELLOW, scale_factor=1.2)
Flash(point, color=YELLOW, line_length=0.2)
Circumscribe(mobject, color=YELLOW)
ShowCreationThenFadeOut(mobject)
AnimationGroup(*animations, lag_ratio=0.0)
LaggedStart(*animations, lag_ratio=0.05)
Succession(*animations)

── .animate SYNTAX ─────────────────────────────────────────────
self.play(obj.animate.shift(UP))
self.play(obj.animate.scale(2))
self.play(obj.animate.rotate(PI/4))
self.play(obj.animate.set_color(RED))
self.play(obj.animate.move_to(ORIGIN))
self.play(obj.animate.set_fill(BLUE, opacity=0.5))
self.play(obj.animate.become(other_obj))

── GROUPING ────────────────────────────────────────────────────
VGroup(*mobjects)          # group of vector mobjects
Group(*mobjects)           # general group
vg.arrange(direction=RIGHT, buff=0.25, center=True)
vg.arrange_in_grid(rows=2, cols=3, buff=0.25)

── GRAPHS AND PLOTS ────────────────────────────────────────────
axes = Axes(
    x_range=[x_min, x_max, x_step],
    y_range=[y_min, y_max, y_step],
    x_length=float,
    y_length=float,
    axis_config={"color": GRAY, "include_numbers": True},
    x_axis_config={},
    y_axis_config={},
)
axes.get_axis_labels(x_label="x", y_label="y")
axes.plot(lambda x: x**2, color=BLUE, x_range=[a, b])
axes.plot_parametric_curve(lambda t: [cos(t), sin(t), 0], t_range=[0, TAU])
axes.get_graph_label(graph, label=r"f(x)", x_val=2.0, color=BLUE)
axes.get_area(graph, x_range=[a, b], color=BLUE, opacity=0.3)
axes.c2p(x, y)             # coordinates to point
axes.p2c(point)            # point to coordinates
dot = Dot(axes.c2p(1, 2))

NumberPlane(
    x_range=[-4, 4, 1],
    y_range=[-3, 3, 1],
    background_line_style={"stroke_color": BLUE_D, "stroke_opacity": 0.4}
)
plane.apply_matrix([[a, b], [c, d]])  # linear transformation

PolarPlane(radius_max=3.0)
BarChart(values=[3, 5, 2, 7], bar_names=["A","B","C","D"])

── GEOMETRY HELPERS ────────────────────────────────────────────
Angle(line1, line2, radius=0.4, color=WHITE)
RightAngle(line1, line2, length=0.2, color=WHITE)
# line_intersection — NOT a method, use the function:
from manim.utils.space_ops import line_intersection
p = line_intersection([l1.get_start(), l1.get_end()],
                      [l2.get_start(), l2.get_end()])

── GETTING POINTS ──────────────────────────────────────────────
obj.get_center()
obj.get_top()    obj.get_bottom()
obj.get_left()   obj.get_right()
obj.get_corner(UL)  # UL UR DL DR
line.get_start()    line.get_end()
line.get_length()   # NOT .length()
obj.get_width()     obj.get_height()

── VALUE TRACKER ───────────────────────────────────────────────
t = ValueTracker(0.0)
t.get_value()
self.play(t.animate.set_value(1.0), run_time=2)
# Use always_redraw for live updates:
dot = always_redraw(lambda: Dot(axes.c2p(t.get_value(), f(t.get_value()))))

── 3D ──────────────────────────────────────────────────────────
class MyScene(ThreeDScene):
    def construct(self):
        self.set_camera_orientation(phi=75*DEGREES, theta=-45*DEGREES)
Sphere(radius=1.0)
Cylinder(radius=1.0, height=2.0)
Cone(base_radius=1.0, height=2.0)
Cube(side_length=1.0)
Prism(dimensions=[1,1,1])
Surface(func, u_range=[0,1], v_range=[0,1])

── CAMERAS ─────────────────────────────────────────────────────
class MyScene(MovingCameraScene):
    def construct(self):
        self.camera.frame.animate.scale(0.5)
        self.camera.frame.animate.move_to(point)
        self.camera.frame.animate.shift(RIGHT)

── COMMON PATTERNS ─────────────────────────────────────────────
# Fade out all:
self.play(*[FadeOut(m) for m in self.mobjects])

# Animate multiple things at once:
self.play(Create(circle), Write(text), run_time=2)

# Transform sequence:
self.play(ReplacementTransform(obj1, obj2))

# Moving along path:
self.play(MoveAlongPath(dot, path), run_time=3)

# Updater:
obj.add_updater(lambda m, dt: m.rotate(dt))
self.wait(2)
obj.remove_updater(...)

── WHAT MANIM CANNOT DO ────────────────────────────────────────
NEVER attempt these — they will fail or produce bad results:
✗ Realistic physics (gravity, collisions, fluid simulation)
✗ Raster images or photos as backgrounds
✗ Video/audio playback inside scenes
✗ Real-world 3D models (cars, people, buildings)
✗ Particle systems with 100+ objects
✗ obj.width= as a setter (read-only) — use set_width()
✗ Line.intersection() — use line_intersection() function
✗ Circle(width=...) — use Circle(radius=...)
✗ Any_Shape(width=...) except Rectangle(width=..., height=...)
✗ Text(width=...) — use Text(..., font_size=n) or .scale()
✗ .length() as a method — use .get_length()
✗ .midpoint() as a method — use .get_center()
✗ Sector(outer_radius=...) — use Sector(radius=...)
"""

MANIM_SYSTEM_PROMPT = f"""{MANIM_API_REFERENCE}

=== GENERATION RULES ===

You are a Manim expert. Output ONLY valid Python code. No markdown. No explanation.

STRUCTURE — every response must follow this exactly:
from manim import *

class DescriptiveName(Scene):
    def construct(self):
        # your code here

PERFORMANCE LIMITS:
- Maximum 15 self.play() calls
- Maximum 20 seconds total self.wait() time
- Maximum 20 seconds total animation duration
- Use Text() instead of MathTex() when LaTeX is not needed

LATEX RULES:
- Always raw strings: r"\\alpha" NEVER "\\alpha"
- Common: r"\\alpha \\beta \\gamma \\theta \\pi \\lambda"
- Fractions: r"\\frac{{a}}{{b}}"
- Subscript: r"x_{{1}}"
- Superscript: r"x^{{2}}"
- Note: in Python f-strings double the braces: {{}}

If the user request cannot be done with Manim primitives,
create a MATHEMATICAL/DIAGRAMMATIC representation:
- "car going uphill" → rectangle moving along angled line
- "water flowing" → animated blue rectangles shifting downward
- "population growth" → animated bar chart or exponential curve
- "sorting" → colored rectangles rearranging by height
"""


def build_prompt(user_prompt: str, template: str = None) -> str:
    parts = [MANIM_SYSTEM_PROMPT]
    parts.append(f"\nAnimate this concept: {user_prompt.strip()}")
    if template:
        parts.append(f"Style reference: {template}")
    return "\n".join(parts)
