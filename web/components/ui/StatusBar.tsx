'use client';
import { useStore } from '@/lib/store';

export default function StatusBar() {
  const { engineStatus, lastProvider, lastModel } = useStore();
  const engineOnline = engineStatus === 'online';

  return (
    <div className="h-[28px] bg-[var(--bg-2)] border-t border-[var(--border)] flex items-center px-4 justify-between shrink-0 z-[100]">
      <div className="flex items-center gap-1.5">
        <div className={`w-[6px] h-[6px] rounded-full ${engineOnline ? 'bg-[var(--green)]' : 'bg-[var(--red)]'}`} />
        <span className="text-[11px] text-[var(--text-3)] lowercase">
          {engineOnline ? 'Engine online' : 'Engine offline'}
        </span>
      </div>
      
      <span className="text-[11px] text-[var(--text-3)] lowercase">
        {lastProvider ? `${lastProvider} · ${lastModel}` : ''}
      </span>
      
      <span className="text-[11px] text-[var(--text-3)]">
        Ctrl+K Generate · Ctrl+Enter Render
      </span>
    </div>
  );
}
