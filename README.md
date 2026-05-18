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

## Contributing

See docs/CONTEXT.md for full architecture documentation.

> Task :app:compileDebugKotlin
e: file:///C:/Users/Abdulfatai/Documents/manim-studio/android/app/src/main/java/com/manimstudio/app/ui/settings/SettingsScreen.kt:9:47 Unresolved reference 'Visibility'.
e: file:///C:/Users/Abdulfatai/Documents/manim-studio/android/app/src/main/java/com/manimstudio/app/ui/settings/SettingsScreen.kt:10:47 Unresolved reference 'VisibilityOff'.
e: file:///C:/Users/Abdulfatai/Documents/manim-studio/android/app/src/main/java/com/manimstudio/app/ui/settings/SettingsScreen.kt:102:69 Unresolved reference 'VisibilityOff'.
e: file:///C:/Users/Abdulfatai/Documents/manim-studio/android/app/src/main/java/com/manimstudio/app/ui/settings/SettingsScreen.kt:102:102 Unresolved reference 'Visibility'.

> Task :app:compileDebugKotlin FAILED

