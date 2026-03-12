import { useStore } from '@/lib/store';

export function BottomBar() {
  const { engineStatus, lastProvider, lastModel, renderStatus, errorMessage } = useStore();

  return (
    <footer className="h-8 bg-[var(--bg-surface)] border-t border-[var(--bg-border)] flex items-center justify-between px-4 shrink-0 relative z-50">
      
      {/* Engine Status */}
      <div className="flex items-center gap-2">
        <span className={`w-1.5 h-1.5 rounded-full ${engineStatus === 'online' ? 'bg-green-500' : 'bg-red-500'}`} />
        <span className="text-[11px] text-[var(--text-dim)]">Engine {engineStatus}</span>
      </div>

      {/* Generation Info */}
      <div className="flex items-center gap-2">
        {errorMessage && (
          <span className="text-[11px] text-red-400 max-w-[400px] truncate">{errorMessage}</span>
        )}
        {!errorMessage && lastProvider && (
          <span className="text-[11px] text-[var(--text-dim)]">
            {lastProvider} · {lastModel}
          </span>
        )}
      </div>

      {/* Shortcuts */}
      <div className="flex items-center gap-3">
        <span className="text-[11px] text-[var(--text-dim)]">⌘K Generate</span>
        <span className="text-[11px] text-[var(--text-dim)]">⌘↵ Render</span>
      </div>

    </footer>
  );
}
