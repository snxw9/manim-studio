import { create } from 'zustand';

export type RenderStatus = 'idle' | 'generating' | 'rendering' | 'done' | 'error';
export type ActiveTab = 'prompt' | 'code';

interface Store {
  // Editor state
  prompt: string;
  generatedCode: string;
  activeTab: ActiveTab;
  selectedTemplate: string | null;

  // Render state
  renderStatus: RenderStatus;
  errorMessage: string | null;
  videoUrl: string | null;
  videoFilename: string | null;
  lastProvider: string | null;
  lastModel: string | null;
  renderTime: number | null;

  // Engine state
  engineOnline: boolean;

  // Settings
  quality: string;
  format: string;
  userKeys: Record<string, string>;
  theme: 'dark' | 'light';

  // Actions
  setPrompt: (p: string) => void;
  setGeneratedCode: (c: string) => void;
  setActiveTab: (t: ActiveTab) => void;
  setSelectedTemplate: (id: string | null) => void;
  setRenderStatus: (s: RenderStatus) => void;
  setErrorMessage: (m: string | null) => void;
  setVideoUrl: (u: string | null) => void;
  setVideoFilename: (f: string | null) => void;
  setLastProvider: (p: string | null) => void;
  setLastModel: (m: string | null) => void;
  setRenderTime: (t: number | null) => void;
  setEngineOnline: (v: boolean) => void;
  setQuality: (q: string) => void;
  setFormat: (f: string) => void;
  setUserKeys: (k: Record<string, string>) => void;
  setTheme: (t: 'dark' | 'light') => void;
  resetTask: () => void;
}

export const useStore = create<Store>((set) => ({
  prompt: '',
  generatedCode: '',
  activeTab: 'prompt',
  selectedTemplate: null,
  renderStatus: 'idle',
  errorMessage: null,
  videoUrl: null,
  videoFilename: null,
  lastProvider: null,
  lastModel: null,
  renderTime: null,
  engineOnline: false,
  quality: '720p',
  format: 'mp4',
  userKeys: {},
  theme: 'dark',

  setPrompt: (p) => set({ prompt: p }),
  setGeneratedCode: (c) => set({ generatedCode: c }),
  setActiveTab: (t) => set({ activeTab: t }),
  setSelectedTemplate: (id) => set({ selectedTemplate: id }),
  setRenderStatus: (s) => set({ renderStatus: s }),
  setErrorMessage: (m) => set({ errorMessage: m }),
  setVideoUrl: (u) => set({ videoUrl: u }),
  setVideoFilename: (f) => set({ videoFilename: f }),
  setLastProvider: (p) => set({ lastProvider: p }),
  setLastModel: (m) => set({ lastModel: m }),
  setRenderTime: (t) => set({ renderTime: t }),
  setEngineOnline: (v) => set({ engineOnline: v }),
  setQuality: (q) => set({ quality: q }),
  setFormat: (f) => set({ format: f }),
  setUserKeys: (k) => set({ userKeys: k }),
  setTheme: (t) => set({ theme: t }),

  resetTask: () => set({
    renderStatus: 'idle',
    errorMessage: null,
    videoUrl: null,
    videoFilename: null,
    renderTime: null,
    lastProvider: null,
    lastModel: null,
  }),
}));
