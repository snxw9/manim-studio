export type ErrorSource = 'generated_code' | 'engine' | 'user_code' | 'network' | 'unknown';

export interface ErrorInfo {
  code: string;
  title: string;
  what: string;        // plain English: what happened
  why: string;         // simple: why it happened  
  action: string;      // clear: what to do
  source: ErrorSource;
  canRetry: boolean;
  shouldReport: boolean;
}

const UNKNOWN_ERROR: ErrorInfo = {
  code: "UNKNOWN",
  title: "Unexpected Error",
  what: "Something went wrong while rendering the animation.",
  why: "An unexpected error occurred that we haven't seen before.",
  action: "Try clicking Generate again for a fresh attempt. If it keeps happening, use the Report button to let the developer know.",
  source: 'unknown',
  canRetry: true,
  shouldReport: true,
};

export const ERROR_DEFINITIONS: Array<{
  pattern: RegExp;
  info: ErrorInfo;
}> = [
  {
    pattern: /ParametricSurface/i,
    info: {
      code: "GEN_001",
      title: "Outdated Manim Feature",
      what: "The AI used a Manim feature that no longer exists in the current version.",
      why: "ParametricSurface was removed from Manim in v0.17 and replaced with Surface(). The AI learned from old examples.",
      action: "Click 'Try Again' — the validator will auto-fix this automatically.",
      source: 'generated_code',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /object has no attribute 'frame'/i,
    info: {
      code: "GEN_002",
      title: "Wrong Scene Type",
      what: "The animation tried to move the camera but used the wrong scene type.",
      why: "Camera movement requires MovingCameraScene instead of Scene. The AI made a mistake choosing the base class.",
      action: "Click 'Try Again' — the validator now catches this automatically.",
      source: 'generated_code',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /NameError: name '(\w+)' is not defined/i,
    info: {
      code: "GEN_003",
      title: "Unknown Manim Feature",
      what: "The AI used a Manim class or function that doesn't exist.",
      why: "The AI invented or misremembered a Manim feature name. This is a known limitation of AI code generation.",
      action: "Click 'Try Again' to generate fresh code. If it keeps failing, try rephrasing your prompt to be more specific about what you want to see.",
      source: 'generated_code',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /SyntaxError/i,
    info: {
      code: "GEN_004",
      title: "Code Syntax Error",
      what: "The generated code has a Python syntax error and cannot run.",
      why: "The AI produced malformed code — this sometimes happens with complex requests.",
      action: "Click 'Try Again'. If you edited the code yourself, check for missing colons, brackets, or quotes in the Code tab.",
      source: 'generated_code',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /latex.*failed|check your latex|LaTeX Error/i,
    info: {
      code: "SYS_001",
      title: "LaTeX Error",
      what: "The math equation renderer (LaTeX) failed to process a formula.",
      why: "Either LaTeX is not properly installed, a required package is missing, or the formula contains invalid syntax.",
      action: "Open MiKTeX Console and click 'Check for updates' to install missing packages. Then try again. If the problem persists, report this to the developer.",
      source: 'engine',
      canRetry: true,
      shouldReport: true,
    },
  },
  {
    pattern: /timed out after \d+s/i,
    info: {
      code: "SYS_002",
      title: "Render Timed Out",
      what: "The animation took too long to render and was stopped.",
      why: "Complex animations take more time at higher quality settings. The current quality setting may be too high for this animation.",
      action: "Try a lower quality setting (480p or 720p) and render again. If the animation is very complex, it may just need more time.",
      source: 'engine',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /ECONNREFUSED|engine.*offline|Engine is offline/i,
    info: {
      code: "SYS_003",
      title: "Engine Offline",
      what: "The rendering engine is not running.",
      why: "The Python engine that processes and renders animations has stopped or was never started.",
      action: "Double-click START.bat in your project folder to restart everything. The green dot in the bottom bar will turn on when it's ready.",
      source: 'engine',
      canRetry: false,
      shouldReport: false,
    },
  },
  {
    pattern: /quota|rate.?limit|429/i,
    info: {
      code: "SYS_004",
      title: "AI Quota Exhausted",
      what: "The AI service has reached its usage limit for today.",
      why: "Free AI APIs have daily limits. All available providers are currently at their limit.",
      action: "Wait a few minutes and try again — limits reset frequently. Or add your own free Groq API key in Settings for unlimited generations.",
      source: 'engine',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /No Scene class found|Missing construct/i,
    info: {
      code: "GEN_005",
      title: "Invalid Animation Structure",
      what: "The generated code is missing required parts to be a valid animation.",
      why: "Every Manim animation needs a class that extends Scene and a construct() method. The AI omitted one of these.",
      action: "Click 'Try Again' for a fresh attempt.",
      source: 'generated_code',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /TypeError.*got.*unexpected keyword argument/i,
    info: {
      code: "GEN_006",
      title: "Invalid Property",
      what: "The AI gave an invalid property to a Manim shape or object.",
      why: "Different Manim objects accept different properties. The AI used one that doesn't apply here (e.g. width= on a Circle).",
      action: "Click 'Try Again' — the validator will catch this automatically.",
      source: 'generated_code',
      canRetry: true,
      shouldReport: false,
    },
  },
  {
    pattern: /No output file found|No mp4 found/i,
    info: {
      code: "SYS_005",
      title: "No Video Produced",
      what: "The animation ran but no video file was created.",
      why: "The animation may have had no visible content, or an issue occurred during video encoding.",
      action: "Make sure the animation has at least one self.play() call with a visible object. Try again or simplify your prompt.",
      source: 'engine',
      canRetry: true,
      shouldReport: true,
    },
  },
];

export function analyzeError(errorText: string): ErrorInfo {
  for (const { pattern, info } of ERROR_DEFINITIONS) {
    if (pattern.test(errorText)) return info;
  }
  return UNKNOWN_ERROR;
}

export function getSourceLabel(source: ErrorSource): string {
  switch (source) {
    case 'generated_code': return 'AI Generation Issue';
    case 'engine':         return 'System Issue';
    case 'user_code':      return 'Code Issue';
    case 'network':        return 'Connection Issue';
    default:               return 'Unknown Issue';
  }
}

export function getSourceColor(source: ErrorSource): string {
  switch (source) {
    case 'generated_code': return '#f59e0b'; // amber — AI's fault, retryable
    case 'engine':         return '#ef4444'; // red — system problem
    case 'user_code':      return '#8b5cf6'; // purple — user's code
    case 'network':        return '#6b7280'; // gray — connection
    default:               return '#6b7280';
  }
}
