import { NextResponse } from 'next/server';
import fs from 'fs/promises';

export async function POST(req: Request) {
  try {
    const { code } = await req.json();

    const engineRes = await fetch('http://localhost:8000/preview', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
      signal: AbortSignal.timeout(90000),
    });
    
    if (!engineRes.ok) {
      const err = await engineRes.text();
      console.error('[preview route] Engine error:', engineRes.status, err);
      return NextResponse.json({ 
        error: `Engine error ${engineRes.status}`,
        detail: err,
        fix: 'Check if the Manim code is valid or if the engine logs show more details.'
      }, { status: 500 });
    }
    
    const data = await engineRes.json();
    console.log('[preview route] Engine response:', data);
    
    if (!data.videoPath) {
      return NextResponse.json({ error: 'Engine returned no video path', detail: data }, { status: 500 });
    }
    
    // Serve the video file as base64 so the browser can play it
    const videoBuffer = await fs.readFile(data.videoPath);
    const base64 = videoBuffer.toString('base64');
    return NextResponse.json({ video: base64, mimeType: 'video/mp4' });
    
  } catch (err) {
    const message = err instanceof Error ? err.message : String(err);
    console.error('[preview route] Fetch failed:', message);
    return NextResponse.json({ 
      error: 'Could not reach Python engine',
      detail: message,
      fix: 'Make sure the engine is running: cd engine && uvicorn main:app --reload --port 8000'
    }, { status: 503 });
  }
}
