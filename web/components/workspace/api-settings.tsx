import { useState } from "react";
import { ChevronUp, ChevronDown, Cpu, Zap, Bot, Plug } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

type ApiOption = "manim" | "openai" | "anthropic" | "custom";

interface ApiConfig {
  id: ApiOption;
  label: string;
  sublabel: string;
  icon: React.ReactNode;
  color: string;
}

const API_OPTIONS: ApiConfig[] = [
  {
    id: "manim",
    label: "Manim Community",
    sublabel: "Local engine · Open source",
    icon: <Cpu className="h-4 w-4" />,
    color: "text-emerald-500",
  },
  {
    id: "openai",
    label: "OpenAI Codex",
    sublabel: "GPT-4o · Cloud",
    icon: <Zap className="h-4 w-4" />,
    color: "text-sky-400",
  },
  {
    id: "anthropic",
    label: "Anthropic Claude",
    sublabel: "Claude 3.5 · Cloud",
    icon: <Bot className="h-4 w-4" />,
    color: "text-purple-400",
  },
  {
    id: "custom",
    label: "Custom Endpoint",
    sublabel: "Bring your own API",
    icon: <Plug className="h-4 w-4" />,
    color: "text-orange-400",
  },
];

interface ApiSettingsProps {
  isOpen: boolean;
  onToggle: () => void;
}

export function ApiSettings({ isOpen, onToggle }: ApiSettingsProps) {
  const [selected, setSelected] = useState<ApiOption>("manim");
  const [customUrl, setCustomUrl] = useState("");
  const [apiKey, setApiKey] = useState("");

  return (
    <div
      className={`border-t border-border bg-background shrink-0 transition-all duration-300 ease-in-out flex flex-col ${
        isOpen ? "h-44" : "h-10"
      }`}
    >
      <div
        className="flex items-center justify-between px-4 h-10 shrink-0 cursor-pointer hover:bg-muted/30 transition-colors"
        onClick={onToggle}
        data-testid="button-api-settings-toggle"
      >
        <div className="flex items-center gap-2 text-xs font-semibold text-foreground/80">
          <Cpu className="h-3.5 w-3.5 text-primary" />
          <span>API</span>
        </div>
        <Button variant="ghost" size="icon" className="h-6 w-6 text-muted-foreground rounded-md">
          {isOpen ? <ChevronDown className="h-4 w-4" /> : <ChevronUp className="h-4 w-4" />}
        </Button>
      </div>

      {isOpen && (
        <div className="flex flex-1 overflow-hidden border-t border-border/50 p-3 gap-3">
          {/* API picker */}
          <div className="flex gap-2 flex-wrap content-start">
            {API_OPTIONS.map(opt => (
              <button
                key={opt.id}
                onClick={() => setSelected(opt.id)}
                data-testid={`api-option-${opt.id}`}
                className={`flex items-center gap-2.5 px-3 py-2 rounded-xl border text-left transition-all text-xs ${
                  selected === opt.id
                    ? "border-primary/60 bg-primary/10 text-foreground shadow-sm"
                    : "border-border/60 bg-card text-muted-foreground hover:border-border hover:text-foreground hover:bg-muted/40"
                }`}
              >
                <span className={selected === opt.id ? opt.color : "text-muted-foreground/60"}>
                  {opt.icon}
                </span>
                <div className="flex flex-col leading-none gap-0.5">
                  <span className="font-semibold">{opt.label}</span>
                  <span className="text-[10px] text-muted-foreground">{opt.sublabel}</span>
                </div>
              </button>
            ))}
          </div>

          {/* Inputs shown for cloud / custom options */}
          {selected !== "manim" && (
            <div className="flex flex-col gap-2 min-w-[220px] justify-center">
              {selected === "custom" && (
                <div className="flex flex-col gap-1">
                  <label className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
                    Endpoint URL
                  </label>
                  <Input
                    value={customUrl}
                    onChange={e => setCustomUrl(e.target.value)}
                    placeholder="https://api.example.com/v1"
                    className="h-8 text-xs font-mono rounded-lg border-border/60"
                    data-testid="input-custom-url"
                  />
                </div>
              )}
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
                  API Key
                </label>
                <Input
                  type="password"
                  value={apiKey}
                  onChange={e => setApiKey(e.target.value)}
                  placeholder="sk-••••••••••••••••"
                  className="h-8 text-xs font-mono rounded-lg border-border/60"
                  data-testid="input-api-key"
                />
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
