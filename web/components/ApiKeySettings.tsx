'use client';
import { useState, useEffect } from 'react';

interface StoredKeys {
  groq?: string;
  gemini?: string;
  openai?: string;
}

const KEY_LINKS = {
  groq: { label: 'Groq', url: 'https://console.groq.com/keys', free: true, recommended: true },
  gemini: { label: 'Gemini', url: 'https://aistudio.google.com', free: true, recommended: false },
  openai: { label: 'OpenAI', url: 'https://platform.openai.com/api-keys', free: false, recommended: false },
};

export default function ApiKeySettings({ onKeysChange }: { onKeysChange: (keys: StoredKeys) => void }) {
  const [keys, setKeys] = useState<StoredKeys>({});
  const [show, setShow] = useState<Record<string, boolean>>({});
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('user_api_keys');
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        setKeys(parsed);
        onKeysChange(parsed);
      } catch (e) {
        console.error("Failed to parse stored API keys", e);
      }
    }
  }, [onKeysChange]);

  const handleSave = () => {
    // Only store non-empty keys
    const filtered = Object.fromEntries(
      Object.entries(keys).filter(([_, v]) => v && v.trim())
    );
    localStorage.setItem('user_api_keys', JSON.stringify(filtered));
    onKeysChange(filtered);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const handleClear = (provider: string) => {
    const updated = { ...keys, [provider]: '' };
    setKeys(updated);
  };

  return (
    <div className="p-4 bg-[#1a1a1a] rounded-xl border border-white/10 space-y-4">
      <div>
        <h3 className="text-sm font-semibold text-white/80">🔑 API Keys (Optional)</h3>
        <p className="text-xs text-white/40 mt-1">
          Add your own key for unlimited generations. Without a key you get 20 free generations/day.
        </p>
      </div>

      {/* Recommended banner */}
      <div className="bg-violet-900/20 border border-violet-700/30 rounded-lg p-3 text-xs text-violet-300">
        <span className="font-semibold">⭐ Recommended:</span> Get a free Groq key — no credit card needed.{' '}
        <a href="https://console.groq.com/keys" target="_blank" rel="noopener noreferrer"
          className="underline hover:text-violet-200">
          console.groq.com →
        </a>
      </div>

      {/* Key inputs */}
      {Object.entries(KEY_LINKS).map(([provider, info]) => (
        <div key={provider}>
          <div className="flex items-center justify-between mb-1">
            <label className="text-xs text-white/60 flex items-center gap-1">
              {info.label}
              {info.free && <span className="text-green-400 text-[10px]">FREE</span>}
              {info.recommended && <span className="text-violet-400 text-[10px]">RECOMMENDED</span>}
            </label>
            <a href={info.url} target="_blank" rel="noopener noreferrer"
              className="text-[10px] text-white/30 hover:text-white/60 underline">
              Get key →
            </a>
          </div>
          <div className="flex gap-2">
            <input
              type={show[provider] ? 'text' : 'password'}
              value={keys[provider as keyof StoredKeys] || ''}
              onChange={(e) => setKeys(prev => ({ ...prev, [provider]: e.target.value }))}
              placeholder={`${info.label} API key (optional)`}
              className="flex-1 bg-white/5 border border-white/10 rounded-lg px-3 py-1.5 text-xs text-white placeholder-white/20 focus:outline-none focus:border-violet-500"
            />
            <button onClick={() => setShow(s => ({ ...s, [provider]: !s[provider] }))}
              className="text-white/30 hover:text-white/60 px-2 text-xs">
              {show[provider] ? '🙈' : '👁️'}
            </button>
            {keys[provider as keyof StoredKeys] && (
              <button onClick={() => handleClear(provider)}
                className="text-red-400/50 hover:text-red-400 px-2 text-xs">✕</button>
            )}
          </div>
        </div>
      ))}

      <button onClick={handleSave}
        className="w-full py-2 bg-violet-600 hover:bg-violet-500 text-white text-sm rounded-lg transition">
        {saved ? '✅ Saved!' : 'Save Keys'}
      </button>

      <p className="text-[10px] text-white/20 text-center">
        Keys are stored locally in your browser only. Never sent anywhere except the AI providers directly.
      </p>
    </div>
  );
}
