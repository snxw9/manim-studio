import { NextRequest, NextResponse } from 'next/server';
export async function POST(req: NextRequest) {
  const { prompt, userKeys } = await req.json();
  if (!prompt?.trim()) {
    return NextResponse.json({ error: 'Prompt is empty' }, { status: 400 });
  }
  try {
    const r = await fetch('http://localhost:8000/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt, user_keys: userKeys || {} }),
      signal: AbortSignal.timeout(120000),
    });
    const data = await r.json();
    if (!r.ok) {
      return NextResponse.json(
        { error: data.detail || `Engine error ${r.status}` },
        { status: r.status }
      );
    }
    return NextResponse.json(data);
  } catch (err: any) {
    return NextResponse.json(
      { error: err.message?.includes('ECONNREFUSED')
          ? 'Engine offline'
          : err.message },
      { status: 503 }
    );
  }
}
