'use client';

import { useEffect, useState, useMemo } from 'react';
import { useStore, Scene } from '@/lib/store';
import { DndContext, DragEndEvent } from '@dnd-kit/core';
import { Loader2, Play, Wand2, RefreshCw, AlertCircle, CheckCircle2, Zap } from 'lucide-react';
import MonacoEditor from '@/components/MonacoEditor';
import VoicePrompt from '@/components/VoicePrompt';
import TemplateLibrary from '@/components/TemplateLibrary';
import TimelineEditor from '@/components/TimelineEditor';
import AISuggestions from '@/components/AISuggestions';
import AssetLibrary from '@/components/AssetLibrary';
import ProjectManager from '@/components/ProjectManager';
import DragDropBuilder from '@/components/DragDropBuilder';
import ExportPanel from '@/components/ExportPanel';
import BrowserNotice from '@/components/BrowserNotice';

export default function Home() {
  const {
    prompt,
    setPrompt,
    generatedCode,
    setGeneratedCode,
    renderStatus,
    setRenderStatus,
    videoUrl,
    setVideoUrl,
    videoBlob,
    setVideoBlob,
    videoFormat,
    videoQuality,
    activeTab,
    setActiveTab,
    selectedTemplate,
    setSuggestions,
    previewUrl,
    previewStatus,
    setPreviewStatus,
    previewError,
    previewErrors,
    setPreviewErrors,
    setScenes,
    engineStatus,
    setEngineStatus,
    lastProvider,
    setLastProvider,
    lastModel,
    setLastModel
  } = useStore();

  const filename = useMemo(() => {
    const match = generatedCode.match(/class\s+(\w+)\s*\(/);
    const sceneName = match ? match[1] : "Animation";
    return `${sceneName}_${videoQuality}.${videoFormat}`;
  }, [generatedCode, videoQuality, videoFormat]);

  // Engine health check on mount
  useEffect(() => {
    const checkEngine = async () => {
      try {
        const res = await fetch('http://localhost:8000/health');
        if (res.ok) setEngineStatus('online');
        else setEngineStatus('offline');
      } catch (err) {
        setEngineStatus('offline');
      }
    };
    checkEngine();
    const interval = setInterval(checkEngine, 10000); // Check every 10s
    return () => clearInterval(interval);
  }, [setEngineStatus]);

  // Auto-cleanup previews on code change
  useEffect(() => {
    if (!generatedCode) return;
    const match = generatedCode.match(/class\s+(\w+)\s*\(/);
    const sceneName = match ? match[1] : null;

    if (sceneName) {
      fetch('/api/cleanup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ scene_name: sceneName })
      }).catch(err => console.error("Cleanup failed:", err));
    }
  }, [generatedCode]);

  const handleDragEnd = (event: DragEndEvent) => {
    const { over, active } = event;
    if (over && over.id === 'canvas-droppable') {
      const assetData = active.data.current as any;
      if (assetData) {
        setPrompt(prompt + (prompt ? ' ' : '') + `Include a ${assetData.type}: ${assetData.label}.`);
      }
    }
  };

  const handleGenerate = async () => {
    if (!prompt) return;
    setRenderStatus('generating');
    setPreviewStatus('auto-correcting');
    try {
      const res = await fetch('/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, template: selectedTemplate })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error);

      setGeneratedCode(data.code);
      setSuggestions(data.suggestions || []);
      setLastProvider(data.provider);
      setLastModel(data.model);

      // Basic mock scene parsing for timeline
      setScenes([{ id: 'scene-1', name: 'Main Animation', duration: 3, order: 0 }]);

      setActiveTab('code');
      setRenderStatus('idle');
      // Success state for banner
      setTimeout(() => setPreviewStatus('idle'), 3000);
    } catch (error) {
      console.error(error);
      setRenderStatus('error');
      setPreviewStatus('error');
    }
  };

  const handleRender = async () => {
    if (!generatedCode) return;
    setRenderStatus('rendering');
    try {
      const res = await fetch('/api/render', {
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
    <DndContext onDragEnd={handleDragEnd}>
      <div className="flex h-screen bg-[#0f0f0f] text-zinc-300 font-sans">
        
        {/* Engine Offline Banner */}
        {engineStatus === 'offline' && (
          <div className="fixed top-0 left-0 right-0 z-[100] bg-red-900 text-red-100 text-[10px] uppercase tracking-widest text-center py-1.5 px-4 font-bold shadow-xl border-b border-red-700/50 backdrop-blur-md">
            <span className="opacity-80 mr-2">⚠️ Python engine is offline — Start it with:</span>
            <code className="bg-black/30 px-2 py-0.5 rounded border border-white/10 lowercase font-mono tracking-normal">
              cd engine && uvicorn main:app --reload --port 8000
            </code>
          </div>
        )}

        {/* Left Sidebar */}
        <div className={`w-64 bg-[#1a1a1a] border-r border-zinc-800 flex flex-col h-full shrink-0 transition-all duration-300 ${engineStatus === 'offline' ? 'pt-8' : ''}`}>
          <div className="p-4 border-b border-zinc-800 flex items-center justify-between">
            <h1 className="font-bold text-white tracking-tight flex items-center gap-2">
              <Wand2 size={18} className="text-purple-500" />
              Manim Studio
            </h1>
          </div>
          <div className="flex-1 overflow-y-auto">
            <TemplateLibrary />
            <AssetLibrary />
          </div>
          <div className="p-4 border-t border-zinc-800">
            <ProjectManager />
          </div>
        </div>

        {/* Main Canvas */}
        <div className={`flex-1 flex flex-col min-w-0 h-full relative bg-[#0a0a0a] transition-all duration-300 ${engineStatus === 'offline' ? 'pt-8' : ''}`}>
          {/* Tabs */}
          <div className="flex items-center gap-1 p-2 bg-[#1a1a1a] border-b border-zinc-800">
            <button 
              onClick={() => setActiveTab('prompt')}
              className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors ${activeTab === 'prompt' ? 'bg-zinc-800 text-white' : 'text-zinc-400 hover:text-zinc-200'}`}
            >
              Design
            </button>
            <button 
              onClick={() => setActiveTab('code')}
              className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors ${activeTab === 'code' ? 'bg-zinc-800 text-white' : 'text-zinc-400 hover:text-zinc-200'}`}
            >
              Code
            </button>
            
            {activeTab === 'code' && lastProvider && (
              <div className="ml-4 flex items-center gap-2 px-2 py-1 rounded-full bg-zinc-900 border border-zinc-800">
                <Zap size={10} className="text-purple-400 fill-purple-400" />
                <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-tighter">
                  {lastProvider} <span className="opacity-30 mx-1">/</span> {lastModel}
                </span>
              </div>
            )}

            <div className="flex-1" />
            <button
              onClick={activeTab === 'code' ? handleRender : handleGenerate}
              disabled={renderStatus === 'generating' || renderStatus === 'rendering'}
              className="flex items-center gap-2 bg-purple-600 hover:bg-purple-700 text-white px-4 py-1.5 rounded-md text-sm font-medium disabled:opacity-50 transition-colors shadow-sm"
            >
              {(renderStatus === 'generating' || renderStatus === 'rendering') ? (
                <Loader2 size={16} className="animate-spin" />
              ) : activeTab === 'code' ? (
                <Play size={16} />
              ) : (
                <Wand2 size={16} />
              )}
              {activeTab === 'code' ? 'Render Final' : 'Generate Code'}
            </button>
          </div>

          {/* Main Area */}
          <div className="flex-1 p-4 overflow-hidden flex flex-col gap-4">
            {activeTab === 'prompt' ? (
              <div className="flex-1 flex flex-col gap-4">
                <div className="flex gap-2">
                  <div className="flex-1 relative">
                    <textarea
                      value={prompt}
                      onChange={(e) => setPrompt(e.target.value)}
                      placeholder="Describe the math animation you want to build..."
                      className="w-full h-32 bg-zinc-900 border border-zinc-800 rounded-lg p-4 text-white placeholder:text-zinc-600 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500 resize-none shadow-inner"
                    />
                    <div className="absolute bottom-3 right-3 flex gap-2">
                      <VoicePrompt />
                    </div>
                  </div>
                </div>
                <AISuggestions />
                <div className="flex-1 bg-zinc-900/50 rounded-lg">
                  <DragDropBuilder />
                </div>
              </div>
            ) : (
              <div className="flex-1 flex flex-col gap-2 relative">
                {previewStatus === 'auto-correcting' && (
                  <div className="absolute top-4 left-1/2 -translate-x-1/2 z-20 bg-yellow-500/90 text-yellow-950 px-4 py-2 rounded-full text-xs font-bold flex items-center gap-2 shadow-lg animate-bounce border border-yellow-400">
                    <RefreshCw size={14} className="animate-spin" />
                    ⚠️ Code contains invalid Manim methods — auto-fixing...
                  </div>
                )}
                {previewStatus === 'idle' && generatedCode && previewErrors.length === 0 && (
                   <div className="absolute top-4 left-1/2 -translate-x-1/2 z-20 bg-green-500/90 text-white px-4 py-2 rounded-full text-xs font-bold flex items-center gap-2 shadow-lg border border-green-400">
                    <CheckCircle2 size={14} />
                    ✅ Code was auto-corrected or is valid
                  </div>
                )}
                <MonacoEditor />
              </div>
            )}
          </div>

          {/* Bottom Preview Panel */}
          {activeTab === 'code' && (
            <div className="h-64 border-t border-zinc-800 bg-[#1a1a1a] flex p-4 gap-4">
              <div className="w-1/3 flex flex-col relative rounded-md overflow-hidden bg-black border border-zinc-800 shadow-inner">
                {previewStatus === 'rendering' ? (
                  <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/80 z-10 text-zinc-400 text-sm">
                    <RefreshCw className="animate-spin mb-2 text-purple-500" size={20} />
                    Rendering preview...
                  </div>
                ) : null}

                {previewError ? (
                  <div className="p-3 text-xs text-red-400 font-mono overflow-auto flex items-start gap-2 h-full bg-red-950/20">
                    <AlertCircle size={14} className="shrink-0 mt-0.5" />
                    {previewError}
                  </div>
                ) : previewUrl ? (
                  <video 
                    src={previewUrl} 
                    autoPlay 
                    muted 
                    loop 
                    playsInline
                    controls
                    className="w-full h-full object-contain" 
                  />
                ) : (
                  <div className="flex items-center justify-center h-full text-zinc-600 text-sm italic">
                    No preview available
                  </div>
                )}
              </div>
              <div className="flex-1 flex flex-col gap-4 overflow-hidden">
                <ExportPanel videoBlob={videoBlob} filename={filename} />
                <div className="flex-1 flex flex-col min-h-0">
                  <h3 className="text-xs font-bold text-zinc-500 uppercase tracking-widest mb-2">Final Output</h3>
                  {renderStatus === 'done' && videoUrl ? (
                    <div className="flex-1 bg-black rounded-lg overflow-hidden border border-zinc-800 shadow-2xl relative group">
                      <video src={videoUrl} controls className="w-full h-full object-contain" />
                      <div className="absolute top-2 right-2 bg-green-500 text-white p-1 rounded-full shadow-lg">
                        <CheckCircle2 size={16} />
                      </div>
                    </div>
                  ) : (
                    <div className="flex-1 flex items-center justify-center border border-dashed border-zinc-800 rounded-lg text-xs text-zinc-600 italic">
                      {(renderStatus === 'rendering') ? (
                        <div className="flex items-center gap-2">
                          <Loader2 size={14} className="animate-spin text-purple-500" />
                          Rendering high-quality version...
                        </div>
                      ) : "Click 'Render Final' to generate the export version."}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Sidebar - Timeline */}
        <div className={`w-72 bg-[#1a1a1a] border-l border-zinc-800 h-full shrink-0 transition-all duration-300 ${engineStatus === 'offline' ? 'pt-8' : ''}`}>
          <TimelineEditor />
        </div>

      </div>
      <BrowserNotice />
    </DndContext>
  );
}
