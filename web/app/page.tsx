'use client';

import { useEffect, useCallback, useRef, useState } from 'react';
import { useStore } from '@/lib/store';
import { TEMPLATES, TEMPLATE_LIST } from '@/lib/templates';
import { ASSETS, ASSETS_BY_CATEGORY } from '@/lib/assets';
import {
  Sun, Moon, Settings, ChevronLeft, ChevronRight,
  ChevronDown, Copy, Play, Wand2, Download, Save,
  X, Check,
} from 'lucide-react';
import dynamic from 'next/dynamic';

const MonacoEditor = dynamic(() => import('@monaco-editor/react'), { ssr: false });

// ─── Inline styles as constants ─────────────────────────────────

const S = {
  // Layout
  root: {
    display: 'flex', flexDirection: 'column' as const,
    height: '100vh', background: 'var(--bg-0)', overflow: 'hidden',
  },
  body: {
    display: 'flex', flex: 1, overflow: 'hidden',
  },

  // Topbar
  topbar: {
    height: 44,
    background: 'var(--bg-1)',
    borderBottom: '1px solid var(--line)',
    display: 'flex', alignItems: 'center',
    padding: '0 16px', gap: 16, flexShrink: 0,
  },
  wordmark: {
    fontSize: 13, fontWeight: 500,
    color: 'var(--t1)', letterSpacing: '0.05em',
    textTransform: 'uppercase' as const,
  },

  // Sidebar
  sidebar: (open: boolean) => ({
    width: open ? 200 : 32,
    background: 'var(--bg-1)',
    borderRight: '1px solid var(--line)',
    display: 'flex', flexDirection: 'column' as const,
    flexShrink: 0, overflow: 'hidden',
    transition: 'width 0.15s ease',
  }),

  // Tabs
  tab: (active: boolean) => ({
    padding: '0 16px', height: 36,
    fontSize: 11, letterSpacing: '0.05em',
    color: active ? 'var(--t1)' : 'var(--t3)',
    borderBottom: active ? '1px solid var(--a)' : '1px solid transparent',
    background: 'none', cursor: 'pointer',
    textTransform: 'uppercase' as const,
  }),

  // Buttons
  btnPrimary: {
    background: 'var(--a)', color: 'var(--bg-0)',
    padding: '6px 20px', borderRadius: 2,
    fontSize: 11, fontWeight: 500,
    letterSpacing: '0.08em', textTransform: 'uppercase' as const,
    cursor: 'pointer', border: 'none',
  },
  btnGhost: {
    background: 'none', border: '1px solid var(--line)',
    color: 'var(--t2)', padding: '5px 12px',
    borderRadius: 2, fontSize: 11,
    cursor: 'pointer', letterSpacing: '0.04em',
  },
  btnIcon: {
    background: 'none', border: 'none',
    color: 'var(--t3)', padding: 6,
    cursor: 'pointer', display: 'flex',
    alignItems: 'center', borderRadius: 2,
  },

  // Input
  textarea: {
    width: '100%', flex: 1,
    background: 'transparent', border: 'none',
    outline: 'none', color: 'var(--t1)',
    fontSize: 13, lineHeight: 1.8,
    fontFamily: 'inherit', padding: '24px',
    resize: 'none' as const,
  },

  // Right panel
  rPanel: {
    width: 260, background: 'var(--bg-1)',
    borderLeft: '1px solid var(--line)',
    display: 'flex', flexDirection: 'column' as const,
    flexShrink: 0,
  },

  // Status bar
  statusBar: {
    height: 26, background: 'var(--bg-1)',
    borderTop: '1px solid var(--line)',
    display: 'flex', alignItems: 'center',
    padding: '0 14px', gap: 16, flexShrink: 0,
  },
  statusText: {
    fontSize: 10, color: 'var(--t3)',
    letterSpacing: '0.04em',
  },
};

// ─── Sub-components ──────────────────────────────────────────────

function Dot({ ok }: { ok: boolean }) {
  return (
    <div style={{
      width: 6, height: 6, borderRadius: '50%',
      background: ok ? 'var(--ok)' : 'var(--err)',
      flexShrink: 0,
    }} />
  );
}

