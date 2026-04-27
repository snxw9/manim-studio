"use client";

import { useState, useEffect, useRef } from "react";
import { Sidebar } from "@/components/layout/sidebar";
import { Header } from "@/components/layout/header";
import { PromptInput } from "@/components/workspace/prompt-input";
import { CodeEditor } from "@/components/workspace/code-editor";
import { VideoOutput } from "@/components/workspace/video-output";
import { ApiSettings } from "@/components/workspace/api-settings";
import { Sparkles, Code2 } from "lucide-react";
import { BUILTIN_TEMPLATES } from "@/lib/builtinTemplates";

// Module-level code store — never stale, never affected by closures
let _currentCode = '';

export function setCurrentCode(code: string) {
  _currentCode = code;
}

export function getCurrentCode(): string {
  return _currentCode;
}

export type Resolution  = "360p" | "720p" | "1080p" | "2k" | "4k";
export type FrameRate   = "24fps" | "30fps" | "60fps" | "120fps";
export type Format      = "mp4" | "mkv" | "gif" | "webm";
export type AspectRatio = "16:9" | "9:16" | "1:1" | "4:3" | "21:9";

type LeftPanel = "prompt" | "editor";

export interface RenderSettings {
  resolution:  Resolution;
  frameRate:   FrameRate;
  format:      Format;
  aspectRatio: AspectRatio;
  setResolution:  (v: Resolution)  => void;
  setFrameRate:   (v: FrameRate)   => void;
  setFormat:      (v: Format)      => void;
  setAspectRatio: (v: AspectRatio) => void;
}

