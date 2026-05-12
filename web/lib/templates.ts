export interface Template {
  id: string;
  name: string;
  category: string;
  description: string;
  code: string;
}

export const TEMPLATES: Template[] = [

  {
    id: "pythagorean_theorem",
    name: "Pythagorean Theorem",
    category: "Mathematics",
    description: "Visual proof with labeled sides",
    code: `from manim import *

class PythagoreanTheorem(Scene):
    def construct(self):
        title = Text("Pythagorean Theorem", font_size=40, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))
        self.wait(0.5)

        a, b, c = 3, 4, 5
        scale = 0.55

        A = np.array([0, 0, 0])
        B = np.array([a * scale, 0, 0])
        C = np.array([0, b * scale, 0])

        triangle = Polygon(A, B, C, color=WHITE, stroke_width=3)
        triangle.move_to(ORIGIN + LEFT * 0.5)
        self.play(Create(triangle))

        right = RightAngle(
            Line(triangle.get_vertices()[0], triangle.get_vertices()[1]),
            Line(triangle.get_vertices()[0], triangle.get_vertices()[2]),
            length=0.22, color=GRAY
        )
        self.play(Create(right))

        verts = triangle.get_vertices()
        mid_ab = (verts[0] + verts[1]) / 2
        mid_ac = (verts[0] + verts[2]) / 2
        mid_bc = (verts[1] + verts[2]) / 2

        la = MathTex(r"a=3", font_size=26, color=BLUE).next_to(mid_ab, DOWN, buff=0.15)
        lb = MathTex(r"b=4", font_size=26, color=GREEN).next_to(mid_ac, LEFT, buff=0.15)
        lc = MathTex(r"c=5", font_size=26, color=RED).next_to(mid_bc, RIGHT, buff=0.15)
        self.play(Write(la), Write(lb), Write(lc))
        self.wait(0.5)

        sq_a = Square(side_length=a * scale, color=BLUE, fill_opacity=0.3)
        sq_a.next_to(triangle, DOWN, buff=0, aligned_edge=LEFT)
        sq_a.shift(RIGHT * 0)

        sq_b = Square(side_length=b * scale, color=GREEN, fill_opacity=0.3)
        sq_b.next_to(triangle, LEFT, buff=0, aligned_edge=DOWN)

        sq_c_side = c * scale
        sq_c = Square(side_length=sq_c_side, color=RED, fill_opacity=0.3)
        sq_c.move_to(mid_bc + RIGHT * 1.6 + UP * 0.3)

        self.play(FadeIn(sq_a), FadeIn(sq_b), FadeIn(sq_c))

        lsa = MathTex(r"a^2=9", font_size=22, color=BLUE).move_to(sq_a.get_center())
        lsb = MathTex(r"b^2=16", font_size=22, color=GREEN).move_to(sq_b.get_center())
        lsc = MathTex(r"c^2=25", font_size=22, color=RED).move_to(sq_c.get_center())
        self.play(Write(lsa), Write(lsb), Write(lsc))
        self.wait(0.5)

        eq1 = MathTex(r"a^2 + b^2 = c^2", font_size=34).to_edge(DOWN, buff=0.8)
        eq2 = MathTex(r"9 + 16 = 25", font_size=30, color=YELLOW)
        eq2.next_to(eq1, DOWN, buff=0.3)
        self.play(Write(eq1))
        self.play(Write(eq2))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "bubble_sort",
    name: "Bubble Sort",
    category: "Computer Science",
    description: "Step by step sorting animation",
    code: `from manim import *

class BubbleSort(Scene):
    def construct(self):
        title = Text("Bubble Sort", font_size=40, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))

        values = [5, 3, 8, 1, 9, 2, 7, 4]
        n = len(values)
        arr = list(values)

        bar_width = 0.7
        gap = 0.15
        max_height = 3.5
        total_width = n * (bar_width + gap) - gap
        start_x = -total_width / 2

        def make_bars(data):
            bars = VGroup()
            for i, val in enumerate(data):
                h = val / max(data) * max_height
                bar = Rectangle(
                    width=bar_width, height=h,
                    color=BLUE, fill_opacity=0.8, stroke_width=1
                )
                bar.move_to(
                    np.array([start_x + i * (bar_width + gap) + bar_width/2,
                               -1.5 + h/2, 0])
                )
                label = Text(str(val), font_size=18)
                label.move_to(bar.get_top() + UP * 0.2)
                bars.add(VGroup(bar, label))
            return bars

        bars = make_bars(arr)
        self.play(LaggedStart(*[FadeIn(b) for b in bars], lag_ratio=0.1))
        self.wait(0.3)

        for i in range(n - 1):
            for j in range(n - i - 1):
                bar_j = bars[j][0]
                bar_j1 = bars[j+1][0]
                self.play(
                    bar_j.animate.set_color(YELLOW),
                    bar_j1.animate.set_color(YELLOW),
                    run_time=0.15
                )
                if arr[j] > arr[j+1]:
                    arr[j], arr[j+1] = arr[j+1], arr[j]
                    pos_j = bars[j].get_center()
                    pos_j1 = bars[j+1].get_center()
                    self.play(
                        bars[j].animate.move_to(
                            np.array([pos_j1[0], bars[j].get_center()[1], 0])
                        ),
                        bars[j+1].animate.move_to(
                            np.array([pos_j[0], bars[j+1].get_center()[1], 0])
                        ),
                        run_time=0.25
                    )
                    bars[j], bars[j+1] = bars[j+1], bars[j]
                self.play(
                    bars[j][0].animate.set_color(BLUE),
                    bars[j+1][0].animate.set_color(BLUE),
                    run_time=0.1
                )
            self.play(bars[n-1-i][0].animate.set_color(GREEN), run_time=0.15)

        self.play(bars[0][0].animate.set_color(GREEN), run_time=0.2)

        done = Text("Sorted!", font_size=30, color=GREEN).to_edge(DOWN, buff=0.5)
        self.play(Write(done))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "waterfall_model",
    name: "Waterfall Model",
    category: "Computer Science",
    description: "Software development lifecycle",
    code: `from manim import *

class WaterfallModel(Scene):
    def construct(self):
        title = Text("Waterfall Model", font_size=38, color=WHITE)
        title.to_edge(UP, buff=0.3)
        self.play(Write(title))
        self.wait(0.3)

        phases = [
            ("Requirements",   BLUE),
            ("System Design",  PURPLE),
            ("Implementation", ORANGE),
            ("Testing",        YELLOW),
            ("Deployment",     GREEN),
            ("Maintenance",    TEAL),
        ]

        box_w = 4.5
        box_h = 0.65
        spacing = 0.78
        start_y = 1.8

        boxes = []
        for i, (name, color) in enumerate(phases):
            y = start_y - i * spacing
            x_shift = i * 0.25

            box = Rectangle(
                width=box_w, height=box_h,
                color=color, fill_opacity=0.25,
                stroke_width=2
            )
            box.move_to(np.array([x_shift, y, 0]))

            label = Text(name, font_size=22, color=color)
            label.move_to(box.get_center())

            number = Text(str(i+1), font_size=16, color=color)
            number.move_to(box.get_left() + RIGHT * 0.3)

            group = VGroup(box, label, number)
            boxes.append((group, box, color, x_shift, y))

        for i, (group, box, color, x_shift, y) in enumerate(boxes):
            self.play(FadeIn(group, shift=RIGHT * 0.3), run_time=0.4)

            if i < len(boxes) - 1:
                next_x = boxes[i+1][3]
                next_y = boxes[i+1][4]
                arrow = Arrow(
                    np.array([x_shift, y - box_h/2, 0]),
                    np.array([next_x, next_y + box_h/2, 0]),
                    buff=0.05, color=GRAY,
                    stroke_width=2, max_tip_length_to_length_ratio=0.15
                )
                self.play(GrowArrow(arrow), run_time=0.25)

        desc = Text(
            "Each phase completes before the next begins",
            font_size=18, color=GRAY
        ).to_edge(DOWN, buff=0.35)
        self.play(FadeIn(desc))
        self.wait(2.5)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "sine_wave",
    name: "Sine & Cosine",
    category: "Mathematics",
    description: "Unit circle and wave generation",
    code: `from manim import *

class SineWave(Scene):
    def construct(self):
        title = Text("Sine and Cosine", font_size=38, color=WHITE)
        title.to_edge(UP, buff=0.3)
        self.play(Write(title))
        self.wait(0.3)
        self.play(FadeOut(title))

        circle_center = LEFT * 3.5
        r = 1.4

        circle = Circle(radius=r, color=GRAY, stroke_width=2)
        circle.move_to(circle_center)
        x_line = Line(circle_center + LEFT * (r+0.3), circle_center + RIGHT * (r+0.3), color=GRAY, stroke_width=1)
        y_line = Line(circle_center + DOWN * (r+0.3), circle_center + UP * (r+0.3), color=GRAY, stroke_width=1)

        axes = Axes(
            x_range=[0, 2*PI, PI/2],
            y_range=[-1.5, 1.5, 1],
            x_length=5.5,
            y_length=3.2,
            axis_config={"color": GRAY},
        )
        axes.move_to(RIGHT * 1.8 + DOWN * 0.2)

        self.play(Create(circle), Create(x_line), Create(y_line), Create(axes))

        sin_label = MathTex(r"\\sin(\\theta)", font_size=24, color=RED)
        sin_label.to_edge(DOWN, buff=0.8).shift(RIGHT * 1.5)
        cos_label = MathTex(r"\\cos(\\theta)", font_size=24, color=BLUE)
        cos_label.next_to(sin_label, LEFT, buff=0.5)
        self.play(Write(sin_label), Write(cos_label))

        t = ValueTracker(0)

        dot = always_redraw(lambda: Dot(
            circle_center + np.array([
                r * np.cos(t.get_value()),
                r * np.sin(t.get_value()),
                0
            ]),
            color=WHITE, radius=0.1
        ))

        h_line = always_redraw(lambda: DashedLine(
            circle_center + np.array([0, r * np.sin(t.get_value()), 0]),
            circle_center + np.array([r * np.cos(t.get_value()), r * np.sin(t.get_value()), 0]),
            color=BLUE, stroke_width=2
        ))
        v_line = always_redraw(lambda: DashedLine(
            circle_center + np.array([r * np.cos(t.get_value()), 0, 0]),
            circle_center + np.array([r * np.cos(t.get_value()), r * np.sin(t.get_value()), 0]),
            color=RED, stroke_width=2
        ))

        sin_curve = axes.plot(np.sin, color=RED, x_range=[0, 0.001])
        cos_curve = axes.plot(np.cos, color=BLUE, x_range=[0, 0.001])

        self.add(h_line, v_line, dot)
        self.play(
            t.animate.set_value(2 * PI),
            Create(axes.plot(np.sin, color=RED, x_range=[0, 2*PI])),
            Create(axes.plot(np.cos, color=BLUE, x_range=[0, 2*PI])),
            run_time=4, rate_func=linear
        )
        self.wait(1.5)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "binary_search",
    name: "Binary Search",
    category: "Computer Science",
    description: "Divide and conquer search algorithm",
    code: `from manim import *

class BinarySearch(Scene):
    def construct(self):
        title = Text("Binary Search", font_size=40, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))

        arr = [2, 5, 8, 12, 16, 23, 38, 45, 56, 72]
        target = 23
        n = len(arr)

        box_w = 0.75
        gap = 0.08
        total_w = n * (box_w + gap) - gap
        start_x = -total_w / 2

        boxes = VGroup()
        labels = VGroup()
        for i, val in enumerate(arr):
            x = start_x + i * (box_w + gap) + box_w / 2
            box = Square(side_length=box_w, color=BLUE, fill_opacity=0.2, stroke_width=2)
            box.move_to(np.array([x, 0.3, 0]))
            label = Text(str(val), font_size=20)
            label.move_to(box.get_center())
            boxes.add(box)
            labels.add(label)

        idx_labels = VGroup()
        for i in range(n):
            x = start_x + i * (box_w + gap) + box_w / 2
            idx = Text(str(i), font_size=14, color=GRAY)
            idx.move_to(np.array([x, -0.2, 0]))
            idx_labels.add(idx)

        self.play(LaggedStart(*[FadeIn(VGroup(boxes[i], labels[i])) for i in range(n)], lag_ratio=0.05))
        self.play(FadeIn(idx_labels))

        target_text = Text(f"Target: {target}", font_size=26, color=YELLOW)
        target_text.to_edge(DOWN, buff=1.2)
        self.play(Write(target_text))

        status = Text("", font_size=22, color=WHITE).to_edge(DOWN, buff=0.5)
        self.add(status)

        lo, hi = 0, n - 1
        while lo <= hi:
            mid = (lo + hi) // 2
            mid_x = start_x + mid * (box_w + gap) + box_w / 2

            range_rect = Rectangle(
                width=(hi - lo + 1) * (box_w + gap),
                height=box_w + 0.15,
                color=GRAY, stroke_width=1, fill_opacity=0.1
            )
            range_rect.move_to(np.array([
                (start_x + lo*(box_w+gap) + start_x + hi*(box_w+gap) + box_w) / 2,
                0.3, 0
            ]))

            new_status = Text(f"lo={lo}  mid={mid}  hi={hi}", font_size=20, color=WHITE)
            new_status.to_edge(DOWN, buff=0.5)

            self.play(
                boxes[mid].animate.set_color(ORANGE).set_fill(ORANGE, opacity=0.5),
                Transform(status, new_status),
                run_time=0.4
            )
            self.wait(0.5)

            if arr[mid] == target:
                found = Text(f"Found {target} at index {mid}!", font_size=26, color=GREEN)
                found.to_edge(DOWN, buff=0.5)
                self.play(
                    boxes[mid].animate.set_color(GREEN).set_fill(GREEN, opacity=0.6),
                    Transform(status, found)
                )
                self.wait(2)
                break
            elif arr[mid] < target:
                for i in range(lo, mid + 1):
                    self.play(boxes[i].animate.set_fill(DARK_GRAY, opacity=0.5).set_color(DARK_GRAY), run_time=0.1)
                lo = mid + 1
            else:
                for i in range(mid, hi + 1):
                    self.play(boxes[i].animate.set_fill(DARK_GRAY, opacity=0.5).set_color(DARK_GRAY), run_time=0.1)
                hi = mid - 1

        self.wait(1)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "shape_morph",
    name: "Shape Morph",
    category: "Mathematics",
    description: "Geometric transformations",
    code: `from manim import *

class ShapeMorph(Scene):
    def construct(self):
        title = Text("Shape Transformations", font_size=38, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))

        shapes = [
            (Circle(radius=1.4, color=BLUE,   fill_opacity=0.4), "Circle"),
            (Square(side_length=2.5, color=RED,    fill_opacity=0.4), "Square"),
            (Triangle(color=GREEN,  fill_opacity=0.4).scale(1.6),     "Triangle"),
            (RegularPolygon(n=5, color=ORANGE, fill_opacity=0.4).scale(1.5), "Pentagon"),
            (RegularPolygon(n=6, color=PURPLE, fill_opacity=0.4).scale(1.5), "Hexagon"),
            (RegularPolygon(n=8, color=TEAL,   fill_opacity=0.4).scale(1.5), "Octagon"),
            (Circle(radius=1.4, color=YELLOW,  fill_opacity=0.4),     "Circle"),
        ]

        label = Text(shapes[0][1], font_size=30, color=WHITE)
        label.to_edge(DOWN, buff=0.6)
        self.play(FadeIn(shapes[0][0].move_to(ORIGIN)), Write(label))
        self.wait(0.4)

        current = shapes[0][0]
        for shape, name in shapes[1:]:
            shape.move_to(ORIGIN)
            new_label = Text(name, font_size=30, color=WHITE)
            new_label.to_edge(DOWN, buff=0.6)
            self.play(
                Transform(current, shape),
                Transform(label, new_label),
                run_time=0.9
            )
            self.wait(0.4)

        info = Text(
            "All polygons approach a circle as sides increase",
            font_size=18, color=GRAY
        ).to_edge(DOWN, buff=0.15)
        self.play(Write(info))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "derivative_calculus",
    name: "Derivative",
    category: "Mathematics",
    description: "Tangent line and slope visualization",
    code: `from manim import *

class DerivativeCalculus(Scene):
    def construct(self):
        title = Text("The Derivative", font_size=38, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))

        defn = MathTex(
            r"f'(x) = \\lim_{h \\to 0} \\frac{f(x+h)-f(x)}{h}",
            font_size=30
        )
        self.play(Write(defn))
        self.wait(1.5)
        self.play(FadeOut(defn), FadeOut(title))

        axes = Axes(
            x_range=[-0.5, 4, 1],
            y_range=[-0.5, 9, 1],
            x_length=7,
            y_length=5.5,
            axis_config={"color": GRAY, "include_numbers": True},
        )
        axes.shift(DOWN * 0.2)
        x_lab = axes.get_axis_labels(x_label="x", y_label="f(x)")
        self.play(Create(axes), Write(x_lab))

        curve = axes.plot(lambda x: x**2, color=BLUE, x_range=[0.01, 3.2])
        curve_label = axes.get_graph_label(curve, r"f(x)=x^2", x_val=3, color=BLUE)
        self.play(Create(curve), Write(curve_label))
        self.wait(0.4)

        for x_val, col in [(0.5, RED), (1.5, ORANGE), (2.5, GREEN)]:
            y_val = x_val ** 2
            slope = 2 * x_val
            dot = Dot(axes.c2p(x_val, y_val), color=col, radius=0.1)
            tang = axes.plot(
                lambda x, xv=x_val, yv=y_val, s=slope: s*(x-xv)+yv,
                color=col,
                x_range=[max(0, x_val-1.2), min(3.5, x_val+1.2)]
            )
            slope_tex = MathTex(
                r"f'(" + f"{x_val}" + r")=" + f"{slope:.0f}",
                font_size=26, color=col
            ).to_edge(DOWN, buff=0.5)
            self.play(FadeIn(dot), Create(tang), Write(slope_tex), run_time=0.5)
            self.wait(0.9)
            if x_val < 2.5:
                self.play(FadeOut(dot), FadeOut(tang), FadeOut(slope_tex), run_time=0.3)

        self.wait(1.5)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "matrix_multiply",
    name: "Matrix Multiply",
    category: "Mathematics",
    description: "2x2 matrix multiplication steps",
    code: `from manim import *

