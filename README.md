# 🎬 Manim Animation Studio

**Manim Animation Studio** is a cross-platform application (Android mobile + web) that allows anyone — regardless of coding ability — to create professional mathematical animations powered by Manim Community Edition. Users describe what they want in plain English, and the AI generates, compiles, and renders the Manim animation locally.

## 🏗️ Architecture

- **Core Engine (Python/FastAPI):** Hosts the Manim renderer, FFmpeg post-processing, and LLM integrations (Gemini / OpenAI).
- **Web App (Next.js 15):** A local browser-based UI running a fully functional Monaco editor, prompt inputs, and video previews.
- **Android App (Kotlin/Compose):** A local mobile app embedding the Python engine natively via Chaquopy.

## 🚀 Getting Started

### Prerequisites
- Python 3.10+
- Node.js 20+
- Android Studio (for mobile development)
- FFmpeg installed and in your system PATH
- Gemini or OpenAI API keys

### 1. Set up the Python Engine
```bash
cd engine/
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 2. Set up Environment Variables
Copy the `.env.example` file to `.env` in the root directory:
```bash
cp .env.example .env
```
Add your `GEMINI_API_KEY` to the `.env` file.

### 3. Run the Engine API
```bash
cd engine/
uvicorn main:app --reload --port 8000
```

### 4. Run the Web App
In a new terminal window:
```bash
cd web/
npm install
npm run dev
```
Open `http://localhost:3000` in your browser.

### 5. Build the Android App
1. Open the `android/` directory in Android Studio.
2. Let Gradle sync and resolve all dependencies.
3. Build and run on an emulator or physical device.

## 🧪 Testing

- **Engine:** `cd engine/ && pytest tests/`
- **Web:** `cd web/ && npm run test`
- **Android:** Run tests via Android Studio or `./gradlew test`

## 📄 License
MIT License