'use client';

import { useStore } from '@/lib/store';
import { Copy, Play, Wand2 } from 'lucide-react';
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
    suggestions,
    setSuggestions,
    setLastProvider,
    setLastModel,
    setRemainingToday,
    setUsingOwnKey,
  } = useStore();

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

  return (
    <div className="flex-1 bg-[var(--bg-base)] flex flex-col h-full overflow-hidden">
      
      {/* Tabs */}
      <div className="flex items-center gap-6 px-6 pt-4 shrink-0 border-b border-[var(--bg-border)]">
        <button 
          onClick={() => setActiveTab('prompt')}
          className={`pb-2 text-[13px] font-medium relative ${activeTab === 'prompt' ? 'text-[var(--text-primary)] border-b-2 border-[var(--accent)]' : 'text-[var(--text-dim)] hover:text-[var(--text-secondary)]'}`}
        >
          Prompt
        </button>
        <button 
          onClick={() => setActiveTab('code')}
          className={`pb-2 text-[13px] font-medium relative ${activeTab === 'code' ? 'text-[var(--text-primary)] border-b-2 border-[var(--accent)]' : 'text-[var(--text-dim)] hover:text-[var(--text-secondary)]'}`}
        >
          Code
        </button>
      </div>

      {/* Content */}
      <div className="flex-1 relative overflow-hidden">
        
        {activeTab === 'prompt' && (
          <div className="absolute inset-0 p-6 flex flex-col">
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Describe the animation..."
              className="flex-1 bg-transparent text-[var(--text-primary)] text-[14px] leading-[1.7] resize-none outline-none border-none placeholder:text-[var(--text-dim)] font-dm-sans"
            />
            
            <div className="flex flex-col gap-3 border-t border-[var(--bg-border)] pt-4">
              <div className="flex items-center justify-between">
                <span className="text-[11px] text-[var(--text-dim)]">Ctrl+K to generate</span>
                
                <button
                  data-action="generate"
                  onClick={handleGenerate}
                  disabled={renderStatus === 'generating' || !prompt.trim()}
                  className={`px-4 py-1.5 rounded-[4px] text-[12px] font-sora font-[500] text-white ${!prompt.trim() ? 'bg-[var(--bg-surface)] text-[var(--text-dim)]' : 'bg-[var(--accent)] hover:bg-[var(--accent-hover)]'}`}
                >
                  {renderStatus === 'generating' ? 'Generating...' : 'Generate'}
                </button>
              </div>

              {suggestions && suggestions.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {suggestions.map((s, i) => (
                    <button
                      key={i}
                      onClick={() => setPrompt(s)}
                      className="px-2 py-0.5 border border-[var(--bg-elevated)] rounded-[3px] text-[11px] text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
                    >
                      {s}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'code' && (
          <div className="absolute inset-0 flex flex-col bg-[var(--bg-base)]">
            <div className="flex items-center justify-between px-4 py-2 border-b border-[var(--bg-border)] shrink-0">
              <span className="text-[11px] text-[var(--text-dim)]">Python</span>
              <div className="flex items-center gap-4">
                <button 
                  data-action="render"
                  className="p-1 text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
                  title="Run Preview"
                >
                  <Play size={15} />
                </button>
                <button 
                  className="p-1 text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
                  title="Fix with AI"
                >
                  <Wand2 size={15} />
                </button>
                <button 
                  onClick={() => navigator.clipboard.writeText(generatedCode)}
                  className="p-1 text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
                  title="Copy Code"
                >
                  <Copy size={15} />
                </button>
              </div>
            </div>
            <div className="flex-1 relative">
              <Editor
                value={generatedCode}
                onChange={(v) => setGeneratedCode(v || '')}
                language="python"
                theme="ash-orange"
                beforeMount={(monaco) => {
                  monaco.editor.defineTheme('ash-orange', {
                    base: 'vs-dark',
                    inherit: true,
                    rules: [
                      { token: 'keyword', foreground: 'c85a0e' },
                      { token: 'string', foreground: '8a7a68' },
                      { token: 'comment', foreground: '554840' },
                      { token: 'identifier', foreground: 'e8ddd0' }
                    ],
                    colors: {
                      'editor.background': '#1a1208',
                      'editor.foreground': '#e8ddd0',
                      'editor.lineHighlightBackground': '#211608',
                      'editor.selectionBackground': '#c85a0e33',
                      'editorCursor.foreground': '#c85a0e',
                      'editorLineNumber.foreground': '#55484080'
                    }
                  });
                }}
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  fontFamily: 'var(--font-mono)',
                  padding: { top: 16 },
                  scrollBeyondLastLine: false,
                  wordWrap: 'on',
                  lineNumbers: 'on',
                  glyphMargin: false,
                  folding: false,
                  lineDecorationsWidth: 0,
                  lineNumbersMinChars: 3
                }}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