function SectionLabel({ children }: { children: string }) {
  return (
    <div style={{
      fontSize: 9, letterSpacing: '0.12em',
      color: 'var(--t3)', padding: '10px 12px 4px',
      textTransform: 'uppercase',
    }}>
      {children}
    </div>
  );
}

function Divider() {
  return <div style={{ height: 1, background: 'var(--line)', flexShrink: 0 }} />;
}

// ─── Settings panel ──────────────────────────────────────────────

function SettingsPanel({ onClose }: { onClose: () => void }) {
  const { quality, format, userKeys, setQuality, setFormat, setUserKeys } = useStore();
  const [keys, setKeys] = useState(userKeys);

  const inputStyle = {
    width: '100%', background: 'var(--bg-3)',
    border: '1px solid var(--line)',
    color: 'var(--t1)', padding: '5px 8px',
    borderRadius: 2, fontSize: 11,
    fontFamily: 'inherit', marginBottom: 8,
  };

  const selectStyle = { ...inputStyle, marginBottom: 12 };

  return (
    <div style={{
      position: 'absolute' as const, top: 44, right: 0,
      width: 280, background: 'var(--bg-2)',
      border: '1px solid var(--line)',
      borderRadius: '0 0 0 4px', zIndex: 100,
      padding: 16,
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 14 }}>
        <span style={{ fontSize: 10, letterSpacing: '0.1em', color: 'var(--t2)', textTransform: 'uppercase' }}>
          Settings
        </span>
        <button style={S.btnIcon} onClick={onClose}><X size={13} /></button>
      </div>

      <label style={{ fontSize: 10, color: 'var(--t3)', letterSpacing: '0.05em' }}>QUALITY</label>
      <select
        value={quality}
        onChange={e => setQuality(e.target.value)}
        style={selectStyle as any}
      >
        <option value="480p">480p — Fast preview</option>
        <option value="720p">720p — Standard</option>
        <option value="1080p">1080p — High quality</option>
        <option value="2160p">2160p — 4K (slow)</option>
      </select>

      <label style={{ fontSize: 10, color: 'var(--t3)', letterSpacing: '0.05em' }}>FORMAT</label>
      <select
        value={format}
        onChange={e => setFormat(e.target.value)}
        style={selectStyle as any}
      >
        <option value="mp4">MP4</option>
        <option value="gif">GIF</option>
        <option value="webm">WebM</option>
        <option value="mov">MOV</option>
      </select>

      <Divider />
      <div style={{ marginTop: 12 }}>
        <span style={{ fontSize: 10, letterSpacing: '0.08em', color: 'var(--t3)', textTransform: 'uppercase' }}>
          API Keys (optional — for unlimited use)
        </span>
        {(['groq', 'gemini', 'openai'] as const).map(provider => (
          <div key={provider} style={{ marginTop: 8 }}>
            <label style={{ fontSize: 10, color: 'var(--t3)' }}>{provider.toUpperCase()}</label>
            <input
              type="password"
              value={keys[provider] || ''}
              onChange={e => setKeys(k => ({ ...k, [provider]: e.target.value }))}
              placeholder={`${provider} api key`}
              style={inputStyle as any}
            />
          </div>
        ))}
        <button
          style={{ ...S.btnGhost, width: '100%', marginTop: 4 }}
          onClick={() => { setUserKeys(keys); onClose(); }}
        >
          Save Keys
        </button>
      </div>
    </div>
  );
}

// ─── Topbar ──────────────────────────────────────────────────────

function Topbar() {
  const { engineOnline, theme, setTheme } = useStore();
  const [settingsOpen, setSettingsOpen] = useState(false);

  const toggleTheme = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
  };

  return (
    <div style={{ ...S.topbar, position: 'relative' }}>
      <span style={S.wordmark}>Manim Studio</span>

      <div style={{ flex: 1 }} />

      {/* Engine status */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Dot ok={engineOnline} />
        <span style={{ fontSize: 10, color: 'var(--t3)', letterSpacing: '0.04em' }}>
          {engineOnline ? 'engine online' : 'engine offline'}
        </span>
      </div>

      <div style={{ width: 1, height: 16, background: 'var(--line)' }} />

      {/* Theme */}
      <button style={S.btnIcon} onClick={toggleTheme} title="Toggle theme">
        {theme === 'dark' ? <Sun size={14} /> : <Moon size={14} />}
      </button>

      {/* Settings */}
      <button
        style={S.btnIcon}
        onClick={() => setSettingsOpen(o => !o)}
        title="Settings"
      >
        <Settings size={14} />
      </button>

      {settingsOpen && <SettingsPanel onClose={() => setSettingsOpen(false)} />}
    </div>
  );
}

