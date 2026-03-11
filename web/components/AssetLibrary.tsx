'use client';
import { useState, useEffect } from 'react';
import { useDraggable } from '@dnd-kit/core';

interface Asset {
  id: string;
  type: string;
  label: string;
  code: string;
}

const assets: Asset[] = [
  { id: 'math_pi', type: 'symbol', label: 'π', code: 'MathTex(r"\\pi")' },
  { id: 'math_sigma', type: 'symbol', label: 'Σ', code: 'MathTex(r"\\sum")' },
  { id: 'shape_circle', type: 'shape', label: '◯', code: 'Circle()' },
  { id: 'shape_square', type: 'shape', label: '□', code: 'Square()' },
  { id: 'arrow_right', type: 'arrow', label: '→', code: 'Arrow(LEFT, RIGHT)' },
  { id: 'grid_axes', type: 'grid', label: '▦', code: 'Axes()' },
];

function DraggableAsset({ asset }: { asset: Asset }) {
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: asset.id,
    data: asset,
  });

  const style = transform ? {
    transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
    zIndex: isDragging ? 999 : undefined,
    opacity: isDragging ? 0.8 : 1,
  } : undefined;

  // Render a non-interactive placeholder during SSR/before mount
  if (!mounted) {
    return (
      <div className="flex flex-col items-center justify-center p-3 bg-zinc-900 border border-zinc-800 rounded-lg cursor-grab shadow-inner opacity-50">
        <span className="text-2xl text-zinc-300">{asset.label}</span>
      </div>
    );
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className="flex flex-col items-center justify-center p-3 bg-zinc-900 border border-zinc-800 rounded-lg cursor-grab hover:border-purple-500 hover:bg-zinc-800 transition shadow-lg active:scale-95 group"
    >
      <span className="text-2xl text-zinc-300 group-hover:text-purple-400 transition-colors">{asset.label}</span>
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
