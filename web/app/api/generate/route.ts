import { NextResponse, NextRequest } from 'next/server';

export async function POST(req: NextRequest) {
  const body = await req.json();
  const { prompt, template, userKeys } = body;

  if (!prompt?.trim()) {
    return NextResponse.json({ error: 'Prompt is empty' }, { status: 400 });
  }

  try {
    const engineRes = await fetch('http://localhost:8000/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        prompt,
        template: template || null,
        user_keys: userKeys || {},
      }),
      signal: AbortSignal.timeout(120000),
    });

    const data = await engineRes.json();
    console.log('[generate route] engine status:', engineRes.status, 'data:', JSON.stringify(data).slice(0, 200));

    if (!engineRes.ok) {
      return NextResponse.json({
        error: data.detail || data.error || `Engine error ${engineRes.status}`,
      }, { status: engineRes.status });
    }

    if (!data.code) {
      return NextResponse.json({
        error: 'Engine returned no code. Check engine logs.',
        detail: data,
      }, { status: 500 });
    }

    return NextResponse.json(data);

  } catch (err: any) {
    console.error('[generate route] error:', err);
    const offline = err.message?.includes('ECONNREFUSED');
    return NextResponse.json({
      error: offline
        ? 'Engine is offline — run: cd engine && uvicorn main:app --reload --port 8000'
        : `Generate failed: ${err.message}`,
    }, { status: 503 });
  }
}
