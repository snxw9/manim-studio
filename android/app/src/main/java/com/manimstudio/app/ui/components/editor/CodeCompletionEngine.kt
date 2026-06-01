package com.manimstudio.app.ui.components.editor

object CodeCompletionEngine {

    // All completions the editor knows about
    private val completions = buildList {
        // Manim scene classes
        addAll(listOf("Scene", "MovingCameraScene", "ThreeDScene",
            "ZoomedScene").map { Completion(it, CompletionType.CLASS, "Scene base class") })

        // Manim shapes
        addAll(listOf(
            "Circle(radius=1.0, color=BLUE)",
            "Square(side_length=2.0, color=RED)",
            "Triangle(color=GREEN)",
            "Rectangle(width=4.0, height=2.0)",
            "Polygon(",
            "RegularPolygon(n=6)",
            "Ellipse(width=3.0, height=1.5)",
            "Arc(radius=1.0, start_angle=0, angle=PI)",
            "Line(start=LEFT, end=RIGHT)",
            "Arrow(start=LEFT, end=RIGHT)",
            "DashedLine(",
            "Dot(point=ORIGIN)",
            "Axes(",
            "NumberPlane(",
            "Text(\"\")",
            "MathTex(r\"\")",
            "Tex(r\"\")",
            "VGroup(",
            "Surface(",
            "Sphere(radius=1.0)",
            "Cylinder(radius=1.0, height=2.0)",
            "Cone(base_radius=1.0, height=2.0)",
        ).map { Completion(it, CompletionType.CLASS, "Manim object") })

        // Animations
        addAll(listOf(
            "Create(", "Write(", "FadeIn(", "FadeOut(",
            "Transform(", "ReplacementTransform(",
            "GrowArrow(", "GrowFromCenter(",
            "LaggedStart(", "AnimationGroup(",
            "Indicate(", "Flash(", "Circumscribe(",
            "MoveAlongPath(", "Rotate(",
        ).map { Completion(it, CompletionType.FUNCTION, "Animation") })

        // Colors
        addAll(listOf(
            "WHITE", "BLACK", "RED", "BLUE", "GREEN", "YELLOW",
            "ORANGE", "PURPLE", "PINK", "GRAY", "GREY",
            "BLUE_A", "BLUE_B", "BLUE_C", "BLUE_D", "BLUE_E",
            "RED_A", "RED_B", "RED_C", "RED_D",
            "GREEN_A", "GREEN_B", "GREEN_C",
            "GOLD", "TEAL", "MAROON",
        ).map { Completion(it, CompletionType.CONSTANT, "Color constant") })

        // Directions
        addAll(listOf(
            "UP", "DOWN", "LEFT", "RIGHT",
            "UL", "UR", "DL", "DR",
            "ORIGIN", "IN", "OUT",
            "LEFT_SIDE", "RIGHT_SIDE",
        ).map { Completion(it, CompletionType.CONSTANT, "Direction vector") })

        // Math constants
        addAll(listOf(
            "PI", "TAU", "DEGREES",
        ).map { Completion(it, CompletionType.CONSTANT, "Math constant") })

        // self.play patterns
        addAll(listOf(
            "self.play()",
            "self.wait(1)",
            "self.add()",
            "self.remove()",
            "self.camera.frame.animate",
            "self.set_camera_orientation(",
        ).map { Completion(it, CompletionType.SNIPPET, "Scene method") })

        // Python basics — for non-programmers
        addAll(listOf(
            "import numpy as np",
            "import math",
            "for i in range(10):",
            "while True:",
            "if condition:",
            "else:",
            "print()",
            "def my_function():",
            "return value",
            "# comment",
        ).map { Completion(it, CompletionType.SNIPPET, "Python syntax") })

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
        ))
        add(Completion(
            label = "animate_text",
            type = CompletionType.SNIPPET,
            description = "Write text to screen",
            expandTo = """text = Text("Your text here", font_size=36, color=WHITE)
text.to_edge(UP)
self.play(Write(text))
self.wait(1)""",
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
        ))
        add(Completion(
            label = "plot_function",
            type = CompletionType.SNIPPET,
            description = "Plot a math function",
            expandTo = """axes = Axes(x_range=[-3, 3, 1], y_range=[-2, 2, 1])
curve = axes.plot(lambda x: np.sin(x), color=BLUE)
label = axes.get_graph_label(curve, r"\\sin(x)")
self.play(Create(axes), Create(curve), Write(label))""",
        ))
        add(Completion(
            label = "fade_all",
            type = CompletionType.SNIPPET,
            description = "Fade everything out",
            expandTo = "self.play(*[FadeOut(m) for m in self.mobjects])",
        ))

        // Common patterns as snippets
        add(Completion(
            "construct_template",
            CompletionType.SNIPPET,
            "Full scene template",
            expandTo = """def construct(self):
        title = Text("Title", font_size=36, color=WHITE)
        title.to_edge(UP)
        self.play(Write(title))
        self.wait(0.5)
        
        # Your animation here
        
        self.wait(1.5)
        self.play(*[FadeOut(m) for m in self.mobjects])""",
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
)

enum class CompletionType {
    CLASS, FUNCTION, CONSTANT, SNIPPET, KEYWORD
}
