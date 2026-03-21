'use client';
import { useState } from 'react';
import { ChevronLeft, ChevronRight, ChevronDown, ChevronUp } from 'lucide-react';
import { useStore } from '@/lib/store';
import { TEMPLATE_LIST, TEMPLATES } from '@/lib/templates';
import { ASSETS_BY_CATEGORY } from '@/lib/assets';

function TemplatesSection({ open, onToggle }: { open: boolean, onToggle: () => void }) {
  const { selectedTemplate, setSelectedTemplate, setGeneratedCode, setActiveTab } = useStore();

  const handleSelect = (templateId: string) => {
    const template = TEMPLATES[templateId];
    if (!template) return;
    setSelectedTemplate(templateId);
    setGeneratedCode(template.code);
    setActiveTab('code');
  };

  return (
    <div className="flex flex-col">
      <button 
        onClick={onToggle}
        className="flex items-center justify-between w-full px-3 py-2 text-[11px] uppercase tracking-widest text-[var(--text-3)] hover:text-[var(--text-2)]"
      >
        <span>Templates</span>
        {open ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
      </button>

      {open && (
        <div className="flex flex-col">
          {TEMPLATE_LIST.map((template) => (
            <button
              key={template.id}
              onClick={() => handleSelect(template.id)}
              className={`
                w-full text-left px-4 py-1.5 text-[13px] hover:bg-[var(--bg-3)]
                ${selectedTemplate === template.id
                  ? 'text-[var(--text-1)] shadow-[inset_2px_0_0_0_var(--accent)]'
                  : 'text-[var(--text-2)]'
                }
              `}
            >
              {template.name}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

function AssetsSection({ open, onToggle }: { open: boolean, onToggle: () => void }) {
  const { generatedCode, setGeneratedCode, prompt, setPrompt } = useStore();

  const handleAssetClick = (asset: any) => {
    if (generatedCode) {
      setGeneratedCode(generatedCode + '\n\n        # ' + asset.description + '\n        ' + asset.snippet);
    } else {
      setPrompt((p) => (p ? p + ', ' : '') + 'include a ' + asset.label.toLowerCase());
    }
  };

  return (
    <div className="flex flex-col">
      <button 
        onClick={onToggle}
        className="flex items-center justify-between w-full px-3 py-2 text-[11px] uppercase tracking-widest text-[var(--text-3)] hover:text-[var(--text-2)]"
      >
        <span>Assets</span>
        {open ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
      </button>

      {open && (
        <div className="flex flex-col pb-4">
          {Object.entries(ASSETS_BY_CATEGORY).map(([category, assets]) => (
            <div key={category} className="mt-2">
              <p className="text-[10px] uppercase tracking-wider text-[var(--text-3)] px-4 py-1">
                {category}
              </p>
              {assets.map(asset => (
                <button
                  key={asset.id}
                  onClick={() => handleAssetClick(asset)}
                  className="px-4 py-1 text-[13px] text-[var(--text-2)] cursor-pointer hover:bg-[var(--bg-3)] w-full text-left"
                >
                  <span className="w-5 inline-block opacity-70">{asset.icon}</span>
                  <span>{asset.label}</span>
                </button>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const [templatesOpen, setTemplatesOpen] = useState(true);
  const [assetsOpen, setAssetsOpen] = useState(true);

  if (collapsed) {
    return (
      <div className="w-[28px] bg-[var(--bg-2)] border-r border-[var(--border)] flex flex-col items-center pt-2 shrink-0">
        <button
          onClick={() => setCollapsed(false)}
          className="text-[var(--text-3)] cursor-pointer bg-none border-none p-1"
        >
          <ChevronRight size={14} />
        </button>
      </div>
    );
  }

  return (
    <div className="w-[200px] bg-[var(--bg-2)] border-r border-[var(--border)] flex flex-col shrink-0 overflow-hidden">
      <div className="flex justify-end p-2 pb-0">
        <button
          onClick={() => setCollapsed(true)}
          className="text-[var(--text-3)] cursor-pointer bg-none border-none p-1"
        >
          <ChevronLeft size={14} />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto py-1">
        <TemplatesSection open={templatesOpen} onToggle={() => setTemplatesOpen(o => !o)} />
        <div className="h-[1px] bg-[var(--border)] my-2 mx-0" />
        <AssetsSection open={assetsOpen} onToggle={() => setAssetsOpen(o => !o)} />
      </div>
    </div>
  );
}
