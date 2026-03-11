'use client';

import { useState, useRef, useEffect } from 'react';
import { useStore, VideoFormat, VideoQuality } from '@/lib/store';
import { Folder, Download, CheckCircle, AlertCircle } from 'lucide-react';
import { saveAs } from 'file-saver';

export default function ExportPanel() {
  const { videoFormat, setVideoFormat, videoQuality, setVideoQuality, videoBlob, generatedCode } = useStore();
  const [saveDirPath, setSaveDirPath] = useState<string>('');
  const dirHandleRef = useRef<FileSystemDirectoryHandle | null>(null);

  useEffect(() => {
    const lastDir = localStorage.getItem('last_save_dir');
    if (lastDir) {
      setSaveDirPath(lastDir);
    }
  }, []);

  const pickSaveDirectory = async () => {
    try {
      const handle = await (window as any).showDirectoryPicker({ mode: 'readwrite' });
      dirHandleRef.current = handle;
      setSaveDirPath(handle.name);
      localStorage.setItem('last_save_dir', handle.name);
    } catch (err) {
      console.error("Directory picker error:", err);
    }
  };

  const handleSaveVideo = async () => {
    if (!videoBlob) return;

    // Extract scene name
    const match = generatedCode.match(/class\s+(\w+)\s*\(/);
    const sceneName = match ? match[1] : "Animation";
    const filename = `${sceneName}_{videoQuality}.${videoFormat}`;

    if (dirHandleRef.current) {
      try {
        const fileHandle = await dirHandleRef.current.getFileHandle(filename, { create: true });
        const writable = await fileHandle.createWritable();
        await writable.write(videoBlob);
        await writable.close();
        alert(`Saved ${filename} to ${dirHandleRef.current.name}`);
      } catch (err) {
        console.error("File System Access API error, falling back:", err);
        saveAs(videoBlob, filename);
      }
    } else {
      saveAs(videoBlob, filename);
    }
  };

  return (
    <div className="flex flex-col gap-4 p-4 bg-zinc-900/50 rounded-lg border border-zinc-800">
      <div className="flex flex-col gap-2">
        <label className="text-xs font-semibold text-zinc-500 uppercase">Quality & Format</label>
        <div className="flex gap-2">
          <select 
            value={videoQuality}
            onChange={(e) => setVideoQuality(e.target.value as VideoQuality)}
            className="flex-1 bg-zinc-800 border border-zinc-700 rounded px-2 py-1.5 text-sm outline-none focus:border-purple-500"
          >
            <option value="480p">480p (Fast)</option>
            <option value="720p">720p (Medium)</option>
            <option value="1080p">1080p (High)</option>
            <option value="2160p">4K (Ultra)</option>
          </select>
          <select 
            value={videoFormat}
            onChange={(e) => setVideoFormat(e.target.value as VideoFormat)}
            className="flex-1 bg-zinc-800 border border-zinc-700 rounded px-2 py-1.5 text-sm outline-none focus:border-purple-500"
          >
            <option value="mp4">MP4</option>
            <option value="gif">GIF</option>
            <option value="webm">WebM</option>
            <option value="mov">MOV</option>
          </select>
        </div>
        {videoFormat === 'gif' && (
          <p className="text-[10px] text-zinc-500 italic">GIF files are larger. Best for short loops.</p>
        )}
      </div>

      <div className="flex flex-col gap-2 pt-2 border-t border-zinc-800">
        <div className="flex items-center justify-between">
           <label className="text-xs font-semibold text-zinc-500 uppercase">Export</label>
           {saveDirPath && (
             <span className="text-[10px] text-zinc-500 flex items-center gap-1">
               <CheckCircle size={10} className="text-green-500" />
               Saving to: {saveDirPath}
             </span>
           )}
        </div>
        
        <div className="flex gap-2">
          <button 
            onClick={pickSaveDirectory}
            className="flex-1 flex items-center justify-center gap-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 py-2 rounded text-sm transition-colors"
            title="Choose a local folder for automatic saving"
          >
            <Folder size={16} />
            Folder
          </button>
          <button 
            onClick={handleSaveVideo}
            disabled={!videoBlob}
            className="flex-[2] flex items-center justify-center gap-2 bg-purple-600 hover:bg-purple-700 disabled:opacity-50 text-white py-2 rounded text-sm font-medium transition-colors shadow-lg"
          >
            <Download size={16} />
            Save Video
          </button>
        </div>
        {!dirHandleRef.current && saveDirPath && (
          <p className="text-[10px] text-zinc-600 text-center">Re-pick folder to enable direct saving.</p>
        )}
      </div>
    </div>
  );
}
