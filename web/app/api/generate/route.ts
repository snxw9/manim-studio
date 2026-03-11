import { NextResponse } from 'next/server';

const ENGINE_URL = 'http://127.0.0.1:8000';

export async function POST(req: Request) {
  try {
    const { prompt, template } = await req.json();

    const response = await fetch(`${ENGINE_URL}/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt, template: template || 'none' }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json({
      code: data.code,
      suggestions: data.suggestions || [
        'Add a zooming camera effect',
        'Change the colors to be vibrant',
        'Add math formulas describing the shapes'
      ],
      provider: data.provider,
      model: data.model
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    return NextResponse.json({ error: message }, { status: 500 });
  }
}
