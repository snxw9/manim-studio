import { useStore } from '@/lib/store';
import { useMemo } from 'react';

export function RightPanel() {
  const { 
    videoUrl, 
    renderStatus, 
    setRenderStatus, 
    generatedCode,
    videoQuality,
    videoFormat,
    engineUrl,
    setVideoUrl,
    setVideoBlob,
    setPreviewErrors
  } = useStore();

  const filename = useMemo(() => {
    const match = generatedCode.match(/class\s+(\w+)\s*\(/);
    const sceneName = match ? match[1] : "Animation";
    return `${sceneName}_${videoQuality}.${videoFormat}`;
  }, [generatedCode, videoQuality, videoFormat]);

  const handleRender = async () => {
    if (!generatedCode) return;
    setRenderStatus('rendering');
    try {
      const res = await fetch(`${engineUrl}/render`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          code: generatedCode,
          quality: videoQuality,
          format: videoFormat
        })
      });

      if (!res.ok) {
        if (res.status === 422) {
          const data = await res.json();
          setPreviewErrors(data.details || []);
          throw new Error(data.error);
        }
        const data = await res.json();
        throw new Error(data.error);
      }

      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      setVideoUrl(url);
      setVideoBlob(blob);
      setRenderStatus('done');
    } catch (error: any) {
      console.error(error);
      setRenderStatus('error');
      alert(error.message);
    }
  };

  return (
    <div className="w-[320px] h-full bg-[var(--bg-surface)] border-l border-[var(--bg-border)] flex flex-col shrink-0 animate-fade-in-right delay-160">
      
      {/* Preview Section */}
      <div className="h-[240px] bg-black relative flex flex-col border-b border-[var(--bg-border)]">
        {videoUrl ? (
          <video src={videoUrl} controls autoPlay muted loop className="w-full h-full object-contain" />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center opacity-20">
            <svg width="100" height="40" viewBox="0 0 100 40">
              <path d="M0 20 Q 25 0, 50 20 T 100 20" fill="none" stroke="var(--accent)" strokeWidth="2" className="animate-[wave-undulate_4s_linear_infinite]" strokeDasharray="100" />
              <path d="M0 20 Q 25 10, 50 20 T 100 20" fill="none" stroke="var(--accent)" strokeWidth="1" className="animate-[wave-undulate_5s_linear_infinite_reverse] opacity-50" strokeDasharray="100" />
              <path d="M0 20 Q 25 30, 50 20 T 100 20" fill="none" stroke="var(--accent)" strokeWidth="1" className="animate-[wave-undulate_6s_linear_infinite] opacity-30" strokeDasharray="100" />
            </svg>
          </div>
        )}
        
        {renderStatus === 'rendering' && (
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-[var(--bg-border)]">
            <div className="h-full bg-[var(--accent)] w-1/2 animate-shimmer bg-[length:200%_100%]" />
          </div>
        )}
      </div>

      {/* Timeline Section */}
      <div className="flex-1 flex flex-col p-4 border-b border-[var(--bg-border)] overflow-y-auto">
        <span className="text-[11px] uppercase tracking-widest text-[var(--text-dim)] mb-4">Timeline</span>
        
        <div className="flex flex-col gap-2">
          <div className="group flex items-center justify-between py-2 px-2 hover:bg-[var(--bg-elevated)] transition-colors relative">
            <div className="absolute left-0 top-1 bottom-1 w-[2px] bg-[var(--bg-border)] group-hover:bg-[var(--text-dim)] transition-colors cursor-grab" />
            <span className="text-sm text-[var(--text-primary)] ml-3">Main Animation</span>
            <input type="text" defaultValue="3.0s" className="w-12 bg-transparent text-right text-xs text-[var(--text-dim)] focus:text-[var(--text-primary)] focus:outline-none" />
          </div>
          
          <button className="mt-2 py-2 border border-dashed border-[var(--bg-border)] rounded text-xs text-[var(--text-dim)] hover:text-[var(--text-primary)] hover:border-[var(--text-dim)] transition-colors">
            + Add Scene
          </button>
        </div>
      </div>

      {/* Export Section */}
      <div className="p-4 flex flex-col gap-3 shrink-0">
        <button
          onClick={handleRender}
          disabled={renderStatus === 'rendering' || !generatedCode}
          className={`w-full py-2.5 rounded text-sm font-medium font-sans text-white transition-all relative overflow-hidden ${(!generatedCode || renderStatus === 'rendering') ? 'bg-[var(--bg-border)] text-[var(--text-dim)]' : 'bg-[var(--gradient-warm)] hover:shadow-[0_0_15px_var(--accent-glow)]'}`}
        >
          {renderStatus === 'rendering' && <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent animate-shimmer" />}
          Render
        </button>
        
        <div className="flex gap-2">
          <button className="flex-1 py-1.5 text-xs text-[var(--text-secondary)] border border-[var(--bg-border)] rounded hover:border-[var(--accent)] hover:text-[var(--text-primary)] transition-colors">
            Save
          </button>
          <button className="flex-1 py-1.5 text-xs text-[var(--text-secondary)] border border-[var(--bg-border)] rounded hover:border-[var(--accent)] hover:text-[var(--text-primary)] transition-colors">
            Download
          </button>
        </div>
        
        <div className="text-center">
          <span className="text-[11px] text-[var(--text-dim)]">{filename}</span>
        </div>
      </div>

    </div>
  );
}
