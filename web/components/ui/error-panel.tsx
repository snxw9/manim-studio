'use client';
import { useState } from 'react';
import { analyzeError, getSourceLabel, getSourceColor, ErrorSource } from '@/lib/errorCodes';
import { ChevronDown, ChevronRight, RefreshCw, Flag, X } from 'lucide-react';

interface ErrorPanelProps {
  error: string;
  onDismiss: () => void;
  onRetry?: () => void;
}

export function ErrorPanel({ error, onDismiss, onRetry }: ErrorPanelProps) {
  const [showRaw, setShowRaw] = useState(false);
  const [reported, setReported] = useState(false);
  const info = analyzeError(error);
  const sourceColor = getSourceColor(info.source);
  const sourceLabel = getSourceLabel(info.source);

  const handleReport = () => {
    // Copy error to clipboard for easy reporting
    navigator.clipboard.writeText(
      `Error Code: ${info.code}\nError: ${info.title}\n\nRaw error:\n${error}`
    ).catch(() => {});
    setReported(true);
  };

  return (
    <div style={{
      background: 'var(--bg-2)',
      border: `1px solid ${sourceColor}40`,
      borderRadius: 4,
      overflow: 'hidden',
      fontSize: 12,
      fontFamily: 'inherit',
    }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        padding: '8px 12px',
        background: `${sourceColor}12`,
        borderBottom: `1px solid ${sourceColor}30`,
      }}>
        {/* Source badge */}
        <div style={{
          background: `${sourceColor}20`,
          border: `1px solid ${sourceColor}40`,
          borderRadius: 100,
          padding: '1px 8px',
          fontSize: 10,
          color: sourceColor,
          letterSpacing: '0.05em',
          flexShrink: 0,
        }}>
          {sourceLabel}
        </div>

        <span style={{ color: 'var(--t1)', fontWeight: 500, flex: 1 }}>
          {info.title}
        </span>

        <span style={{ fontSize: 10, color: 'var(--t3)', flexShrink: 0 }}>
          {info.code}
        </span>

        <button
          onClick={onDismiss}
          style={{
            color: 'var(--t3)', background: 'none',
            border: 'none', cursor: 'pointer',
            padding: 2, display: 'flex',
          }}
        >
          <X size={13} />
        </button>
      </div>

      {/* Three layers */}
      <div style={{ padding: '12px 14px', display: 'flex', flexDirection: 'column', gap: 8 }}>

        {/* Layer 1 — What happened */}
        <p style={{ color: 'var(--t2)', lineHeight: 1.7, margin: 0 }}>
          {info.what}
        </p>

        {/* Layer 2 — Why */}
        <div style={{
          background: 'var(--bg-3)',
          borderRadius: 3,
          padding: '7px 10px',
          borderLeft: `2px solid ${sourceColor}60`,
        }}>
          <span style={{
            fontSize: 9,
            color: 'var(--t3)',
            letterSpacing: '0.08em',
            textTransform: 'uppercase',
            display: 'block',
            marginBottom: 3,
          }}>
            Why this happened
          </span>
          <p style={{ color: 'var(--t2)', lineHeight: 1.6, margin: 0, fontSize: 11 }}>
            {info.why}
          </p>
        </div>

        {/* Layer 3 — What to do */}
        <div style={{
          background: 'var(--bg-3)',
          borderRadius: 3,
          padding: '7px 10px',
          borderLeft: '2px solid var(--accent)',
        }}>
          <span style={{
            fontSize: 9,
            color: 'var(--t3)',
            letterSpacing: '0.08em',
            textTransform: 'uppercase',
            display: 'block',
            marginBottom: 3,
          }}>
            What to do
          </span>
          <p style={{ color: 'var(--t1)', lineHeight: 1.6, margin: 0, fontSize: 11 }}>
            {info.action}
          </p>
        </div>

        {/* Action buttons */}
        <div style={{ display: 'flex', gap: 6, marginTop: 2 }}>
          {info.canRetry && onRetry && (
            <button
              onClick={onRetry}
              style={{
                display: 'flex', alignItems: 'center', gap: 5,
                background: 'var(--accent)', color: 'var(--bg-0)',
                border: 'none', borderRadius: 3,
                padding: '5px 12px', fontSize: 11,
                cursor: 'pointer', fontFamily: 'inherit',
                fontWeight: 500,
              }}
            >
              <RefreshCw size={11} />
              Try Again
            </button>
          )}

          {info.shouldReport && (
            <button
              onClick={handleReport}
              style={{
                display: 'flex', alignItems: 'center', gap: 5,
                background: 'none',
                border: '1px solid var(--border)',
                color: reported ? 'var(--ok)' : 'var(--t2)',
                borderRadius: 3,
                padding: '5px 12px', fontSize: 11,
                cursor: 'pointer', fontFamily: 'inherit',
              }}
            >
              <Flag size={11} />
              {reported ? 'Copied to clipboard' : 'Report Issue'}
            </button>
          )}

          <button
            onClick={() => setShowRaw(o => !o)}
            style={{
              display: 'flex', alignItems: 'center', gap: 4,
              background: 'none', border: 'none',
              color: 'var(--t3)', fontSize: 10,
              cursor: 'pointer', fontFamily: 'inherit',
              marginLeft: 'auto', padding: '5px 4px',
              letterSpacing: '0.03em',
            }}
          >
            {showRaw
              ? <><ChevronDown size={11} /> hide technical details</>
              : <><ChevronRight size={11} /> show technical details</>
            }
          </button>
        </div>

        {/* Raw error — collapsed by default */}
        {showRaw && (
          <pre style={{
            margin: 0,
            padding: '8px 10px',
            background: 'var(--bg-0)',
            border: '1px solid var(--border)',
            borderRadius: 3,
            fontSize: 10,
            color: 'var(--t2)',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            maxHeight: 200,
            overflowY: 'auto',
            lineHeight: 1.5,
          }}>
            {error}
          </pre>
        )}
      </div>
    </div>
  );
}
