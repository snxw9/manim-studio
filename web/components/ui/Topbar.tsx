import { useState, useRef, useEffect } from 'react';
import { useStore } from '@/lib/store';
import { SlidersHorizontal, Sun, Moon } from 'lucide-react';

export function Topbar() {
  const { 
    theme, 
    setTheme,
    videoQuality,
    setVideoQuality,
    videoFormat,
    setVideoFormat,
    engineUrl,
    setEngineUrl,
    userKeys,
    setUserKeys,
    engineStatus
  } = useStore();

  const [poolStatus, setPoolStatus] = useState<{ current_provider: string, uses_remaining: number } | null>(null);
  const [showSettings, setShowSettings] = useState(false);
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
    <header className="h-[44px] bg-[var(--bg-surface)] border-b border-[var(--bg-border)] flex items-center justify-between px-4 shrink-0 relative z-50">
      
      {/* Left: Wordmark */}
      <div className="flex items-center">
        <span className="font-sora text-[14px] font-[500] text-[var(--text-primary)] lowercase tracking-tight">manim studio</span>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-4">
        {/* Provider Status */}
        {poolStatus && (
          <div className="flex items-center gap-1.5 text-[11px] text-[var(--text-dim)]">
            <span className={`w-[6px] h-[6px] rounded-full ${engineStatus === 'online' ? 'bg-[var(--green)]' : 'bg-[var(--red)]'}`} />
            {poolStatus.current_provider}
          </div>
        )}

        {/* Theme Toggle */}
        <button 
          onClick={toggleTheme}
          className="p-1 text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
        >
          {theme === 'dark' ? <Moon size={16} /> : <Sun size={16} />}
        </button>

        {/* Settings Dropdown */}
        <div className="relative" ref={settingsRef}>
          <button 
            onClick={() => setShowSettings(!showSettings)}
            className="p-1 text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
          >
            <SlidersHorizontal size={16} />
          </button>

          {showSettings && (
            <div className="absolute right-0 top-full mt-2 w-72 bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded shadow-2xl p-4 flex flex-col gap-4 z-[100]">
              
              <div className="flex flex-col gap-1">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)] font-medium">Quality</span>
                <select 
                  value={videoQuality} 
                  onChange={(e) => setVideoQuality(e.target.value as any)}
                  className="w-full bg-[var(--bg-input)] border border-[var(--bg-border)] rounded px-2 py-1.5 text-xs text-[var(--text-primary)]"
                >
                  <option value="480p">480p</option>
                  <option value="720p">720p</option>
                  <option value="1080p">1080p</option>
                </select>
              </div>

              <div className="flex flex-col gap-1">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)] font-medium">Format</span>
                <select 
                  value={videoFormat} 
                  onChange={(e) => setVideoFormat(e.target.value as any)}
                  className="w-full bg-[var(--bg-input)] border border-[var(--bg-border)] rounded px-2 py-1.5 text-xs text-[var(--text-primary)]"
                >
                  <option value="mp4">MP4</option>
                  <option value="gif">GIF</option>
                  <option value="webm">WebM</option>
                </select>
              </div>

              <div className="flex flex-col gap-1">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)] font-medium">Engine URL</span>
                <input
                  type="text"
                  value={engineUrl}
                  onChange={(e) => setEngineUrl(e.target.value)}
                  className="w-full bg-[var(--bg-input)] border border-[var(--bg-border)] rounded px-2 py-1.5 text-xs text-[var(--text-primary)]"
                />
              </div>

              <div className="flex flex-col gap-2 pt-2 border-t border-[var(--bg-border)]">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)] font-medium">API Keys</span>
                {['groq', 'gemini', 'openai'].map(provider => (
                  <input
                    key={provider}
                    type="password"
                    placeholder={`${provider} key`}
                    value={(userKeys as any)[provider] || ''}
                    onChange={(e) => setUserKeys({ ...userKeys, [provider]: e.target.value })}
                    className="w-full bg-[var(--bg-input)] border border-[var(--bg-border)] rounded px-2 py-1.5 text-xs text-[var(--text-primary)]"
                  />
                ))}
              </div>

              {poolStatus && (
                <div className="pt-2 border-t border-[var(--bg-border)]">
                  <span className="text-[11px] text-[var(--text-dim)]">
                    Pool: {poolStatus.current_provider} · {poolStatus.uses_remaining} uses left
                  </span>
                </div>
              )}

            </div>
          )}
        </div>
      </div>

    </header>
  );
}
