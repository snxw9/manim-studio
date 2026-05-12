export type SaveStrategy = 'filesystem-api' | 'input-directory' | 'download';

export function getBestSaveStrategy(): SaveStrategy {
  if (typeof window === 'undefined') return 'download';
  // Test if API exists
  if ('showDirectoryPicker' in window) return 'filesystem-api';
  // webkitdirectory input works in Firefox, Brave, Safari
  const testInput = document.createElement('input');
  if ('webkitdirectory' in testInput) return 'input-directory';
  return 'download';
}

export type BrowserInfo = {
  name: string;
  supportsFilesystemAPI: boolean;
  shieldsMayBlock: boolean;
};

export function detectBrowser(): BrowserInfo {
  if (typeof window === 'undefined') {
    return { name: 'unknown', supportsFilesystemAPI: false, shieldsMayBlock: false };
  }
  
  const ua = navigator.userAgent;
  const isBrave = (navigator as any).brave !== undefined;
  const isChrome = /Chrome/.test(ua) && !isBrave && !/Edg/.test(ua);
  const isEdge = /Edg/.test(ua);
  const isFirefox = /Firefox/.test(ua);
  const isSafari = /Safari/.test(ua) && !/Chrome/.test(ua);
  
  let name = 'unknown';
  if (isBrave) name = 'Brave';
  else if (isEdge) name = 'Edge';
  else if (isChrome) name = 'Chrome';
  else if (isFirefox) name = 'Firefox';
  else if (isSafari) name = 'Safari';

  const supportsFilesystemAPI = 'showDirectoryPicker' in window;

  return {
    name,
    supportsFilesystemAPI,
    shieldsMayBlock: isBrave,
  };
}

// Call this to trigger directory selection — handles all strategies
export async function pickSaveLocation(): Promise<{
  strategy: SaveStrategy;
  dirHandle?: FileSystemDirectoryHandle;
  dirName?: string;
}> {
  const strategy = getBestSaveStrategy();
  
  if (strategy === 'filesystem-api') {
    try {
      const handle = await (window as any).showDirectoryPicker({ 
        mode: 'readwrite',
        startIn: 'videos',
      });
      return { strategy, dirHandle: handle, dirName: handle.name };
    } catch (err: any) {
      if (err.name === 'AbortError') return { strategy: 'download' };
      // Shields blocked it — fall through to input strategy
      console.warn('showDirectoryPicker blocked, falling back:', err);
    }
  }
  
  if (strategy === 'input-directory' || strategy === 'filesystem-api') {
    return new Promise((resolve) => {
      const input = document.createElement('input');
      input.type = 'file';
      (input as any).webkitdirectory = true;
      input.style.display = 'none';
      document.body.appendChild(input);
      
      input.onchange = () => {
        const files = input.files;
        if (files && files.length > 0) {
          const dirName = files[0].webkitRelativePath.split('/')[0];
          document.body.removeChild(input);
          resolve({ strategy: 'download', dirName });
        } else {
          document.body.removeChild(input);
          resolve({ strategy: 'download' });
        }
      };
      
      input.oncancel = () => {
        document.body.removeChild(input);
        resolve({ strategy: 'download' });
      };
      
      input.click();
    });
  }
  
  return { strategy: 'download' };
}

export async function saveVideoFile(
  blob: Blob,
  filename: string,
  dirHandle?: FileSystemDirectoryHandle | null
): Promise<{ success: boolean; message: string }> {
  // Try File System Access API write if we have a handle
  if (dirHandle) {
    try {
      const fileHandle = await dirHandle.getFileHandle(filename, { create: true });
      const writable = await fileHandle.createWritable();
      await writable.write(blob);
      await writable.close();
      return { success: true, message: `Saved ${filename} to ${dirHandle.name}/` };
    } catch (err: any) {
      console.warn('Direct write failed, falling back to download:', err);
    }
  }
  
  // Universal download fallback
  try {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 5000);
    return { 
      success: true, 
      message: `Downloaded ${filename} to your default downloads folder` 
    };
  } catch (err: any) {
    return { success: false, message: `Save failed: ${err.message}` };
  }
}
