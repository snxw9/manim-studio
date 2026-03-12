'use client';
import { ASSETS_BY_CATEGORY, Asset } from '@/lib/assets';
import { useStore } from '@/lib/store';

function AssetItem({ asset }: { asset: Asset }) {
  const { generatedCode, setGeneratedCode, prompt, setPrompt } = useStore();

  const handleClick = () => {
    // Append snippet to generated code if in code view,
    // otherwise append description to prompt
    if (generatedCode) {
      setGeneratedCode(generatedCode + '\n\n        # ' + asset.description + '\n        ' + asset.snippet);
    } else {
      setPrompt((prompt ? prompt + ', ' : '') + 'include a ' + asset.label.toLowerCase());
    }
  };

  const handleDragStart = (e: React.DragEvent) => {
    e.dataTransfer.setData('text/plain', asset.snippet);
    e.dataTransfer.setData('application/asset-id', asset.id);
  };

  return (
    <button
      draggable
      onDragStart={handleDragStart}
      onClick={handleClick}
      title={asset.description}
      className="
        flex items-center gap-2 w-full px-2 py-1.5 rounded text-[13px]
        text-[var(--text-secondary)] hover:text-[var(--text-primary)]
        hover:bg-[var(--bg-elevated)] transition-all cursor-grab active:cursor-grabbing
        text-left
      "
    >
      <span className="text-[15px] w-5 text-center opacity-70">{asset.icon}</span>
      <span>{asset.label}</span>
    </button>
  );
}

export default function AssetLibrary() {
  return (
    <div>
      <p className="text-[11px] uppercase tracking-widest text-[var(--text-dim)] mb-2 px-1 mt-4">
        Assets
      </p>
      {Object.entries(ASSETS_BY_CATEGORY).map(([category, assets]) => (
        <div key={category} className="mb-3">
          <p className="text-[10px] uppercase tracking-wider text-[var(--text-dim)] opacity-60 px-2 mb-1">
            {category}
          </p>
          {assets.map(asset => (
            <AssetItem key={asset.id} asset={asset} />
          ))}
        </div>
      ))}
    </div>
  );
}
