'use client';

import { useState, useEffect } from 'react';
import { Save, Download, FolderOpen, Upload, X, Trash2 } from 'lucide-react';
import { useStore } from '@/lib/store';
import { db, Project } from '@/lib/db';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';

export default function ProjectManager() {
  const { 
    generatedCode, 
    videoBlob, 
    prompt, 
    selectedTemplate, 
    scenes, 
    loadProject 
  } = useStore();
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [savedProjects, setSavedProjects] = useState<Project[]>([]);

  const loadSavedProjects = async () => {
    const projects = await db.projects.orderBy('updatedAt').reverse().toArray();
    setSavedProjects(projects);
  };

  useEffect(() => {
    if (isModalOpen) {
      const frame = requestAnimationFrame(() => loadSavedProjects());
      return () => cancelAnimationFrame(frame);
    }
  }, [isModalOpen]);

  const handleSave = async () => {
    if (typeof window === 'undefined') return;
    const name = window.prompt("Enter project name:", "New Project") || "Untitled";
    const now = Date.now();
    
    await db.projects.add({
      name,
      prompt,
      code: generatedCode,
      videoBlob: videoBlob || undefined,
      template: selectedTemplate,
      scenes,
      createdAt: now,
      updatedAt: now,
    });
    
    alert("Project saved locally!");
  };

  const handleDelete = async (id: number) => {
    if (confirm("Are you sure you want to delete this project?")) {
      await db.projects.delete(id);
      loadSavedProjects();
    }
  };

  const handleExport = async () => {
    if (!generatedCode) {
      alert("No code generated to export.");
      return;
    }
    
    const zip = new JSZip();
    zip.file("main.py", generatedCode);
    zip.file("prompt.txt", prompt);
    
    if (videoBlob) {
      zip.file("animation.mp4", videoBlob);
    }
    
    const metadata = {
      name: "Exported Project",
      template: selectedTemplate,
      scenes,
      exportedAt: new Date().toISOString()
    };
    zip.file("metadata.json", JSON.stringify(metadata));
    
    const blob = await zip.generateAsync({ type: "blob" });
    saveAs(blob, "project.mstudio");
  };

  const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const zip = await JSZip.loadAsync(file);
      const code = await zip.file("main.py")?.async("string") || "";
      const promptText = await zip.file("prompt.txt")?.async("string") || "";
      const metadataStr = await zip.file("metadata.json")?.async("string") || "{}";
      const metadata = JSON.parse(metadataStr);
      
      let vBlob = null;
      const videoFile = zip.file("animation.mp4");
      if (videoFile) {
        vBlob = await videoFile.async("blob");
      }

      loadProject({
        prompt: promptText,
        code,
        template: metadata.template,
        scenes: metadata.scenes,
        videoBlob: vBlob
      });
      
      alert("Project imported successfully!");
    } catch (err) {
      console.error(err);
      alert("Failed to import project.");
    }
  };

  return (
    <div className="flex gap-2">
      <button 
        onClick={() => setIsModalOpen(true)}
        className="p-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-md transition-colors" 
        title="Load Project"
      >
        <FolderOpen size={18} />
      </button>
      <button 
        onClick={handleSave}
        className="p-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-md transition-colors" 
        title="Save to Browser"
      >
        <Save size={18} />
      </button>
      <button 
        onClick={handleExport}
        className="p-2 text-zinc-400 hover:text-purple-400 hover:bg-purple-400/10 rounded-md transition-colors" 
        title="Export .mstudio"
      >
        <Download size={18} />
      </button>
      <label className="p-2 text-zinc-400 hover:text-green-400 hover:bg-green-400/10 rounded-md transition-colors cursor-pointer" title="Import .mstudio">
        <Upload size={18} />
        <input type="file" accept=".mstudio" onChange={handleImport} className="hidden" />
      </label>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4">
          <div className="bg-[#1a1a1a] border border-zinc-800 rounded-xl w-full max-w-2xl max-h-[80vh] flex flex-col shadow-2xl">
            <div className="p-4 border-b border-zinc-800 flex items-center justify-between">
              <h2 className="text-lg font-bold text-white flex items-center gap-2">
                <FolderOpen className="text-purple-500" size={20} />
                Saved Projects
              </h2>
              <button onClick={() => setIsModalOpen(false)} className="text-zinc-500 hover:text-white">
                <X size={20} />
              </button>
            </div>
            
            <div className="flex-1 overflow-y-auto p-4">
              {savedProjects.length === 0 ? (
                <div className="text-center py-12 text-zinc-500">
                  No saved projects found.
                </div>
              ) : (
                <div className="grid gap-3">
                  {savedProjects.map((p) => (
                    <div 
                      key={p.id}
                      className="group flex items-center justify-between p-4 bg-zinc-900/50 hover:bg-zinc-800 rounded-lg border border-zinc-800 transition-colors"
                    >
                      <button 
                        onClick={() => {
                          loadProject(p);
                          setIsModalOpen(false);
                        }}
                        className="flex-1 text-left"
                      >
                        <div className="font-medium text-zinc-100">{p.name}</div>
                        <div className="text-xs text-zinc-500 flex gap-3 mt-1">
                          <span>Updated {new Date(p.updatedAt).toLocaleDateString()}</span>
                          {p.videoBlob && <span className="text-purple-400">● Has Video</span>}
                        </div>
                      </button>
                      <button 
                        onClick={() => p.id && handleDelete(p.id)}
                        className="p-2 text-zinc-600 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
