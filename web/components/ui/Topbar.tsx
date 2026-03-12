import { useState, useRef, useEffect } from 'react';
import { useStore } from '@/lib/store';
import { SlidersHorizontal, Sun, Moon, Check } from 'lucide-react';

export function Topbar() {
  const { 
    projectName, 
    setProjectName, 
    theme, 
    setTheme,
    videoQuality,
    setVideoQuality,
    videoFormat,
    setVideoFormat,
    engineUrl,
    setEngineUrl,
    userKeys,
    setUserKeys
  } = useStore();

  const [poolStatus, setPoolStatus] = useState<{ current_provider: string, uses_remaining: number } | null>(null);
  const [showSettings, setShowSettings] = useState(false);
  const settingsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetch(`${engineUrl}/pool/status`)
      .then(res => res.json())
      .then(data => setPoolStatus(data))
      .catch(() => {});
  }, [engineUrl]);

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
  };

  return (
    <header className="h-12 bg-[var(--bg-surface)] border-b border-[var(--bg-border)] flex items-center justify-between px-4 shrink-0 relative z-50">
      
      {/* Left: Logo */}
      <div className="flex items-center gap-2">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 12H8L12 4L16 20L20 12" stroke="url(#orange-grad)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          <defs>
            <linearGradient id="orange-grad" x1="4" y1="4" x2="20" y2="20" gradientUnits="userSpaceOnUse">
              <stop stopColor="#e8621a" />
              <stop offset="1" stopColor="#d14500" />
            </linearGradient>
          </defs>
        </svg>
        <span className="font-medium text-[var(--text-primary)] text-sm tracking-tight lowercase">manim studio</span>
      </div>

      {/* Center: Project Name */}
      <div className="absolute left-1/2 -translate-x-1/2">
        <input 
          type="text" 
          value={projectName}
          onChange={(e) => setProjectName(e.target.value)}
          placeholder="Untitled Project"
          className="bg-transparent border border-transparent hover:border-[var(--bg-border)] focus:border-[var(--accent)] focus:outline-none rounded px-2 py-1 text-sm font-medium text-[var(--text-primary)] text-center w-48 transition-colors placeholder-[var(--text-dim)]"
        />
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-3">
        {/* Provider Pill */}
        {poolStatus && (
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[var(--bg-elevated)] border border-[var(--bg-border)] text-xs text-[var(--text-secondary)] cursor-pointer hover:border-[var(--accent)] transition-colors">
            <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse-dot" />
            {poolStatus.current_provider} {10 - poolStatus.uses_remaining}/10
          </div>
        )}

        {/* Settings Dropdown */}
        <div className="relative" ref={settingsRef}>
          <button 
            onClick={() => setShowSettings(!showSettings)}
            className="p-1.5 text-[var(--text-dim)] hover:text-[var(--text-primary)] transition-colors rounded"
          >
            <SlidersHorizontal size={16} />
          </button>

          {showSettings && (
            <div className="absolute right-0 top-full mt-2 w-72 bg-[var(--bg-surface)] border border-[var(--bg-border)] rounded-lg shadow-xl p-4 flex flex-col gap-4 animate-fade-in origin-top-right">
              
              {/* Theme Toggle */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-[var(--text-secondary)]">Theme</span>
                <button onClick={toggleTheme} className="p-1.5 bg-[var(--bg-base)] border border-[var(--bg-border)] rounded text-[var(--text-primary)] hover:border-[var(--accent)] transition-colors">
                  {theme === 'dark' ? <Moon size={14} /> : <Sun size={14} />}
                </button>
              </div>

              {/* API Keys */}
              <div className="flex flex-col gap-2 border-t border-[var(--bg-border)] pt-3">
                <span className="text-sm text-[var(--text-secondary)]">API Keys</span>
                {['groq', 'gemini', 'openai'].map(provider => (
                  <input
                    key={provider}
                    type="password"
                    placeholder={`${provider} key`}
                    value={(userKeys as any)[provider] || ''}
                    onChange={(e) => setUserKeys({ ...userKeys, [provider]: e.target.value })}
                    className="bg-[var(--bg-base)] border border-[var(--bg-border)] focus:border-[var(--accent)] focus:outline-none rounded px-2 py-1.5 text-xs text-[var(--text-primary)]"
                  />
                ))}
              </div>

              {/* Quality & Format */}
              <div className="flex gap-2 border-t border-[var(--bg-border)] pt-3">
                <div className="flex-1 flex flex-col gap-1">
                  <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)]">Quality</span>
                  <select 
                    value={videoQuality} 
                    onChange={(e) => setVideoQuality(e.target.value as any)}
                    className="bg-[var(--bg-base)] border border-[var(--bg-border)] rounded px-2 py-1 text-xs text-[var(--text-primary)]"
                  >
                    <option value="480p">480p</option>
                    <option value="720p">720p</option>
                    <option value="1080p">1080p</option>
                    <option value="2160p">4K</option>
                  </select>
                </div>
                <div className="flex-1 flex flex-col gap-1">
                  <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)]">Format</span>
                  <select 
                    value={videoFormat} 
                    onChange={(e) => setVideoFormat(e.target.value as any)}
                    className="bg-[var(--bg-base)] border border-[var(--bg-border)] rounded px-2 py-1 text-xs text-[var(--text-primary)]"
                  >
                    <option value="mp4">MP4</option>
                    <option value="gif">GIF</option>
                    <option value="webm">WebM</option>
                    <option value="mov">MOV</option>
                  </select>
                </div>
              </div>

              {/* Engine URL */}
              <div className="flex flex-col gap-1 border-t border-[var(--bg-border)] pt-3">
                <span className="text-[10px] uppercase tracking-wider text-[var(--text-dim)]">Engine URL</span>
                <input
                  type="text"
                  value={engineUrl}
                  onChange={(e) => setEngineUrl(e.target.value)}
                  className="bg-[var(--bg-base)] border border-[var(--bg-border)] focus:border-[var(--accent)] focus:outline-none rounded px-2 py-1.5 text-xs text-[var(--text-primary)]"
                />
              </div>

            </div>
          )}
        </div>

        {/* Save Button */}
        <button className="text-sm text-[var(--text-secondary)] hover:text-[var(--accent)] transition-colors px-2 py-1 font-medium">
          Save
        </button>
      </div>

    </header>
  );
}
