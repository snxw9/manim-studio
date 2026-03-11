'use client';

import { useStore } from '@/lib/store';
import Editor, { OnMount } from '@monaco-editor/react';
import { useDebounce } from 'use-debounce';
import { useEffect, useRef } from 'react';

export default function MonacoEditor() {
  const { 
    generatedCode, 
    setGeneratedCode, 
    setPreviewStatus, 
    setPreviewUrl, 
    setPreviewError,
    previewErrors,
    setPreviewErrors
  } = useStore();
  const [debouncedCode] = useDebounce(generatedCode, 2000);
  const currentUrlRef = useRef<string | null>(null);
  const editorRef = useRef<any>(null);
  const monacoRef = useRef<any>(null);

  const handleEditorDidMount: OnMount = (editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;
  };

  useEffect(() => {
    if (monacoRef.current && editorRef.current) {
      const markers = previewErrors.map(err => {
        const lineMatch = err.match(/line (\d+)/);
        const lineNumber = lineMatch ? parseInt(lineMatch[1]) : 1;
        return {
          severity: monacoRef.current.MarkerSeverity.Error,
          startLineNumber: lineNumber,
          endLineNumber: lineNumber,
          startColumn: 1,
          endColumn: 100,
          message: err,
        };
      });
      monacoRef.current.editor.setModelMarkers(editorRef.current.getModel(), 'manim-validator', markers);
    }
  }, [previewErrors]);

  useEffect(() => {
    if (!debouncedCode) return;

    const fetchPreview = async () => {
      setPreviewStatus('rendering');
      setPreviewError(null);
      setPreviewErrors([]);
      try {
        const response = await fetch('/api/preview', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ code: debouncedCode })
        });
        
        const data = await response.json();

        if (!response.ok || data.error) {
          if (response.status === 422) {
            setPreviewErrors(data.details || []);
            throw new Error(data.error || 'Validation failed');
          }
          let msg = data.error || 'Preview failed';
          if (data.fix) msg += `\n\nFix: ${data.fix}`;
          throw new Error(msg);
        }
        
        // Render from base64
        const blob = await fetch(`data:video/mp4;base64,${data.video}`).then(r => r.blob());
        const url = URL.createObjectURL(blob);
        
        if (currentUrlRef.current) {
          URL.revokeObjectURL(currentUrlRef.current);
        }
        
        currentUrlRef.current = url;
        setPreviewUrl(url);
        setPreviewStatus('idle');
      } catch (error: any) {
        setPreviewError(error.message);
        setPreviewStatus('error');
      }
    };

    fetchPreview();
  }, [debouncedCode, setPreviewStatus, setPreviewUrl, setPreviewError, setPreviewErrors]);

  return (
    <div className="w-full h-full border border-zinc-800 rounded-md overflow-hidden">
      <Editor
        height="100%"
        defaultLanguage="python"
        theme="vs-dark"
        value={generatedCode}
        onChange={(value) => setGeneratedCode(value || '')}
        onMount={handleEditorDidMount}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          padding: { top: 16 },
        }}
      />
    </div>
  );
}