class MatrixMultiply(Scene):
    def construct(self):
        title = Text("Matrix Multiplication", font_size=36, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))

        A = [[1, 2], [3, 4]]
        B = [[5, 6], [7, 8]]
        C = [[1*5+2*7, 1*6+2*8], [3*5+4*7, 3*6+4*8]]

        def make_matrix(data, color):
            return Matrix(
                [[str(data[i][j]) for j in range(2)] for i in range(2)],
                h_buff=0.9, v_buff=0.7,
                bracket_h_buff=0.15,
            ).set_color(color)

        mat_a = make_matrix(A, BLUE)
        mat_b = make_matrix(B, GREEN)
        times = MathTex(r"\\times", font_size=40)
        equals = MathTex(r"=", font_size=40)
        mat_c = make_matrix(C, ORANGE)
        mat_c_q = MathTex(r"\\begin{pmatrix}?&?\\\\?&?\\end{pmatrix}", font_size=36, color=ORANGE)

        eq = VGroup(mat_a, times, mat_b, equals, mat_c_q)
        eq.arrange(RIGHT, buff=0.4)
        eq.move_to(ORIGIN + UP * 0.5)
        self.play(Write(mat_a), Write(times), Write(mat_b), Write(equals), Write(mat_c_q))
        self.wait(0.5)

        results = []
        positions = [
            (0, 0, BLUE, GREEN, C[0][0]),
            (0, 1, BLUE, GREEN, C[0][1]),
            (1, 0, BLUE, GREEN, C[1][0]),
            (1, 1, BLUE, GREEN, C[1][1]),
        ]

        step_y = -1.5
        for row, col, ca, cb, result in positions:
            ra = [A[row][k] for k in range(2)]
            rb = [B[k][col] for k in range(2)]
            step = MathTex(
                f"C_{{{row+1}{col+1}}} = "
                f"{ra[0]}\\times{rb[0]} + {ra[1]}\\times{rb[1]} = {result}",
                font_size=26, color=YELLOW
            ).move_to(np.array([0, step_y, 0]))
            self.play(Write(step), run_time=0.5)
            self.wait(0.5)
            self.play(FadeOut(step), run_time=0.2)

        mat_c_final = make_matrix(C, ORANGE)
        mat_c_final.move_to(mat_c_q.get_center())
        self.play(Transform(mat_c_q, mat_c_final))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "fibonacci_spiral",
    name: "Fibonacci Spiral",
    category: "Mathematics",
    description: "Golden ratio and nature pattern",
    code: `from manim import *

class FibonacciSpiral(Scene):
    def construct(self):
        title = Text("Fibonacci Spiral", font_size=38, color=WHITE)
        title.to_edge(UP, buff=0.4)
        self.play(Write(title))

        fibs = [1, 1, 2, 3, 5, 8, 13]
        scale = 0.28

        origin = np.array([-0.5, -0.3, 0])
        directions = [RIGHT, UP, LEFT, DOWN]
        pos = origin.copy()
        squares = VGroup()
        colors = [BLUE, GREEN, RED, ORANGE, PURPLE, TEAL, YELLOW]

        x, y = 0.0, 0.0
        corners = []

        for i, f in enumerate(fibs):
            d = directions[i % 4]
            side = f * scale
            sq = Square(side_length=side, color=colors[i % len(colors)],
                        fill_opacity=0.25, stroke_width=2)

            if i == 0:
                sq.move_to(origin)
            elif i == 1:
                sq.next_to(squares[0], UP, buff=0)
            elif i == 2:
                sq.next_to(VGroup(squares[0], squares[1]), LEFT, buff=0)
            elif i == 3:
                sq.next_to(VGroup(squares[0], squares[1], squares[2]), DOWN, buff=0)
            elif i == 4:
                sq.next_to(VGroup(*squares), RIGHT, buff=0)
            elif i == 5:
                sq.next_to(VGroup(*squares), UP, buff=0)
            elif i == 6:
                sq.next_to(VGroup(*squares), LEFT, buff=0)

            num = Text(str(f), font_size=max(10, int(side * 50)), color=WHITE)
            num.move_to(sq.get_center())
            squares.add(sq)
            self.play(Create(sq), Write(num), run_time=0.35)

        golden = MathTex(
            r"\\varphi = \\frac{1+\\sqrt{5}}{2} \\approx 1.618",
            font_size=28, color=YELLOW
        ).to_edge(DOWN, buff=0.5)
        self.play(Write(golden))
        self.wait(2)
        self.play(*[FadeOut(m) for m in self.mobjects])`,
  },

  {
    id: "name_animation",
    name: "Name Animation",
    category: "Creative",
    description: "Animate letters with style",
    code: `from manim import *

class NameAnimation(Scene):
    def construct(self):
        name = "MANIM"
        colors = [RED, ORANGE, YELLOW, GREEN, BLUE]

        letters = VGroup(*[
            Text(c, font_size=96, color=colors[i % len(colors)], weight=BOLD)
            for i, c in enumerate(name)
        ])
        letters.arrange(RIGHT, buff=0.1)
        letters.move_to(ORIGIN)

        for letter in letters:
            letter.save_state()
            letter.scale(0.1).set_opacity(0)

        self.play(LaggedStart(
            *[letter.animate.restore() for letter in letters],
            lag_ratio=0.15,
            run_time=1.5
        ))
        self.wait(0.5)

        self.play(letters.animate.shift(UP * 0.8))

        subtitle = Text("Mathematical Animation Engine", font_size=28, color=GRAY)
        subtitle.next_to(letters, DOWN, buff=0.4)
        self.play(Write(subtitle))
        self.wait(0.5)

        underline = Line(
            letters.get_left() + DOWN * 0.1,
            letters.get_right() + DOWN * 0.1,
            color=WHITE, stroke_width=3
        )
        self.play(Create(underline))
        self.wait(0.5)

        self.play(
            letters.animate.set_color(WHITE),
            run_time=0.8
        )
        self.play(
            *[letters[i].animate.set_color(colors[i]) for i in range(len(name))],
            run_time=0.8
        )

        self.wait(1.5)
        self.play(
            *[FadeOut(m, shift=UP * 0.5) for m in self.mobjects],
            run_time=1
        )`,
  },

];
