'use client';

import { useStore } from '@/lib/store';
import { Calculator, LineChart, Shapes, Combine } from 'lucide-react';

const templates = [
  { id: 'calculus', name: 'Calculus', icon: Calculator, desc: 'Derivatives, integrals, and limits' },
  { id: 'graph_viz', name: 'Graph Viz', icon: LineChart, desc: 'Function graphs and parametric curves' },
  { id: 'geometry', name: 'Geometry Proofs', icon: Shapes, desc: 'Angles, polygons, and theorems' },
  { id: 'matrix', name: 'Matrix Transforms', icon: Combine, desc: 'Linear transformations and eigenvectors' },
];

export default function TemplateLibrary() {
  const { selectedTemplate, setSelectedTemplate, setPrompt } = useStore();

  return (
    <div className="flex flex-col gap-2 p-4">
      <h3 className="text-sm font-semibold text-zinc-400 uppercase tracking-wider mb-2">Templates</h3>
      {templates.map((t) => {
        const Icon = t.icon;
        const isSelected = selectedTemplate === t.id;
        return (
          <button
            key={t.id}
            onClick={() => {
              setSelectedTemplate(t.id);
              setPrompt(`Create an animation showing ${t.desc.toLowerCase()} using the ${t.name} template.`);
            }}
            className={`flex flex-col items-start p-3 rounded-lg border text-left transition-all ${
              isSelected 
                ? 'border-purple-500 bg-purple-500/10' 
                : 'border-zinc-800 bg-zinc-900/50 hover:bg-zinc-800 hover:border-zinc-700'
            }`}
          >
            <div className="flex items-center gap-2 mb-1">
              <Icon size={16} className={isSelected ? 'text-purple-400' : 'text-zinc-400'} />
              <span className={`text-sm font-medium ${isSelected ? 'text-purple-100' : 'text-zinc-200'}`}>
                {t.name}
              </span>
            </div>
            <span className="text-xs text-zinc-500">{t.desc}</span>
          </button>
        );
      })}
    </div>
  );
}
