# Manim Studio — Project Context

## 1. Project Overview
**Manim Animation Studio** is a cross-platform application (Android mobile + web) that allows anyone — regardless of coding ability — to create professional mathematical animations powered by [Manim Community Edition](https://www.manim.community/).

Users describe what they want in plain English (or via voice), and the AI generates, compiles, and renders the Manim animation locally on their device. Developers can also edit the generated code directly via a built-in Monaco editor.

## 2. Architecture  
Manim Studio follows a "shared brain" architecture where both the Android and Web interfaces share the same core engine logic.

- **Core Engine (Python/FastAPI):** Shared Brain. Manages AI code generation, Manim rendering, and asset/template management.
- **Web App (Next.js 16):** Desktop/Browser UI. Interacts with the local Python engine via a local API server.
- **Android App (Kotlin/Compose):** Mobile UI. Embeds the Python engine natively using Chaquopy.

## 3. Tech Stack
### 🤖 AI / LLM Layer
- **Google Gemini API** (Primary): Best for long context, multimodal.
- **Groq API**: High-speed inference with Llama models.
- **OpenAI API**: Secondary / user choice fallback.

### 🐍 Python Engine
- **Manim Community Edition**: Core animation engine.
- **FFmpeg**: Video encoding/compression.
- **FastAPI**: Local API server for web communication.

### 🌐 Web App
- **Next.js 16** (App Router)
- **TypeScript** & **Tailwind CSS**
- **Monaco Editor**: High-performance code editing.
- **Zustand**: Global state management.

### 📱 Android App (Planned)
- **Kotlin** & **Jetpack Compose**
- **Chaquopy**: Python execution on Android.
- **Room Database**: Local project storage.

## 4. Repository Structure
```
manim-studio/
├── android/            # Kotlin Android App (In Progress)
├── engine/             # Shared Python Core
│   ├── ai/             # AI Routing & Prompting
│   ├── renderer/       # Manim execution logic
│   ├── templates/      # Animation templates (Empty - Rebuilding)
│   ├── main.py         # FastAPI Entry Point
│   └── requirements.txt
├── web/                # Next.js Web App
│   ├── app/            # Pages & API routes
│   ├── components/     # UI Components
│   └── lib/            # Utilities & Store
└── MANIM_STUDIO_CONTEXT.md
```

## 5. API Endpoints
The following endpoints are currently active in the Engine API:
- `GET  /health`: Check engine status.
- `POST /generate`: Convert user prompts to Manim code.
- `POST /render`: Execute Manim code and return video.
- `GET  /pool/status`: Check AI provider pool health.
- `POST /render/cancel`: Cancel an ongoing render.
- `GET  /templates`: List available templates (Currently empty).
- `GET  /assets`: List available assets (Currently empty).

## 6. Engine Details
The engine uses a sophisticated provider pool system with automatic rotation and circuit breaking. It validates generated code for syntax errors and common Manim-specific mistakes before attempting to render.

## 7. Web App Details
The web app provides a dual-panel experience:
1. **Prompt View**: For AI-assisted animation creation.
2. **Editor View**: For manual code refinement using a VS Code-grade editor.

It communicates with the local Python engine on port 8000 (proxied via Next.js API routes) and provides real-time feedback on render status.

## 8. Android App (Planned)
The Android app will bring the full power of Manim Studio to mobile devices, utilizing Chaquopy to run the same engine code as the web version. Features like voice prompts and on-device rendering are core priorities.

## 9. AI Provider System
The system is designed to be resilient. It supports:
- **Developer Pool**: A rotation of API keys for anonymous users.
- **User Keys**: Users can provide their own Groq/Gemini/OpenAI keys for unlimited use.
- **Model Tiers**: Automatically chooses between "fast", "standard", and "smart" models based on the complexity of the request.

## 10. Development Setup
1. **Clone Repo**
2. **Engine Setup**:
   ```bash
   cd engine
   python -m venv venv
   source venv/bin/activate
   pip install -r requirements.txt
   pip install manim
   ```
3. **Web Setup**:
   ```bash
   cd web
   npm install
   npm run dev
   ```
4. **API Keys**: Add `GEMINI_API_KEY`, `GROQ_API_KEY`, or `OPENAI_API_KEY` to an `.env` file in the root directory.

## 11. Known Issues and Status
- **Templates**: REMOVED — being rebuilt cleanly for better modularity.
- **Preview**: Not working — fast preview mode is currently deferred.
- **Asset Library**: REMOVED — being rebuilt to integrate seamlessly with templates.

## 12. Roadmap
- **Phase 1**: Core Cleanup (Complete)
- **Phase 2**: New Modular Template System
- **Phase 3**: Asset Library Integration
- **Phase 4**: Android App Alpha
- **Phase 5**: Interactive Animations
