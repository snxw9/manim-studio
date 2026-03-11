@echo off
echo Starting Manim Studio Engine...
cd /d "%~dp0engine"

if not exist "venv\" (
    echo Creating virtual environment...
    python -m venv venv
)

call venv\Scripts\activate.bat

pip install -r requirements.txt -q

echo Engine starting on http://localhost:8000
uvicorn main:app --reload --port 8000 --host 0.0.0.0
pause
