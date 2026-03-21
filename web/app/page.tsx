'use client';

import { useEffect, useCallback } from 'react';
import { useStore } from '@/lib/store';
import Topbar from '@/components/ui/Topbar';
import Sidebar from '@/components/ui/Sidebar';
import MainArea from '@/components/ui/MainArea';
import RightPanel from '@/components/ui/RightPanel';
import StatusBar from '@/components/ui/StatusBar';
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts';

export default function Home() {
  const { 
    setEngineStatus, 
    engineUrl,
    activeTab,
    setActiveTab,
  } = useStore();

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
  }, []);

  const handleGenerateCommand = () => {
    if (activeTab !== 'prompt') setActiveTab('prompt');
    // Using a more reliable way to find the button
    setTimeout(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      const genBtn = buttons.find(b => b.textContent === 'Generate');
      genBtn?.click();
    }, 0);
  };

  const handleRenderCommand = () => {
    if (activeTab !== 'code') setActiveTab('code');
    setTimeout(() => {
      const buttons = Array.from(document.querySelectorAll('button'));
      const renderBtn = buttons.find(b => b.textContent === 'Render');
      renderBtn?.click();
    }, 0);
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
    <div className="flex flex-col h-screen bg-[var(--bg)] text-[var(--text-1)] font-sans overflow-hidden">
      <Topbar />
      <div className="flex-1 flex overflow-hidden">
        <Sidebar />
        <MainArea />
        <RightPanel />
      </div>
      <StatusBar />
    </div>
  );
}
