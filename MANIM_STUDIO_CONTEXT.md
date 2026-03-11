
# 🎬 Manim Animation Studio — Project Context File

> **Version:** 1.0.0  
> **Created:** 2026-03-11  
> **Stack:** Kotlin (Android) · Next.js (Web) · Python/FastAPI (Engine) · Gemini / OpenAI (AI)  
> **Editor:** VS Code · **VCS:** Git + GitHub · **AI CLI:** Gemini CLI / OpenAI Codex

---

## 📌 Project Overview

**Manim Animation Studio** is a cross-platform application (Android mobile + web) that allows anyone — regardless of coding ability — to create professional mathematical animations powered by [Manim Community Edition](https://www.manim.community/).

Users describe what they want in plain English (or via voice), and the AI generates, compiles, and renders the Manim animation locally on their device. Developers can also edit the generated code directly via a built-in Monaco editor.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    USER INTERFACES                       │
│  ┌───────────────────┐    ┌───────────────────────────┐  │
│  │  Android App      │    │  Web App (Next.js)        │  │
│  │  (Kotlin +        │    │  Runs in browser,         │  │
│  │  Jetpack Compose) │    │  processes on host PC     │  │
│  └────────┬──────────┘    └────────────┬──────────────┘  │
└───────────┼─────────────────────────────┼────────────────┘
            │                             │
┌───────────▼─────────────────────────────▼────────────────┐
│                    CORE ENGINE (Shared Brain)             │
│                                                          │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  AI Module   │  │ Manim Renderer │  │ Code Editor  │  │
│  │  (Gemini /   │  │ (Python +      │  │ (Monaco)     │  │
│  │  OpenAI)     │  │  FFmpeg)       │  │              │  │
│  └──────────────┘  └────────────────┘  └──────────────┘  │
│                                                          │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  Template    │  │ Timeline/Scene │  │  Plugin      │  │
│  │  Library     │  │ Manager        │  │  System      │  │
│  └──────────────┘  └────────────────┘  └──────────────┘  │
└──────────────────────────────────────────────────────────┘
            │
┌───────────▼──────────────────────────────────────────────┐
│                    LOCAL STORAGE LAYER                   │
│   Projects · Generated Code · Rendered Videos · Assets  │
└──────────────────────────────────────────────────────────┘
```

**Key Design Principle:** Both interfaces share the same core engine logic. On Android, this runs as an embedded Python process. On web, it runs as a local Next.js server on the user's machine.

---

## 📁 Repository Structure

```
manim-studio/
├── .github/
│   ├── workflows/
│   │   ├── android-ci.yml          # Build & test Android APK
│   │   ├── web-ci.yml              # Build & test Next.js
│   │   └── engine-tests.yml        # Python engine unit tests
│   └── ISSUE_TEMPLATE/
├── android/                        # Kotlin Android App
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/manimstudio/
│   │   │   │   ├── ui/             # Jetpack Compose screens
│   │   │   │   ├── engine/         # Python bridge (Chaquopy)
│   │   │   │   ├── ai/             # AI API clients
│   │   │   │   ├── editor/         # Monaco WebView wrapper
│   │   │   │   ├── timeline/       # Timeline editor
│   │   │   │   ├── plugins/        # Plugin loader
│   │   │   │   └── storage/        # Room DB + file storage
│   │   │   └── assets/
│   │   │       └── python/         # Bundled Python engine
│   │   └── build.gradle.kts
│   └── gradle/
├── web/                            # Next.js Web App
│   ├── app/
│   │   ├── page.tsx                # Main studio page
│   │   ├── editor/                 # Monaco code editor
│   │   ├── timeline/               # Timeline editor
│   │   ├── preview/                # Live preview component
│   │   └── api/
│   │       ├── generate/route.ts   # AI → Manim code
│   │       ├── render/route.ts     # Trigger Python render
│   │       └── preview/route.ts    # Low-res preview
│   ├── components/
│   │   ├── DragDropBuilder.tsx
│   │   ├── TemplateLibrary.tsx
│   │   ├── AssetLibrary.tsx
│   │   ├── VoicePrompt.tsx
│   │   ├── TimelineEditor.tsx
│   │   └── ProjectManager.tsx
│   ├── lib/
│   │   ├── ai-client.ts            # Gemini / OpenAI wrapper
│   │   └── manim-bridge.ts         # Calls local Python engine
│   └── package.json
├── engine/                         # Shared Python Core
│   ├── main.py                     # Entry point (CLI + API mode)
│   ├── ai/
│   │   ├── prompt_builder.py       # Constructs LLM prompts
│   │   ├── gemini_client.py
│   │   └── openai_client.py
│   ├── renderer/
│   │   ├── manim_runner.py         # Executes Manim scenes
│   │   ├── preview_renderer.py     # Low-res fast preview
│   │   └── video_exporter.py       # FFmpeg post-processing
│   ├── templates/
│   │   ├── calculus.py
│   │   ├── graph_viz.py
│   │   ├── geometry_proofs.py
│   │   └── matrix_transforms.py
│   ├── assets/
│   │   ├── symbols/
│   │   ├── shapes/
│   │   └── grids/
│   ├── plugins/
│   │   └── plugin_loader.py
│   ├── tests/
│   │   ├── test_ai_generation.py
│   │   ├── test_renderer.py
│   │   └── test_templates.py
│   └── requirements.txt
├── docs/
│   ├── architecture.md
│   ├── plugin-api.md
│   └── contributing.md
├── .gitignore
├── .env.example
└── README.md
```

---

## 🛠️ Full Tech Stack

### 🤖 AI / LLM Layer
| Tool | Purpose | Notes |
|------|---------|-------|
| **Google Gemini API** (gemini-2.0-flash / pro) | Primary: Text/Voice → Manim code | Best for long context, multimodal |
| **OpenAI API** (GPT-4o / o3) | Secondary / user choice | Fallback or user preference |
| **Gemini CLI** | Local dev & testing of prompts | `npm install -g @google/gemini-cli` |
| **OpenAI Codex CLI** | Code editing suggestions | For the advanced code editor |
| **LangChain / LangGraph** (Python) | Orchestrate multi-step AI pipelines | Scene suggestion chaining |

### 🐍 Python Engine
| Package | Purpose |
|---------|---------|
| `manim` (Community Edition) | Core animation engine |
| `ffmpeg-python` | Video encoding/compression |
| `fastapi` + `uvicorn` | Local API server (web mode) |
| `websockets` | Live preview streaming |
| `Pillow` | Image/thumbnail processing |
| `pytest` | Engine unit tests |
| `black` + `ruff` | Code formatting/linting |
| `langchain` | LLM prompt chaining |
| `google-generativeai` | Gemini Python SDK |
| `openai` | OpenAI Python SDK |

### 📱 Android (Kotlin)
| Tool | Purpose |
|------|---------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Modern declarative UI |
| **Chaquopy** | Run Python (Manim engine) directly on Android |
| **Room Database** | Local project/asset storage |
| **DataStore** | User preferences & settings |
| **WorkManager** | Background rendering jobs |
| **Media3 / ExoPlayer** | Video playback |
| **WebView + Monaco** | In-app code editor |
| **CameraX / SpeechRecognizer** | Voice prompt input |
| **Hilt** | Dependency injection |
| **Kotlin Coroutines + Flow** | Async rendering pipeline |
| **Retrofit** | Optional cloud API calls |
| **OkHttp** | HTTP client (Gemini/OpenAI) |
| **Material 3** | UI design system |
| **Android Drag & Drop API** | Drag-and-drop scene builder |

### 🌐 Web App
| Tool | Purpose |
|------|---------|
| **Next.js 15** (App Router) | Web framework |
| **TypeScript** | Type safety |
| **Tailwind CSS** | Styling |
| **Monaco Editor** | VS Code-grade in-browser code editor |
| **React DnD** / **dnd-kit** | Drag-and-drop scene builder |
| **Zustand** | Global state (project, timeline, scenes) |
| **Web Speech API** | Voice prompt (browser-native) |
| **WebSockets** (via Next.js API routes) | Live preview streaming |
| **IndexedDB** (via Dexie.js) | Local project storage in browser |
| **FFmpeg.wasm** | Optional: client-side video processing |
| **Framer Motion** | UI animations |
| **shadcn/ui** | Component library |
| **Electron** (optional, future) | Package web app as desktop app |

### 🗄️ Storage
| Layer | Tech | Purpose |
|-------|------|---------|
| Android local | Room DB + File System | Projects, code, videos |
| Web local | IndexedDB + File System Access API | Projects, exports |
| Cloud (optional) | Firebase / Supabase | Sync across devices |
| Video output | Device gallery / Downloads | Rendered MP4 files |

### 🔧 Dev Tools & Infrastructure
| Tool | Purpose |
|------|---------|
| **VS Code** | Primary IDE |
| **Git** | Version control |
| **GitHub** | Remote repository + CI/CD |
| **GitHub Actions** | Automated testing & builds |
| **Android Studio** | Android build/emulator (can open Kotlin from VS Code) |
| **Gradle (Kotlin DSL)** | Android build system |
| **ESLint + Prettier** | Web code quality |
| **Vitest** | Web unit tests |
| **Detekt** | Kotlin static analysis |
| **Docker** | Containerise Python engine for web dev |
| **pre-commit hooks** | Enforce formatting before commits |

---

## 🔌 MCP Servers (for Gemini CLI / AI-assisted development)

These MCP servers supercharge your Gemini CLI and AI coding workflow:

| MCP Server | Purpose | Install |
|------------|---------|---------|
| **filesystem** | Let AI read/write project files | Built into Gemini CLI |
| **github** | AI can create PRs, branches, issues | `@modelcontextprotocol/server-github` |
| **fetch** | AI can read Manim docs / APIs | `@modelcontextprotocol/server-fetch` |
| **sqlite** | Inspect local databases during dev | `@modelcontextprotocol/server-sqlite` |
| **sequential-thinking** | Better multi-step reasoning for complex scenes | `@modelcontextprotocol/server-sequential-thinking` |
| **memory** | Persistent project context across sessions | `@modelcontextprotocol/server-memory` |
| **puppeteer** | Screenshot/test web UI automatically | `@modelcontextprotocol/server-puppeteer` |
| **android-mcp** *(community)* | Interact with Android emulator | Search GitHub for Android MCP |

**Gemini CLI Config (~/.gemini/settings.json):**
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/manim-studio"]
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": { "GITHUB_PERSONAL_ACCESS_TOKEN": "YOUR_TOKEN" }
    },
    "fetch": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-fetch"]
    },
    "memory": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-memory"]
    }
  }
}
```

---

## 🔑 API Keys Needed

| Service | Where to Get | Used For |
|---------|-------------|---------|
| `GEMINI_API_KEY` | [aistudio.google.com](https://aistudio.google.com) | Primary AI generation |
| `OPENAI_API_KEY` | [platform.openai.com](https://platform.openai.com) | Secondary AI + Codex |
| `GITHUB_TOKEN` | GitHub Settings → Tokens | MCP + GitHub Actions |
| Firebase / Supabase | Their consoles | Optional cloud sync |

**`.env.example`:**
```env
# AI
GEMINI_API_KEY=your_gemini_key_here
OPENAI_API_KEY=your_openai_key_here

# Optional Cloud
FIREBASE_PROJECT_ID=
SUPABASE_URL=
SUPABASE_ANON_KEY=

# GitHub (for MCP)
GITHUB_TOKEN=your_github_token_here

# Engine Config
MANIM_OUTPUT_DIR=./outputs
PREVIEW_RESOLUTION=480p
FINAL_RESOLUTION=1080p
MAX_RENDER_TIME_SECONDS=120
```

---

## ✨ Feature Implementation Map

### 1. Text → Manim Animation (Core)
- **AI Flow:** User prompt → `prompt_builder.py` constructs a detailed Manim-specific system prompt → Gemini API → validated Python/Manim code → `manim_runner.py` → MP4
- **Android:** Chaquopy runs the Python engine in a background thread via WorkManager
- **Web:** Next.js API route `/api/generate` calls Python subprocess or FastAPI server

### 2. Voice Prompt
- **Android:** Android `SpeechRecognizer` API → transcribed text → same AI flow
- **Web:** Browser `Web Speech API` → transcribed text → same AI flow

### 3. Live Preview
- **Renderer:** `preview_renderer.py` renders at 480p with reduced quality flags
- **Streaming:** WebSocket pushes frame-by-frame updates to the UI
- **Android:** WebSocket client in Kotlin receives preview frames

### 4. Code Editor (Developer Mode)
- **Both platforms:** Monaco Editor (same editor as VS Code)
- **Android:** Monaco runs inside a WebView with a Kotlin ↔ JS bridge
- **Web:** Monaco runs natively in Next.js
- **AI assist:** Gemini/Codex provides inline suggestions and error fixes

### 5. Drag & Drop Builder
- **Android:** Android Drag & Drop API with Compose
- **Web:** `dnd-kit` library
- **Output:** Builds a scene graph → serialised to JSON → converted to Manim code by engine

### 6. Timeline Editor
- **State:** Zustand (web) / ViewModel (Android) stores scene order, delays, durations
- **UI:** Custom timeline component with draggable clips
- **Output:** Timeline JSON → injected into Manim scene as `run_time`, `Wait()`, etc.

### 7. Template Library
- **Storage:** Python template files + JSON metadata (name, description, preview image)
- **Templates included:**
  - `calculus.py` — derivatives, integrals, limits
  - `graph_viz.py` — function graphs, parametric curves
  - `geometry_proofs.py` — angle proofs, Pythagorean theorem
  - `matrix_transforms.py` — linear transformations, eigenvectors
- **Loading:** Templates are parsed and injected as starting code in editor

### 8. Asset Library
- **Assets:** SVG/PNG mathematical symbols, arrows, coordinate grids, shapes
- **Android:** Bundled in `assets/` folder
- **Web:** Served as static files in Next.js `public/`

### 9. Plugin System
- **Plugin spec:** Each plugin is a Python module with a defined interface (`ManimStudioPlugin`)
- **Android:** Plugins installed as APK add-ons or sideloaded Python files
- **Web:** Plugins as npm packages or local Python modules
- **Plugin API:**
  ```python
  class ManimStudioPlugin:
      name: str
      version: str
      def register_animation_type(self): ...
      def register_template(self): ...
      def register_asset(self): ...
  ```

### 10. AI Scene Suggestions
- When user types a concept (e.g. "derivatives"), AI suggests 3-5 scene ideas
- Powered by a lightweight Gemini Flash call with low latency
- Displayed as tappable chips below the prompt input

### 11. Project Saving
- **Android:** Room DB stores project metadata; files stored in app-private storage
- **Web:** IndexedDB via Dexie.js; video files saved via File System Access API
- **Export:** Users can share/export as `.mstudio` project bundle (zip of code + assets + video)

### 12. Interactive Animations
- Generated Manim code can include `ValueTracker` and interactive elements
- For truly interactive output, exports to **Manim Web** or **HTML5 Canvas** format

---

## 🧪 Testing Strategy

### Python Engine Tests
```bash
cd engine/
pytest tests/ -v --cov=. --cov-report=html
```
- Unit tests for AI prompt generation
- Unit tests for Manim code validation
- Integration tests for full render pipeline
- Snapshot tests for template outputs

### Android Tests
```bash
cd android/
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests on emulator
```

### Web Tests
```bash
cd web/
npm run test          # Vitest unit tests
npm run test:e2e      # Playwright end-to-end tests
```

### GitHub Actions CI
- On every PR: run all three test suites
- On merge to main: build Android APK + Next.js production build
- Nightly: full render integration tests

---

## 💾 Memory & Performance Optimisations

### Android
- Rendering runs in `WorkManager` (background, survives app kill)
- Preview frames cached in memory with LRU cache
- Chaquopy Python environment initialises once at app start
- Room DB with proper indexing on project queries
- Video thumbnails generated at 10% of original resolution

### Web
- Next.js API routes run Python as subprocess (not in Node thread)
- Preview rendered at 480p max, streamed via WebSocket
- Monaco editor loaded lazily (code-split)
- IndexedDB for large binary storage (videos), localStorage only for settings

### Python Engine
- Manim quality flags: `--quality l` for preview, `--quality h` for final
- Render timeout enforced via `subprocess.timeout`
- Generated code sandboxed (no file system access, no network)
- Async rendering with asyncio for concurrent preview frames

---

## 🚀 Getting Started (Development Setup)

```bash
# 1. Clone the repo
git clone https://github.com/YOUR_USERNAME/manim-studio.git
cd manim-studio

# 2. Set up Python engine
cd engine/
python -m venv venv
source venv/bin/activate          # Windows: venv\Scripts\activate
pip install -r requirements.txt
pip install manim

# 3. Set up web app
cd ../web/
npm install
cp ../.env.example .env.local
# Fill in your API keys in .env.local

# 4. Run web app (dev)
npm run dev                        # Opens at localhost:3000

# 5. Run engine as local API
cd ../engine/
uvicorn main:app --reload --port 8000

# 6. Android setup
# Open android/ folder in Android Studio OR
# Use VS Code with Kotlin extension
cd ../android/
./gradlew assembleDebug           # Build debug APK

# 7. Install Gemini CLI
npm install -g @google/gemini-cli
gemini configure                  # Set up API key + MCP servers
```

---

## 📐 AI Prompt Engineering (Core System Prompt)

The following system prompt is used for the core text → Manim generation:

```
You are an expert Manim (Community Edition v0.18+) animator and Python developer.

Your job is to convert user descriptions into complete, working Manim Python scripts.

Rules:
1. Always import from manim: `from manim import *`
2. Always define a class inheriting from Scene or MovingCameraScene
3. Always implement the `construct(self)` method
4. Use smooth animations: FadeIn, Write, Create, Transform, etc.
5. Add appropriate Wait() calls for pacing
6. Use self.play() for all animations
7. Keep code clean and well-commented
8. For math, use MathTex or Tex with LaTeX syntax
9. Output ONLY valid Python code, no explanation
10. The scene must be self-contained and renderable

User request: {USER_PROMPT}
Template used: {TEMPLATE_NAME or "none"}
Scene elements requested: {ELEMENTS}
Duration target: {DURATION} seconds
```

---

## 🗺️ Development Roadmap

### Phase 1 — Foundation (Weeks 1-4)
- [ ] Repo setup with Git + GitHub + CI
- [ ] Python engine: AI → Manim code → render pipeline
- [ ] Web app: basic prompt input → render → video download
- [ ] Android app: basic prompt input → render → video save
- [ ] Core tests for engine

### Phase 2 — Editor & Preview (Weeks 5-8)
- [ ] Monaco editor integration (web + Android WebView)
- [ ] Live preview rendering (WebSocket)
- [ ] Voice prompt (web + Android)
- [ ] Project saving (web: IndexedDB, Android: Room)

### Phase 3 — Builder & Templates (Weeks 9-12)
- [ ] Template library (4 core templates)
- [ ] Asset library
- [ ] Drag & drop scene builder
- [ ] Timeline editor

### Phase 4 — AI & Plugins (Weeks 13-16)
- [ ] AI scene suggestions
- [ ] Plugin system API
- [ ] Interactive animation export
- [ ] Cloud sync (optional)

### Phase 5 — Polish & Launch (Weeks 17-20)
- [ ] Performance optimisation
- [ ] Full test coverage
- [ ] Play Store submission (Android)
- [ ] Web app deployment (Vercel or self-hosted)

---

## 📎 Key External Resources

| Resource | URL |
|----------|-----|
| Manim Community Docs | https://docs.manim.community |
| Manim GitHub | https://github.com/ManimCommunity/manim |
| Chaquopy (Python in Android) | https://chaquo.com/chaquopy |
| Gemini API Docs | https://ai.google.dev/docs |
| OpenAI API Docs | https://platform.openai.com/docs |
| Monaco Editor | https://microsoft.github.io/monaco-editor |
| MCP Servers | https://github.com/modelcontextprotocol/servers |
| Gemini CLI | https://github.com/google-gemini/gemini-cli |
| Next.js Docs | https://nextjs.org/docs |
| Jetpack Compose | https://developer.android.com/jetpack/compose |
| Room Database | https://developer.android.com/training/data-storage/room |
| dnd-kit | https://dndkit.com |
| FFmpeg | https://ffmpeg.org |

---

*This context file should be kept in the root of the repository and updated as the project evolves. It serves as the primary reference for AI coding assistants (Gemini CLI, Codex) working on this project.*
