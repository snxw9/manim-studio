'use client';

import { useEffect, useCallback } from 'react';
import { useStore } from '@/lib/store';
import { DndContext, DragEndEvent } from '@dnd-kit/core';
import { Topbar } from '@/components/ui/Topbar';
import { LeftPanel } from '@/components/ui/LeftPanel';
import { MainCanvas } from '@/components/ui/MainCanvas';
import { RightPanel } from '@/components/ui/RightPanel';
import { BottomBar } from '@/components/ui/BottomBar';
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts';

export default function Home() {
  const { 
    setEngineStatus, 
    prompt, 
    setPrompt, 
    engineUrl,
    activeTab,
    setActiveTab,
    renderStatus,
    generatedCode
  } = useStore();

  const checkEngine = useCallback(async () => {
    try {
      const res = await fetch(`${engineUrl}/health`, { signal: AbortSignal.timeout(3000) });
      if (res.ok) setEngineStatus('online');
      else setEngineStatus('offline');
    } catch {
      setEngineStatus('offline');
    }
  }, [engineUrl, setEngineStatus]);

  useEffect(() => {
    checkEngine();
    const interval = setInterval(checkEngine, 5000);
    return () => clearInterval(interval);
  }, [checkEngine]);

  useEffect(() => {
    const theme = localStorage.getItem('theme') || (window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');
    useStore.getState().setTheme(theme as any);
    document.documentElement.setAttribute('data-theme', theme);
  }, []);

  const handleDragEnd = (event: DragEndEvent) => {
    const { over, active } = event;
    if (over && over.id === 'canvas-droppable') {
      const assetData = active.data.current;
      if (assetData && typeof assetData === 'object' && 'label' in assetData && 'type' in assetData) {
        setPrompt(prompt + (prompt ? ' ' : '') + `Include a ${assetData.type}: ${assetData.label}.`);
      }
    }
  };

  const handleGenerateCommand = () => {
    // We would trigger generate here, but the actual logic is inside MainCanvas.
    // For a cleaner architecture, generate logic could be moved up or to the store.
    // For now, we'll just focus the prompt if it's not active.
    if (activeTab !== 'prompt') setActiveTab('prompt');
    const btn = document.querySelector('button:has(.animate-shimmer)') as HTMLButtonElement | null;
    if (!btn) {
       const genBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent?.includes('Generate'));
       genBtn?.click();
    }
  };

  const handleRenderCommand = () => {
    if (activeTab !== 'code') setActiveTab('code');
    const renderBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent?.includes('Render') && !b.disabled);
    renderBtn?.click();
  };

  const handleToggleTab = () => {
    setActiveTab(activeTab === 'prompt' ? 'code' : 'prompt');
  };

  const handleFocusPrompt = () => {
    setActiveTab('prompt');
    setTimeout(() => {
      document.querySelector('textarea')?.focus();
    }, 50);
  };

  useKeyboardShortcuts({
    onGenerate: handleGenerateCommand,
    onRender: handleRenderCommand,
    onToggleTab: handleToggleTab,
    onFocusPrompt: handleFocusPrompt,
  });

  return (
    <DndContext onDragEnd={handleDragEnd}>
      <div className="flex flex-col h-screen bg-[var(--bg-base)] text-[var(--text-primary)] font-sans overflow-hidden">
        
        <Topbar />

        <div className="flex-1 flex overflow-hidden">
          <LeftPanel />
          <MainCanvas />
          <RightPanel />
        </div>

        <BottomBar />

      </div>
    </DndContext>
  );
}
