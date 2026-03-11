'use client';
import { useState, useEffect, ReactNode } from 'react';

export default function ClientOnly({ children, fallback = null }: { 
  children: ReactNode;
  fallback?: ReactNode;
}) {
  const [mounted, setMounted] = useState(false);
  useEffect(() => {
    const frame = requestAnimationFrame(() => setMounted(true));
    return () => cancelAnimationFrame(frame);
  }, []);
  if (!mounted) return <>{fallback}</>;
  return <>{children}</>;
}
