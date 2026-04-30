"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import { Sidebar } from "@/components/layout/sidebar";
import { Header } from "@/components/layout/header";
import { PromptInput } from "@/components/workspace/prompt-input";
import { CodeEditor } from "@/components/workspace/code-editor";
import { VideoOutput } from "@/components/workspace/video-output";
import { ApiSettings } from "@/components/workspace/api-settings";
import { Sparkles, Code2 } from "lucide-react";
import { TEMPLATES } from "@/lib/templates";

// Module-level code store — never stale, never affected by closures
let _currentCode = '';

export function setCurrentCode(code: string) {
  _currentCode = code;
}

export function getCurrentCode(): string {
  return _currentCode;
}

export type Resolution  = "480p" | "720p" | "1080p" | "2k" | "4k";
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

  const [resolution,  setResolution]  = useState<Resolution>("720p");
  const [frameRate,   setFrameRate]   = useState<FrameRate>("30fps");
  const [format,      setFormat]      = useState<Format>("mp4");
  const [aspectRatio, setAspectRatio] = useState<AspectRatio>("16:9");

  const [generatedCode, setGeneratedCode] = useState<string>(" ");
  const [isGenerating, setIsGenerating] = useState(false);
  const [isRendering, setIsRendering] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [videoUrl, setVideoUrl] = useState<string | null>(null);
  const [videoFilename, setVideoFilename] = useState<string | null>(null);
  const [engineOnline, setEngineOnline] = useState(false);
  const [selectedApi, setSelectedApi] = useState("auto");
  const [selectedTemplate, setSelectedTemplate] = useState<string | null>(null);
  const [qualityChanged, setQualityChanged] = useState(false);

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

  const clearVideo = useCallback(() => {
    if (videoUrl) {
      URL.revokeObjectURL(videoUrl);
      setVideoUrl(null);
    }
    setVideoFilename(null);
  }, [videoUrl]);

  const handleQualityChange = (newQuality: Resolution) => {
    // Validate — reject 360p (effectively done by TS type but for robustness)
    const valid: Resolution[] = ['480p', '720p', '1080p', '2k', '4k'];
    const q = valid.includes(newQuality) ? newQuality : '720p';
    
    setResolution(q);
    
    // Clear video so user knows they need to re-render
    clearVideo();
    
    // Show a subtle hint
    setQualityChanged(true);
    setTimeout(() => setQualityChanged(false), 3000);
  };

  const handleGenerate = async (prompt: string) => {
    if (!prompt.trim()) return;
    setIsGenerating(true);
    setError(null);
    setSelectedTemplate(null);
    clearVideo();
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
    
    console.log('[render] Class:', code?.match(/class\s+(\w+)/)?.[1]);
    console.log('[render] Length:', code?.length);

    if (!code?.trim()) {
      alert('No code to render. Pick a template or write code first.');
      return;
    }

    // Clear previous render completely
    clearVideo();
    setError(null);
    setRenderTime(null);
    setElapsed(0);
    
    setIsRendering(true);
    const startTime = Date.now();
    timerRef.current = setInterval(() => {
      setElapsed(Math.floor((Date.now() - startTime) / 1000));
    }, 1000);

    const qualityMap: Record<string, string> = {
      "480p": "480p",
      "720p": "720p",
      "1080p": "1080p",
      "2k": "1080p", // 2K maps to 1080p flag in engine for now
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

  const handleTemplateClick = (templateId: string) => {
    const template = TEMPLATES.find(t => t.id === templateId);
    
    if (!template) {
      console.warn('[template] ID not found:', templateId);
      return;
    }
  
    console.log('[template] Loading:', template.name);
    console.log('[template] Class:', template.code.match(/class\s+(\w+)/)?.[1]);
  
    // Clear previous video
    clearVideo();

    // Set module-level store
    _currentCode = template.code;
  
    // Update Monaco directly
    if (editorRef.current) {
      editorRef.current.setValue(template.code);
    }
  
    // Update React state
    setGeneratedCode(template.code);
    setSelectedTemplate(templateId);
    setLeftPanel("editor");
  };


  const settings: RenderSettings = {
    resolution, frameRate, format, aspectRatio,
    setResolution: handleQualityChange as any, setFrameRate, setFormat, setAspectRatio,
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
        <Sidebar onTemplateClick={handleTemplateClick} selectedTemplate={selectedTemplate} />

        <main className="flex flex-1 flex-col relative min-w-0 min-h-0">
          {error && (
            <div className="bg-destructive/15 text-destructive text-xs px-4 py-2 border-b border-destructive/20">
              {error}
            </div>
          )}

          {qualityChanged && (
            <div className="bg-amber-500/10 text-amber-500 text-[10px] px-4 py-1 border-b border-amber-500/20 font-medium animate-pulse">
              Quality changed — render again to update video output
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
                      ? "border-primary text-foreground"
                      : "border-transparent text-muted-foreground hover:text-foreground"
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
                      ? "border-primary text-foreground"
                      : "border-transparent text-muted-foreground hover:text-foreground"
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
                      setSelectedTemplate(null);
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
                onClear={clearVideo}
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
