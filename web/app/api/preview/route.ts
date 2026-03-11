import { NextResponse } from 'next/server';

const ENGINE_URL = 'http://127.0.0.1:8000';

export async function POST(req: Request) {
  try {
    const { code } = await req.json();

    const response = await fetch(`${ENGINE_URL}/render`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        code, 
        is_preview: true,
        quality: '480p',
        format: 'mp4' 
      }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.error("Engine /preview error:", errorData);
      return NextResponse.json({ error: errorData.detail || 'Preview failed' }, { status: response.status });
    }

    const blob = await response.blob();
    return new Response(blob, {
      headers: { 'Content-Type': 'video/mp4' }
    });
  } catch (error: any) {
    console.error("API /preview error:", error.message);
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
