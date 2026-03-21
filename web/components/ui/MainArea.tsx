'use client';

import { useStore } from '@/lib/store';
import { Copy, Play, Wand2 } from 'lucide-react';
import Editor from '@monaco-editor/react';

export default function MainArea() {
  const { 
    activeTab, 
    setActiveTab, 
    prompt, 
    setPrompt, 
    generatedCode, 
    setGeneratedCode,
    renderStatus,
    setRenderStatus,
    errorMessage,
    setErrorMessage,
    selectedTemplate,
    userKeys,
    setSuggestions,
    setLastProvider,
    setLastModel,
    setRemainingToday,
    setUsingOwnKey,
    resetTask,
    setPreviewUrl,
    setVideoUrl,
  } = useStore();

  const handleGenerate = async () => {
    if (!prompt.trim()) return;
    resetTask();
    setRenderStatus('generating');
    try {
      const res = await fetch(`/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, template: selectedTemplate || 'none', userKeys }),
      });
      const data = await res.json();
      if (!res.ok) {
        setRenderStatus('error');
        setErrorMessage(data.error || 'Generation failed');
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

  const handleRunPreview = async () => {
    const btn = document.querySelector('[data-action="run-preview"]') as HTMLButtonElement | null;
    btn?.click();
  };

  return (
    <div className="flex-1 flex flex-col min-w-0 bg-[var(--bg)] overflow-hidden">
      
      {/* TabBar */}
      <div className="flex border-b border-[var(--border)] shrink-0">
        <button 
          onClick={() => setActiveTab('prompt')}
          className={`px-4 py-2.5 text-[13px] font-medium border-b-2 ${activeTab === 'prompt' ? 'text-[var(--text-1)] border-[var(--accent)]' : 'text-[var(--text-3)] border-transparent'}`}
        >
          Prompt
        </button>
        <button 
          onClick={() => setActiveTab('code')}
          className={`px-4 py-2.5 text-[13px] font-medium border-b-2 ${activeTab === 'code' ? 'text-[var(--text-1)] border-[var(--accent)]' : 'text-[var(--text-3)] border-transparent'}`}
        >
          Code
        </button>
      </div>

      {/* TabContent */}
      <div className="flex-1 relative overflow-hidden">
        
        {activeTab === 'prompt' && (
          <div className="absolute inset-0 flex flex-col p-6 lg:p-8 gap-4 overflow-y-auto">
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Describe the animation you want to create..."
              className="flex-1 bg-transparent text-[var(--text-1)] text-[15px] leading-[1.7] resize-none outline-none border-none placeholder:text-[var(--text-3)] font-sans"
              onKeyDown={(e) => {
                if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                  e.preventDefault();
                  handleGenerate();
                }
              }}
            />
            
            {renderStatus === 'error' && errorMessage && (
              <div className="bg-[var(--bg-3)] border border-[var(--border)] rounded-md p-3 text-[12px] color-[var(--red)] whitespace-pre-wrap font-mono">
                {errorMessage}
              </div>
            )}

            <div className="flex items-center justify-between shrink-0 pt-2 border-t border-[var(--border)]">
              <span className="text-[11px] text-[var(--text-3)] uppercase tracking-wider">Ctrl+K to generate</span>
              
              <button
                onClick={handleGenerate}
                disabled={renderStatus === 'generating' || !prompt.trim()}
                className={`px-4 py-1.5 rounded-md text-[13px] font-medium transition-colors ${renderStatus === 'generating' ? 'bg-[var(--bg-4)] text-[var(--text-3)] cursor-not-allowed' : 'bg-[var(--text-1)] text-[var(--bg)] cursor-pointer'}`}
              >
                {renderStatus === 'generating' ? 'Generating...' : 'Generate'}
              </button>
            </div>
          </div>
        )}

        {activeTab === 'code' && (
          <div className="absolute inset-0 flex flex-col bg-[var(--bg)]">
            <div className="flex items-center justify-between px-3 py-2 border-b border-[var(--border)] shrink-0">
              <span className="text-[11px] text-[var(--text-3)] uppercase tracking-wider">Python</span>
              <div className="flex items-center gap-1">
                <button 
                  onClick={handleRunPreview}
                  className="p-1 text-[var(--text-3)] hover:text-[var(--text-1)]"
                  title="Run Preview"
                >
                  <Play size={15} />
                </button>
                <button 
                  className="p-1 text-[var(--text-3)] hover:text-[var(--text-1)]"
                  title="Fix with AI"
                >
                  <Wand2 size={15} />
                </button>
                <button 
                  onClick={() => navigator.clipboard.writeText(generatedCode)}
                  className="p-1 text-[var(--text-3)] hover:text-[var(--text-1)]"
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
                theme="studio-dark"
                beforeMount={(monaco) => {
                  monaco.editor.defineTheme('studio-dark', {
                    base: 'vs-dark',
                    inherit: true,
                    rules: [
                      { token: 'keyword', foreground: 'd97706' },
                      { token: 'string', foreground: 'a3a3a3' },
                      { token: 'comment', foreground: '525252', fontStyle: 'italic' },
                      { token: 'number', foreground: 'ececec' },
                    ],
                    colors: {
                      'editor.background': '#0d0d0d',
                      'editor.foreground': '#ececec',
                      'editor.lineHighlightBackground': '#171717',
                      'editor.selectionBackground': '#2a2a2a',
                      'editorLineNumber.foreground': '#333333',
                      'editorLineNumber.activeForeground': '#555555',
                      'editorCursor.foreground': '#ececec',
                      'editor.findMatchBackground': '#d9770620',
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
