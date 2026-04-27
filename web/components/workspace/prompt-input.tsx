import { useState, useEffect } from "react";
import { ArrowUp, Sparkles, Lightbulb, RefreshCw, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";

interface PromptInputProps {
  onSubmit?: (prompt: string) => void;
  isGenerating?: boolean;
}

const ALL_SUGGESTIONS = [
  "Draw a unit circle and animate sine and cosine wave projections",
  "Visualize Riemann sums converging to the integral of x²",
  "Animate a 2D matrix transformation stretching a coordinate grid",
  "Show a Taylor series approximating sin(x) term by term",
  "Morph a square into a circle using Manim's Transform",
  "Trace the cycloid path of a point on a rolling circle",
  "Animate eigenvalues and eigenvectors of a 2×2 matrix",
  "Draw a 3D surface plot of z = sin(x)·cos(y)",
  "Visualize the Fourier series building a square wave",
  "Show the Sierpiński triangle drawn recursively to depth 6",
  "Animate gradient descent rolling down a parabolic surface",
  "Graph a parametric curve and sweep its arc length in real time",
  "Show complex number multiplication rotating on the Argand plane",
  "Animate the Sieve of Eratosthenes crossing out composite numbers",
  "Draw Bézier curves by lerping between control points",
  "Visualize the dot product as a vector projection",
  "Show the Collatz sequence as a branching tree animation",
  "Animate a pendulum and trace its phase space trajectory",
  "Draw the Koch snowflake fractal iterating to depth 5",
  "Show a vector field with colour-coded magnitude arrows",
];

const BATCH_SIZE = 4;
const ROTATE_MS  = 10000;

function getBatch(index: number): string[] {
  const start = (index * BATCH_SIZE) % ALL_SUGGESTIONS.length;
  const result: string[] = [];
  for (let i = 0; i < BATCH_SIZE; i++) {
    result.push(ALL_SUGGESTIONS[(start + i) % ALL_SUGGESTIONS.length]);
  }
  return result;
}

export function PromptInput({ onSubmit, isGenerating }: PromptInputProps) {
  const [value,        setValue]        = useState("");
  const [focused,      setFocused]      = useState(false);
  const [batchIndex,   setBatchIndex]   = useState(0);
  const [visible,      setVisible]      = useState(true);

  useEffect(() => {
    const id = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setBatchIndex(i => i + 1);
        setVisible(true);
      }, 300);
    }, ROTATE_MS);
    return () => clearInterval(id);
  }, []);

  const suggestions = getBatch(batchIndex);

  const handleSubmit = () => {
    if (!value.trim() || isGenerating) return;
    onSubmit?.(value.trim());
    setValue("");
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const rotateSuggestions = () => {
    setVisible(false);
    setTimeout(() => {
      setBatchIndex(i => i + 1);
      setVisible(true);
    }, 200);
  };

  return (
    <div className="flex flex-col h-full bg-background p-4">
      <div className="flex-1 flex flex-col justify-center max-w-2xl mx-auto w-full gap-6">
        <div className="text-center space-y-1.5">
          <h2 className="text-lg font-semibold text-foreground">Describe your animation</h2>
          <p className="text-sm text-muted-foreground">
            Tell Manim Studio what you want to animate and it will generate the code for you.
          </p>
        </div>

        <div
          className={`relative flex flex-col bg-card border rounded-2xl shadow-sm transition-all duration-200 ${
            focused
              ? "border-primary/60 shadow-md glow-orange"
              : "border-border hover:border-border/80"
          }`}
        >
          <div className="flex items-start gap-3 px-4 pt-3.5 pb-2">
            <Sparkles className="h-4 w-4 text-primary mt-0.5 shrink-0" />
            <textarea
              rows={3}
              value={value}
              onChange={e => setValue(e.target.value)}
              onFocus={() => setFocused(true)}
              onBlur={() => setFocused(false)}
              onKeyDown={handleKeyDown}
              disabled={isGenerating}
              placeholder="e.g. 'Animate a 2D linear transformation on a vector grid'"
              className="flex-1 bg-transparent border-none outline-none text-sm text-foreground placeholder:text-muted-foreground resize-none leading-relaxed"
              data-testid="input-prompt"
            />
          </div>

          <div className="flex items-center justify-between px-3 pb-2.5">
            <p className="text-[11px] text-muted-foreground/50">
              {isGenerating ? "Generating code..." : "Press Enter to generate · Shift+Enter for new line"}
            </p>
            <Button
              size="icon"
              disabled={!value.trim() || isGenerating}
              onClick={handleSubmit}
              className="h-8 w-8 rounded-xl bg-primary hover:bg-primary/90 text-primary-foreground disabled:opacity-30 shrink-0 shadow-sm"
              data-testid="button-send-prompt"
            >
              {isGenerating ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <ArrowUp className="h-4 w-4" />
              )}
            </Button>
          </div>
        </div>

        {/* Suggestions with rotate button */}
        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between px-0.5">
            <span className="text-[10px] text-muted-foreground/50 uppercase tracking-wider font-semibold">
              Try an example
            </span>
            <button
              onClick={rotateSuggestions}
              className="flex items-center gap-1 text-[10px] text-muted-foreground/50 hover:text-primary transition-colors"
              data-testid="button-rotate-suggestions"
              title="Show more suggestions"
            >
              <RefreshCw className="h-3 w-3" />
              More
            </button>
          </div>

          <div
            className="grid grid-cols-2 gap-2 transition-opacity duration-300"
            style={{ opacity: visible ? 1 : 0 }}
          >
            {suggestions.map((s, i) => (
              <button
                key={`${batchIndex}-${i}`}
                onClick={() => setValue(s)}
                className="text-left text-[12px] text-muted-foreground hover:text-primary bg-muted/40 hover:bg-primary/10 border border-border/60 hover:border-primary/40 rounded-xl px-3 py-2.5 transition-colors cursor-pointer leading-snug"
                data-testid={`button-suggestion-${i}`}
              >
                {s}
              </button>
            ))}
          </div>
        </div>

        <div className="flex items-center justify-center gap-1.5">
          <Lightbulb className="h-3 w-3 text-muted-foreground/50" />
          <p className="text-[11px] text-muted-foreground/50">
            Describe the math, the motion, and the visual style you want.
          </p>
        </div>
      </div>
    </div>
  );
}
