'use client';

import { useEffect, useCallback, useState } from 'react';
import { useStore } from '@/lib/store';
import { Topbar } from '@/components/ui/Topbar';
import { LeftPanel } from '@/components/ui/LeftPanel';
import { MainCanvas } from '@/components/ui/MainCanvas';
import { RightPanel } from '@/components/ui/RightPanel';
import { BottomBar } from '@/components/ui/BottomBar';
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts';

export default function Home() {
  const { 
    setEngineStatus, 
    engineUrl,
    activeTab,
    setActiveTab,
  } = useStore();

  const [isSidebarOpen, setSidebarOpen] = useState(true);

  const checkEngine = useCallback(async () => {
    try {
      const res = await fetch(`${engineUrl}/health`, { cache: 'no-store' });
      const data = await res.json();
      setEngineStatus(data.status === 'ok' ? 'online' : 'offline');
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
    const theme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', theme);
    const sidebarState = localStorage.getItem('sidebarOpen');
    if (sidebarState !== null) {
      setSidebarOpen(sidebarState === 'true');
    }
  }, []);

  const toggleSidebar = () => {
    const newState = !isSidebarOpen;
    setSidebarOpen(newState);
    localStorage.setItem('sidebarOpen', String(newState));
  };

  const handleGenerateCommand = () => {
    if (activeTab !== 'prompt') setActiveTab('prompt');
    // Actual generate logic is in the Generate button in MainCanvas
    const genBtn = document.querySelector('[data-action="generate"]') as HTMLButtonElement | null;
    genBtn?.click();
  };

  const handleRenderCommand = () => {
    if (activeTab !== 'code') setActiveTab('code');
    const renderBtn = document.querySelector('[data-action="render"]') as HTMLButtonElement | null;
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
    <div className="flex flex-col h-screen bg-[var(--bg-base)] text-[var(--text-primary)] font-sans overflow-hidden">
      
      <Topbar />

      <div className="flex-1 flex overflow-hidden">
        {/* Left Sidebar */}
        <div 
          className={`flex-shrink-0 border-r border-[var(--bg-border)] transition-all duration-0`}
          style={{ width: isSidebarOpen ? '200px' : '0px', display: isSidebarOpen ? 'block' : 'none' }}
        >
          <LeftPanel />
        </div>

        {/* Sidebar Toggle Bar */}
        <div 
          className="w-1 flex-shrink-0 cursor-pointer hover:bg-[var(--accent)] transition-colors border-r border-[var(--bg-border)]"
          onClick={toggleSidebar}
          title={isSidebarOpen ? "Collapse Sidebar" : "Expand Sidebar"}
        />

        {/* Main Editor */}
        <div className="flex-1 flex flex-col min-w-0">
          <MainCanvas />
        </div>

        {/* Right Panel */}
        <div className="w-[280px] flex-shrink-0 border-l border-[var(--bg-border)] bg-[var(--bg-surface)]">
          <RightPanel />
        </div>
      </div>

      <BottomBar />

    </div>
  );
}
