import { useStore } from '@/lib/store';
import { Mic, Copy, Loader2 } from 'lucide-react';
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

  useEffect(() => {
    if (typeof window !== 'undefined' && ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window)) {
      const SpeechRec = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
      const rec = new SpeechRec();
      rec.continuous = true;
      rec.interimResults = true;
      
      rec.onresult = (e: any) => {
        let transcript = '';
        for (let i = 0; i < e.results.length; i++) transcript += e.results[i][0].transcript;
        setPrompt((prev: string) => prev + (prev ? ' ' : '') + transcript);
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
    setGeneratedCode('');
    try {
      const res = await fetch(`${engineUrl}/generate`, {
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
                <button className="text-xs text-[var(--text-secondary)] px-3 py-1 border border-[var(--bg-border)] rounded hover:border-[var(--accent)] transition-colors">
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
            <div className="flex-1 relative">
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
          </div>
        )}
      </div>
    </div>
  );
}
