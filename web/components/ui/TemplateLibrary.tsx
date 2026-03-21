'use client';
import { useState } from 'react';
import { TEMPLATE_LIST, TEMPLATES } from '@/lib/templates';
import { useStore } from '@/lib/store';
import { ChevronDown, ChevronRight } from 'lucide-react';

export default function TemplateLibrary() {
  const { selectedTemplate, setSelectedTemplate, setGeneratedCode, setActiveTab } = useStore();
  const [isOpen, setIsOpen] = useState(true);

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
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between w-full px-3 py-2 text-[11px] uppercase tracking-widest text-[var(--text-dim)] hover:text-[var(--text-secondary)]"
      >
        <span>Templates</span>
        {isOpen ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
      </button>

      {isOpen && (
        <div className="flex flex-col">
          {TEMPLATE_LIST.map((template) => (
            <button
              key={template.id}
              onClick={() => handleSelect(template.id)}
              className={`
                w-full text-left px-4 py-1.5 text-[13px] relative
                ${selectedTemplate === template.id
                  ? 'text-[var(--text-primary)]'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                }
              `}
            >
              {selectedTemplate === template.id && (
                <span className="absolute left-0 top-0 bottom-0 w-[2px] bg-[var(--accent)]" />
              )}
              {template.name}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
