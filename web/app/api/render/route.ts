import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const { code, quality, format } = await req.json();
    
    if (!code?.trim()) {
      return NextResponse.json({ error: 'No code provided' }, { status: 400 });
    }

    const engineRes = await fetch('http://localhost:8000/render', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, quality, format }),
      signal: AbortSignal.timeout(300000), // 5 min timeout for high quality renders
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
        ? 'Engine is offline or render timed out.'
        : `Render failed: ${err.message}`,
    }, { status: 503 });
  }
}
