# Manim Studio — Unified Context

## 1. Project Overview
**Manim Animation Studio** is a cross-platform application (Android mobile + web) that allows anyone — regardless of coding ability — to create professional mathematical animations powered by [Manim Community Edition](https://www.manim.community/).

Users describe what they want in plain English (or via voice), and the AI generates, compiles, and renders the Manim animation locally on their device. Developers can also edit the generated code directly via a built-in Monaco editor.

## 2. Architecture
Manim Studio follows a "shared brain" architecture where both the Android and Web interfaces share the same core engine logic.

- **Core Engine (Python/FastAPI):** Shared Brain. Manages AI code generation, Manim rendering, and asset/template management.
- **Web App (Next.js 16):** Desktop/Browser UI. Interacts with the local Python engine via a local API server.
- **Android App (Kotlin/Compose):** Mobile UI. Embeds the Python engine natively using Chaquopy.

## 3. Tech Stack
### AI / LLM Layer
- **Google Gemini API** (Primary): Best for long context, multimodal.
- **Groq API**: High-speed inference with Llama models.
- **OpenAI API**: Secondary / user choice fallback.

### Python Engine
- **Manim Community Edition**: Core animation engine.
- **FFmpeg**: Video encoding/compression.
- **FastAPI**: Local API server for web communication.

### Web App
- **Next.js 16** (App Router)
- **TypeScript** & **Tailwind CSS**
- **Monaco Editor**: High-performance code editing.
- **Zustand**: Global state management.

### Android App
- **Kotlin** & **Jetpack Compose**
- **Chaquopy**: Python execution on Android.
- **Room Database**: Local project storage.
- **ExoPlayer**: Video playback.

### Replit Workspace (Specialized)
- **Monorepo tool**: pnpm workspaces
- **API framework**: Express 5
- **Database**: PostgreSQL + Drizzle ORM
- **API codegen**: Orval (from OpenAPI spec)
- **Artifacts**: Math Animation Studio (React + Vite shell)

## 4. Repository Structure
```
manim-studio/
+-- .github/            # GitHub workflows and issue templates
+-- android/            # Kotlin Android App
+-- engine/             # Shared Python Core
¦   +-- ai/             # AI Routing & Prompting
¦   +-- renderer/       # Manim execution logic
¦   +-- templates/      # Animation templates
¦   +-- tests/          # Engine unit/integration tests
¦   +-- main.py         # FastAPI Entry Point
+-- web/                # Next.js Web App
¦   +-- app/            # Pages & API routes
¦   +-- components/     # UI Components
¦   +-- lib/            # Utilities & Store
+-- shared/             # Data shared across platforms (JSON)
¦   +-- templates.json
¦   +-- error-codes.json
¦   +-- assets.json
+-- scripts/            # Management and automation scripts
+-- docs/               # Project documentation
```

## 5. API Endpoints
The following endpoints are currently active in the Engine API:
- `GET  /health`: Check engine status.
- `POST /generate`: Convert user prompts to Manim code.
- `POST /render`: Execute Manim code and return video.
- `GET  /pool/status`: Check AI provider pool health.
- `POST /render/cancel`: Cancel an ongoing render.
- `GET  /templates`: List available templates.
- `GET  /assets`: List available assets.

## 6. Shared Data layer
The `shared/` directory contains JSON files that act as the single source of truth for:
- **Templates**: Pre-defined animations available on all platforms.
- **Error Codes**: Consistent error reporting and troubleshooting across the system.
- **Assets**: Definitions for mathematical shapes and symbols.

## 7. Development Setup
1. **Clone Repo**
2. **Engine Setup**:
   ```bash
   cd engine
   python setup.py
   ```
3. **Web Setup**:
   ```bash
   cd web
   npm install
   npm run dev
   ```
4. **API Keys**: Add `GEMINI_API_KEY`, `GROQ_API_KEY`, or `OPENAI_API_KEY` to an `.env` file in the `engine/` directory.

## 8. Status and Roadmap
- **Phase 1**: Core Cleanup & Monorepo Restructuring (Complete)
- **Phase 2**: Modular Template System (In Progress)
- **Phase 3**: Asset Library Integration
- **Phase 4**: Android App Alpha
- **Phase 5**: Interactive Animations
