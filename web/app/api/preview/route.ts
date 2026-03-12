import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const { code } = await req.json();
    
    if (!code?.trim()) {
      return NextResponse.json({ error: 'No code provided' }, { status: 400 });
    }

    const engineRes = await fetch('http://localhost:8000/preview', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
      signal: AbortSignal.timeout(120000),
    });

    const data = await engineRes.json();

    if (!engineRes.ok) {
      return NextResponse.json({
        error: data.detail || 'Engine error',
      }, { status: engineRes.status });
    }

    return NextResponse.json(data);

  } catch (err: any) {
    const isOffline = err.message?.includes('ECONNREFUSED') || err.name === 'TimeoutError';
    return NextResponse.json({
      error: isOffline
        ? 'Engine is offline. Start it with: cd engine && uvicorn main:app --reload --port 8000'
        : `Preview failed: ${err.message}`,
    }, { status: 503 });
  }
}
