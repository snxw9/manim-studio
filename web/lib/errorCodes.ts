export interface ErrorEntry {
  code: string;
  pattern: string | RegExp;
  title: string;
  explanation: string;
  fix: string;
}

export const ERROR_CODES: ErrorEntry[] = [
  {
    code: "MANIM_001",
    pattern: /unexpected keyword argument '(\w+)'/,
    title: "Invalid Constructor Argument",
    explanation: "A Manim class was called with an argument it does not accept.",
    fix: "Common fixes: Circle(radius=r) not Circle(width=w). Square(side_length=s) not Square(width=w). Text('...', font_size=n) not Text(width=n).",
  },
  {
    code: "MANIM_002",
    pattern: /object has no attribute '(\w+)'/,
    title: "Method Does Not Exist",
    explanation: "The generated code called a method that does not exist in Manim.",
    fix: "Common fixes: use get_length() not length(). Use get_center() not midpoint(). Use line_intersection() function not line.intersection().",
  },
  {
    code: "MANIM_003",
    pattern: /FileNotFoundError.*False/,
    title: "Invalid Manim Command",
    explanation: "A boolean value was passed as a command argument to Manim.",
    fix: "This is an engine bug. Restart the engine: cd engine && uvicorn main:app --reload --port 8000",
  },
  {
    code: "MANIM_004",
    pattern: /LaTeX.*error|latex.*failed|! Emergency stop/i,
    title: "LaTeX Compilation Error",
    explanation: "A MathTex or Tex call contains invalid LaTeX syntax.",
    fix: "Check all MathTex strings use raw strings: r\"\alpha\" not \"\alpha\". Check for unmatched braces { }.",
  },
  {
    code: "MANIM_005",
    pattern: /SyntaxError/,
    title: "Python Syntax Error",
    explanation: "The generated Python code has a syntax error.",
    fix: "Switch to the Code tab, find the highlighted error line, and fix the syntax. Or click Generate again for a fresh attempt.",
  },
  {
    code: "MANIM_006",
    pattern: /timed out|TimeoutError/i,
    title: "Render Timed Out",
    explanation: "The animation took too long to render.",
    fix: "Try a simpler prompt, or reduce animation complexity. Use 480p quality for faster renders. Avoid requests for very long animations.",
  },
  {
    code: "MANIM_007",
    pattern: /No Scene class found/,
    title: "Missing Scene Class",
    explanation: "The generated code has no class that inherits from Scene.",
    fix: "The code must contain: class MyScene(Scene): with a construct(self) method. Click Generate again.",
  },
  {
    code: "MANIM_008",
    pattern: /ECONNREFUSED|engine.*offline|Engine is offline/i,
    title: "Engine Offline",
    explanation: "The Python engine is not running.",
    fix: "Start the engine: cd engine && uvicorn main:app --reload --port 8000. Or run npm run dev from the web folder which auto-starts it.",
  },
  {
    code: "MANIM_009",
    pattern: /quota|rate.?limit|429/i,
    title: "API Quota Exceeded",
    explanation: "All AI providers have hit their usage limits.",
    fix: "Wait a few minutes for rate limits to reset, or add your own Groq API key in Settings for unlimited use. Get a free key at console.groq.com.",
  },
  {
    code: "MANIM_010",
    pattern: /multiple values for keyword argument/,
    title: "Duplicate Argument",
    explanation: "A Manim class received the same argument twice.",
    fix: "Common cause: Sector(outer_radius=...) — use Sector(radius=...) only.",
  },
  {
    code: "MANIM_011",
    pattern: /No output file found|No mp4 found/,
    title: "No Video Produced",
    explanation: "Manim ran but did not produce a video file.",
    fix: "Check the Code tab for logic errors. Make sure construct(self) calls at least one self.play() or self.add(). Try rendering again.",
  },
  {
    code: "MANIM_012",
    pattern: /ModuleNotFoundError|ImportError/,
    title: "Missing Python Module",
    explanation: "A required Python package is not installed.",
    fix: "Run in terminal: cd engine && venv\\Scripts\\pip install -r requirements.txt",
  },
];

export function matchError(errorText: string): ErrorEntry | null {
  for (const entry of ERROR_CODES) {
    const pattern = typeof entry.pattern === 'string'
      ? new RegExp(entry.pattern, 'i')
      : entry.pattern;
    if (pattern.test(errorText)) return entry;
  }
  return null;
}
