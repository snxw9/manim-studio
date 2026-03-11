'use client';
import { useState, useEffect } from 'react';

export default function BrowserNotice() {
  const [show, setShow] = useState(false);

  useEffect(() => {
    const dismissed = localStorage.getItem('browser-notice-dismissed');
    const isChromium = typeof window !== 'undefined' && 'showDirectoryPicker' in window;
    if (!isChromium && !dismissed) setShow(true);
  }, []);

  if (!show) return null;

  return (
    <div className="fixed bottom-4 right-4 max-w-sm bg-yellow-900/80 border border-yellow-600/50 text-yellow-200 text-xs p-3 rounded-xl z-50 shadow-2xl backdrop-blur-md">
      <p className="font-semibold mb-1 flex items-center gap-2">
        <span>⚠️</span> Limited browser support
      </p>
      <p className="opacity-90">
        Some advanced features (custom save folder, direct file writing) require Chrome or Edge. 
        Standard downloads and all core animation features work in any browser.
      </p>
      <button
        onClick={() => { localStorage.setItem('browser-notice-dismissed', '1'); setShow(false); }}
        className="mt-2 text-yellow-400 underline hover:text-yellow-300 transition-colors font-medium"
      >
        Got it, dismiss
      </button>
    </div>
  );
}
