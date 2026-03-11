'use client';
import { useState, useEffect } from 'react';
import { detectBrowser, saveVideoFile, pickSaveLocation, BrowserInfo } from '@/lib/fileSaver';

interface ExportPanelProps {
  videoBlob: Blob | null;
  filename: string;
}

export default function ExportPanel({ videoBlob, filename }: ExportPanelProps) {
  const [dirHandle, setDirHandle] = useState<FileSystemDirectoryHandle | null>(null);
  const [saveDirName, setSaveDirName] = useState<string>('');
  const [browserInfo, setBrowserInfo] = useState<BrowserInfo | null>(null);
  const [status, setStatus] = useState<{ type: 'success' | 'error' | 'info'; message: string } | null>(null);

  useEffect(() => {
    const frame = requestAnimationFrame(() => {
      setBrowserInfo(detectBrowser());
    });
    const lastName = localStorage.getItem('last_save_dir');
    if (lastName) setSaveDirName(lastName);
    return () => cancelAnimationFrame(frame);
  }, []);

  const handlePickDirectory = async () => {
    setStatus(null);
    try {
      const result = await pickSaveLocation();
      
      if (result.dirName) {
        setSaveDirName(result.dirName);
        localStorage.setItem('last_save_dir', result.dirName);
        
        if (result.dirHandle) {
          setDirHandle(result.dirHandle);
          setStatus({ type: 'info', message: `Direct saving enabled to: ${result.dirName}/` });
        } else {
          setDirHandle(null);
          setStatus({ type: 'info', message: `Folder selected: ${result.dirName}/ (Manual move required)` });
        }
      }
    } catch (err: any) {
      setStatus({ type: 'error', message: `Folder picker failed: ${err.message}` });
    }
  };

  const handleSave = async () => {
    if (!videoBlob) {
      setStatus({ type: 'error', message: 'No video yet — render first.' });
      return;
    }
    setStatus({ type: 'info', message: 'Saving...' });
    const result = await saveVideoFile(videoBlob, filename, dirHandle);
    setStatus({ type: result.success ? 'success' : 'error', message: result.message });
  };

  if (!browserInfo) return null;

  return (
    <div className="flex flex-col gap-3 p-4 bg-[#1a1a1a] rounded-xl border border-white/10">
      <h3 className="text-sm font-semibold text-white/80 uppercase tracking-wider">Export Video</h3>

      <div className="flex items-center gap-2">
        <button
          onClick={handlePickDirectory}
          className="px-3 py-1.5 text-xs bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors flex items-center gap-2"
        >
          📁 Choose Folder
        </button>
        <span className="text-xs text-white/50 truncate max-w-[180px]">
          {saveDirName ? `→ ${saveDirName}/` : 'No folder selected'}
        </span>
      </div>

      {!dirHandle && saveDirName && (
        <p className="text-[10px] text-orange-400/80 italic">
          Note: Direct writing not supported. File will download — please move it to {saveDirName}/ manually.
        </p>
      )}

      {browserInfo.name === 'Firefox' && !saveDirName && (
        <p className="text-[10px] text-white/40 italic">
          Firefox downloads to your default folder. Pick a folder to show its name here.
        </p>
      )}

      <button
        onClick={handleSave}
        disabled={!videoBlob}
        className="px-4 py-2 bg-purple-600 hover:bg-purple-500 disabled:bg-white/10 disabled:text-white/30 text-white text-sm rounded-lg transition-all font-medium shadow-lg"
      >
        {dirHandle ? `💾 Save to ${saveDirName}/` : '⬇️ Download Video'}
      </button>

      {status && (
        <p className={`text-xs rounded px-2 py-1 flex items-center gap-2 ${
          status.type === 'success' ? 'bg-green-900/40 text-green-400' :
          status.type === 'error'   ? 'bg-red-900/40 text-red-400' :
                                      'bg-white/5 text-white/50'
        }`}>
          {status.type === 'success' ? '✅' : status.type === 'error' ? '❌' : 'ℹ️'} {status.message}
        </p>
      )}
    </div>
  );
}
