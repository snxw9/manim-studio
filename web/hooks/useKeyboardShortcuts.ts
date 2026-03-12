import { useEffect } from 'react';

export function useKeyboardShortcuts({
  onGenerate,
  onRender,
  onToggleTab,
  onFocusPrompt,
}: {
  onGenerate: () => void;
  onRender: () => void;
  onToggleTab: () => void;
  onFocusPrompt: () => void;
}) {
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const mod = e.metaKey || e.ctrlKey;
      if (mod && e.key === 'k') { e.preventDefault(); onGenerate(); }
      if (mod && e.key === 'Enter') { e.preventDefault(); onRender(); }
      if (mod && e.key === '/') { e.preventDefault(); onToggleTab(); }
      if (mod && e.key === 'l') { e.preventDefault(); onFocusPrompt(); }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onGenerate, onRender, onToggleTab, onFocusPrompt]);
}
