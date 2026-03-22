import { NextRequest } from 'next/server';

export async function POST(req: NextRequest) {
  const { code, quality, format } = await req.json();

  if (!code?.trim()) {
    return new Response(JSON.stringify({ error: 'No code' }), { status: 400 });
  }

  const encoder = new TextEncoder();

  const stream = new ReadableStream({
    async start(controller) {
      const send = (data: object) => {
        controller.enqueue(encoder.encode(JSON.stringify(data) + '\n'));
      };

      try {
        send({ status: 'started', message: 'Render started...' });

        const res = await fetch('http://localhost:8000/render', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            code,
            quality: quality || '720p',
            format: format || 'mp4',
          }),
          signal: AbortSignal.timeout(600000),
        });

        const data = await res.json();

        if (!res.ok) {
          send({ status: 'error', error: data.detail || 'Render failed' });
        } else {
          send({ status: 'done', ...data });
        }
      } catch (err: any) {
        send({ status: 'error', error: err.message });
      } finally {
        controller.close();
      }
    },
  });

  return new Response(stream, {
    headers: {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Connection': 'keep-alive',
    },
  });
}
