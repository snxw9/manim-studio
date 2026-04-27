import { NextRequest, NextResponse } from 'next/server';
export async function POST(req: NextRequest) {
  const { code, quality, format } = await req.json();
  if (!code?.trim()) return NextResponse.json({ error: 'No code' }, { status: 400 });
  try {
    const r = await fetch('http://localhost:8000/render', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, quality: quality || '720p', format: format || 'mp4' }),
      signal: AbortSignal.timeout(600000),
    });
    const data = await r.json();
    if (!r.ok) return NextResponse.json({ error: data.detail || 'Render failed' }, { status: r.status });
    return NextResponse.json(data);
  } catch (err: any) {
    return NextResponse.json({ error: err.message }, { status: 503 });
  }
}
