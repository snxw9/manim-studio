package com.manimstudio.app.ui.components.editor

object CodeCompletionEngine {

    // All completions the editor knows about
    private val completions = buildList {
        // Manim scene classes
        add(Completion(
            label = "Scene",
            type = CompletionType.CLASS,
            description = "Standard 2D scene",
            expandTo = "Scene",
            tutorHint = "The main base class for standard 2D animations in Manim. All your visual scripts should inherit from this.",
            tutorExample = "class MyScene(Scene):\n    def construct(self):\n        pass",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "MovingCameraScene",
            type = CompletionType.CLASS,
            description = "Scene with movable camera",
            expandTo = "MovingCameraScene",
            tutorHint = "Allows you to dynamically pan, zoom, and rotate the camera frame during animations to focus on specific elements.",
            tutorExample = "class ZoomScene(MovingCameraScene):\n    def construct(self):\n        self.play(self.camera.frame.animate.scale(0.5))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "ThreeDScene",
            type = CompletionType.CLASS,
            description = "Scene for 3D mobjects",
            expandTo = "ThreeDScene",
            tutorHint = "Provides the 3D rendering setup so you can draw spheres, cylinders, and surfaces and rotate the camera in 3D space.",
            tutorExample = "class My3D(ThreeDScene):\n    def construct(self):\n        self.set_camera_orientation(phi=75 * DEGREES)",
            difficulty = Difficulty.ADVANCED
        ))
        add(Completion(
            label = "ZoomedScene",
            type = CompletionType.CLASS,
            description = "Scene with a zoomed inset picture",
            expandTo = "ZoomedScene",
            tutorHint = "Sets up an auxiliary zoomed-in camera viewport showing a close-up detail of the main scene layout.",
            tutorExample = "class ZoomDetail(ZoomedScene):\n    def construct(self):\n        self.activate_zooming()",
            difficulty = Difficulty.ADVANCED
        ))

        // Manim shapes
        add(Completion(
            label = "Circle(",
            type = CompletionType.CLASS,
            description = "A circle shape",
            expandTo = "Circle(radius=1.0, color=BLUE, fill_opacity=0.5)",
            tutorHint = "Creates a circle you can animate. radius controls the size, color sets the outline color, fill_opacity (0.0 to 1.0) controls how filled-in it looks.",
            tutorExample = "circle = Circle(radius=1.5, color=BLUE)\nself.play(Create(circle))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Square(",
            type = CompletionType.CLASS,
            description = "A square shape",
            expandTo = "Square(side_length=2.0, color=RED)",
            tutorHint = "Creates a perfect square. side_length controls the width and height, color sets the border color.",
            tutorExample = "square = Square(side_length=1.8, color=RED)\nself.play(FadeIn(square))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Triangle(",
            type = CompletionType.CLASS,
            description = "An equilateral triangle shape",
            expandTo = "Triangle(color=GREEN)",
            tutorHint = "Creates a standard equilateral triangle, ideal for geometric proofs and vector representations.",
            tutorExample = "triangle = Triangle(color=GREEN)\nself.play(Create(triangle))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Rectangle(",
            type = CompletionType.CLASS,
            description = "A rectangle shape",
            expandTo = "Rectangle(width=4.0, height=2.0)",
            tutorHint = "Creates a 4-sided polygon with independent width and height parameters.",
            tutorExample = "rect = Rectangle(width=3.0, height=1.5, color=YELLOW)\nself.play(Write(rect))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Polygon(",
            type = CompletionType.CLASS,
            description = "A custom vertex polygon",
            expandTo = "Polygon(point1, point2, point3)",
            tutorHint = "Generates a shape by connecting a custom list of points (vectors) sequentially.",
            tutorExample = "poly = Polygon(LEFT, UP*2, RIGHT, DOWN)\nself.play(Create(poly))",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "RegularPolygon(",
            type = CompletionType.CLASS,
            description = "A symmetric polygon",
            expandTo = "RegularPolygon(n=6)",
            tutorHint = "Creates a regular polygon with n equal sides (e.g., n=5 for pentagon, n=6 for hexagon).",
            tutorExample = "hexagon = RegularPolygon(n=6, color=GOLD)\nself.play(Create(hexagon))",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "Ellipse(",
            type = CompletionType.CLASS,
            description = "An elliptical shape",
            expandTo = "Ellipse(width=3.0, height=1.5)",
            tutorHint = "Draws an oval shape defined by major and minor axes (width and height).",
            tutorExample = "oval = Ellipse(width=2.5, height=1.2)\nself.play(FadeIn(oval))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Arc(",
            type = CompletionType.CLASS,
            description = "A curved circular arc segment",
            expandTo = "Arc(radius=1.0, start_angle=0, angle=PI)",
            tutorHint = "Draws a portion of a circle's circumference. start_angle determines where it starts, angle sets the span.",
            tutorExample = "arc = Arc(radius=1.5, angle=PI/2)\nself.play(Create(arc))",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "Line(",
            type = CompletionType.CLASS,
            description = "A straight line segment",
            expandTo = "Line(start=LEFT, end=RIGHT)",
            tutorHint = "Draws a straight line between two coordinate points. start and end take 3D coordinate vectors.",
            tutorExample = "line = Line(start=LEFT*2, end=RIGHT*2, color=GRAY)\nself.play(Create(line))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Arrow(",
            type = CompletionType.CLASS,
            description = "A directed line with an arrow head",
            expandTo = "Arrow(start=LEFT, end=RIGHT)",
            tutorHint = "Draws a line with an arrowhead pointing from start to end, perfect for representing vectors.",
            tutorExample = "vector = Arrow(start=ORIGIN, end=UP+RIGHT, color=BLUE)\nself.play(GrowArrow(vector))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "DashedLine(",
            type = CompletionType.CLASS,
            description = "A dashed straight line",
            expandTo = "DashedLine(start=LEFT, end=RIGHT)",
            tutorHint = "Draws a line made of short dashed segments instead of a solid line, useful for auxiliary grid lines.",
            tutorExample = "dline = DashedLine(start=UP, end=DOWN)\nself.play(Create(dline))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Dot(",
            type = CompletionType.CLASS,
            description = "A small filled point circle",
            expandTo = "Dot(point=ORIGIN)",
            tutorHint = "Draws a tiny filled circle representing a single coordinate point.",
            tutorExample = "point = Dot(point=LEFT+UP, color=RED)\nself.play(Create(point))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "Axes(",
            type = CompletionType.CLASS,
            description = "A coordinate coordinate axis system",
            expandTo = "Axes(x_range=[-3, 3, 1], y_range=[-3, 3, 1])",
            tutorHint = "Generates a 2D Cartesian coordinate plane with horizontal and vertical axes.",
            tutorExample = "axes = Axes(x_range=[-2, 2], y_range=[-1, 1])\nself.play(Create(axes))",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "NumberPlane(",
            type = CompletionType.CLASS,
            description = "A coordinate grid system backdrop",
            expandTo = "NumberPlane()",
            tutorHint = "Creates a fully gridded coordinate system. Extremely helpful as a backdrop for plotting vector movement.",
            tutorExample = "grid = NumberPlane()\nself.add(grid)",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "Text(",
            type = CompletionType.CLASS,
            description = "A simple text string rendering",
            expandTo = "Text(\"Hello\")",
            tutorHint = "Draws plain text strings using system fonts. Good for annotations, labels, and explanations.",
            tutorExample = "txt = Text(\"Hello World\", font_size=28)\nself.play(Write(txt))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "MathTex(",
            type = CompletionType.CLASS,
            description = "Render a math equation",
            expandTo = """MathTex(r"\frac{a}{b}")""",
            tutorHint = "Renders a LaTeX math equation as an animation object. Use raw strings (r\"...\") to avoid Python escaping issues. Common notation: \\frac{a}{b} for fractions, ^2 for squared.",
            tutorExample = """eq = MathTex(r"E = mc^2")\nself.play(Write(eq))""",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "Tex(",
            type = CompletionType.CLASS,
            description = "Render standard LaTeX text block",
            expandTo = """Tex(r"Standard LaTeX")""",
            tutorHint = "Uses LaTeX engine to compile complex text blocks, support custom layouts, symbols, and formatting.",
            tutorExample = """t = Tex(r"Euler's formula: ${'$'}e^{i\\pi} + 1 = 0${'$'}")
self.play(Write(t))""",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "VGroup(",
            type = CompletionType.CLASS,
            description = "Group multiple objects",
            expandTo = "VGroup(obj1, obj2)",
            tutorHint = "Groups multiple objects so you can move, scale, or animate them together as one unit. Like grouping layers in a design app.",
            tutorExample = "group = VGroup(circle, square, text)\ngroup.shift(LEFT * 2)",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "Surface(",
            type = CompletionType.CLASS,
            description = "A parametric 3D surface mesh",
            expandTo = "Surface(func)",
            tutorHint = "Creates a 3D parametric function surface grid representation inside a ThreeDScene.",
            tutorExample = "surface = Surface(lambda u, v: np.array([u, v, u*v]))\nself.add(surface)",
            difficulty = Difficulty.ADVANCED,
        ))
        add(Completion(
            label = "Sphere(",
            type = CompletionType.CLASS,
            description = "A 3D sphere object",
            expandTo = "Sphere(radius=1.0)",
            tutorHint = "Renders a perfect 3D spherical mesh. Must be used inside a ThreeDScene.",
            tutorExample = "ball = Sphere(radius=1.2, color=BLUE)\nself.play(Create(ball))",
            difficulty = Difficulty.ADVANCED,
        ))
        add(Completion(
            label = "Cylinder(",
            type = CompletionType.CLASS,
            description = "A 3D cylinder shape",
            expandTo = "Cylinder(radius=1.0, height=2.0)",
            tutorHint = "Renders a 3D cylinder. Must be used inside a ThreeDScene.",
            tutorExample = "pipe = Cylinder(radius=0.5, height=3.0)\nself.add(pipe)",
            difficulty = Difficulty.ADVANCED,
        ))
        add(Completion(
            label = "Cone(",
            type = CompletionType.CLASS,
            description = "A 3D cone shape",
            expandTo = "Cone(base_radius=1.0, height=2.0)",
            tutorHint = "Renders a 3D cone mesh. Must be used inside a ThreeDScene.",
            tutorExample = "cone = Cone(base_radius=0.8, height=1.5)\nself.add(cone)",
            difficulty = Difficulty.ADVANCED,
        ))

        // Animations
        add(Completion(
            label = "Create(",
            type = CompletionType.FUNCTION,
            description = "Draw borders and outlines",
            expandTo = "Create(mobject)",
            tutorHint = "Draws the contour lines of a vector shape over time. Outstanding for introducing line art, circles, and curves.",
            tutorExample = "self.play(Create(circle, run_time=2))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "Write(",
            type = CompletionType.FUNCTION,
            description = "Handwriting text animation",
            expandTo = "Write(text)",
            tutorHint = "Simulates handwriting or letter-by-letter typesetting. Best suited for Text, Tex, and MathTex objects.",
            tutorExample = "self.play(Write(text))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "FadeIn(",
            type = CompletionType.FUNCTION,
            description = "Gradually make object visible",
            expandTo = "FadeIn(mobject)",
            tutorHint = "Smoothly transitions an object from 100% transparency to fully visible.",
            tutorExample = "self.play(FadeIn(square))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "FadeOut(",
            type = CompletionType.FUNCTION,
            description = "Gradually make object invisible",
            expandTo = "FadeOut(mobject)",
            tutorHint = "Smoothly transitions an object from fully visible to 100% transparent.",
            tutorExample = "self.play(FadeOut(circle))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "Transform(",
            type = CompletionType.FUNCTION,
            description = "Morph shape A into shape B",
            expandTo = "Transform(mobjectA, mobjectB)",
            tutorHint = "Morphs one shape into another. The physical properties of shape A are morph-animated to look like shape B.",
            tutorExample = "self.play(Transform(circle, square))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "ReplacementTransform(",
            type = CompletionType.FUNCTION,
            description = "Morph and replace object",
            expandTo = "ReplacementTransform(mobjectA, mobjectB)",
            tutorHint = "Similar to Transform, but completely removes shape A from the memory grid and replaces it with shape B.",
            tutorExample = "self.play(ReplacementTransform(circle, square))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "GrowArrow(",
            type = CompletionType.FUNCTION,
            description = "Grow vector arrow from start",
            expandTo = "GrowArrow(arrow)",
            tutorHint = "Animates an arrow growing outward from its starting tail point to its tip.",
            tutorExample = "self.play(GrowArrow(vector))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "GrowFromCenter(",
            type = CompletionType.FUNCTION,
            description = "Scale up shape from its center",
            expandTo = "GrowFromCenter(mobject)",
            tutorHint = "Scales up a geometric shape from size 0 to full size, originating from its gravitational center.",
            tutorExample = "self.play(GrowFromCenter(star))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "LaggedStart(",
            type = CompletionType.FUNCTION,
            description = "Run animations sequentially",
            expandTo = "LaggedStart(anim1, anim2, lag_ratio=0.5)",
            tutorHint = "Plays a list of animations with a delay gap ratio between the start of each. Very elegant for group reveals.",
            tutorExample = "self.play(LaggedStart(*[Create(m) for m in group], lag_ratio=0.2))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "AnimationGroup(",
            type = CompletionType.FUNCTION,
            description = "Group animations together",
            expandTo = "AnimationGroup(anim1, anim2)",
            tutorHint = "Combines several animations into a single block. Useful when nesting layout animations inside LaggedStart.",
            tutorExample = "self.play(AnimationGroup(FadeIn(a), Write(b)))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "Indicate(",
            type = CompletionType.FUNCTION,
            description = "Highlight/scale object briefly",
            expandTo = "Indicate(mobject)",
            tutorHint = "Briefly enlarges and flashes the color of an object to direct the viewer's attention to it.",
            tutorExample = "self.play(Indicate(equation))",
            difficulty = Difficulty.BEGINNER
        ))
        add(Completion(
            label = "Flash(",
            type = CompletionType.FUNCTION,
            description = "Emit light rays from point",
            expandTo = "Flash(point)",
            tutorHint = "Emits bright outward radial lines from a coordinate point, mimicking a flash of light.",
            tutorExample = "self.play(Flash(point))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "Circumscribe(",
            type = CompletionType.FUNCTION,
            description = "Draw outline boundary frame",
            expandTo = "Circumscribe(mobject)",
            tutorHint = "Draws a temporary highlight circle or box outline wrapping around a target object.",
            tutorExample = "self.play(Circumscribe(text))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "MoveAlongPath(",
            type = CompletionType.FUNCTION,
            description = "Move object along a vector line",
            expandTo = "MoveAlongPath(mobject, path)",
            tutorHint = "Translates an object over time along the curvature of another vector shape (like a line or arc).",
            tutorExample = "self.play(MoveAlongPath(dot, line))",
            difficulty = Difficulty.INTERMEDIATE
        ))
        add(Completion(
            label = "Rotate(",
            type = CompletionType.FUNCTION,
            description = "Spin object by an angle",
            expandTo = "Rotate(mobject, angle=PI)",
            tutorHint = "Spins a shape around its axis by a specified angle in radians.",
            tutorExample = "self.play(Rotate(square, angle=PI/4))",
            difficulty = Difficulty.INTERMEDIATE
        ))

        // Colors
        val colors = listOf(
            "WHITE" to "Standard white color",
            "BLACK" to "Standard black color",
            "RED" to "Bright red color",
            "BLUE" to "Vibrant blue color",
            "GREEN" to "Clean green color",
            "YELLOW" to "Saturated yellow color",
            "ORANGE" to "Vibrant orange color",
            "PURPLE" to "Deep purple color",
            "PINK" to "Soft pink color",
            "GRAY" to "Neutral gray tone",
            "GREY" to "Neutral gray tone",
            "GOLD" to "Golden accent color",
            "TEAL" to "Calm teal/cyan color",
            "MAROON" to "Deep maroon/burgundy color"
        )
        colors.forEach { (colorName, desc) ->
            add(Completion(
                label = colorName,
                type = CompletionType.CONSTANT,
                description = desc,
                expandTo = colorName,
                tutorHint = "Constant color vector. Can be passed directly to shape builders or used to color text.",
                tutorExample = "circle = Circle(color=$colorName)",
                difficulty = Difficulty.BEGINNER
            ))
        }

        // Directions
        val directions = listOf(
            "UP" to "Vector offset of [0, 1, 0] (shift up)",
            "DOWN" to "Vector offset of [0, -1, 0] (shift down)",
            "LEFT" to "Vector offset of [-1, 0, 0] (shift left)",
            "RIGHT" to "Vector offset of [1, 0, 0] (shift right)",
            "ORIGIN" to "Vector at center coordinate [0, 0, 0]",
            "UL" to "Upper left corner vector offset",
            "UR" to "Upper right corner vector offset",
            "DL" to "Down left corner vector offset",
            "DR" to "Down right corner vector offset"
        )
        directions.forEach { (dirName, desc) ->
            add(Completion(
                label = dirName,
                type = CompletionType.CONSTANT,
                description = desc,
                expandTo = dirName,
                tutorHint = "Direction vector constant. Used to shift or align objects relative to the screen axes.",
                tutorExample = "title.to_edge($dirName)\ncircle.shift($dirName * 1.5)",
                difficulty = Difficulty.BEGINNER
            ))
        }

        // Math constants
        add(Completion("PI", CompletionType.CONSTANT, "Pi constant (3.1415)", "PI", "Standard mathematical constant representing half-rotation in radians.", "circle.rotate(PI / 2)", Difficulty.BEGINNER))
        add(Completion("TAU", CompletionType.CONSTANT, "Tau constant (2*Pi)", "TAU", "Mathematical constant representing full-rotation in radians.", "circle.rotate(TAU)", Difficulty.BEGINNER))
        add(Completion("DEGREES", CompletionType.CONSTANT, "Degree conversion unit", "DEGREES", "Unit constant to convert normal angles (degrees) into radians.", "self.play(Rotate(square, angle=45 * DEGREES))", Difficulty.BEGINNER))

        // self.play patterns
        add(Completion(
            label = "self.play(",
            type = CompletionType.SNIPPET,
            description = "Play an animation",
            expandTo = "self.play()",
            tutorHint = "Runs one or more animations simultaneously. Put any animation function inside the brackets. Everything inside plays at the same time.",
            tutorExample = "self.play(Create(circle), Write(text))\n# Plays both at once",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "self.wait(",
            type = CompletionType.SNIPPET,
            description = "Wait / Pause scene",
            expandTo = "self.wait(1.0)",
            tutorHint = "Pauses scene progression. This holds the screen static for a specified length of seconds before showing the next animation.",
            tutorExample = "self.play(FadeIn(circle))\nself.wait(1.5)\nself.play(FadeOut(circle))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "self.add(",
            type = CompletionType.SNIPPET,
            description = "Display object instantly",
            expandTo = "self.add(mobject)",
            tutorHint = "Places an object directly on the canvas grid immediately without playing any transition animation.",
            tutorExample = "self.add(grid)\nself.play(Create(circle))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "self.remove(",
            type = CompletionType.SNIPPET,
            description = "Remove object instantly",
            expandTo = "self.remove(mobject)",
            tutorHint = "Erases an object from the screen layout instantly without showing any fading or transition animation.",
            tutorExample = "self.remove(circle)",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "self.camera.frame.animate",
            type = CompletionType.SNIPPET,
            description = "Camera zoom animation anchor",
            expandTo = "self.camera.frame.animate",
            tutorHint = "Target object to animate camera panning and zooming. Must inherit from MovingCameraScene.",
            tutorExample = "self.play(self.camera.frame.animate.scale(0.5).move_to(dot))",
            difficulty = Difficulty.INTERMEDIATE,
        ))

        // Python basics — for non-programmers
        add(Completion(
            label = "for i in range(10):",
            type = CompletionType.SNIPPET,
            description = "Loop code block 10 times",
            expandTo = "for i in range(10):\n    ",
            tutorHint = "Repeats the code block placed underneath. Very helpful for playing the same animation repeatedly on a series of objects.",
            tutorExample = "for i in range(4):\n    self.play(Rotate(square, angle=90*DEGREES))",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "import numpy as np",
            type = CompletionType.SNIPPET,
            description = "Import numeric computations library",
            expandTo = "import numpy as np",
            tutorHint = "Imports the standard NumPy mathematics array package. Perfect for generating custom equations and graph calculations.",
            tutorExample = "curve = axes.plot(lambda x: np.sin(x))",
            difficulty = Difficulty.INTERMEDIATE,
        ))

        // Manim common patterns — full working snippets
        add(Completion(
            label = "add_axes",
            type = CompletionType.SNIPPET,
            description = "Add labeled axes to scene",
            expandTo = """axes = Axes(
    x_range=[-3, 3, 1],
    y_range=[-3, 3, 1],
    x_length=6,
    y_length=6,
    axis_config={"color": GRAY, "include_numbers": True},
)
self.play(Create(axes))""",
            tutorHint = "A pre-configured coordinate system complete with number labels. Adds Cartesian boundaries to the screen immediately.",
            tutorExample = "# Simply choose add_axes to generate coordinate lines",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "animate_text",
            type = CompletionType.SNIPPET,
            description = "Write text to screen",
            expandTo = """text = Text("Your text here", font_size=36, color=WHITE)
text.to_edge(UP)
self.play(Write(text))
self.wait(1)""",
            tutorHint = "A complete prebuilt flow to declare some text at the top margin and write it beautifully.",
            tutorExample = "# Complete text header boilerplate",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "morph_shapes",
            type = CompletionType.SNIPPET,
            description = "Transform one shape into another",
            expandTo = """shape1 = Circle(radius=1, color=BLUE, fill_opacity=0.5)
shape2 = Square(side_length=2, color=RED, fill_opacity=0.5)
self.play(Create(shape1))
self.wait(0.5)
self.play(Transform(shape1, shape2))
self.wait(1)""",
            tutorHint = "Complete demonstration snippet drawing a circle and transforming it smoothly into a square.",
            tutorExample = "# Complete morphing animation boilerplate",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "plot_function",
            type = CompletionType.SNIPPET,
            description = "Plot a math function",
            expandTo = """axes = Axes(x_range=[-3, 3, 1], y_range=[-2, 2, 1])
curve = axes.plot(lambda x: np.sin(x), color=BLUE)
label = axes.get_graph_label(curve, r"\\sin(x)")
self.play(Create(axes), Create(curve), Write(label))""",
            tutorHint = "Plots a continuous NumPy function (sine wave) with coordinate plane grid lines and a math label automatically.",
            tutorExample = "# Complete mathematical curve plotting flow",
            difficulty = Difficulty.INTERMEDIATE,
        ))
        add(Completion(
            label = "fade_all",
            type = CompletionType.SNIPPET,
            description = "Fade everything out",
            expandTo = "self.play(*[FadeOut(m) for m in self.mobjects])",
            tutorHint = "A quick one-liner that clears the screen by fading out every active object currently displayed in the scene.",
            tutorExample = "self.play(*[FadeOut(m) for m in self.mobjects])",
            difficulty = Difficulty.BEGINNER,
        ))
        add(Completion(
            label = "construct_template",
            type = CompletionType.SNIPPET,
            description = "Full scene template",
            expandTo = """def construct(self):
        title = Text("Title", font_size=36, color=WHITE)
        title.to_edge(UP)
        self.play(Write(title))
        self.wait(0.5)
        
        # Your animation here
        
        self.wait(1.5)
        self.play(*[FadeOut(m) for m in self.mobjects])""",
            tutorHint = "Generates the standard structural entry method for a Manim script with headers and exit wipes ready to modify.",
            tutorExample = "# Complete scene layout skeleton",
            difficulty = Difficulty.BEGINNER,
        ))
    }

    fun getSuggestions(
        currentWord: String,
        fullCode: String,
        cursorLine: Int,
    ): List<Completion> {
        if (currentWord.length < 2) return emptyList()

        val query = currentWord.lowercase()
        return completions
            .filter { it.label.lowercase().startsWith(query) ||
                      it.label.lowercase().contains(query) }
            .sortedWith(compareBy(
                { !it.label.lowercase().startsWith(query) },
                { it.label.length },
            ))
            .take(6)
    }

    fun getCurrentWord(text: String, cursorPosition: Int): String {
        if (cursorPosition <= 0 || cursorPosition > text.length) return ""
        var start = cursorPosition - 1
        while (start >= 0 && (text[start].isLetterOrDigit() || text[start] == '_')) {
            start--
        }
        return text.substring(start + 1, cursorPosition)
    }
}

data class Completion(
    val label: String,
    val type: CompletionType,
    val description: String,
    val expandTo: String? = null,
    // Tutor fields
    val tutorHint: String? = null,    // "What does this do?"
    val tutorExample: String? = null, // Short example code
    val difficulty: Difficulty = Difficulty.BEGINNER,
)

enum class CompletionType {
    CLASS, FUNCTION, CONSTANT, SNIPPET, KEYWORD
}

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
