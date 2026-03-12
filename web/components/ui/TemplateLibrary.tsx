'use client';
import { TEMPLATE_LIST, TEMPLATES } from '@/lib/templates';
import { useStore } from '@/lib/store';

export default function TemplateLibrary() {
  const { selectedTemplate, setSelectedTemplate, setGeneratedCode, setActiveTab } = useStore();

  const handleSelect = (templateId: string) => {
    const template = TEMPLATES[templateId];
    if (!template) return;
    
    setSelectedTemplate(templateId);
    setGeneratedCode(template.code);   // Load code instantly — no API call
    setActiveTab('code');              // Switch to code view immediately
  };

  return (
    <div className="mb-4">
      <p className="text-[11px] uppercase tracking-widest text-[var(--text-dim)] mb-2 px-1">
        Templates
      </p>
      <div className="space-y-0.5">
        {TEMPLATE_LIST.map((template) => (
          <button
            key={template.id}
            onClick={() => handleSelect(template.id)}
            className={`
              w-full text-left px-2 py-1.5 rounded text-[13px] transition-all
              relative overflow-hidden
              ${selectedTemplate === template.id
                ? 'text-[var(--text-primary)]'
                : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-elevated)]'
              }
            `}
          >
            {selectedTemplate === template.id && (
              <span className="absolute left-0 top-1 bottom-1 w-[3px] bg-[var(--accent)] rounded-r" />
            )}
            <span className={selectedTemplate === template.id ? 'ml-2' : ''}>
              {template.name}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
