import { NextResponse } from 'next/server';

const ENGINE_URL = 'http://127.0.0.1:8000';

export async function POST(req: Request) {
  try {
    const { code, quality, format } = await req.json();

    const response = await fetch(`${ENGINE_URL}/render`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        code, 
        is_preview: false,
        quality: quality || '1080p',
        format: format || 'mp4'
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Engine /render error:", errorText);
      return NextResponse.json({ error: errorText }, { status: response.status });
    }

    const blob = await response.blob();
    return new Response(blob, {
      headers: { 
        'Content-Type': response.headers.get('Content-Type') || 'video/mp4',
        'Content-Disposition': response.headers.get('Content-Disposition') || ''
      }
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error("API /render error:", message);
    return NextResponse.json({ error: message }, { status: 500 });
  }
}
