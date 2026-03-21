'use client';

import { useStore } from '@/lib/store';
import { useMemo } from 'react';

function PreviewSection() {
  const { 
    previewUrl, 
    setPreviewUrl, 
    previewStatus, 
    setPreviewStatus, 
    previewError, 
    setPreviewError,
    generatedCode,
    engineUrl
  } = useStore();

  const handleRunPreview = async () => {
    if (!generatedCode) return;
    
    // Clear previous preview immediately
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    }
    setPreviewError(null);
    setPreviewStatus('rendering');

    try {
      const res = await fetch(`${engineUrl}/preview`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code: generatedCode }),
      });

      const data = await res.json();

      if (!res.ok) {
        setPreviewStatus('error');
        setPreviewError(data.detail || data.error || 'Preview failed');
        return;
      }

      // Convert base64 to blob URL
      const byteCharacters = atob(data.video);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: data.mimeType });
      const url = URL.createObjectURL(blob);
      
      setPreviewUrl(url);
      setPreviewStatus('idle');
    } catch (err: any) {
      setPreviewStatus('error');
      setPreviewError(err.message);
    }
  };

  return (
    <div className="h-[220px] flex flex-col shrink-0">
      <div className="flex-1 bg-black relative overflow-hidden">
        {previewUrl ? (
          <video
            key={previewUrl}
            src={previewUrl}
            autoPlay muted loop playsInline controls
            className="w-full h-full object-contain"
          />
        ) : previewStatus === 'rendering' ? (
          <div className="flex items-center justify-center h-full">
            <span className="text-[12px] text-[var(--text-3)]">Rendering preview...</span>
          </div>
        ) : previewError ? (
          <div className="p-3 text-[11px] text-[var(--red)] font-mono overflow-auto h-full">
            {previewError}
          </div>
        ) : (
          <div className="flex items-center justify-center h-full">
            <span className="text-[12px] text-[var(--text-3)]">No preview</span>
          </div>
        )}
      </div>
      <button
        data-action="run-preview"
        onClick={handleRunPreview}
        disabled={!generatedCode || previewStatus === 'rendering'}
        className={`px-3 py-2 bg-none border-none border-t border-[var(--border)] text-[12px] w-full text-center transition-colors font-sans
          ${previewStatus === 'rendering' ? 'text-[var(--text-3)] cursor-not-allowed' : 'text-[var(--text-2)] hover:text-[var(--text-1)] cursor-pointer'}
        `}
      >
        {previewStatus === 'rendering' ? 'Rendering...' : 'Run Preview'}
      </button>
    </div>
  );
}

function TimelineSection() {
  const { scenes, setScenes } = useStore();

  const addScene = () => {
    const newScene = {
      id: Math.random().toString(36).substr(2, 9),
      name: `Scene ${scenes.length + 1}`,
      duration: 3.0,
      order: scenes.length
    };
    setScenes([...scenes, newScene]);
  };

  return (
    <div className="flex-1 flex flex-col overflow-y-auto">
      <div className="px-3 py-2 text-[11px] uppercase tracking-widest text-[var(--text-3)]">
        Timeline
      </div>
      <div className="flex flex-col">
        {(scenes.length > 0 ? scenes : [{ id: 'default', name: 'Main Scene', duration: 3.0 }]).map((scene, i) => (
          <div key={scene.id || i} className="flex items-center justify-between px-3 py-1.5 group">
            <input 
              type="text" 
              defaultValue={scene.name} 
              className="bg-transparent border-none text-[12px] text-[var(--text-1)] outline-none focus:border-b border-[var(--border)] w-2/3" 
            />
            <input 
              type="text" 
              defaultValue={`${scene.duration.toFixed(1)}s`} 
              className="bg-transparent border-none text-[12px] text-[var(--text-2)] text-right outline-none focus:border-b border-[var(--border)] w-1/4" 
            />
          </div>
        ))}
        <button 
          onClick={addScene}
          className="px-3 py-2 text-[12px] text-[var(--text-3)] hover:text-[var(--text-2)] cursor-pointer text-left"
        >
          + Add scene
        </button>
      </div>
    </div>
  );
}

function ExportSection() {
  const { 
    videoUrl, 
    setVideoUrl,
    previewUrl,
    setPreviewUrl,
    renderStatus, 
    setRenderStatus, 
    generatedCode,
    videoQuality,
    videoFormat,
    errorMessage,
    setErrorMessage,
    engineUrl
  } = useStore();

  const filename = useMemo(() => {
    const match = (generatedCode || '').match(/class\s+(\w+)\s*\(/);
    const sceneName = match ? match[1] : "Animation";
    return `${sceneName}_${videoQuality}.${videoFormat}`;
  }, [generatedCode, videoQuality, videoFormat]);

  const handleRender = async () => {
    if (!generatedCode) return;
    
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    }
    if (videoUrl) {
      URL.revokeObjectURL(videoUrl);
      setVideoUrl(null);
    }
    
    setRenderStatus('rendering');
    setErrorMessage(null);

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

      const data = await res.json();

      if (!res.ok) {
        setRenderStatus('error');
        setErrorMessage(data.detail || data.error || 'Render failed');
        return;
      }

      const fullUrl = `${engineUrl}${data.videoUrl}`;
      setVideoUrl(fullUrl);
      setRenderStatus('done');
    } catch (error: any) {
      setRenderStatus('error');
      setErrorMessage(error.message);
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
    <div className="p-3 flex flex-col gap-2 shrink-0">
      <button
        onClick={handleRender}
        disabled={!generatedCode || renderStatus === 'rendering'}
        className={`w-full py-2 rounded-md text-[13px] font-medium transition-colors cursor-pointer
          ${(generatedCode && renderStatus !== 'rendering') ? 'bg-[var(--text-1)] text-[var(--bg)]' : 'bg-[var(--bg-4)] text-[var(--text-3)]'}
        `}
      >
        {renderStatus === 'rendering' ? 'Rendering...' : 'Render'}
      </button>
      
      {videoUrl && (
        <div className="flex flex-col gap-2">
          <div className="flex gap-2">
            <button className="flex-1 py-1.5 bg-[var(--bg-3)] border border-[var(--border)] rounded-md text-[12px] text-[var(--text-2)] hover:text-[var(--text-1)] cursor-pointer">
              Save
            </button>
            <button 
              onClick={handleDownload}
              className="flex-1 py-1.5 bg-[var(--bg-3)] border border-[var(--border)] rounded-md text-[12px] text-[var(--text-2)] hover:text-[var(--text-1)] cursor-pointer"
            >
              Download
            </button>
          </div>
          <div className="text-center text-[11px] text-[var(--text-3)] truncate">
            {filename}
          </div>
        </div>
      )}

      {renderStatus === 'error' && errorMessage && (
        <div className="text-[11px] text-[var(--red)] text-center mt-1">
          {errorMessage}
        </div>
      )}
    </div>
  );
}

export default function RightPanel() {
  return (
    <div className="w-[280px] bg-[var(--bg-2)] border-l border-[var(--border)] flex flex-col shrink-0">
      <PreviewSection />
      <div className="h-[1px] bg-[var(--border)]" />
      <TimelineSection />
      <div className="h-[1px] bg-[var(--border)]" />
      <ExportSection />
    </div>
  );
}
