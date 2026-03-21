'use client';
import { useState } from 'react';
import { ASSETS_BY_CATEGORY, Asset } from '@/lib/assets';
import { useStore } from '@/lib/store';
import { ChevronDown, ChevronRight } from 'lucide-react';

function AssetItem({ asset }: { asset: Asset }) {
  const { generatedCode, setGeneratedCode, prompt, setPrompt } = useStore();

  const handleClick = () => {
    if (generatedCode) {
      setGeneratedCode(generatedCode + '\n\n        # ' + asset.description + '\n        ' + asset.snippet);
    } else {
      setPrompt((prompt ? prompt + ', ' : '') + 'include a ' + asset.label.toLowerCase());
    }
  };

  return (
    <button
      onClick={handleClick}
      title={asset.description}
      className="
        flex items-center gap-2 w-full px-4 py-1.5 text-[13px]
        text-[var(--text-secondary)] hover:text-[var(--text-primary)]
        text-left
      "
    >
      <span className="text-[13px] w-4 text-center opacity-70">{asset.icon}</span>
      <span>{asset.label}</span>
    </button>
  );
}

export default function AssetLibrary() {
  const [isOpen, setIsOpen] = useState(true);

  return (
    <div className="flex flex-col border-t border-[var(--bg-border)] mt-2">
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between w-full px-3 py-2 text-[11px] uppercase tracking-widest text-[var(--text-dim)] hover:text-[var(--text-secondary)]"
      >
        <span>Assets</span>
        {isOpen ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
      </button>

      {isOpen && (
        <div className="flex flex-col pb-4">
          {Object.entries(ASSETS_BY_CATEGORY).map(([category, assets]) => (
            <div key={category} className="mt-2">
              <p className="text-[10px] uppercase tracking-wider text-[var(--text-dim)] px-4 mb-1">
                {category}
              </p>
              {assets.map(asset => (
                <AssetItem key={asset.id} asset={asset} />
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
