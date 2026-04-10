import { NextResponse } from 'next/server';

export async function GET() {
  try {
    const res = await fetch('http://localhost:8000/health', {
      signal: AbortSignal.timeout(3000),
      cache: 'no-store'
    });
    const data = res.ok ? await res.json() : {};
    return NextResponse.json({ online: res.ok, ...data });
  } catch {
    return NextResponse.json({ online: false });
  }
}
