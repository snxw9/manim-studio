'use client';

import { useDroppable } from '@dnd-kit/core';
import { useStore } from '@/lib/store';

export default function DragDropBuilder() {
  const { prompt, setPrompt } = useStore();
  const { isOver, setNodeRef } = useDroppable({
    id: 'canvas-droppable',
  });

  const style = {
    backgroundColor: isOver ? 'rgba(124, 58, 237, 0.1)' : 'transparent',
    borderColor: isOver ? 'rgb(124, 58, 237)' : 'rgb(39, 39, 42)',
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="w-full h-full border-2 border-dashed rounded-lg flex items-center justify-center transition-colors relative"
    >
      {isOver ? (
        <span className="text-purple-400 font-medium">Drop asset here</span>
      ) : (
        <div className="text-center">
          <p className="text-zinc-500 mb-2">Drag assets here to add to prompt</p>
          <p className="text-xs text-zinc-600">or type your description above</p>
        </div>
      )}
    </div>
  );
}
