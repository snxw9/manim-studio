'use client';

import { useStore } from '@/lib/store';

export default function AISuggestions() {
  const { suggestions, setPrompt } = useStore();

  if (!suggestions || suggestions.length === 0) return null;

  return (
    <div className="flex flex-wrap gap-2 mt-3">
      {suggestions.map((suggestion, i) => (
        <button
          key={i}
          onClick={() => setPrompt(suggestion)}
          className="px-3 py-1.5 text-xs rounded-full bg-zinc-800 text-zinc-300 hover:bg-zinc-700 hover:text-white transition-colors border border-zinc-700 whitespace-nowrap"
        >
          {suggestion}
        </button>
      ))}
    </div>
  );
}
