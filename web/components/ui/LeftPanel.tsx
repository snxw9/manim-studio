import { useStore } from '@/lib/store';
import { useDraggable } from '@dnd-kit/core';

const TEMPLATES = ['Blank Scene', 'Coordinate System', '3D Scene', 'Data Visualization'];
const ASSETS = [
  { id: 'pi', label: 'Pi (π)', type: 'symbol' },
  { id: 'sigma', label: 'Sigma (Σ)', type: 'symbol' },
  { id: 'arrow', label: 'Arrow', type: 'shape' },
  { id: 'grid', label: 'Grid', type: 'layout' },
  { id: 'circle', label: 'Circle', type: 'shape' },
  { id: 'square', label: 'Square', type: 'shape' },
  { id: 'matrix', label: 'Matrix', type: 'math' },
];

function DraggableAsset({ asset }: { asset: { id: string, label: string, type: string } }) {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `asset-${asset.id}`,
    data: asset,
  });

  return (
    <div
      ref={setNodeRef}
      {...listeners}
      {...attributes}
      className={`text-[13px] text-[var(--text-secondary)] py-1.5 px-2 cursor-grab active:cursor-grabbing hover:bg-[var(--bg-elevated)] transition-colors border-l-2 border-transparent hover:border-[var(--text-dim)] ${isDragging ? 'opacity-50' : ''}`}
    >
      {asset.label}
    </div>
  );
}

export function LeftPanel() {
  const { selectedTemplate, setSelectedTemplate } = useStore();

  return (
    <div className="w-[200px] h-full bg-[var(--bg-base)] py-4 px-3 flex flex-col shrink-0 overflow-y-auto animate-fade-in-left">
      
      {/* Templates */}
      <div className="flex flex-col gap-1 mb-6">
        <span className="text-[11px] uppercase tracking-widest text-[var(--text-dim)] mb-2 px-2">Templates</span>
        {TEMPLATES.map(t => {
          const isActive = selectedTemplate === t;
          return (
            <button
              key={t}
              onClick={() => setSelectedTemplate(isActive ? null : t)}
              className={`text-left text-[13px] py-1.5 px-2 transition-colors relative ${isActive ? 'text-[var(--text-primary)]' : 'text-[var(--text-secondary)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]'}`}
            >
              {isActive && (
                <span className="absolute left-0 top-0 bottom-0 w-[3px] bg-[var(--accent)] rounded-r origin-top animate-fade-in" style={{ animation: 'fade-in 0.2s ease-out' }} />
              )}
              {t}
            </button>
          );
        })}
      </div>

      <div className="h-[1px] bg-[var(--bg-border)] mb-4 mx-2" />

      {/* Assets */}
      <div className="flex flex-col gap-1">
        <span className="text-[11px] uppercase tracking-widest text-[var(--text-dim)] mb-2 px-2">Assets</span>
        {ASSETS.map(asset => (
          <DraggableAsset key={asset.id} asset={asset} />
        ))}
      </div>

    </div>
  );
}
