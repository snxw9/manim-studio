import { useStore } from '@/lib/store';

export function BottomBar() {
  const { engineStatus, lastProvider, lastModel } = useStore();

  return (
    <footer className="h-[28px] bg-[var(--bg-surface)] border-t border-[var(--bg-border)] flex items-center justify-between px-4 shrink-0 relative z-50">
      
      {/* Left: Engine Status */}
      <div className="flex items-center gap-2">
        <span className={`w-[6px] h-[6px] rounded-full ${
          engineStatus === 'online' ? 'bg-[var(--green)]' : 
          engineStatus === 'offline' ? 'bg-[var(--red)]' : 
          'bg-[var(--yellow)]'
        }`} />
        <span className="text-[11px] text-[var(--text-dim)] lowercase">
          {engineStatus || 'checking...'}
        </span>
      </div>

      {/* Center: Model Info */}
      <div className="flex items-center">
        {lastProvider && (
          <span className="text-[11px] text-[var(--text-dim)]">
            {lastProvider} · {lastModel}
          </span>
        )}
      </div>

      {/* Right: Shortcuts */}
      <div className="flex items-center gap-4">
        <span className="text-[11px] text-[var(--text-dim)]">Ctrl+K Generate</span>
        <span className="text-[11px] text-[var(--text-dim)]">Ctrl+Enter Render</span>
      </div>

    </footer>
  );
}