// ─── Sidebar ─────────────────────────────────────────────────────

function Sidebar() {
  const [open, setOpen] = useState(true);
  const [templatesOpen, setTemplatesOpen] = useState(true);
  const [assetsOpen, setAssetsOpen] = useState(true);
  const { selectedTemplate, setSelectedTemplate, setGeneratedCode, setActiveTab } = useStore();

  const handleTemplate = (id: string) => {
    const t = TEMPLATES[id];
    if (!t) return;
    setSelectedTemplate(id);
    setGeneratedCode(t.code);
    setActiveTab('code');
  };

  const handleAsset = (snippet: string, label: string) => {
    // Always wrap asset in a complete valid Scene
    const className = label.replace(/\s+/g, '') + 'Scene';
    const fullCode = `from manim import *

class ${className}(Scene):
    def construct(self):
        ${snippet.split('\n').join('\n        ')}
`;
    setGeneratedCode(fullCode);
    setActiveTab('code');
  };


  const rowStyle = (active?: boolean) => ({
    display: 'flex', alignItems: 'center',
    padding: '4px 12px', fontSize: 11,
    color: active ? 'var(--t1)' : 'var(--t2)',
    cursor: 'pointer', borderLeft: active ? '2px solid var(--a)' : '2px solid transparent',
    background: 'none', width: '100%', textAlign: 'left' as const,
    letterSpacing: '0.02em',
  });

  return (
    <div style={S.sidebar(open)}>
      {/* Toggle button */}
      <div style={{
        display: 'flex',
        justifyContent: open ? 'flex-end' : 'center',
        padding: '8px 6px',
      }}>
        <button
          style={{ ...S.btnIcon, color: 'var(--t3)' }}
          onClick={() => setOpen(o => !o)}
        >
          {open ? <ChevronLeft size={13} /> : <ChevronRight size={13} />}
        </button>
      </div>

      {open && (
        <div style={{ flex: 1, overflowY: 'auto' }}>
          {/* Templates */}
          <button
            style={{ ...rowStyle(), justifyContent: 'space-between', paddingTop: 8 }}
            onClick={() => setTemplatesOpen(o => !o)}
          >
            <SectionLabel>Templates</SectionLabel>
            <ChevronDown size={11} style={{
              color: 'var(--t3)',
              transform: templatesOpen ? 'rotate(0)' : 'rotate(-90deg)',
            }} />
          </button>

          {templatesOpen && TEMPLATE_LIST.map(t => (
            <button
              key={t.id}
              style={rowStyle(selectedTemplate === t.id)}
              onClick={() => handleTemplate(t.id)}
            >
              {t.name}
            </button>
          ))}

          <Divider />

          {/* Assets */}
          <button
            style={{ ...rowStyle(), justifyContent: 'space-between', marginTop: 4 }}
            onClick={() => setAssetsOpen(o => !o)}
          >
            <SectionLabel>Assets</SectionLabel>
            <ChevronDown size={11} style={{
              color: 'var(--t3)',
              transform: assetsOpen ? 'rotate(0)' : 'rotate(-90deg)',
            }} />
          </button>

          {assetsOpen && Object.entries(ASSETS_BY_CATEGORY).map(([cat, items]) => (
            <div key={cat}>
              <div style={{
                fontSize: 9, color: 'var(--t3)',
                padding: '6px 12px 2px',
                letterSpacing: '0.1em',
                textTransform: 'uppercase',
              }}>
                {cat}
              </div>
              {items.map((a: any) => (
                <button
                  key={a.id}
                  style={rowStyle()}
                  onClick={() => handleAsset(a.snippet, a.label)}
                >
                  <span style={{ width: 18, opacity: 0.6, fontSize: 13 }}>{a.icon}</span>
                  {a.label}
                </button>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Main editor area ────────────────────────────────────────────

function MainArea({
  onGenerate,
  onRender,
}: {
  onGenerate: () => void;
  onRender: () => void;
}) {
  const {
    prompt, setPrompt,
    generatedCode, setGeneratedCode,
    activeTab, setActiveTab,
    renderStatus, errorMessage,
  } = useStore();

  const [copied, setCopied] = useState(false);

  const copyCode = () => {
    navigator.clipboard.writeText(generatedCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  const monacoTheme = {
    base: 'vs-dark' as const,
    inherit: true,
    rules: [
      { token: 'keyword', foreground: 'd4900a' },
      { token: 'string', foreground: 'a09070' },
      { token: 'comment', foreground: '4a4438', fontStyle: 'italic' },
      { token: 'number', foreground: 'e8e0d0' },
      { token: 'type', foreground: 'c8a860' },
    ],
    colors: {
      'editor.background': '#0c0a08',
      'editor.foreground': '#e8e0d0',
      'editor.lineHighlightBackground': '#131109',
      'editor.selectionBackground': '#d4900a18',
      'editorLineNumber.foreground': '#2e2a20',
      'editorLineNumber.activeForeground': '#4a4438',
      'editorCursor.foreground': '#d4900a',
      'editorIndentGuide.background': '#1a1710',
      'editorIndentGuide.activeBackground': '#2a2518',
    },
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      {/* Tab bar */}
      <div style={{
        display: 'flex', alignItems: 'center',
        borderBottom: '1px solid var(--line)',
        background: 'var(--bg-1)', flexShrink: 0,
        padding: '0 4px',
      }}>
        <button style={S.tab(activeTab === 'prompt')} onClick={() => setActiveTab('prompt')}>
          Prompt
        </button>
        <button style={S.tab(activeTab === 'code')} onClick={() => setActiveTab('code')}>
          Code
        </button>
        <div style={{ flex: 1 }} />
        {activeTab === 'code' && (
          <div style={{ display: 'flex', gap: 2, paddingRight: 8 }}>
            <button style={S.btnIcon} title="Copy" onClick={copyCode}>
              {copied ? <Check size={13} color="var(--ok)" /> : <Copy size={13} />}
            </button>
          </div>
        )}
      </div>

      {/* Content */}
      {activeTab === 'prompt' ? (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <textarea
            style={S.textarea}
            value={prompt}
            onChange={e => setPrompt(e.target.value)}
            placeholder="Describe the animation you want to create..."
            onKeyDown={e => {
              if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                e.preventDefault();
                onGenerate();
              }
            }}
            autoFocus
          />

          {/* Error */}
          {renderStatus === 'error' && errorMessage && (
            <div style={{
              margin: '0 24px 12px',
              background: 'var(--bg-2)',
              border: '1px solid var(--err)',
              borderRadius: 2, padding: '10px 14px',
              fontSize: 11, color: 'var(--err)',
              whiteSpace: 'pre-wrap',
            }}>
              {errorMessage}
            </div>
          )}

          {/* Bottom toolbar */}
          <div style={{
            display: 'flex', alignItems: 'center',
            justifyContent: 'space-between',
            padding: '10px 24px',
            borderTop: '1px solid var(--line)',
            background: 'var(--bg-1)', flexShrink: 0,
          }}>
            <span style={{ fontSize: 10, color: 'var(--t3)', letterSpacing: '0.04em' }}>
              ctrl+k to generate
            </span>
            <button
              style={{
                ...S.btnPrimary,
                opacity: renderStatus === 'generating' || !prompt.trim() ? 0.5 : 1,
                cursor: renderStatus === 'generating' || !prompt.trim() ? 'not-allowed' : 'pointer',
              }}
              onClick={onGenerate}
              disabled={renderStatus === 'generating' || !prompt.trim()}
            >
              {renderStatus === 'generating' ? 'generating...' : 'Generate'}
            </button>
          </div>
        </div>
      ) : (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          {/* Monaco toolbar */}
          <div style={{
            display: 'flex', alignItems: 'center',
            padding: '4px 12px', gap: 4,
            borderBottom: '1px solid var(--line)',
            background: 'var(--bg-1)', flexShrink: 0,
          }}>
            <span style={{ fontSize: 10, color: 'var(--t3)', flex: 1, letterSpacing: '0.05em' }}>
              python
            </span>
            <button style={S.btnIcon} title="Copy" onClick={copyCode}>
              {copied ? <Check size={13} color="var(--ok)" /> : <Copy size={13} />}
            </button>
          </div>

          <MonacoEditor
            height="100%"
            language="python"
            value={generatedCode}
            onChange={v => setGeneratedCode(v || '')}
            theme="studio-dark"
            beforeMount={monaco => {
              monaco.editor.defineTheme('studio-dark', monacoTheme);
            }}
            options={{
              fontSize: 12,
              fontFamily: "'JetBrains Mono', monospace",
              fontLigatures: true,
              lineHeight: 20,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              renderLineHighlight: 'line',
              overviewRulerLanes: 0,
              hideCursorInOverviewRuler: true,
              scrollbar: { verticalScrollbarSize: 4, horizontalScrollbarSize: 4 },
              padding: { top: 16, bottom: 16 },
              wordWrap: 'on',
            }}
          />
        </div>
      )}
    </div>
  );
}

// ─── Right panel ─────────────────────────────────────────────────

function RightPanel({ onRender, elapsed }: { onRender: () => void, elapsed: number }) {
  const {
    generatedCode, renderStatus, errorMessage,
    videoUrl, setVideoUrl, quality, format,
    setRenderStatus, setRenderTime, setErrorMessage
  } = useStore();

  const [scenes, setScenes] = useState([{ id: '1', name: 'Scene 1', duration: 5 }]);

  const sectionHead = {
    fontSize: 9, letterSpacing: '0.12em',
    color: 'var(--t3)', padding: '10px 12px 6px',
    textTransform: 'uppercase' as const,
  };

  const download = () => {
    if (!videoUrl) return;
    const a = document.createElement('a');
    a.href = videoUrl;
    a.download = `animation_${quality}.${format}`;
    a.click();
  };

  return (
    <div style={S.rPanel}>
      {/* Output / video */}
      <div style={{ display: 'flex', alignItems: 'center', padding: '10px 12px 6px' }}>
        <span style={{ fontSize: 9, letterSpacing: '0.12em', color: 'var(--t3)', textTransform: 'uppercase', flex: 1 }}>
          Output
        </span>
        {videoUrl && (
          <button
            onClick={() => {
              URL.revokeObjectURL(videoUrl);
              setVideoUrl(null);
              setRenderStatus('idle');
              setRenderTime(null);
            }}
            style={{
              fontSize: 9, color: 'var(--t3)',
              letterSpacing: '0.05em', cursor: 'pointer',
              background: 'none', border: 'none',
              fontFamily: 'inherit', padding: '2px 4px',
            }}
            onMouseOver={e => e.currentTarget.style.color = 'var(--t1)'}
            onMouseOut={e => e.currentTarget.style.color = 'var(--t3)'}
          >
            clear
          </button>
        )}
      </div>
      <div style={{
        height: 180, background: '#000',
        margin: '0 12px', borderRadius: 2,
        display: 'flex', alignItems: 'center',
        justifyContent: 'center', flexShrink: 0,
        overflow: 'hidden', border: '1px solid var(--line)',
      }}>
        {videoUrl ? (
          <video
            key={videoUrl}
            src={videoUrl}
            controls
            autoPlay
            muted
            loop
            style={{ width: '100%', height: '100%', objectFit: 'contain' }}
          />
        ) : (
          <span style={{ fontSize: 10, color: 'var(--t3)', letterSpacing: '0.04em' }}>
            {renderStatus === 'rendering' ? 'rendering...' : 'no output'}
          </span>
        )}
      </div>

      {/* Render button */}
      <div style={{ padding: '10px 12px' }}>
        <button
          onClick={onRender}
          disabled={!generatedCode || renderStatus === 'rendering'}
          style={{
            width: '100%', padding: '8px 0',
            background: generatedCode && renderStatus !== 'rendering'
              ? 'var(--a)' : 'var(--bg-3)',
            color: generatedCode && renderStatus !== 'rendering'
              ? 'var(--bg-0)' : 'var(--t3)',
            border: 'none', borderRadius: 2,
            fontSize: 11, fontWeight: 500,
            letterSpacing: '0.08em',
            textTransform: 'uppercase',
            cursor: generatedCode && renderStatus !== 'rendering'
              ? 'pointer' : 'not-allowed',
            fontFamily: 'inherit',
          }}
        >
          {renderStatus === 'rendering' ? 'rendering...' : 'Render'}
        </button>

        {renderStatus === 'rendering' && (
          <>
            <div style={{
              marginTop: 8, fontSize: 10,
              color: 'var(--t3)', textAlign: 'center',
              letterSpacing: '0.04em',
            }}>
              {elapsed}s elapsed
              {elapsed > 30 && ' — complex scene, please wait...'}
              {elapsed > 90 && ' — this is taking longer than usual'}
            </div>
            <button
              onClick={async () => {
                await fetch('/api/render/cancel', { 
                  method: 'POST',
                  body: JSON.stringify({ render_id: 'default' }) 
                });
                setRenderStatus('idle');
                setErrorMessage('Render cancelled');
              }}
              style={{
                marginTop: 6, width: '100%',
                padding: '5px 0', fontSize: 10,
                color: '#ff4444', background: 'none',
                border: '1px solid #ff4444',
                borderRadius: 2, cursor: 'pointer',
                fontFamily: 'inherit', letterSpacing: '0.05em',
              }}
            >
              cancel render
            </button>
          </>
        )}

        {videoUrl && (
          <button
            onClick={download}
            style={{ ...S.btnGhost, width: '100%', marginTop: 6, textAlign: 'center' }}
          >
            <Download size={11} style={{ marginRight: 6, display: 'inline' }} />
            download
          </button>
        )}

        {renderStatus === 'error' && errorMessage && (
          <div style={{
            marginTop: 8, fontSize: 10,
            color: 'var(--err)', lineHeight: 1.6,
            whiteSpace: 'pre-wrap',
          }}>
            {errorMessage}
          </div>
        )}
      </div>

      <Divider />

      {/* Timeline */}
      <div style={sectionHead}>Timeline</div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '0 12px' }}>
        {scenes.map((scene, i) => (
          <div key={scene.id} style={{
            display: 'flex', gap: 6, alignItems: 'center',
            padding: '4px 0', borderBottom: '1px solid var(--line)',
          }}>
            <input
              value={scene.name}
              onChange={e => setScenes(s => s.map((sc, j) =>
                j === i ? { ...sc, name: e.target.value } : sc
              ))}
              style={{
                flex: 1, fontSize: 11, color: 'var(--t1)',
                background: 'none', border: 'none',
                fontFamily: 'inherit',
              }}
            />
            <input
              type="number"
              value={scene.duration}
              onChange={e => setScenes(s => s.map((sc, j) =>
                j === i ? { ...sc, duration: +e.target.value } : sc
              ))}
              style={{
                width: 32, fontSize: 11, color: 'var(--t2)',
                background: 'none', border: 'none',
                textAlign: 'right', fontFamily: 'inherit',
              }}
            />
            <span style={{ fontSize: 10, color: 'var(--t3)' }}>s</span>
          </div>
        ))}
        <button
          onClick={() => setScenes(s => [...s, {
            id: Date.now().toString(),
            name: `Scene ${s.length + 1}`,
            duration: 5,
          }])}
          style={{
            fontSize: 10, color: 'var(--t3)',
            padding: '8px 0', cursor: 'pointer',
            background: 'none', border: 'none',
            fontFamily: 'inherit', letterSpacing: '0.04em',
          }}
        >
          + add scene
        </button>
      </div>
    </div>
  );
}

// ─── Status bar ──────────────────────────────────────────────────

function StatusBar() {
  const { engineOnline, lastProvider, lastModel, renderTime } = useStore();
  return (
    <div style={S.statusBar}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Dot ok={engineOnline} />
        <span style={S.statusText}>
          {engineOnline ? 'engine online' : 'engine offline'}
        </span>
      </div>
      <div style={{ flex: 1 }} />
      {lastProvider && (
        <span style={S.statusText}>
          {lastProvider} · {lastModel}
          {renderTime ? ` · ${renderTime.toFixed(1)}s` : ''}
        </span>
      )}
      <div style={{ width: 1, height: 12, background: 'var(--line)' }} />
      <span style={S.statusText}>ctrl+k generate · ctrl+enter render</span>
    </div>
  );
}

// ─── Root page ───────────────────────────────────────────────────

export default function Home() {
  const {
    prompt, generatedCode,
    quality, format, userKeys,
    setGeneratedCode, setActiveTab,
    setRenderStatus, setErrorMessage,
    setVideoUrl, setLastProvider, setLastModel,
    setRenderTime, setEngineOnline,
    setTheme, resetTask,
    renderStatus,
  } = useStore();

  const [elapsed, setElapsed] = useState(0);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (renderStatus === 'rendering') {
      setElapsed(0);
      timerRef.current = setInterval(() => setElapsed(e => e + 1), 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [renderStatus]);

  // Theme init
  useEffect(() => {
    const saved = localStorage.getItem('theme') as 'dark' | 'light' | null;
    const preferred = window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    const t = saved || preferred;
    setTheme(t);
    document.documentElement.setAttribute('data-theme', t);
  }, []);

  // Engine health polling
  const checkEngine = useCallback(async () => {
    try {
      const r = await fetch('/api/health', { cache: 'no-store' });
      const d = await r.json();
      setEngineOnline(!!d.online);
    } catch {
      setEngineOnline(false);
    }
  }, [setEngineOnline]);

  useEffect(() => {
    checkEngine();
    const id = setInterval(checkEngine, 5000);
    return () => clearInterval(id);
  }, [checkEngine]);

  // Keyboard shortcuts
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const mod = e.metaKey || e.ctrlKey;
      if (mod && e.key === 'k') { e.preventDefault(); handleGenerate(); }
      if (mod && e.key === 'Enter') { e.preventDefault(); handleRender(); }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [prompt, generatedCode]);

  const handleGenerate = async () => {
    if (!prompt.trim()) return;
    resetTask();
    setRenderStatus('generating');
    setErrorMessage(null);

    try {
      const res = await fetch('/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, userKeys }),
      });
      const data = await res.json();

      if (!res.ok || !data.code) {
        setRenderStatus('error');
        setErrorMessage(data.error || 'Generation failed — check engine logs');
        return;
      }

      setGeneratedCode(data.code);
      setLastProvider(data.provider || null);
      setLastModel(data.model || null);
      setActiveTab('code');
      setRenderStatus('idle');
    } catch (err: any) {
      setRenderStatus('error');
      setErrorMessage(`Network error: ${err.message}`);
    }
  };

  const handleRender = async () => {
    if (!generatedCode.trim()) return;
    setRenderStatus('rendering');
    setErrorMessage(null);
    setVideoUrl(null);
    const t0 = Date.now();

    try {
      const res = await fetch('/api/render', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code: generatedCode, quality, format }),
      });

      if (!res.body) throw new Error('No response body');

      const reader = res.body.getReader();
      const decoder = new TextDecoder();

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const lines = decoder.decode(value).split('\n').filter(Boolean);
        for (const line of lines) {
          try {
            const data = JSON.parse(line);

            if (data.status === 'error') {
              setRenderStatus('error');
              setErrorMessage(data.error);
              return;
            }

            if (data.status === 'done') {
              setRenderTime((Date.now() - t0) / 1000);

              if (data.video) {
                const bytes = atob(data.video);
                const arr = new Uint8Array(bytes.length);
                for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
                const blob = new Blob([arr], { type: 'video/mp4' });
                setVideoUrl(URL.createObjectURL(blob));
              } else if (data.videoUrl) {
                setVideoUrl(`http://localhost:8000${data.videoUrl}`);
              }

              setRenderStatus('done');
            }
          } catch {}
        }
      }
    } catch (err: any) {
      setRenderStatus('error');
      setErrorMessage(`Render error: ${err.message}`);
    }
  };

  return (
    <div style={S.root}>
      <Topbar />
      <div style={S.body}>
        <Sidebar />
        <MainArea onGenerate={handleGenerate} onRender={handleRender} />
        <RightPanel onRender={handleRender} elapsed={elapsed} />
      </div>
      <StatusBar />
    </div>
  );
}
