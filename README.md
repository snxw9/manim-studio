# 🎬 Manim Animation Studio

**Manim Animation Studio** is a cross-platform application (Android mobile + web) that allows anyone — regardless of coding ability — to create professional mathematical animations powered by Manim Community Edition. Users describe what they want in plain English, and the AI generates, compiles, and renders the Manim animation locally.

## 🏗️ Architecture

- **Core Engine (Python/FastAPI):** Hosts the Manim renderer, FFmpeg post-processing, and LLM integrations (Gemini / OpenAI).
- **Web App (Next.js 15):** A local browser-based UI running a fully functional Monaco editor, prompt inputs, and video previews.
- **Android App (Kotlin/Compose):** A local mobile app embedding the Python engine natively via Chaquopy.

## 👥 For Users

### Using Manim Studio
- **Free (no setup needed):** Just open the app — you get 10 free animations per day automatically.
- **Unlimited (free API key):**
  1. Get a free Groq key at [console.groq.com](https://console.groq.com/keys) (30 seconds, no credit card).
  2. **Web:** Open Settings → API Keys → paste your Groq key.
  3. **Android:** Settings → AI Configuration → paste your Groq key.
  4. Done — unlimited animations forever.

## 🚀 Getting Started (Developers)

### Quick Start (2 minutes)
1. Get a free Groq API key at [console.groq.com](https://console.groq.com/keys) — no credit card needed.
2. Run: `cd engine && python setup.py` (paste your key when prompted).
3. Run: `cd ../web && npm install && npm run dev`.
4. Open `http://localhost:3000` and start animating.

### For Android
1. Build and install the APK.
2. Open **Settings → AI Configuration**.
3. Paste your Groq API key.
4. Tap **Test Connection** → Start animating.

### Prerequisites
- Python 3.10+
- Node.js 20+
- Android Studio (for mobile development)
- FFmpeg installed and in your system PATH
- Groq, Gemini, or OpenAI API keys

## 🧪 Testing

- **Engine:** `cd engine/ && pytest tests/`
- **Web:** `cd web/ && npm run test`
- **Android:** Run tests via Android Studio or `./gradlew test`

## 📄 License
MIT License