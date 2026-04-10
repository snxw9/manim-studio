import Editor from "@monaco-editor/react";
import { Terminal, Code2, CheckCircle, Clock } from "lucide-react";
import { useTheme } from "@/app/providers";

const INITIAL_CODE = `# Manim Animation Code
from manim import *

class MyAnimation(Scene):
    def construct(self):
        text = Text("Hello Manim!")
        self.play(Write(text))
        self.wait(1)
`;

interface CodeEditorProps {
  code?: string;
  onChange?: (code: string) => void;
}

export function CodeEditor({ code, onChange }: CodeEditorProps) {
  const { theme } = useTheme();

  return (
    <div className="flex flex-col h-full w-full relative">
      <div className="flex items-center justify-between h-10 px-3 border-b border-border bg-card shrink-0 rounded-t-xl">
        <div className="flex items-center gap-2 text-xs font-semibold text-foreground/80">
          <Code2 className="h-3.5 w-3.5 text-primary" />
          <span>Editor</span>
        </div>
        <div className="flex items-center gap-3 text-[10px] text-muted-foreground">
          <span className="font-mono bg-muted px-2 py-0.5 rounded-md">Python (Manim)</span>
        </div>
      </div>

      <div className="flex-1 relative w-full" style={{ background: theme === "dark" ? "#0f0f0f" : "#fafafa" }}>
        <Editor
          height="100%"
          defaultLanguage="python"
          value={code || INITIAL_CODE}
          onChange={(v) => onChange?.(v || "")}
          theme={theme === "dark" ? "vs-dark" : "light"}
          options={{
            minimap: { enabled: false },
            fontSize: 13,
            fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
            lineHeight: 1.7,
            padding: { top: 16, bottom: 16 },
            scrollBeyondLastLine: false,
            smoothScrolling: true,
            cursorBlinking: "smooth",
            renderLineHighlight: "line",
            scrollbar: { verticalScrollbarSize: 6, horizontalScrollbarSize: 6 },
            overviewRulerLanes: 0,
          }}
        />
      </div>

      <div className="h-28 border-t border-border bg-card flex flex-col shrink-0">
        <div className="flex items-center justify-between h-8 px-3 border-b border-border/60">
          <div className="flex items-center gap-1.5 text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
            <Terminal className="h-3 w-3" />
            Console
          </div>
          <div className="flex items-center gap-1 text-[10px] text-green-500 font-medium">
            <CheckCircle className="h-3 w-3" />
            Ready
          </div>
        </div>
        <div className="p-3 overflow-auto font-mono text-[11px] space-y-1.5 leading-relaxed">
          <div className="flex items-center gap-2 text-muted-foreground/60">
            <Clock className="h-3 w-3" />
            <span>Manim Engine initialized.</span>
          </div>
          <div className="text-muted-foreground/50">Ready to render.</div>
        </div>
      </div>
    </div>
  );
}
