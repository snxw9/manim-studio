import { useState } from "react";
import { ChevronUp, ChevronDown, Cpu, Zap, Bot, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";

const API_OPTIONS = [
  { id: 'auto', label: 'Auto', description: 'Best available · Free', icon: <Sparkles className="h-4 w-4" />, color: "text-amber-500" },
  { id: 'groq', label: 'Groq', description: 'Llama 3.3 · Free', icon: <Zap className="h-4 w-4" />, color: "text-sky-400" },
  { id: 'gemini', label: 'Gemini', description: 'Flash 2.0 · Free tier', icon: <Bot className="h-4 w-4" />, color: "text-purple-400" },
  { id: 'openai', label: 'OpenAI', description: 'GPT-4o · Paid', icon: <Cpu className="h-4 w-4" />, color: "text-emerald-500" },
];

interface ApiSettingsProps {
  isOpen: boolean;
  onToggle: () => void;
  selectedApi: string;
  onApiChange: (id: string) => void;
}

export function ApiSettings({ isOpen, onToggle, selectedApi, onApiChange }: ApiSettingsProps) {
  return (
    <div
      className={`border-t border-border bg-background shrink-0 transition-all duration-300 ease-in-out flex flex-col ${
        isOpen ? "h-32" : "h-10"
      }`}
    >
      <div
        className="flex items-center justify-between px-4 h-10 shrink-0 cursor-pointer hover:bg-muted/30 transition-colors"
        onClick={onToggle}
        data-testid="button-api-settings-toggle"
      >
        <div className="flex items-center gap-2 text-xs font-semibold text-foreground/80">
          <Cpu className="h-3.5 w-3.5 text-primary" />
          <span>API Provider</span>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-[10px] font-mono text-muted-foreground bg-muted px-2 py-0.5 rounded-md uppercase">
            {API_OPTIONS.find(o => o.id === selectedApi)?.label}
          </span>
          <Button variant="ghost" size="icon" className="h-6 w-6 text-muted-foreground rounded-md">
            {isOpen ? <ChevronDown className="h-4 w-4" /> : <ChevronUp className="h-4 w-4" />}
          </Button>
        </div>
      </div>

      {isOpen && (
        <div className="flex flex-1 overflow-hidden border-t border-border/50 p-3 gap-3">
          {/* API picker */}
          <div className="flex gap-2 flex-wrap content-start">
            {API_OPTIONS.map(opt => (
              <button
                key={opt.id}
                onClick={() => onApiChange(opt.id)}
                data-testid={`api-option-${opt.id}`}
                className={`flex items-center gap-2.5 px-3 py-2 rounded-xl border text-left transition-all text-xs ${
                  selectedApi === opt.id
                    ? "border-primary/60 bg-primary/10 text-foreground shadow-sm"
                    : "border-border/60 bg-card text-muted-foreground hover:border-border hover:text-foreground hover:bg-muted/40"
                }`}
              >
                <span className={selectedApi === opt.id ? opt.color : "text-muted-foreground/60"}>
                  {opt.icon}
                </span>
                <div className="flex flex-col leading-none gap-0.5">
                  <span className="font-semibold">{opt.label}</span>
                  <span className="text-[10px] text-muted-foreground">{opt.description}</span>
                </div>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
