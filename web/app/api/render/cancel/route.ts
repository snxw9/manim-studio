import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const res = await fetch('http://localhost:8000/render/cancel', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: await req.text(),
      signal: AbortSignal.timeout(5000),
    });
    return NextResponse.json(await res.json());
  } catch {
    return NextResponse.json({ cancelled: false });
  }
}
