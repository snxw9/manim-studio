'use client';

import { useStore } from '@/lib/store';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripVertical, Clock } from 'lucide-react';
import ClientOnly from '@/components/ClientOnly';

function SortableItem({ id, scene }: { id: string, scene: any }) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div ref={setNodeRef} style={style} className="flex items-center gap-2 p-2 bg-zinc-800 rounded border border-zinc-700 mb-2">
      <div {...attributes} {...listeners} className="cursor-grab text-zinc-500 hover:text-zinc-300">
        <GripVertical size={16} />
      </div>
      <div className="flex-1 text-sm text-zinc-300 truncate">{scene.name}</div>
      <div className="flex items-center gap-1 text-xs text-zinc-400 bg-zinc-900 px-2 py-1 rounded">
        <Clock size={12} />
        <span>{scene.duration}s</span>
      </div>
    </div>
  );
}

export default function TimelineEditor() {
  const { scenes, setScenes } = useStore();

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const handleDragEnd = (event: any) => {
    const { active, over } = event;

    if (active.id !== over.id) {
      const oldIndex = scenes.findIndex((s) => s.id === active.id);
      const newIndex = scenes.findIndex((s) => s.id === over.id);
      
      setScenes(arrayMove(scenes, oldIndex, newIndex));
    }
  };

  if (scenes.length === 0) {
    return (
      <div className="p-4 text-center text-sm text-zinc-500 border border-dashed border-zinc-800 rounded-lg m-4">
        No scenes yet. Generate code to populate the timeline.
      </div>
    );
  }

  return (
    <div className="p-4 h-full flex flex-col">
      <h3 className="text-sm font-semibold text-zinc-400 uppercase tracking-wider mb-4">Timeline</h3>
      <ClientOnly>
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
          <SortableContext items={scenes.map(s => s.id)} strategy={verticalListSortingStrategy}>
            <div className="flex-1 overflow-y-auto pr-2">
              {scenes.map((scene) => (
                <SortableItem key={scene.id} id={scene.id} scene={scene} />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      </ClientOnly>
    </div>
  );
}
