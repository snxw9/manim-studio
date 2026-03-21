'use client';
import { useState, useRef, useEffect } from 'react';
import { useStore } from '@/lib/store';
import { SlidersHorizontal, Sun, Moon, Eye, EyeOff } from 'lucide-react';

function ProjectNameInput() {
  const { projectName, setProjectName } = useStore();
  return (
    <input
      type="text"
      value={projectName}
      onChange={(e) => setProjectName(e.target.value)}
      placeholder="Untitled Project"
      className="bg-transparent border-none text-center text-[15px] font-medium text-[var(--text-1)] placeholder:text-[var(--text-2)] outline-none min-w-[200px]"
    />
  );
}

function ProviderPill() {
  const { engineStatus, lastProvider } = useStore();
  return (
    <div className="flex items-center gap-1.5 text-[12px] text-[var(--text-3)] lowercase">
      <div className={`w-[6px] h-[6px] rounded-full ${engineStatus === 'online' ? 'bg-[var(--green)]' : 'bg-[var(--red)]'}`} />
      {lastProvider || 'engine'}
    </div>
  );
}

export default function Topbar() {
  const { 
    theme, 
    setTheme,
    videoQuality,
    setVideoQuality,
    videoFormat,
    setVideoFormat,
    userKeys,
    setUserKeys,
    engineStatus,
    engineUrl
  } = useStore();

  const [showSettings, setShowSettings] = useState(false);
  const [showKey, setShowKey] = useState<Record<string, boolean>>({});
  const [poolStatus, setPoolStatus] = useState<{ current_provider: string, uses_remaining: number } | null>(null);
  const settingsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (engineStatus === 'online') {
      fetch(`${engineUrl}/pool/status`)
        .then(res => res.json())
        .then(status => setPoolStatus(status))
        .catch(() => {});
    }
  }, [engineStatus, engineUrl]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (settingsRef.current && !settingsRef.current.contains(event.target as Node)) {
        setShowSettings(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
  };

  return (
    <header className="h-[48px] bg-[var(--bg-2)] border-b border-[var(--border)] flex items-center px-4 gap-4 shrink-0 relative z-[100]">
      
      {/* Left: Wordmark */}
      <span className="text-[15px] font-medium text-[var(--text-1)] lowercase tracking-tight shrink-0">
        Manim Studio
      </span>

      {/* Center: Project name */}
      <div className="flex-1 flex justify-center overflow-hidden">
        <ProjectNameInput />
      </div>

      {/* Right Actions */}
      <div className="flex items-center gap-3">
        <ProviderPill />
        
        <button 
          onClick={toggleTheme}
          className="text-[var(--text-3)] hover:text-[var(--text-1)] p-1"
        >
          {theme === 'dark' ? <Moon size={17} /> : <Sun size={17} />}
        </button>

        <div className="relative" ref={settingsRef}>
          <button 
            onClick={() => setShowSettings(!showSettings)}
            className="text-[var(--text-3)] hover:text-[var(--text-1)] p-1"
          >
            <SlidersHorizontal size={17} />
          </button>

          {showSettings && (
            <div className="absolute right-0 top-full mt-2 w-72 bg-[var(--bg-3)] border border-[var(--border)] rounded-lg shadow-2xl p-4 flex flex-col gap-4 z-[100]">
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-3)] font-medium">Quality</span>
                <select 
                  value={videoQuality} 
                  onChange={(e) => setVideoQuality(e.target.value as any)}
                  className="w-full bg-[var(--bg-4)] border border-[var(--border)] rounded-md px-2.5 py-1.5 text-[13px] text-[var(--text-1)] outline-none"
                >
                  <option value="480p">480p</option>
                  <option value="720p">720p</option>
                  <option value="1080p">1080p</option>
                </select>
              </div>

              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-3)] font-medium">Format</span>
                <select 
                  value={videoFormat} 
                  onChange={(e) => setVideoFormat(e.target.value as any)}
                  className="w-full bg-[var(--bg-4)] border border-[var(--border)] rounded-md px-2.5 py-1.5 text-[13px] text-[var(--text-1)] outline-none"
                >
                  <option value="mp4">MP4</option>
                  <option value="gif">GIF</option>
                  <option value="webm">WebM</option>
                </select>
              </div>

              <div className="flex flex-col gap-2 pt-2 border-t border-[var(--border)]">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-3)] font-medium">API Keys</span>
                {['groq', 'gemini', 'openai'].map(provider => (
                  <div key={provider} className="relative">
                    <input
                      type={showKey[provider] ? "text" : "password"}
                      placeholder={`${provider} key`}
                      value={(userKeys as any)[provider] || ''}
                      onChange={(e) => setUserKeys({ ...userKeys, [provider]: e.target.value })}
                      className="w-full bg-[var(--bg-4)] border border-[var(--border)] rounded-md px-2.5 py-1.5 text-[13px] text-[var(--text-1)] outline-none pr-8"
                    />
                    <button 
                      onClick={() => setShowKey(prev => ({ ...prev, [provider]: !prev[provider] }))}
                      className="absolute right-2 top-1/2 -translate-y-1/2 text-[var(--text-3)] hover:text-[var(--text-2)]"
                    >
                      {showKey[provider] ? <EyeOff size={14} /> : <Eye size={14} />}
                    </button>
                  </div>
                ))}
              </div>

              {poolStatus && (
                <div className="pt-2 border-t border-[var(--border)] text-[11px] text-[var(--text-3)]">
                  Using {poolStatus.current_provider} · {poolStatus.uses_remaining} uses left
                </div>
              )}
            </div>
          )}
        </div>
      </div>

    </header>
  );
}
