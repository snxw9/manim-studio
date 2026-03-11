'use client';

import { useDraggable } from '@dnd-kit/core';

const assets = [
  { id: 'math_pi', type: 'symbol', label: 'π', code: 'MathTex(r"\\pi")' },
  { id: 'math_sigma', type: 'symbol', label: 'Σ', code: 'MathTex(r"\\sum")' },
  { id: 'shape_circle', type: 'shape', label: '◯', code: 'Circle()' },
  { id: 'shape_square', type: 'shape', label: '□', code: 'Square()' },
  { id: 'arrow_right', type: 'arrow', label: '→', code: 'Arrow(LEFT, RIGHT)' },
  { id: 'grid_axes', type: 'grid', label: '▦', code: 'Axes()' },
];

function DraggableAsset({ asset }: { asset: any }) {
  const { attributes, listeners, setNodeRef, transform } = useDraggable({
    id: asset.id,
    data: asset,
  });

  const style = transform ? {
    transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
  } : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className="flex flex-col items-center justify-center p-3 bg-zinc-900 border border-zinc-800 rounded-lg cursor-grab hover:border-zinc-600 transition-colors"
    >
      <span className="text-2xl text-zinc-300">{asset.label}</span>
    </div>
  );
}

export default function AssetLibrary() {
  return (
    <div className="p-4 mt-4 border-t border-zinc-800">
      <h3 className="text-sm font-semibold text-zinc-400 uppercase tracking-wider mb-3">Assets</h3>
      <div className="grid grid-cols-3 gap-2">
        {assets.map((asset) => (
          <DraggableAsset key={asset.id} asset={asset} />
        ))}
      </div>
    </div>
  );
}
