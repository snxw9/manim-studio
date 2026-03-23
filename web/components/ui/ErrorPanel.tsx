'use client';
import { matchError } from '@/lib/errorCodes';
import { X, AlertTriangle, ChevronDown, ChevronRight } from 'lucide-react';
import { useState } from 'react';

interface ErrorPanelProps {
  error: string;
  onDismiss: () => void;
}

export function ErrorPanel({ error, onDismiss }: ErrorPanelProps) {
  const [showRaw, setShowRaw] = useState(false);
  const matched = matchError(error);

  return (
    <div style={{
      margin: '0 0 12px 0',
      background: 'var(--bg-2)',
      border: '1px solid var(--err)',
      borderRadius: 3,
      overflow: 'hidden',
      fontSize: 11,
    }}>
      {/* Header */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8,
        padding: '8px 12px',
        background: '#8a404018',
        borderBottom: '1px solid var(--err)',
      }}>
        <AlertTriangle size={13} color="var(--err)" style={{ flexShrink: 0 }} />
        <span style={{ color: 'var(--err)', fontWeight: 500, flex: 1, letterSpacing: '0.02em' }}>
          {matched ? `${matched.code} — ${matched.title}` : 'Render Error'}
        </span>
        <button
          onClick={onDismiss}
          style={{ color: 'var(--t3)', background: 'none', border: 'none', cursor: 'pointer' }}
        >
          <X size={13} />
        </button>
      </div>

      {/* Matched error explanation */}
      {matched && (
        <div style={{ padding: '10px 12px', borderBottom: '1px solid var(--line)' }}>
          <p style={{ color: 'var(--t2)', lineHeight: 1.7, marginBottom: 6 }}>
            {matched.explanation}
          </p>
          <div style={{
            background: 'var(--bg-3)', borderRadius: 2,
            padding: '6px 10px', borderLeft: '2px solid var(--a)',
          }}>
            <span style={{ color: 'var(--t3)', letterSpacing: '0.04em', fontSize: 9, textTransform: 'uppercase' }}>
              How to fix
            </span>
            <p style={{ color: 'var(--t1)', marginTop: 3, lineHeight: 1.6 }}>
              {matched.fix}
            </p>
          </div>
        </div>
      )}

      {/* Raw error toggle */}
      <div style={{ padding: '4px 12px 8px' }}>
        <button
          onClick={() => setShowRaw(o => !o)}
          style={{
            display: 'flex', alignItems: 'center', gap: 4,
            color: 'var(--t3)', background: 'none', border: 'none',
            cursor: 'pointer', fontSize: 10, letterSpacing: '0.04em',
            padding: '4px 0',
          }}
        >
          {showRaw ? <ChevronDown size={11} /> : <ChevronRight size={11} />}
          {showRaw ? 'hide' : 'show'} raw error
        </button>

        {showRaw && (
          <pre style={{
            marginTop: 6,
            padding: '8px 10px',
            background: 'var(--bg-0)',
            border: '1px solid var(--line)',
            borderRadius: 2,
            fontSize: 10,
            color: 'var(--err)',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            maxHeight: 200,
            overflowY: 'auto',
            lineHeight: 1.6,
          }}>
            {error}
          </pre>
        )}
      </div>
    </div>
  );
}