export default function Home() {
  const [isApiSettingsOpen, setIsApiSettingsOpen] = useState(false);
  const [leftPanel, setLeftPanel] = useState<LeftPanel>("prompt");

  const [resolution,  setResolution]  = useState<Resolution>("1080p");
  const [frameRate,   setFrameRate]   = useState<FrameRate>("30fps");
  const [format,      setFormat]      = useState<Format>("mp4");
  const [aspectRatio, setAspectRatio] = useState<AspectRatio>("16:9");

  const [generatedCode, setGeneratedCode] = useState<string>("");
  const [isGenerating, setIsGenerating] = useState(false);
  const [isRendering, setIsRendering] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [videoUrl, setVideoUrl] = useState<string | null>(null);
  const [videoFilename, setVideoFilename] = useState<string | null>(null);
  const [engineOnline, setEngineOnline] = useState(false);
  const [selectedApi, setSelectedApi] = useState("auto");
  const [selectedTemplate, setSelectedTemplate] = useState<string | null>(null);

  // Monaco editor refs
  const editorRef = useRef<any>(null);

  // Render timer state
  const [elapsed, setElapsed] = useState(0);
  const [renderTime, setRenderTime] = useState<number | null>(null);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  // Initialize module-level store
  useEffect(() => {
    if (generatedCode) {
      _currentCode = generatedCode;
    }
  }, []);

  // Health check
  useEffect(() => {
    const check = async () => {
      try {
        const r = await fetch('/api/health', { cache: 'no-store' });
        const d = await r.json();
        setEngineOnline(!!d.online);
      } catch {
        setEngineOnline(false);
      }
    };
    check();
    const id = setInterval(check, 5000);
    return () => clearInterval(id);
  }, []);

  // Timer cleanup on unmount
  useEffect(() => {
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, []);

  const handleGenerate = async (prompt: string) => {
    if (!prompt.trim()) return;
    setIsGenerating(true);
    setError(null);
    try {
      const res = await fetch('/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, preferredProvider: selectedApi }),
      });
      const data = await res.json();
      if (data.code) {
        _currentCode = data.code;
        setGeneratedCode(data.code);
        if (editorRef.current) editorRef.current.setValue(data.code);
        setLeftPanel("editor");
      } else {
        setError(data.error || 'Generation failed');
      }
    } catch (err: any) {
      setError('Engine offline or error: ' + err.message);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleRender = async () => {
    // Read from module-level variable — NEVER stale
    const code = _currentCode;
    
    console.log('[render] Code class name:', code.match(/class\s+(\w+)/)?.[1]);
    console.log('[render] Code length:', code.length);

    if (!code?.trim()) {
      alert('No code to render. Select a template or write code first.');
      return;
    }

    // Clear previous render completely
    if (videoUrl) {
      URL.revokeObjectURL(videoUrl);
    }
    setVideoUrl(null);
    setVideoFilename(null);
    setError(null);
    setRenderTime(null);
    setElapsed(0);
    
    setIsRendering(true);
    const startTime = Date.now();
    timerRef.current = setInterval(() => {
      setElapsed(Math.floor((Date.now() - startTime) / 1000));
    }, 1000);

    const qualityMap: Record<string, string> = {
      "360p": "480p",
      "720p": "720p",
      "1080p": "1080p",
      "2k": "1080p",
      "4k": "2160p"
    };
    const qualityValue = qualityMap[resolution] || resolution || '720p';

    try {
      const res = await fetch('/api/render', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          code: code, 
          quality: qualityValue,
          format: format || 'mp4'
        }),
      });
      const data = await res.json();
      if (data.error) {
        setError(data.error);
        return;
      }
      if (data.video) {
        const bytes = atob(data.video);
        const arr = new Uint8Array(bytes.length);
        for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
        const blob = new Blob([arr], { type: 'video/mp4' });
        setVideoUrl(URL.createObjectURL(blob));
        setVideoFilename(data.filename || 'animation.mp4');
      } else if (data.videoUrl) {
        setVideoUrl('http://localhost:8000' + data.videoUrl);
      }
    } catch (err: any) {
      setError('Render failed: ' + err.message);
    } finally {
      if (timerRef.current) clearInterval(timerRef.current);
      setRenderTime(Math.floor((Date.now() - startTime) / 1000));
      setIsRendering(false);
    }
  };

  const handleTemplateClick = (templateName: string) => {
    // Map display names to template IDs
    const nameToId: Record<string, string> = {
      // Geometry
      'Circle & Arcs':    'circle_and_arcs',
      'Polygon Morph':    'polygon_morph',
      '3D Axes Scene':    'three_d_axes',
      'Angle Bisector':   'angle_bisector',
      // Calculus
      'Riemann Sums':     'riemann_sums',
      'Taylor Series':    'taylor_series',
      'Derivative Slope': 'derivative_slope',
      'Arc Length Sweep': 'arc_length_sweep',
      // Linear Algebra
      'Matrix Transform': 'matrix_transform',
      'Eigenvectors':     'eigenvectors',
      'Dot Product':      'dot_product',
      'Gram–Schmidt':     'gram_schmidt',
      'Gram-Schmidt':     'gram_schmidt',
      // Transforms
      'Fourier Series':   'fourier_series',
      'Cycloid Trace':    'cycloid_trace',
      // Fractals
      'Koch Snowflake':     'koch_snowflake',
      'Sierpiński Triangle':'sierpinski_triangle',
      'Dragon Curve':       'dragon_curve',
      // Number Theory
      'Sieve of Eratosthenes': 'sieve_eratosthenes',
      'Prime Ulam Spiral':     'prime_ulam_spiral',
      'Collatz Sequence':      'collatz_sequence',
      'GCD Euclidean':         'gcd_euclidean',
    };

    const id = nameToId[templateName];
    if (!id) return;

    const template = BUILTIN_TEMPLATES[id];
    if (!template) return;

    const code = template.code;

    // Update module-level store FIRST
    _currentCode = code;

    // Clear stale state visually
    if (videoUrl) {
      URL.revokeObjectURL(videoUrl);
      setVideoUrl(null);
      setVideoFilename(null);
    }
    setError(null);

    // Update state AND Monaco directly
    setGeneratedCode(code);
    if (editorRef.current) {
      editorRef.current.setValue(code);
    }

    setLeftPanel("editor");
    setSelectedTemplate(templateName);

    console.log('[template] Set code for:', templateName);
    console.log('[template] Class name in code:', code.match(/class\s+(\w+)/)?.[1]);
  };

  const settings: RenderSettings = {
    resolution, frameRate, format, aspectRatio,
    setResolution, setFrameRate, setFormat, setAspectRatio,
  };

  return (
    <div className="flex h-screen w-full flex-col overflow-hidden bg-background text-foreground selection:bg-primary/25">
      <Header 
        settings={settings} 
        engineOnline={engineOnline} 
        onRender={handleRender}
        isRendering={isRendering}
        elapsed={elapsed}
        renderTime={renderTime}
        generatedCode={generatedCode}
      />

      <div className="flex flex-1 overflow-hidden min-h-0">
        <Sidebar onTemplateClick={handleTemplateClick} />

        <main className="flex flex-1 flex-col relative min-w-0 min-h-0">
          {error && (
            <div className="bg-destructive/15 text-destructive text-xs px-4 py-2 border-b border-destructive/20">
              {error}
            </div>
          )}
          
          <div className="flex flex-1 overflow-hidden p-3 gap-3 min-h-0">

            {/* Left panel — Prompt or Editor */}
            <div className="flex-1 flex flex-col min-w-0 rounded-2xl border border-border bg-card overflow-hidden shadow-sm">
              <div className="flex items-center gap-1 px-3 pt-2.5 pb-0 shrink-0 border-b border-border">
                <button
                  onClick={() => setLeftPanel("prompt")}
                  className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-t-lg border-b-2 transition-colors mb-[-1px] ${
                    leftPanel === "prompt"
                      ? "border-primary text-primary bg-primary/5"
                      : "border-transparent text-muted-foreground hover:text-foreground hover:bg-muted/40"
                  }`}
                  data-testid="tab-prompt"
                >
                  <Sparkles className="h-3.5 w-3.5" />
                  Prompt
                </button>
                <button
                  onClick={() => setLeftPanel("editor")}
                  className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-t-lg border-b-2 transition-colors mb-[-1px] ${
                    leftPanel === "editor"
                      ? "border-primary text-primary bg-primary/5"
                      : "border-transparent text-muted-foreground hover:text-foreground hover:bg-muted/40"
                  }`}
                  data-testid="tab-editor"
                >
                  <Code2 className="h-3.5 w-3.5" />
                  Editor
                </button>
              </div>

              <div className="flex-1 overflow-hidden">
                {leftPanel === "prompt" ? (
                  <PromptInput onSubmit={handleGenerate} isGenerating={isGenerating} />
                ) : (
                  <CodeEditor 
                    code={generatedCode} 
                    onChange={(val) => {
                      _currentCode = val;
                      setGeneratedCode(val);
                    }} 
                    onMount={(editor) => { 
                      editorRef.current = editor;
                      _currentCode = editor.getValue();
                    }}
                  />
                )}
              </div>
            </div>

            {/* Right panel — Video output */}
            <div className="flex-1 flex flex-col min-w-0 rounded-2xl border border-border bg-card overflow-hidden shadow-sm">
              <VideoOutput 
                resolution={resolution} 
                frameRate={frameRate} 
                aspectRatio={aspectRatio} 
                videoUrl={videoUrl}
                videoFilename={videoFilename}
                onClear={() => {
                  if (videoUrl) URL.revokeObjectURL(videoUrl);
                  setVideoUrl(null);
                  setVideoFilename(null);
                }}
              />
            </div>
          </div>

          {/* API settings bottom bar */}
          <ApiSettings
            isOpen={isApiSettingsOpen}
            onToggle={() => setIsApiSettingsOpen(p => !p)}
            selectedApi={selectedApi}
            onApiChange={setSelectedApi}
          />
        </main>
      </div>
    </div>
  );
}
