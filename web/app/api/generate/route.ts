import { NextRequest, NextResponse } from 'next/server';
export async function POST(req: NextRequest) {
  const body = await req.json();
  const { prompt, userKeys, preferredProvider } = body;
  if (!prompt?.trim()) {
    return NextResponse.json({ error: 'Prompt is empty' }, { status: 400 });
  }
  try {
    const r = await fetch('http://localhost:8000/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        prompt, 
        user_keys: userKeys || {},
        preferred_provider: preferredProvider
      }),
      signal: AbortSignal.timeout(120000),
    });
    const data = await r.json();
    if (!r.ok) return NextResponse.json(
      { error: data.detail || 'Generation failed' }, { status: r.status }
    );
    return NextResponse.json(data);
  } catch (err: any) {
    return NextResponse.json({ error: err.message }, { status: 503 });
  }
}
