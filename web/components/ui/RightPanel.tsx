'use client';

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
    setVideoUrl,
    engineUrl
  } = useStore();

  const filename = useMemo(() => {
    const match = generatedCode.match(/class\s+(\w+)\s*\(/);
    const sceneName = match ? match[1] : "Animation";
    return `${sceneName}_${videoQuality}.${videoFormat}`;
  }, [generatedCode, videoQuality, videoFormat]);

  const runPreview = async () => {
    const btn = document.querySelector('[data-action="render"]') as HTMLButtonElement | null;
    btn?.click();
  };

  const handleRender = async () => {
    if (!generatedCode) return;
    setRenderStatus('rendering');
    try {
      const res = await fetch(`/api/render`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          code: generatedCode,
          quality: videoQuality,
          format: videoFormat
        })
      });

      const data = await res.json();

      if (!res.ok) {
        setRenderStatus('error');
        alert(data.error || 'Render failed');
        return;
      }

      const fullUrl = `${engineUrl}${data.videoUrl}`;
      setVideoUrl(fullUrl);
      setRenderStatus('done');
    } catch (error: any) {
      console.error(error);
      setRenderStatus('error');
      alert(error.message);
    }
  };

  const handleDownload = async () => {
    if (!videoUrl) return;
    try {
      const res = await fetch(videoUrl);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert('Download failed');
    }
  };

  return (
    <div className="w-full h-full flex flex-col shrink-0 overflow-hidden">
      
      {/* PREVIEW */}
      <div className="h-[200px] bg-black relative flex flex-col border-b border-[var(--bg-border)] shrink-0">
        {videoUrl ? (
          <video key={videoUrl} src={videoUrl} controls autoPlay muted loop className="w-full h-full object-contain" />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="text-[12px] text-[var(--text-dim)]">No preview</span>
          </div>
        )}
      </div>
      <div className="p-2 border-b border-[var(--bg-border)] shrink-0">
        <button 
          onClick={runPreview}
          className="w-full py-1.5 border border-[var(--bg-border)] rounded text-[12px] text-[var(--text-secondary)] hover:border-[var(--accent)] hover:text-[var(--accent)]"
        >
          Run Preview
        </button>
      </div>

      {/* TIMELINE */}
      <div className="flex-1 flex flex-col p-4 border-b border-[var(--bg-border)] overflow-y-auto">
        <span className="text-[11px] uppercase tracking-widest text-[var(--text-dim)] mb-4">Timeline</span>
        
        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between py-1 border-b border-transparent">
            <input 
              type="text" 
              defaultValue="Main Scene" 
              className="bg-transparent border-none text-[13px] text-[var(--text-primary)] focus:outline-none focus:border-b-[var(--bg-border)] w-2/3" 
            />
            <input 
              type="text" 
              defaultValue="3.0s" 
              className="bg-transparent border-none text-[12px] text-[var(--text-dim)] text-right focus:outline-none focus:border-b-[var(--bg-border)] w-1/4" 
            />
          </div>
          
          <button className="text-left mt-2 text-[11px] text-[var(--text-dim)] hover:text-[var(--text-primary)]">
            + Add scene
          </button>
        </div>
      </div>

      {/* EXPORT */}
      <div className="p-4 flex flex-col gap-3 shrink-0">
        <button
          onClick={handleRender}
          disabled={renderStatus === 'rendering' || !generatedCode}
          className={`w-full py-2.5 rounded-[4px] text-[13px] font-sora font-[500] text-white ${(!generatedCode || renderStatus === 'rendering') ? 'bg-[var(--bg-border)] text-[var(--text-dim)]' : 'bg-[var(--accent)] hover:bg-[var(--accent-hover)]'}`}
        >
          {renderStatus === 'rendering' ? 'Rendering...' : 'Render'}
        </button>
        
        <div className="flex gap-2">
          <button className="flex-1 py-1.5 text-[12px] text-[var(--text-secondary)] border border-[var(--bg-border)] rounded hover:border-[var(--text-primary)]">
            Save
          </button>
          <button 
            onClick={handleDownload}
            disabled={!videoUrl}
            className="flex-1 py-1.5 text-[12px] text-[var(--text-secondary)] border border-[var(--bg-border)] rounded hover:border-[var(--text-primary)] disabled:opacity-30"
          >
            Download
          </button>
        </div>
        
        <div className="text-center mt-1">
          <span className="text-[11px] text-[var(--text-dim)]">{filename}</span>
        </div>
      </div>

    </div>
  );
}
