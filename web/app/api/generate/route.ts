import { NextResponse } from 'next/server';

const ENGINE_URL = 'http://127.0.0.1:8000';

export async function POST(req: Request) {
  try {
    const { prompt, template, user_keys } = await req.json();

    const response = await fetch(`${ENGINE_URL}/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        prompt, 
        template: template || 'none',
        user_keys: user_keys || {}
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      return NextResponse.json(
        { error: errorData.detail || 'Generation failed' }, 
        { status: response.status }
      );
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
      model: data.model,
      remaining_today: data.remaining_today,
      using_own_key: data.using_own_key
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    return NextResponse.json({ error: message }, { status: 500 });
  }
}
