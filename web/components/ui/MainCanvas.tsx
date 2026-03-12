import { useStore } from '@/lib/store';
import { Mic, Copy, Play } from 'lucide-react';
import { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';

export function MainCanvas() {
  const { 
    activeTab, 
    setActiveTab, 
    prompt, 
    setPrompt, 
    generatedCode, 
    setGeneratedCode,
    renderStatus,
    setRenderStatus,
    setErrorMessage,
    selectedTemplate,
    userKeys,
    setSuggestions,
    setLastProvider,
    setLastModel,
    setRemainingToday,
    setUsingOwnKey,
    engineUrl
  } = useStore();

  const [isRecording, setIsRecording] = useState(false);
  const [recognition, setRecognition] = useState<any>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [previewStatus, setPreviewStatus] = useState<'idle' | 'rendering' | 'ready' | 'error'>('idle');

  useEffect(() => {
    if (typeof window !== 'undefined' && ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window)) {
      const SpeechRec = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
      const rec = new SpeechRec();
      rec.continuous = true;
      rec.interimResults = true;
      
      rec.onresult = (e: any) => {
        let transcript = '';
        for (let i = 0; i < e.results.length; i++) {
          if (e.results[i].isFinal) transcript += e.results[i][0].transcript;
        }
        if (transcript) setPrompt((prev: string) => prev + (prev ? ' ' : '') + transcript);
      };
      rec.onerror = () => setIsRecording(false);
      rec.onend = () => setIsRecording(false);
      setRecognition(rec);
    }
  }, [setPrompt]);

  const toggleVoice = () => {
    if (isRecording) {
      recognition?.stop();
      setIsRecording(false);
    } else {
      recognition?.start();
      setIsRecording(true);
    }
  };

  const handleGenerate = async () => {
    setRenderStatus('generating');
    try {
      const res = await fetch(`/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, template: selectedTemplate || 'none', user_keys: userKeys }),
      });
      const data = await res.json();
      if (!res.ok) {
        setRenderStatus('error');
        setErrorMessage(data.error?.message || data.error || 'Generation failed');
        return;
      }
      setGeneratedCode(data.code);
      if (data.suggestions) setSuggestions(data.suggestions);
      if (data.provider) setLastProvider(data.provider);
      if (data.model) setLastModel(data.model);
      if (data.remaining_today !== undefined) setRemainingToday(data.remaining_today);
      if (data.using_own_key !== undefined) setUsingOwnKey(data.using_own_key);
      setRenderStatus('idle');
      setActiveTab('code');
    } catch (err: any) {
      setRenderStatus('error');
      setErrorMessage(err.message);
    }
  };

  const runPreview = async () => {
    if (!generatedCode.trim()) return;
    setPreviewStatus('rendering');
    setPreviewError(null);

    try {
      const res = await fetch('/api/preview', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code: generatedCode }),
      });

      const data = await res.json();

      if (!res.ok) {
        setPreviewError(data.error || 'Preview failed');
        setPreviewStatus('error');
        return;
      }

      // Convert base64 to blob URL
      const byteChars = atob(data.video);
      const byteNums = new Array(byteChars.length);
      for (let i = 0; i < byteChars.length; i++) {
        byteNums[i] = byteChars.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNums);
      const blob = new Blob([byteArray], { type: 'video/mp4' });
      
      // Revoke old URL
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      
      const url = URL.createObjectURL(blob);
      setPreviewUrl(url);
      setPreviewStatus('ready');

    } catch (err: any) {
      setPreviewError(`Network error: ${err.message}`);
      setPreviewStatus('error');
    }
  };

  const suggestions = ["Add zooming camera", "Vibrant colors", "Include equations"];

  return (
    <div className="flex-1 bg-[var(--bg-base)] flex flex-col h-full overflow-hidden animate-fade-in delay-80">
      
      {/* Tabs */}
      <div className="flex items-center gap-4 px-6 pt-4 shrink-0">
        <button 
          onClick={() => setActiveTab('prompt')}
          className={`pb-2 text-sm font-medium transition-colors relative ${activeTab === 'prompt' ? 'text-[var(--text-primary)]' : 'text-[var(--text-dim)] hover:text-[var(--text-secondary)]'}`}
        >
          Prompt
          {activeTab === 'prompt' && <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full bg-[var(--accent)]" />}
        </button>
        <button 
          onClick={() => setActiveTab('code')}
          className={`pb-2 text-sm font-medium transition-colors relative ${activeTab === 'code' ? 'text-[var(--text-primary)]' : 'text-[var(--text-dim)] hover:text-[var(--text-secondary)]'}`}
        >
          Code
          {activeTab === 'code' && <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full bg-[var(--accent)]" />}
        </button>
      </div>

      {/* Content */}
      <div className="flex-1 relative overflow-hidden">
        
        {activeTab === 'prompt' && (
          <div className="absolute inset-0 p-6 flex flex-col tab-content-enter">
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Describe the animation..."
              className="flex-1 bg-transparent text-[var(--text-primary)] text-[15px] leading-[1.6] resize-none outline-none placeholder:text-[var(--text-dim)] font-sans"
            />
            
            {/* Toolbar */}
            <div className="flex flex-col gap-4 mt-4 shrink-0">
              {/* Suggestions */}
              <div className="flex overflow-x-auto gap-2 pb-2 scrollbar-hide mask-edges">
                {suggestions.map((s, i) => (
                  <button 
                    key={i} 
                    onClick={() => setPrompt(p => p + (p ? ' ' : '') + s)}
                    className="px-3 py-1 rounded-full border border-[var(--bg-border)] text-xs text-[var(--text-secondary)] whitespace-nowrap hover:border-[var(--accent)] hover:text-[var(--text-primary)] transition-colors animate-fade-in"
                    style={{ animationDelay: `${i * 30}ms` }}
                  >
                    {s}
                  </button>
                ))}
              </div>

              <div className="flex items-center justify-between border-t border-[var(--bg-border)] pt-4">
                <div className="flex items-center gap-4">
                  <button 
                    onClick={toggleVoice}
                    className={`p-2 rounded-full transition-colors ${isRecording ? 'text-[var(--accent)] bg-[var(--accent-glow)] animate-pulse-dot' : 'text-[var(--text-dim)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-surface)]'}`}
                  >
                    <Mic size={16} />
                  </button>
                  <span className="text-xs text-[var(--text-dim)]">{prompt.length} chars</span>
                </div>
                
                <button
                  onClick={handleGenerate}
                  disabled={renderStatus === 'generating' || !prompt.trim()}
                  className={`px-6 py-2 rounded-full text-sm font-medium text-white transition-all relative overflow-hidden ${!prompt.trim() ? 'bg-[var(--bg-surface)] text-[var(--text-dim)]' : 'bg-[var(--gradient-warm)] shadow-[0_0_10px_var(--accent-glow)] hover:shadow-[0_0_20px_var(--accent-glow)]'}`}
                >
                  {renderStatus === 'generating' && <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent animate-shimmer" />}
                  Generate
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'code' && (
          <div className="absolute inset-0 flex flex-col tab-content-enter bg-[#0a0602]">
            <div className="flex items-center justify-between px-4 py-2 border-b border-[var(--bg-border)] shrink-0">
              <span className="text-[10px] font-mono text-[var(--text-dim)] uppercase tracking-widest bg-[#1a1510] px-2 py-0.5 rounded">Python</span>
              <div className="flex items-center gap-2">
                <button 
                  onClick={runPreview}
                  disabled={previewStatus === 'rendering'}
                  className="flex items-center gap-1.5 text-xs text-[var(--text-secondary)] px-3 py-1 border border-[var(--bg-border)] rounded hover:border-[var(--accent)] transition-colors disabled:opacity-50"
                >
                  {previewStatus === 'rendering' ? <div className="w-3 h-3 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" /> : <Play size={12} />}
                  Run Preview
                </button>
                <button className="text-xs text-[var(--text-secondary)] px-3 py-1 border border-[var(--bg-border)] rounded hover:border-[var(--accent)] transition-colors">
                  Fix with AI
                </button>
                <button 
                  onClick={() => navigator.clipboard.writeText(generatedCode)}
                  className="p-1 text-[var(--text-dim)] hover:text-[var(--text-primary)] transition-colors"
                >
                  <Copy size={14} />
                </button>
              </div>
            </div>
            <div className="flex-1 flex flex-col lg:flex-row overflow-hidden">
              <div className="flex-1 relative border-r border-[var(--bg-border)]">
                <Editor
                  value={generatedCode}
                  onChange={(v) => setGeneratedCode(v || '')}
                  language="python"
                  theme="manim-dark"
                  beforeMount={(monaco) => {
                    monaco.editor.defineTheme('manim-dark', {
                      base: 'vs-dark',
                      inherit: true,
                      rules: [
                        { token: 'keyword', foreground: 'e8621a' },
                        { token: 'string', foreground: 'c4a882' },
                        { token: 'comment', foreground: '605040' },
                        { token: 'identifier', foreground: 'f5f0e8' }
                      ],
                      colors: {
                        'editor.background': '#0a0602',
                        'editor.foreground': '#f5f0e8',
                        'editor.lineHighlightBackground': '#1a1510',
                        'editor.selectionBackground': '#e8621a22',
                        'editorCursor.foreground': '#e8621a',
                        'editorLineNumber.foreground': '#60504080'
                      }
                    });
                  }}
                  options={{
                    minimap: { enabled: false },
                    fontSize: 14,
                    fontFamily: 'var(--font-mono)',
                    padding: { top: 16 },
                    scrollBeyondLastLine: false,
                    wordWrap: 'on'
                  }}
                />
              </div>
              
              {/* Quick Preview Area */}
              {(previewUrl || previewError || previewStatus === 'rendering') && (
                <div className="w-full lg:w-80 bg-black border-l border-[var(--bg-border)] flex flex-col shrink-0">
                  <div className="flex-1 flex flex-col relative overflow-hidden">
                    {previewStatus === 'rendering' && (
                      <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/80 z-10 text-xs text-orange-500 font-medium animate-pulse">
                        <div className="w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mb-4" />
                        Rendering preview...
                      </div>
                    )}
                    
                    {previewError && (
                      <div className="p-4 text-red-400 text-xs font-mono whitespace-pre-wrap overflow-auto h-full bg-red-950/20">
                        <div className="font-bold mb-2 uppercase tracking-widest text-[10px] opacity-60">Manim Error</div>
                        {previewError}
                      </div>
                    )}
                    
                    {previewUrl && !previewError && (
                      <video
                        key={previewUrl}
                        src={previewUrl}
                        autoPlay
                        muted
                        loop
                        playsInline
                        controls
                        className="w-full h-full object-contain"
                      />
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
