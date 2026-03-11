#!/bin/bash
echo "🚀 Starting Manim Studio Engine..."
cd "$(dirname "$0")/engine"

# Check if venv exists
if [ ! -d "venv" ]; then
  echo "📦 Creating virtual environment..."
  python3 -m venv venv
fi

# Activate venv
source venv/bin/activate

# Install dependencies if needed
pip install -r requirements.txt -q

# Check if manim is installed
if ! command -v manim &> /dev/null; then
  echo "📦 Installing manim..."
  pip install manim
fi

# Kill any existing process on port 8000
lsof -ti:8000 | xargs kill -9 2>/dev/null || true

echo "✅ Engine starting on http://localhost:8000"
uvicorn main:app --reload --port 8000 --host 0.0.0.0
