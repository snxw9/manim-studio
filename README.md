# Manim Studio

A cross-platform mathematical animation studio powered by Manim.
Create animations from natural language descriptions or write code directly.

## Platforms

| Platform | Status | Description |
|----------|--------|-------------|
| Web | Active | Next.js app with Monaco editor |
| Android | In development | Kotlin/Compose with on-device rendering |
| Engine | Active | Python/FastAPI AI + Manim renderer |

## Quick Start

### Web App
1. Get a free API key at [console.groq.com](https://console.groq.com)
2. Run `cd engine && python setup.py` and enter your key
3. Run `cd web && npm install && npm run dev`
4. Open http://localhost:3000

### Engine Only
```bash
cd engine
python -m venv venv
venv/Scripts/pip install -r requirements.txt   # Windows
source venv/bin/activate && pip install -r requirements.txt  # Mac/Linux
python setup.py
uvicorn main:app --reload --port 8000
```

## Repository Structure

```
manim-studio/
+-- engine/     Python FastAPI engine + AI + Manim renderer
+-- web/        Next.js web application  
+-- android/    Kotlin Android application (in development)
+-- shared/     Shared data (templates, error codes)
+-- scripts/    START/STOP scripts
+-- docs/       Project documentation
```

## Tech Stack

- **AI:** Groq (primary, free), Gemini, OpenAI (fallbacks)
- **Rendering:** Manim Community Edition
- **Web:** Next.js 15, TypeScript, Monaco Editor
- **Android:** Kotlin, Jetpack Compose, Material Design 3
- **Engine:** Python 3.11, FastAPI, Chaquopy (Android)

## Releases

- **GitHub Releases:** APK downloads for each version tag
- **Play Store:** Coming when ready for public release

## Documentation

Detailed documentation is available for each component of the project:

- [Engine Documentation](file:///C:/Users/Abdulfatai/Documents/manim-studio/docs/ENGINE.md): Python FastAPI backend, AI model router, code validator, and Manim rendering.
- [Web App Documentation](file:///C:/Users/Abdulfatai/Documents/manim-studio/docs/WEB.md): Next.js web interface, Monaco editor workspace, and proxy services.
- [Android App Documentation](file:///C:/Users/Abdulfatai/Documents/manim-studio/docs/ANDROID.md): Kotlin app structure, Jetpack Compose screens, and the PRoot virtualized guest OS environment.

## Contributing

See [CONTEXT.md](file:///C:/Users/Abdulfatai/Documents/manim-studio/docs/CONTEXT.md) for full architecture context and historical blueprints.

