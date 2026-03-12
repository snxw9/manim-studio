import { NextResponse } from 'next/server';

export async function GET() {
  try {
    const res = await fetch('http://localhost:8000/health', {
      signal: AbortSignal.timeout(3000),
      cache: 'no-store',
    });
    if (res.ok) {
      const data = await res.json();
      return NextResponse.json({ online: true, ...data });
    }
    return NextResponse.json({ online: false }, { status: 200 });
  } catch {
    return NextResponse.json({ online: false }, { status: 200 });
  }
}
